#!/usr/bin/env python3
"""Paints the 16x16 item icons (bear meat, cooked bear meat, bear pelt, bear head). Run from the repo root."""
import os
import random

from PIL import Image

random.seed(42)
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "src/main/resources/assets/grizzlymod/textures/item")
os.makedirs(OUT, exist_ok=True)

PAL = {
    ".": None,
    "o": (40, 22, 16),      # outline dark
    "r": (176, 58, 62),     # raw meat
    "R": (140, 38, 46),     # raw meat shadow
    "p": (214, 112, 112),   # raw meat highlight
    "w": (236, 214, 206),   # fat
    "c": (150, 92, 52),     # cooked
    "C": (106, 62, 36),     # cooked shadow
    "h": (190, 130, 78),    # cooked highlight
    "g": (62, 36, 22),      # grill mark
    "b": (92, 60, 36),      # fur
    "d": (56, 36, 24),      # dark fur
    "l": (140, 100, 62),    # light fur tip
    "t": (150, 114, 80),    # muzzle
    "k": (14, 10, 9),       # black
    "e": (206, 194, 166),   # claw
}

RAW = [
    "................",
    "................",
    "....oooooo......",
    "...orrrrrrro....",
    "..orpprrrrrrro..",
    ".orpprrwwwrrrRo.",
    ".orprrwwrrrrrRo.",
    ".orrrrrwwwrrRRo.",
    ".orrrrrrrwwrRRo.",
    "..orrrRrrrrRRo..",
    "..orrrRRRRRRo...",
    "...oRRRRRRoo....",
    "....oooooo......",
    "................",
    "................",
    "................",
]
COOKED = [
    "................",
    "................",
    "....oooooo......",
    "...occcccco.....",
    "..ochhcccccco...",
    ".ochhcgcccgccCo.",
    ".occcccgcccgcCo.",
    ".occgccccgccCCo.",
    ".occccgcccgcCCo.",
    "..occcccgccCCo..",
    "..occcCCCCCCo...",
    "...oCCCCCCoo....",
    "....oooooo......",
    "................",
    "................",
    "................",
]
PELT = [
    "................",
    "..oo........oo..",
    ".obbo.oooo.obbo.",
    ".obbooobbboobbo.",
    "..obbbbllbbbbo..",
    "..obblbbbblbbo..",
    ".obbbbbbbbbbbbo.",
    ".obblbbbblbbbbo.",
    ".obbbbblbbbbbdo.",
    ".obbbbbbbbblbdo.",
    "..obblbbbbbbdo..",
    ".obbbbbbbbdddbo.",
    ".obbo.obbdo.obo.",
    "..oo..obbo...o..",
    "......oo........",
    "................",
]
HEAD = [
    "................",
    "...oo......oo...",
    "..obbo....obbo..",
    "..obbooooooobbo.",
    "..obbbbbbbbbbo..",
    ".obblbbbbbblbbo.",
    ".obbbbbbbbbbbbo.",
    ".obbkbbbbbbkbbo.",
    ".obbbbbbbbbbbbo.",
    "..obbbttttbbbo..",
    "..obbttkkttbbo..",
    "..obbtttttttbo..",
    "...obbtkkktbo...",
    "....obbbbbbo....",
    ".....oooooo.....",
    "................",
]


def save(name, rows):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 16, (name, y, len(row))
        for x, ch in enumerate(row):
            col = PAL[ch]
            if col:
                j = random.randint(-6, 6) if ch not in "ok" else 0
                col = tuple(max(0, min(255, v + j)) for v in col)
                img.putpixel((x, y), col + (255,))
    img.save(os.path.join(OUT, name + ".png"))
    img.resize((128, 128), Image.NEAREST).save("/tmp/icon_%s.png" % name)


save("bear_meat", RAW)
save("cooked_bear_meat", COOKED)
save("bear_pelt", PELT)
save("bear_head", HEAD)
print("icons ok")
