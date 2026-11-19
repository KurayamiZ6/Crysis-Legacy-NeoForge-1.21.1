"""Export V3 ammunition geometry, without baked ammunition or fictional weapon data."""
from pathlib import Path
OUT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/crysismod/textures/gui/classic_weapon'
OUT.mkdir(exist_ok=True)
def write(name,w,h,view,body):
 (OUT/f'{name}.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{w*2}" height="{h*2}" viewBox="{view}">{body}</svg>',encoding='utf-8')
write('frame',440,125,'0 0 440 125','''<path d="M132 13L150 0L349 8L366 26V119L144 108L132 96Z" fill="#111111" fill-opacity=".8"/><path d="M349 8L366 26V119M132 49L366 61M278 6V113" fill="none" stroke="white" stroke-width="1.7" opacity=".78"/>''')
write('bullet',18,48,'305 6 18 48','<path d="M311 20Q311 9 315 8Q319 9 319 20V52H311Z" fill="white"/>')
write('infinity',45,34,'0 0 45 34','<path d="M3 17C3 0 22 18 27 20C48 35 50 0 31 10L16 23C2 32 0 13 7 10C16 5 29 28 39 22" fill="none" stroke="white" stroke-width="2"/>')
write('strip',230,125,'0 0 230 125','<path d="M0 15L20 0H226V62H0Z" fill="#111111" fill-opacity=".8"/><path d="M224 6V59" stroke="white" stroke-width="3"/>')
bullets=''.join(f'<path d="M{x} 14Q{x} 3 {x+4} 2Q{x+8} 3 {x+8} 14V40H{x}Z" fill="white"/>' for x in [7,25,43])
write('auto',60,48,'0 0 60 48',bullets)
write('burst',60,48,'0 0 60 48',bullets+'<path d="M7 44H51" stroke="white" stroke-width="2"/>')
