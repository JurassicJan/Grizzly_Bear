#!/usr/bin/env python3
"""
Converts model_src/grizzly_bear.bbmodel (Blockbench, Modded Entity) into
  - src/main/java/.../client/GrizzlyBearModel.java
  - src/main/resources/assets/grizzlymod/textures/entity/grizzly_bear.png  (painted fur atlas)

Usage: python3 tools/generate_model.py   (run from the repo root)

Coordinate conversion Blockbench -> Minecraft entity model space:
  x -> x, y -> 24 - y, z -> -(z - 2.5)    (head is at +z in Blockbench, vanilla front is -z)
  rotation: x stays, y and z flip sign
Legs get their pivot moved to the top so they can swing; the body parts hang on a "torso"
part pivoted at the hip so the bear can rear up.
"""
import json
import math
import os
import random

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "model_src", "grizzly_bear.bbmodel")
JAVA = os.path.join(ROOT, "src/main/java/com/example/grizzlymod/client/GrizzlyBearModel.java")
TEX = os.path.join(ROOT, "src/main/resources/assets/grizzlymod/textures/entity/grizzly_bear.png")
TEX_CANNIBAL = os.path.join(ROOT, "src/main/resources/assets/grizzlymod/textures/entity/grizzly_bear_cannibal.png")

ATLAS = 128
Z0 = 2.5
random.seed(1337)

bb = json.load(open(SRC))
E = {e["name"]: e for e in bb["elements"]}


def absolute_pivot(o):
    return (o[0], 24.0 - o[1], -(o[2] - Z0))


def cube_info(name):
    e = E[name]
    f, t, o = e["from"], e["to"], e["origin"]
    size = (round(t[0] - f[0]), round(t[1] - f[1]), round(t[2] - f[2]))
    rel = (f[0] - o[0], -(t[1] - o[1]), -(t[2] - o[2]))
    rot = e.get("rotation", [0, 0, 0])
    rot_m = (math.radians(rot[0]), math.radians(-rot[1]), math.radians(-rot[2]))
    return {"size": size, "rel": rel, "rot": rot_m, "pivot": absolute_pivot(o)}


# ---------------------------------------------------------------- parts
# key -> bbmodel element(s) sharing one texture region
PARTS = {
    "head": cube_info("head"),
    "ear_a": cube_info("ear1"),
    "ear_b": cube_info("ear2"),
    "snout": cube_info("snaut"),
    "jaw": cube_info("snaut2"),
    "body_rear": cube_info("body_middle"),
    "body_mid": cube_info("body"),
    "neck": cube_info("bodypart"),
    "hump": cube_info("bodypart2"),
    "tail": cube_info("tail"),
}

# legs: pivot at the top centre so they can swing
LEG_DEFS = {
    "leg_rear_a": "leg1",   # x < 0, rear
    "leg_rear_b": "leg2",   # x > 0, rear
    "leg_front_b": "leg3",  # x > 0, front
    "leg_front_a": "leg4",  # x < 0, front
}
for key, src in LEG_DEFS.items():
    e = E[src]
    f, t = e["from"], e["to"]
    size = (round(t[0] - f[0]), round(t[1] - f[1]), round(t[2] - f[2]))
    cx, cz = (f[0] + t[0]) / 2.0, (f[2] + t[2]) / 2.0
    top = 24.0 - t[1]
    PARTS[key] = {
        "size": size,
        "rel": (-size[0] / 2.0, 0.0, -size[2] / 2.0),
        "rot": (0.0, 0.0, 0.0),
        "pivot": (cx, top, -(cz - Z0)),
    }

# hip pivot: top of the rear legs, centred on them
HIP = (0.0, 15.0, PARTS["leg_rear_a"]["pivot"][2])


def rel(a, b):
    return tuple(round(a[i] - b[i], 4) for i in range(3))


# ---------------------------------------------------------------- UV packing
REGIONS = {  # texture region key -> parts using it
    "head": ["head"], "ear": ["ear_a", "ear_b"], "snout": ["snout"], "jaw": ["jaw"],
    "body_rear": ["body_rear"], "body_mid": ["body_mid"], "neck": ["neck"], "hump": ["hump"],
    "tail": ["tail"], "leg_front": ["leg_front_a", "leg_front_b"], "leg_rear": ["leg_rear_a", "leg_rear_b"],
}


def region_size(key):
    w, h, d = PARTS[REGIONS[key][0]]["size"]
    return (2 * d + 2 * w, d + h)


tex_off = {}
x = y = shelf_h = 0
for key in sorted(REGIONS, key=lambda k: -region_size(k)[1]):
    rw, rh = region_size(key)
    if x + rw > ATLAS:
        x, y, shelf_h = 0, y + shelf_h + 1, 0
    tex_off[key] = (x, y)
    x += rw + 1
    shelf_h = max(shelf_h, rh)
