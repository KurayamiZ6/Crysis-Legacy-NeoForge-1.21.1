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
# Basic editable silhouettes from the supplied low-resolution references. All are neutral tint masks.
for kind,body in {
 'shell':'<path d="M8 16H13V32H8Z M15 17H48V31H15Z" fill="white"/>',
 'rocket':'<path d="M5 14L15 19H38L54 24L38 29H15L5 34L9 24Z" fill="white"/>',
 'grenade_round':'<g transform="rotate(-32 30 24)"><path d="M11 16H16V32H11Z M18 17H39Q49 24 39 31H18Z" fill="white"/></g>'
}.items():
 for mode in ['semi','auto','burst']:
  if mode=='semi':shape=body
  else:shape=''.join(f'<g transform="translate(0 {y}) scale(1 .29)">{body}</g>' for y in [1,15,29])
  if mode=='burst':shape+='<path d="M8 46H53" fill="none" stroke="white" stroke-width="2"/>'
  write(kind+'-'+mode,60,48,'0 0 60 48',shape)
write('throwable-frag',40,40,'0 0 40 40','<path d="M17 6H23V12Q31 16 30 25Q29 36 20 37Q10 36 10 25Q9 16 17 12Z M22 5L30 11L32 22L29 23L27 13L21 9Z" fill="white"/><circle cx="16" cy="6" r="3" fill="none" stroke="white" stroke-width="2"/>')
write('throwable-smoke',40,40,'0 0 40 40','<g transform="rotate(-32 20 20)"><path d="M15 5H25V9H15Z M13 11H27V35H13Z M22 4L29 11V19H27V12L21 7Z" fill="white"/><circle cx="16" cy="5" r="3" fill="none" stroke="white" stroke-width="2"/></g>')
write('throwable-flash',40,40,'0 0 40 40','<g transform="rotate(-32 20 20)"><path d="M15 5H25V9H15Z M13 11H27V35H13Z" fill="white"/><path d="M16 16H24M16 23H24M16 30H24" stroke="#333" stroke-width="2"/></g>')
write('throwable-incendiary',40,40,'0 0 40 40','<g transform="rotate(-32 20 20)"><path d="M17 3H23V13L27 17V35H13V17L17 13Z M20 3Q26 1 28 7L25 8Q24 4 20 5Z" fill="white"/></g>')
