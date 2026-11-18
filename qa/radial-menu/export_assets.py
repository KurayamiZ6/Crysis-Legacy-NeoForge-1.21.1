"""Export editable V3 symbols into transparent layered atlases (no icon redraw).

Writes SVG sources next to the PNGs. Rasterize the SVGs with a transparent
browser screenshot at their declared dimensions; see render-assets.cjs.
"""
from pathlib import Path
import math
import re

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/crysismod/textures/gui/nanosuit1_radial"
OUT.mkdir(parents=True, exist_ok=True)
source = (ROOT / "design/nanosuit-1-radial/radial-neutral.svg").read_text(encoding="utf-8")
symbols = "".join(re.findall(r"<symbol\b.*?</symbol>", source, re.S))
modes = ("armor", "strength", "speed", "cloak")
centers = {"speed": (132, 56), "armor": (41, 141), "strength": (223, 141), "cloak": (132, 219)}
panels = {mode: re.search(r'<g class="mode" id="mode-' + mode + r'".*?<path class="panel" d="([^"]+)"', source, re.S).group(1) for mode in modes}
uses = {mode: re.search(r'<use class="icon" href="#' + mode + r'"[^>]*/>', source).group(0) for mode in modes}
radius = 4.8
width = math.sqrt(3) * radius
height = radius * 3
hexagons = []
for cx, cy in [(width/2, 0), (0, height/2), (width, height/2), (width/2, height)]:
    points = " ".join(f"{cx+radius*math.cos(math.radians(-90+60*i)):.4f},{cy+radius*math.sin(math.radians(-90+60*i)):.4f}" for i in range(6))
    hexagons.append(f'<polygon points="{points}"/>')
defs = symbols + f'<pattern id="hex" width="{width}" height="{height}" patternUnits="userSpaceOnUse"><g fill="none" stroke="#332409" stroke-width="2.1">{"".join(hexagons)}</g></pattern>'
defs += f'<pattern id="hex-glow" width="{width}" height="{height}" patternUnits="userSpaceOnUse"><g fill="none" stroke="#ffe17a" stroke-width="2.1">{"".join(hexagons)}</g></pattern>'
defs += '<filter id="glow" x="-30%" y="-30%" width="160%" height="160%"><feGaussianBlur stdDeviation=".65"/></filter>'

def write(name, w, h, body, definitions=""):
    (OUT / f"{name}.svg").write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}"><defs>{definitions}</defs>{body}</svg>', encoding="utf-8")

for layer in ("green-fill", "amber-fill", "flash-fill", "green-frame", "amber-frame", "flash-frame", "hex", "hex-glow", "icons"):
    tiles = []
    for i, mode in enumerate(modes):
        d = panels[mode]
        content = {
            "green-fill": f'<path d="{d}" fill="#12381c"/>',
            "amber-fill": f'<path d="{d}" fill="#b67d13"/>',
            "flash-fill": f'<path d="{d}" fill="#ffc43c"/>',
            "green-frame": f'<path d="{d}" fill="none" stroke="#93ad8a" stroke-width="1.8"/>',
            "amber-frame": f'<path d="{d}" fill="none" stroke="#ffc943" stroke-width="3" opacity=".28" filter="url(#glow)"/><path d="{d}" fill="none" stroke="#ffda50" stroke-width="2.1"/>',
            "flash-frame": f'<path d="{d}" fill="none" stroke="#ffe787" stroke-width="4" opacity=".6" filter="url(#glow)"/><path d="{d}" fill="none" stroke="#fff4bc" stroke-width="2.5"/>',
            "hex": f'<path d="{d}" fill="url(#hex)"/>',
            "hex-glow": f'<path d="{d}" fill="url(#hex-glow)"/>',
            "icons": uses[mode],
        }[layer]
        x, y = centers[mode]
        tiles.append(f'<g transform="translate({i*256+128} 128) scale(3) translate({-x} {-y})" color="white">{content}</g>')
    write(layer, 1024, 256, "".join(tiles), defs)
write("ring", 96, 96, '<circle cx="48" cy="48" r="34" fill="none" stroke="#77976d" stroke-width="6.4" stroke-dasharray="95 11.81415022" transform="rotate(-38 48 48)"/>')
write("pointer", 112, 112, '<path d="M45 13L56 8L67 13" fill="none" stroke="#f1f0da" stroke-width="2.2"/>')
print(f"Exported {len(modes)} unchanged V3 icons and 11 layered SVGs into {OUT}")