assert y + shelf_h <= ATLAS, "atlas too small"


def region_of(part):
    for k, ps in REGIONS.items():
        if part in ps:
            return k


# ---------------------------------------------------------------- painting
img = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0))
px = img.load()

RAMP = [(44, 22, 15), (88, 42, 24), (134, 65, 34), (174, 93, 46), (210, 136, 78)]
MUZZLE = [(84, 46, 30), (122, 70, 44), (156, 96, 62), (184, 122, 82), (206, 150, 106)]
BLACK = (14, 10, 9)
CLAW = (206, 194, 166)
PINK = (186, 84, 88)
TEETH = (226, 220, 204)
EYE_RED = (128, 8, 12)
BLOOD = (92, 12, 15)
CANNIBAL = False

PALETTES = {
    False: dict(RAMP=RAMP, MUZZLE=MUZZLE, PINK=PINK, TEETH=TEETH),
    # cannibal: much darker fur, dried blood around the mouth, dark red eyes
    True: dict(
        RAMP=[(12, 8, 7), (30, 18, 15), (54, 31, 25), (80, 46, 36), (110, 68, 52)],
        MUZZLE=[(30, 20, 18), (54, 34, 28), (78, 52, 42), (100, 70, 56), (124, 90, 72)],
        PINK=(104, 24, 32),
        TEETH=(192, 182, 150),
    ),
}


def ramp(pal, v):
    v = max(0.0, min(0.9999, v)) * (len(pal) - 1)
    i = int(v)
    f = v - i
    a, b = pal[i], pal[i + 1]
    return tuple(int(a[k] + (b[k] - a[k]) * f) for k in range(3))


def draw_region(key, painter):
    w, h, d = PARTS[REGIONS[key][0]]["size"]
    u, v = tex_off[key]
    faces = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "west": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "east": (u + d + w, v + d, d, h),
        "south": (u + 2 * d + w, v + d, w, h),
    }
    for face, (fx, fy, fw, fh) in faces.items():
        # fur flows along the body: strands run along the long axis of the face
        strands_cols = [random.uniform(-0.16, 0.16) for _ in range(fw)]
        strands_rows = [random.uniform(-0.16, 0.16) for _ in range(fh)]
        for yy in range(fh):
            for xx in range(fw):
                if face in ("west", "east"):
                    strand = strands_rows[yy]
                else:
                    strand = strands_cols[xx]
                col = painter(face, xx, yy, fw, fh, strand)
                px[fx + xx, fy + yy] = tuple(col) + (255,)


def fur(pal, base, strand, tip_chance=0.0):
    n = random.uniform(-0.09, 0.09)
    v = base + strand + n
    if tip_chance and random.random() < tip_chance:
        v += 0.28  # gold/silver tipped guard hairs ("grizzled")
    if random.random() < 0.05:
        v -= 0.2
    return ramp(pal, v)


def body_painter(top_tips, base_top=0.62):
    def p(face, x, y, fw, fh, strand):
        if face == "top":
            return fur(RAMP, base_top, strand, top_tips)
        if face == "bottom":
            return fur(RAMP, 0.12, strand)
        g = 1.0 - y / max(1, fh - 1)               # lighter on the back, darker towards the belly
        return fur(RAMP, 0.30 + 0.34 * g, strand, top_tips * g)
    return p


def leg_painter(front):
    def p(face, x, y, fw, fh, strand):
        if face == "bottom":
            return fur(RAMP, 0.05, 0.0)
        g = y / max(1, fh - 1)
        base = 0.42 - 0.22 * g
        if y >= fh - 2:                              # dark paws
            base = 0.08
        if face == "north" and y == fh - 1 and x % 2 == 0:
            return CLAW                              # claws
        if face == "top":
            return fur(RAMP, 0.5, strand, 0.1)
        return fur(RAMP, base, strand, 0.04 * (1 - g))
    return p


def head_painter(face, x, y, fw, fh, strand):
    if face == "north":
        if CANNIBAL and y == 2 and x in (1, 2, fw - 3, fw - 2):
            return EYE_RED                            # dark red eyes
        if not CANNIBAL and (x, y) in ((1, 2), (fw - 2, 2)):
            return BLACK                              # eyes
        if y == 1 and x in (1, 2, fw - 3, fw - 2):
            return ramp(RAMP, 0.18)                   # brow
        if y >= fh - 2 and (x <= 1 or x >= fw - 2):
            return fur(RAMP, 0.6, strand, 0.2)        # lighter cheek fur
        return fur(RAMP, 0.46, strand, 0.05)
    if face == "top":
        return fur(RAMP, 0.6, strand, 0.2)
    if face == "bottom":
        return fur(RAMP, 0.2, strand)
    return fur(RAMP, 0.45, strand, 0.05)


