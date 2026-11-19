"""Design only: editable SVG HUD studies, no Minecraft resources are changed.

Regenerate with Python 3. The four core images contain simulated telemetry.
Background is explicitly a cropped/reframed QA screenshot, not an in-game HUD test.
"""
from pathlib import Path
import base64
import json
import re

ROOT = Path(__file__).resolve().parent
REPO = ROOT.parents[1]
GREEN = '#719671'

def uri(path):
    mime = 'image/jpeg' if path.suffix == '.jpg' else 'image/png'
    return f'data:{mime};base64,' + base64.b64encode(path.read_bytes()).decode()

symbols = ''.join(re.findall(r'<symbol\b.*?</symbol>', (REPO / 'src/main/resources/assets/crysismod/textures/gui/nanosuit1_radial/icons.svg').read_text(), re.S))
bg = uri(ROOT / 'assets/minecraft-scene-cropped.jpg')
terrain = uri(ROOT / 'assets/minecraft-terrain-sample.png')
defs = f'''<defs>{symbols}
<clipPath id="map-clip"><path d="M166 844L398 831V964L373 986L166 994Z"/></clipPath>
<pattern id="scan" width="4" height="4" patternUnits="userSpaceOnUse"><path d="M0 1H1920" stroke="#051c00" stroke-opacity=".15"/></pattern>
<filter id="night"><feColorMatrix values="0 .40 0 0 .015  .20 .73 .07 0 .02  0 .09 0 0 0  0 0 0 1 0"/></filter>
<filter id="map-mono"><feColorMatrix values=".14 .28 .04 0 .08 .16 .30 .05 0 .10 .13 .24 .04 0 .07 0 0 0 1 0"/></filter>
<linearGradient id="glass" x2="0" y2="1"><stop stop-color="#08131c" stop-opacity=".80"/><stop offset="1" stop-color="#08131c" stop-opacity=".65"/></linearGradient>
</defs>'''

def text(x,y,value,size=21,color='#bbefa4',width=None,opacity=1):
    stretch = f' textLength="{width}" lengthAdjust="spacingAndGlyphs"' if width else ''
    return f'<text x="{x}" y="{y}" fill="{color}" font-family="Bahnschrift,Arial,sans-serif" font-weight="600" font-size="{size}" opacity="{opacity}"{stretch}>{value}</text>'

def navigation(threat=False):
    color = '#f05d4c' if threat else GREEN
    p = f'''<g id="navigation" data-state="{'simulated-alert' if threat else 'simulated-calm'}">
<path d="M166 844V831L178 819L378 809L398 831V964L373 986L166 994Z" fill="url(#glass)" stroke="{GREEN}" stroke-width="1.5"/>
<g clip-path="url(#map-clip)"><image href="{terrain}" x="166" y="831" width="232" height="169" preserveAspectRatio="xMidYMid slice" opacity=".85" filter="url(#map-mono)"/>
<path d="M166 844L398 831" stroke="#14252a" stroke-width="3"/></g>
<g id="compass-text" transform="matrix(1 -.055 0 1 166 844)">
{text(47,-6,'S',19,GREEN,6.5)}{text(166,-6,'O',19,GREEN,6.5)}</g>
<path d="M280 921L289 911L299 922" fill="none" stroke="#a9ee88" stroke-width="2"/>
<path d="M189 864L198 881L207 864Z" fill="#82f65b"/>
<g id="threat-alert-gauge"><path d="M168 845L180 844V943H168Z" fill="#09181b" fill-opacity=".9" stroke="#527758" stroke-width="1"/>
{''.join(f'<path d="M170 {848+i*24}H178V{869+i*24}H170Z" fill="{color if threat or i==3 else chr(35)+"294338"}"/>' for i in range(4))}</g>
<path d="M165 940L201 961V998L165 1019L130 998V961Z" fill="url(#glass)" stroke="{GREEN}" stroke-width="2.3"/>
<path d="M126 982V1001L163 1026M130 954L164 934" fill="none" stroke="{GREEN}" stroke-width="1.8"/>
<g transform="translate(165 981) scale({1.65 if threat else 1.3})"><path d="M0 -10L9 7H-9Z" fill="none" stroke="{color}" stroke-width="1.8"/>
<path d="M0 -5V1M0 3V5" stroke="{color}" stroke-width="2"/></g>'''
    if threat:
        p += '<path d="M327 891L334 902L340 888Z" fill="#f15e4b"/><path d="M356 918L363 929L369 915Z" fill="#f3d25e"/>'
    return p + '</g>'

