"""Exact static V3 binocular polygons; permanent upper-corner strokes deliberately excluded."""
from pathlib import Path
import html
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'src/main/resources/assets/crysismod/textures/gui/classic_binoculars';OUT.mkdir(exist_ok=True)
def write(name,w,h,view,body):
 (OUT/f'{name}.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="{view}">{body}</svg>',encoding='utf-8')
write('frame',1920,1080,'0 0 1920 1080','''<g fill="none" stroke="white">
<path d="M255 0L325 74Q960 105 1590 74L1660 0M300 1080L330 1050Q960 1026 1590 1050L1620 1080" opacity=".55" stroke-width="2"/>
<path d="M465 310L495 269L605 278L647 310Q960 330 1273 310L1315 278L1425 269L1455 310M466 808L495 850L605 842L647 806L770 800V815Q960 795 1150 815V800L1273 806L1315 842L1425 850L1455 808" opacity=".7" stroke-width="1.8"/>
<path d="M587 340L620 310L685 316M1235 316L1300 310L1333 340M587 774L620 805L685 799M1235 799L1300 805L1333 774" opacity=".55"/>
<path d="M935 526L943 518H953M967 518H977L985 526M935 554L943 562H953M967 562H977L985 554" stroke-width="2" opacity=".7"/>
<path d="M423 550H503V573H423Z M1418 550H1498V573H1418Z"/>
</g>''')
write('label-background',1920,1080,'0 0 1920 1080','<path d="M423 550H503V573H423Z M1418 550H1498V573H1418Z" fill="#202020" fill-opacity=".18"/>')
write('glyphs',1888,80,'0 0 944 40',''.join(f'<text x="{(i-32)*16+1}" y="31" fill="white" font-family="Bahnschrift,Arial,sans-serif" font-weight="600" font-size="31" textLength="14" lengthAdjust="spacingAndGlyphs">{html.escape(chr(i))}</text>' for i in range(32,91)))
write('brackets',1920,1080,'0 0 1920 1080','<path d="M539 395L525 408V420H539V759H525V771L539 784M1381 395L1395 408V420H1381V759H1395V771L1381 784" fill="white" stroke="white" stroke-width="1"/>')
print('Exported binocular frame and live-telemetry glyph atlas.')
write('vignette',1920,1080,'0 0 1920 1080','''<defs><radialGradient id="corners" cx="50%" cy="50%" r="70.710678%"><stop offset="72.125%" stop-color="black" stop-opacity="0"/><stop offset="82%" stop-color="black" stop-opacity=".06"/><stop offset="92%" stop-color="black" stop-opacity=".22"/><stop offset="100%" stop-color="black" stop-opacity=".32"/></radialGradient></defs><rect width="1920" height="1080" fill="url(#corners)"/>''')
