"""V3 map frame only: no baked terrain, goal, enemy or threat value."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'src/main/resources/assets/crysismod/textures/gui/classic_map';OUT.mkdir(exist_ok=True)
def write(name,w,h,view,body):
 (OUT/f'{name}.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="{view}">{body}</svg>',encoding='utf-8')
write('frame',600,500,'120 809 300 250','''<g stroke="white" stroke-width="1.5" fill="none">
<path d="M166 844V831L178 819L378 809L398 831V964L373 986L166 994Z"/>
<path d="M166 844V831L178 819L378 809L398 831Z" fill="#111111" fill-opacity=".88"/>
<path d="M165 940L201 961V998L165 1019L130 998V961Z" fill="#111111" fill-opacity=".8" stroke-width="2"/>
<path d="M126 982V1001L163 1026M130 954L164 934" stroke-width="1.8"/>
</g>''')
write('player',96,130,'0 0 48 65','''<path d="M24 46.5L6 0H42Z" fill="#e0edd7" opacity=".18"/><path d="M17 53L24 46.5L31 53" fill="none" stroke="#dcefc7" stroke-width="2"/>''')
print('Exported map chrome and player marker, without simulated cartography.')
write('threat',96,96,'-12 -12 24 24','''<path d="M0 -10L9 7H-9Z" fill="none" stroke="white" stroke-width="2.3"/><path d="M0 -5V1M0 3V5" stroke="white" stroke-width="2.4"/>''')
