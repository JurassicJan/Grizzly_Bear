package com.example.grizzlymod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
 * Grizzly bear: attacks players on sight and runs away for a while after being hit by a projectile.
 */
public class GrizzlyBear extends PathfinderMob {

    /** How long (ticks) the bear keeps fleeing after being shot. 300 ticks = 15 seconds. */
    private static final int FLEE_DURATION = 300;

    private int fleeTicks;
    @Nullable
    private Vec3 fleeFrom;

    public GrizzlyBear(EntityType<? extends GrizzlyBear> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.ARMOR, 2.0D);
    }

    /** Spawns on typical taiga ground (grass, podzol, snow, coarse dirt) in reasonable light. */
    public static boolean checkSpawnRules(EntityType<GrizzlyBear> type, ServerLevelAccessor level,
                                          MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.WOLVES_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
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
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
