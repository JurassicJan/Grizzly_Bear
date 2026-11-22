package com.example.grizzlymod.entity;

import com.example.grizzlymod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Grizzly bear: attacks players on sight, rears up and roars when close, and runs away for a while
 * after being hit by a projectile. Players wearing a bear head are noticed from 5 blocks less far away.
 */
public class GrizzlyBear extends PathfinderMob {

    /** How far away (blocks) the bear notices a player. Matches the FOLLOW_RANGE attribute. */
    public static final double AGGRO_RANGE = 24.0D;
    /** Wearing the bear head shrinks the aggro range by this many blocks. */
    public static final double BEAR_HEAD_RANGE_REDUCTION = 5.0D;
    /** The bear rears up and roars when its target is this close (distance centre to centre, in blocks). */
    public static final double STAND_DISTANCE = 2.5D;
    /** How long (ticks) the bear keeps fleeing after being shot. 300 ticks = 15 seconds. */
    private static final int FLEE_DURATION = 300;
    private static final int ROAR_INTERVAL = 70;

    private static final EntityDataAccessor<Boolean> DATA_STANDING =
            SynchedEntityData.defineId(GrizzlyBear.class, EntityDataSerializers.BOOLEAN);

    private int fleeTicks;
    @Nullable
    private Vec3 fleeFrom;
    private int roarCooldown;
    private float standAnim;
    private float standAnimO;

    public GrizzlyBear(EntityType<? extends GrizzlyBear> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE, AGGRO_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.ARMOR, 2.0D);
    }

    /** Spawns on typical taiga ground (grass, podzol, snow, coarse dirt) in reasonable light. */
    public static boolean checkSpawnRules(EntityType<GrizzlyBear> type, ServerLevelAccessor level,
                                          MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.WOLVES_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
    }

    public static boolean wearsBearHead(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.BEAR_HEAD.get());
    }

    /** Aggro range for one specific player: 5 blocks shorter while that player wears the bear head. */
    private static double aggroRangeFor(Player player) {
        return wearsBearHead(player) ? AGGRO_RANGE - BEAR_HEAD_RANGE_REDUCTION : AGGRO_RANGE;
    }

    private boolean canNotice(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        double range = aggroRangeFor(player);
        return this.distanceToSqr(player) <= range * range;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FleeWhenShotGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::canNotice));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STANDING, false);
    }

    public boolean isStanding() {
        return this.entityData.get(DATA_STANDING);
    }

    private void setStanding(boolean standing) {
        this.entityData.set(DATA_STANDING, standing);
    }

    /** 0 = on all fours, 1 = fully reared up; interpolated for smooth rendering. */
    public float getStandAnim(float partialTick) {
        return Mth.lerp(partialTick, this.standAnimO, this.standAnim);
    }

    @Override
    public void tick() {
        super.tick();
        this.standAnimO = this.standAnim;
        if (this.isStanding()) {
            this.standAnim = Math.min(1.0F, this.standAnim + 0.12F);
        } else {
            this.standAnim = Math.max(0.0F, this.standAnim - 0.12F);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // A player who put on the bear head is only followed up to 5 blocks less far away.
        LivingEntity target = this.getTarget();
        if (target instanceof Player player && this.getLastHurtByMob() != player) {
            double range = aggroRangeFor(player);
            if (this.distanceToSqr(player) > range * range) {
                this.setTarget(null);
                target = null;
            }
        }

        // Rear up and roar when the target is close.
        boolean shouldStand = false;
        if (this.fleeTicks <= 0 && target != null && target.isAlive()) {
            double limit = this.isStanding() ? STAND_DISTANCE + 1.0D : STAND_DISTANCE;
            shouldStand = this.distanceTo(target) <= limit;
        }
        if (shouldStand != this.isStanding()) {
            this.setStanding(shouldStand);
            if (shouldStand) {
                this.roar();
            }
        } else if (shouldStand && --this.roarCooldown <= 0) {
            this.roar();
        }
    }

    private void roar() {
        this.roarCooldown = ROAR_INTERVAL;
        this.playSound(SoundEvents.POLAR_BEAR_WARNING, 1.8F, 0.55F + this.random.nextFloat() * 0.1F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);
        if (wasHurt && !this.level().isClientSide && source.getDirectEntity() instanceof Projectile projectile) {
            Entity shooter = source.getEntity();
            startFleeing(shooter != null ? shooter.position() : projectile.position());
        }
        return wasHurt;
    }

    private void startFleeing(Vec3 from) {
        this.fleeTicks = FLEE_DURATION;
        this.fleeFrom = from;
        this.setAggressive(false);
        this.setStanding(false);
        super.setTarget(null);
    }

    /** While fleeing the bear does not pick a new attack target. */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.fleeTicks > 0) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.fleeTicks > 0) {
            this.fleeTicks--;
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.15F, 1.0F);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    /** Runs away from the position the shot came from until the flee timer runs out. */
    private static class FleeWhenShotGoal extends Goal {
        private final GrizzlyBear bear;

        FleeWhenShotGoal(GrizzlyBear bear) {
            this.bear = bear;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.bear.fleeTicks > 0 && this.bear.fleeFrom != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.bear.fleeTicks > 0 && this.bear.fleeFrom != null;
        }

        @Override
        public void start() {
            runAway();
        }

        @Override
        public void tick() {
            if (this.bear.getNavigation().isDone()) {
                runAway();
            }
        }

        @Override
        public void stop() {
            this.bear.fleeFrom = null;
            this.bear.getNavigation().stop();
        }

        private void runAway() {
            Vec3 target = LandRandomPos.getPosAway(this.bear, 20, 7, this.bear.fleeFrom);
            if (target != null) {
                this.bear.getNavigation().moveTo(target.x, target.y, target.z, 1.7D);
            }
        }
    }
}