def snout_painter(face, x, y, fw, fh, strand):
    if face == "north":
        if y == 0 and fw // 2 - 1 <= x <= fw // 2:
            return BLACK                              # nose
        return fur(MUZZLE, 0.5, strand)
    if face == "bottom":
        if x in (0, fw - 1) and y % 2 == 0:
            return TEETH
        return fur([PINK, PINK, PINK, PINK, PINK], 0.4, 0.0)
    if face == "top":
        return fur(MUZZLE, 0.45, strand)
    return fur(MUZZLE, 0.4, strand)


def jaw_painter(face, x, y, fw, fh, strand):
    if face == "top":
        if x in (0, fw - 1) and y in (0, 2):
            return TEETH
        return fur([PINK, PINK, PINK, PINK, PINK], 0.35, 0.0)
    if face == "north":
        return ramp(MUZZLE, 0.3)
    return fur(MUZZLE, 0.3, strand)


def ear_painter(face, x, y, fw, fh, strand):
    if face == "north":
        return fur(MUZZLE, 0.55, 0.0) if (x, y) in ((0, 1), (1, 1)) else fur(RAMP, 0.3, strand)
    return fur(RAMP, 0.28, strand)


def with_blood(painter, prob):
    """Cannibal only: dried blood patches (never over eyes, nose, teeth or claws)."""
    def p(face, x, y, fw, fh, strand):
        col = painter(face, x, y, fw, fh, strand)
        if CANNIBAL and tuple(col) not in (BLACK, TEETH, CLAW, EYE_RED) and random.random() < prob:
            j = random.randint(-10, 10)
            return (BLOOD[0] + j, BLOOD[1], BLOOD[2])
        return col
    return p


def build(cannibal, path):
    global img, px, RAMP, MUZZLE, PINK, TEETH, CANNIBAL
    CANNIBAL = cannibal
    RAMP, MUZZLE = PALETTES[cannibal]["RAMP"], PALETTES[cannibal]["MUZZLE"]
    PINK, TEETH = PALETTES[cannibal]["PINK"], PALETTES[cannibal]["TEETH"]
    img = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0))
    px = img.load()
    draw_region("head", with_blood(head_painter, 0.07))
    draw_region("snout", with_blood(snout_painter, 0.32))
    draw_region("jaw", with_blood(jaw_painter, 0.38))
    draw_region("ear", ear_painter)
    draw_region("body_rear", body_painter(0.10))
    draw_region("body_mid", body_painter(0.18))
    draw_region("neck", with_blood(body_painter(0.22, 0.66), 0.03))
    draw_region("hump", body_painter(0.34, 0.72))
    draw_region("tail", body_painter(0.05, 0.45))
    draw_region("leg_front", with_blood(leg_painter(True), 0.10))
    draw_region("leg_rear", leg_painter(False))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    name = "cannibal" if cannibal else "normal"
    img.resize((ATLAS * 4, ATLAS * 4), Image.NEAREST).save("/tmp/grizzly_atlas_%s.png" % name)


build(False, TEX)
build(True, TEX_CANNIBAL)


# ---------------------------------------------------------------- Java emission
def f(v):
    s = ("%.4f" % v).rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "F"


def box_line(part, indent):
    p = PARTS[part]
    u, v = tex_off[region_of(part)]
    x, y, z = p["rel"]
    w, h, d = p["size"]
    return "%s.texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (
        indent, u, v, f(x), f(y), f(z), f(float(w)), f(float(h)), f(float(d)))


def pose(part, parent_pivot):
    p = PARTS[part]
    o = rel(p["pivot"], parent_pivot)
    r = p["rot"]
    if abs(r[0]) < 1e-9 and abs(r[1]) < 1e-9 and abs(r[2]) < 1e-9:
        return "PartPose.offset(%s, %s, %s)" % (f(o[0]), f(o[1]), f(o[2]))
    return "PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s)" % (
        f(o[0]), f(o[1]), f(o[2]), f(r[0]), f(r[1]), f(r[2]))


def add_child(var_parent, name, part, parent_pivot, indent="        "):
    return '%s%s.addOrReplaceChild("%s", CubeListBuilder.create()\n%s,\n%s        %s);\n' % (
        indent, var_parent, name, box_line(part, indent + "        "), indent, pose(part, parent_pivot))


head_pivot = PARTS["head"]["pivot"]
head_rel_torso = rel(head_pivot, HIP)

