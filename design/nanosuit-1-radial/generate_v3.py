"""Generate V3 SVG mockups using the unchanged, validated V2 icon symbols."""
from pathlib import Path
import base64
import math
import re

ROOT = Path(__file__).resolve().parent
old = (ROOT / "v2/radial-neutral.svg").read_text(encoding="utf-8")
icon_symbols = "".join(re.findall(r"<symbol\b.*?</symbol>", old, re.S))
modes = ("armor", "strength", "speed", "cloak")
boxes = {"speed": (110, 44, 49, 28), "strength": (195, 94, 38, 47),
         "armor": (32, 101, 36, 43), "cloak": (177, 196, 34, 47)}
centers = {"speed": (132, 56), "armor": (41, 141),
           "strength": (223, 141), "cloak": (132, 219)}
# Four cardinal notched panels, aligned to the supplied Remastered captures.
panels = {
    "speed": "M132 21L170 44V75L151 87L132 76L113 87L94 75V44Z",
    "strength": "M204 101H242L263 141L242 181H204L186 162L198 141L186 120Z",
    "armor": "M22 101H60L78 120L66 141L78 162L60 181H22L1 141Z",
    "cloak": "M113 185L132 196L151 185L170 197V235L132 258L94 235V197Z",
}
style = """
.mode { color:#769675; cursor:pointer; }
.panel { fill:#12381c; fill-opacity:.30; stroke:#93ad8a; stroke-opacity:.78; stroke-width:1.8; }
.panel-pattern { opacity:0; pointer-events:none; }
.panel-glow { opacity:0; fill:none; stroke:#ffc943; stroke-width:3; filter:url(#edge-glow); pointer-events:none; }
.mode.selected { color:#ffc43c; }
.mode.selected .panel { fill:#b67d13; fill-opacity:.48; stroke:#ffda50; stroke-opacity:1; stroke-width:2.1; }
.mode.selected .panel-pattern { opacity:.62; }
.mode.selected .panel-glow { opacity:.28; }
.icon { pointer-events:none; }
"""
radius = 4.8
width = math.sqrt(3) * radius
height = 3 * radius
hexagons = []
for cx, cy in [(width / 2, 0), (0, height / 2), (width, height / 2), (width / 2, height)]:
    vertices = " ".join(f"{cx + radius * math.cos(math.radians(-90+60*i)):.3f},{cy + radius * math.sin(math.radians(-90+60*i)):.3f}" for i in range(6))
    hexagons.append(f'<polygon points="{vertices}"/>')
defs = icon_symbols + f"""
<filter id="edge-glow" x="-30%" y="-30%" width="160%" height="160%"><feGaussianBlur stdDeviation=".65"/></filter>
<pattern id="active-hex" width="{width:.3f}" height="{height:.3f}" patternUnits="userSpaceOnUse">
<g fill="none" stroke="#332409" stroke-width="2.1">{"".join(hexagons)}</g></pattern>
""" + "".join(f'<clipPath id="clip-{mode}"><path d="{panels[mode]}"/></clipPath>' for mode in modes)

def wheel(state):
    pointer_angle = {"neutral": 0, "speed": 0, "strength": 90, "cloak": 180, "armor": -90}[state]
    parts = []
    for mode in modes:
        target = centers[mode]
        old_x, old_y, w, h = boxes[mode]
        x, y = target[0] - w / 2, target[1] - h / 2
        if mode == "speed":
            x, y = old_x, old_y
        selected = " selected" if state == mode else ""
        parts.append(f"""
<g class="mode{selected}" id="mode-{mode}" data-mode="{mode}" role="button" tabindex="0" aria-label="{mode.upper()}">
<path class="panel-glow" d="{panels[mode]}"/>
<path class="panel" d="{panels[mode]}"/>
<path class="panel-pattern" d="{panels[mode]}" fill="url(#active-hex)" clip-path="url(#clip-{mode})"/>
<use class="icon" href="#{mode}" x="{x}" y="{y}" width="{w}" height="{h}"/>
</g>""")
    parts.append(f"""
<g id="ring-pivot" transform="translate(132 141)" pointer-events="none">
<g id="ring-rotation">
<circle cx="0" cy="0" r="34" fill="none" stroke="#77976d" stroke-opacity=".76" stroke-width="6.4"
 stroke-dasharray="95 11.81415022" transform="rotate(-38)"/>
<animateTransform id="ring-animation" attributeName="transform" type="rotate" from="0" to="360"
 dur="10s" calcMode="linear" repeatCount="indefinite"/>
</g></g>
<path id="direction-pointer" transform="rotate({pointer_angle} 132 141)" d="M121 98L132 93L143 98" fill="none" stroke="#f1f0da" stroke-width="2.2" pointer-events="none"/>
""")
    return "".join(parts)

def svg(state, context=None):
    if context:
        if context.startswith("data:"):
            href = context
        else:
            href = f"assets/{context}"
        size = 'width="1280" height="720" viewBox="0 0 1280 720"'
        background = f'<image id="context-background" href="{href}" width="1280" height="720" preserveAspectRatio="xMidYMid slice"/>'
        content = f'{background}<g id="radial-menu" transform="translate(389.2 92.1) scale(1.9)">{wheel(state)}</g>'
    else:
        size = 'width="300" height="309" viewBox="-5 0 274 282"'
        content = f'<g id="radial-menu">{wheel(state)}</g>'
    return f"""<svg xmlns="http://www.w3.org/2000/svg" id="radial-prototype" {size} data-selected="{state}">
<title>Maquette Nanosuit 1 — V3 — {state}</title>
<desc>Prototype graphique. Anneau seul en rotation horaire uniforme, 360 degrés en dix secondes.</desc>
<defs>{defs}</defs><style>{style}</style>{content}</svg>"""

for state in ("neutral", *modes):
    (ROOT / f"radial-{state}.svg").write_text(svg(state), encoding="utf-8")
    encoded = base64.b64encode((ROOT / "assets/minecraft-context.png").read_bytes()).decode()
    (ROOT / f"{state}.svg").write_text(svg(state, "data:image/png;base64," + encoded), encoding="utf-8")

template = (ROOT / "prototype-template.html").read_text(encoding="utf-8")
(ROOT / "index.html").write_text(template.replace("<!-- RADIAL_SVG -->", svg("neutral", "minecraft-context.png")), encoding="utf-8")
print("V3 sources generated; V2 icon symbols preserved verbatim.")
