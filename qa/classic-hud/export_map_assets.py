"""V3 map frame only: no baked terrain, goal, enemy or threat value."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'src/main/resources/assets/crysismod/textures/gui/classic_map';OUT.mkdir(exist_ok=True)
def write(name,w,h,view,body):
 (OUT/f'{name}.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="{view}">{body}</svg>',encoding='utf-8')
write('background',600,500,'120 809 300 250','''<g fill="#202020">
<path d="M166 844V831L178 819L378 809L398 831Z" fill-opacity=".88"/>
<path d="M165 940L201 961V998L165 1019L130 998V961Z" fill-opacity=".8"/>
</g>''')
write('frame',600,500,'120 809 300 250','''<g stroke="white" stroke-width="1.5" fill="none">
<path d="M166 933.64V831L178 819M398 964L373 986L207 994"/>
<path d="M165 940L201 961V998L165 1019L130 998V961Z" stroke-width="2"/>
<path d="M126 982V1001L163 1026M130 954L164 934" stroke-width="1.8"/>
</g>''')
write('player',464,326,'0 0 232 163','''<path d="M116 81.5L87.6 8.1L145.7 4.8Z" fill="#e0edd7" opacity=".18"/>''')
write('player-arrow',464,326,'0 0 232 163','<path d="M107.6 89.3L116 81.5L124.4 89.3" fill="none" stroke="white" stroke-width="2.3"/>')
print('Exported map chrome and player marker, without simulated cartography.')
write('threat',96,96,'-12 -12 24 24','''<path d="M0 -10L9 7H-9Z" fill="none" stroke="white" stroke-width="2.3"/><path d="M0 -5V1M0 3V5" stroke="white" stroke-width="2.4"/>''')