def status(energy, mode, ammo=True):
    # Screen-height scale of the supplied Remastered reference (1596 -> 1080).
    ecolor = '#d84939' if energy <= 15 else '#0075aa'
    ewidth = 116 * energy / 100
    p = f'''<g id="suit-status" data-energy="{energy}" data-health="100" data-mode="{mode}">
<path d="M1477 946L1489 933L1665 940L1708 947V999L1486 990L1477 982Z" fill="url(#glass)" stroke="#4d6743" stroke-width="1.4"/>
<path d="M1490 940L1606 945V960L1490 955Z" fill="#003c57"/>
<path d="M1490 940L{1490+ewidth:.3f} {940+5*energy/100:.3f}V{955+5*energy/100:.3f}L1490 955Z" fill="{ecolor}"/>
<path d="M1490 963L1606 968V984L1490 979Z" fill="#7bc926"/>
<path d="M1610 945L1637 946V962L1610 961Z" fill="{ecolor}"/><path d="M1610 968L1637 969V985L1610 984Z" fill="#7bc926"/>
<path d="M1624 948L1620 954H1624L1622 960L1628 952H1624Z" fill="#c8eece"/>
<path d="M1624 972V981M1620 976H1628" fill="none" stroke="#d6f4a8" stroke-width="2.5"/>
{text(1650,964,str(energy).rjust(3,'0'),25,'#bded9e',22)}{text(1650,986,'100',25,'#bded9e',22)}
<path d="M1749 945L1785 967V1009L1749 1032L1713 1009V968Z" fill="url(#glass)" stroke="#526e4e" stroke-width="1.7"/>
<path d="M1713 963L1749 940L1788 963M1788 998V1016L1751 1039" fill="none" stroke="#526e4e" stroke-width="1.4"/>
<use href="#{mode}" x="1730" y="969" width="38" height="45" color="{GREEN}"/>
</g>'''
    if ammo:
        p += f'''<g id="ammo" data-adapter="simulated-tacz">
<path d="M1570 864L1583 852L1706 857L1722 871V935L1570 927Z" fill="url(#glass)" stroke="#4d6743" stroke-width="1"/>
<path d="M1570 891L1722 899M1665 858V931" stroke="#556953" stroke-width="1" opacity=".7"/>
<g id="ammo-counter-text" transform="matrix(1 .052 0 1 1570 852)">
{text(12,31,'<tspan opacity=".35">0</tspan>40',30,'#c9edaf',35)}{text(61,26,'198',22,GREEN,26)}</g>
<g fill="{GREEN}" opacity=".85">{''.join(f'<path d="M{x} {864+.052*(x-1671):.2f}L{x+2} {860+.052*(x-1671):.2f}L{x+4} {864+.052*(x-1671):.2f}V{883+.052*(x-1671):.2f}H{x}Z"/>' for x in range(1671,1706,5))}</g>
</g>'''
    return p

def battery():
    p = '<g id="night-vision-battery" data-charge="0.63"><path d="M1585 71L1599 57H1771L1787 73" fill="none" stroke="#719671" stroke-width="1.5" opacity=".7"/>'
    for i in range(4):
        x = 1601+i*38
        part = max(0,min(1,.63*4-i))
        p += f'<path d="M{x} 73L{x+35} 71V82L{x} 84Z" fill="#565728"/>'
        if part:
            p += f'<path d="M{x} 73L{x+35*part:.2f} {73-2*part:.2f}V{84-2*part:.2f}L{x} 84Z" fill="#eef13b"/>'
    return p + '</g>'

def make(state, energy=100, mode='armor', threat=False, night=False, visor=False):
    p = f'''<svg xmlns="http://www.w3.org/2000/svg" width="1920" height="1080" viewBox="0 0 1920 1080">
<title>Maquette HUD Nanosuit 1 — {state}</title>
<desc>Composition graphique hors jeu. Toutes les valeurs et menaces sont simulées. Fond Minecraft QA recadré. Aucun nouveau HUD intégré.</desc>
{defs}<g id="scene"{' filter="url(#night)"' if night else ''}><image href="{bg}" width="1920" height="1080"/><rect width="1920" height="1080" fill="#06111b" opacity=".20"/></g>'''
    if night:
        p += '<rect width="1920" height="1080" fill="url(#scan)"/>'
    p += '<g id="visor-corner-details" fill="none" stroke="#719671" stroke-width="1.8" opacity=".45"><path d="M125 87L137 73H183M1735 73H1778L1792 87"/></g>'
    p += navigation(threat) + status(energy,mode,not visor)
    if night: p += battery()
    if visor:
        p += f'''<g id="binocular-study" data-fidelity="provisional-not-directly-observed" fill="none" stroke="{GREEN}">
<path d="M766 458H728V499M1192 458H1230V499M728 580V620H766M1192 620H1230V580" stroke-width="2"/>
<path d="M924 540H949M971 540H996M960 515V530M960 550V565"/>
<path d="M764 665H1156M764 660V671M862 662V668M960 660V671M1058 662V668M1156 660V671"/>
</g>{text(744,438,'4×',24,GREEN)}{text(1110,438,'083 m',23,GREEN)}'''
    else:
        p += '<path d="M953 540H958M962 540H967M960 533V538M960 542V547" stroke="#bce5b4" stroke-width="1.2" opacity=".8"/>'
    p += '</svg>'
    (ROOT/f'hud-{state}.svg').write_text(p,encoding='utf-8')

make('normal')
make('critical',energy=12,mode='cloak')
make('night-vision',mode='strength',night=True)
make('threat',energy=73,mode='speed',threat=True)
make('binocular-study',visor=True)
(ROOT/'simulated-states.json').write_text(json.dumps({
    'purpose':'Design only; not runtime observations or test results',
    'canvas':[1920,1080],
    'states':{'normal':{'energy':100,'health':100,'mode':'armor'},'critical':{'energy':12,'health':100,'mode':'cloak'},'night-vision':{'energy':100,'health':100,'mode':'strength','nvBattery':0.63},'threat':{'energy':73,'health':100,'mode':'speed','detectionRisk':'simulated-high','enemyAlert':'simulated-high'}},
    'font':'Local Bahnschrift, approximate; original game font not extracted',
    'reference':'references/user-remastered-full.png; see RAPPORT.md for measurement limits'
},ensure_ascii=False,indent=2),encoding='utf-8')
print('Generated five editable SVG design studies.')