java = []
java.append('''package com.example.grizzlymod.client;

import com.example.grizzlymod.GrizzlyMod;
import com.example.grizzlymod.entity.GrizzlyBear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * GENERATED by tools/generate_model.py from model_src/grizzly_bear.bbmodel - edit the generator, not this file.
 * Texture atlas: 128x128, see textures/entity/grizzly_bear.png
 */
public class GrizzlyBearModel<T extends GrizzlyBear> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(GrizzlyMod.MOD_ID, "grizzly_bear"), "main");

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart legRearA;
    private final ModelPart legRearB;
    private final ModelPart legFrontA;
    private final ModelPart legFrontB;
    private final float jawBaseRot;

    public GrizzlyBearModel(ModelPart root) {
        this.root = root;
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.legFrontA = this.torso.getChild("leg_front_a");
        this.legFrontB = this.torso.getChild("leg_front_b");
        this.legRearA = root.getChild("leg_rear_a");
        this.legRearB = root.getChild("leg_rear_b");
        this.jawBaseRot = this.jaw.xRot;
    }

    /** Head with ears, snout and lower jaw. Also used by the bear-head helmet model. */
    public static PartDefinition addHead(PartDefinition parent, PartPose headPose) {
        PartDefinition head = parent.addOrReplaceChild("head", CubeListBuilder.create()
''')
java.append(box_line("head", "                ") + ",\n                headPose);\n")
for name, part in (("ear_a", "ear_a"), ("ear_b", "ear_b"), ("snout", "snout"), ("jaw", "jaw")):
    java.append(add_child("head", name, part, head_pivot))
java.append('''        return head;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // rear legs stand on the ground and carry the "torso" (hip pivot) so the bear can rear up
''')
for name in ("leg_rear_a", "leg_rear_b"):
    java.append(add_child("root", name, name, (0, 0, 0)))
java.append('''
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create(),
                PartPose.offset(%s, %s, %s));
''' % (f(HIP[0]), f(HIP[1]), f(HIP[2])))
for name in ("body_rear", "body_mid", "neck", "hump", "tail", "leg_front_a", "leg_front_b"):
    java.append(add_child("torso", name, name, HIP))
java.append("\n        addHead(torso, PartPose.offset(%s, %s, %s));\n" % tuple(f(v) for v in head_rel_torso))
java.append('''
        return LayerDefinition.create(mesh, %d, %d);
    }

    @Override
    public void setupAnim(T bear, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float partial = ageInTicks - bear.tickCount;
        float stand = bear.getStandAnim(partial);            // 0 = on all fours, 1 = fully reared up

        // look direction
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F) + stand * 0.7F;

        // rearing: the whole torso tips back around the hip
        this.torso.xRot = -stand * 1.05F;

        // walk cycle (diagonal pairs)
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount * (1.0F - stand);
        this.legRearA.xRot = swing;
        this.legRearB.xRot = -swing;
        float frontA = -swing;
        float frontB = swing;

        // claw swipe during the attack swing
        float swipe = bear.swinging ? Mth.sin(bear.swingTime / 6.0F * (float) Math.PI) * 1.6F : 0.0F;

        // front legs: when reared up they hang forward and paw the air
        float paw = stand * (0.55F + Mth.sin(ageInTicks * 0.45F) * 0.25F);
        this.legFrontA.xRot = frontA * (1.0F - stand) + paw - swipe;
        this.legFrontB.xRot = frontB * (1.0F - stand) + paw - swipe;

        // mouth: slightly open when aggressive, wide and shaking while roaring
        float open = 0.0F;
        if (bear.isAggressive()) {
            open = 0.35F + Mth.sin(ageInTicks * 0.3F) * 0.05F;
        }
        if (stand > 0.0F) {
            open = Math.max(open, stand * (0.85F + Mth.sin(ageInTicks * 1.2F) * 0.08F));
        }
        this.jaw.xRot = this.jawBaseRot + open;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
''' % (ATLAS, ATLAS))

os.makedirs(os.path.dirname(JAVA), exist_ok=True)
open(JAVA, "w").write("".join(java))

# ---------------------------------------------------------------- silhouette sanity check (side view)
S = 12
view = Image.new("RGB", (40 * S, 28 * S), (240, 240, 240))
from PIL import ImageDraw
dr = ImageDraw.Draw(view)
for key, p in PARTS.items():
    px0, py0, pz0 = p["pivot"]
    w, h, d = p["size"]
    zmin = pz0 + p["rel"][2]
    ymin = py0 + p["rel"][1]
    x0 = (zmin + 20) * S
    y0 = ymin * S
    dr.rectangle((x0, y0, x0 + d * S, y0 + h * S), outline=(200, 40, 40), width=2)
dr.line((0, 24 * S, 40 * S, 24 * S), fill=(0, 0, 0))
view.save("/tmp/grizzly_side_check.png")
print("OK  hip =", HIP, " head_rel_torso =", head_rel_torso)
for k, v in sorted(tex_off.items()):
    print(" ", k, v)
