"""Editable design studies from user screenshots, 19 November 2026. No runtime assets."""
from pathlib import Path
import base64, json, re
ROOT = Path(__file__).resolve().parent
GREEN = '#719671'
def uri(p):
    return 'data:image/jpeg;base64,' + base64.b64encode(p.read_bytes()).decode()
bg = uri(ROOT/'assets/minecraft-scene-cropped.jpg')
terrain = 'data:image/png;base64,' + base64.b64encode((ROOT/'assets/minecraft-terrain-sample.png').read_bytes()).decode()
symbols = ''.join(re.findall(r'<symbol\b.*?</symbol>', (ROOT.parents[1]/'src/main/resources/assets/crysismod/textures/gui/nanosuit1_radial/icons.svg').read_text(), re.S))
def txt(x,y,s,size=20,color='#c0efa6',width=None):
    length = f' textLength="{width}" lengthAdjust="spacingAndGlyphs"' if width else ''
    return f'<text x="{x}" y="{y}" fill="{color}" font-family="Bahnschrift,Arial,sans-serif" font-weight="600" font-size="{size}"{length}>{s}</text>'
defs = f'''<defs>{symbols}<linearGradient id="glass" x2="0" y2="1"><stop stop-color="#030b12" stop-opacity=".88"/><stop offset="1" stop-color="#030b12" stop-opacity=".68"/></linearGradient>
<clipPath id="map"><path d="M166 844L398 831V964L373 986L166 994Z"/></clipPath>
<pattern id="scan" width="4" height="4" patternUnits="userSpaceOnUse"><path d="M0 1H4" stroke="#061600" stroke-opacity=".25"/></pattern>
<filter id="nv"><feColorMatrix values="0 .55 0 0 .025 .25 .9 .10 0 .05 0 .04 0 0 0 0 0 0 1 0"/></filter>
<filter id="mono"><feColorMatrix values=".17 .34 .05 0 .06 .18 .35 .05 0 .08 .16 .30 .04 0 .05 0 0 0 1 0"/></filter>
<radialGradient id="vignette"><stop offset=".85" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#000" stop-opacity=".18"/></radialGradient></defs>'''

def navigation(state='calm'):
    color={'calm':GREEN,'detected':'#ed3029','lost-orange':'#ff670c','lost-yellow':'#e8d653'}[state]
    alert=state!='calm'
    p=f'''<g id="navigation" data-visibility="{state}"><path d="M166 844V831L178 819L378 809L398 831V964L373 986L166 994Z" fill="url(#glass)" stroke="{GREEN}" stroke-width="1.5"/>
<g clip-path="url(#map)"><image href="{terrain}" x="166" y="831" width="232" height="169" preserveAspectRatio="xMidYMid slice" filter="url(#mono)"/>
<path d="M287 922L259 842L315 841Z" fill="#e0edd7" opacity=".18"/>
<path d="M279 925L288 914L298 924" fill="none" stroke="#dcefc7" stroke-width="2.2"/>
<circle cx="365" cy="907" r="8" fill="#81f65f" opacity=".17"/><circle cx="365" cy="907" r="4" fill="#81f65f" opacity=".65"/>
<path d="M294 895L291 906L286 895" fill="none" stroke="{'#f15c49' if state=='detected' else '#e4d95b'}" stroke-width="2.6"/>
<path d="M328 912L338 916L329 919" fill="none" stroke="#e4d95b" stroke-width="2.6"/>
<g id="global-alert-bar">{''.join(f'<path d="M169 {848+i*24}H181V{869+i*24}H169Z" fill="{chr(35)+"e54c38" if alert else chr(35)+"344b36"}"/>' for i in range(4))}</g></g>
<g transform="matrix(1 -.055 0 1 166 844)">{txt(38,-6,'E',19,GREEN,7)}{txt(174,-6,'S',19,GREEN,7)}</g>
<path d="M165 940L201 961V998L165 1019L130 998V961Z" fill="url(#glass)" stroke="{GREEN}" stroke-width="2"/>
<path d="M126 982V1001L163 1026M130 954L164 934" fill="none" stroke="{GREEN}" stroke-width="1.8"/>
<g id="visibility-triangle" transform="translate(165 981) scale({1.75 if state in ('detected','lost-orange') else 1.1})" color="{color}"><path d="M0 -10L9 7H-9Z" fill="none" stroke="currentColor" stroke-width="2.3"/><path d="M0 -5V1M0 3V5" stroke="currentColor" stroke-width="2.4"/></g></g>'''
    return p

def status(energy=100,mode='armor',weapon='pistol',swap=False):
    # Local coordinate tracing of detail-status.jpg; same geometry for all states.
    p=f'''<g id="status" transform="translate(1483 880) scale(.70)">
<path d="M12 121L31 105L366 120L305 154V197L19 183L7 171Z" fill="url(#glass)" stroke="#58704d" stroke-width="1.6"/>
<path d="M24 117L189 123V143L24 137Z" fill="#003549"/>
<path d="M24 117L{24+165*energy/100:.3f} {117+6*energy/100:.3f}V{137+6*energy/100:.3f}L24 137Z" fill="{'#dd4133' if energy<=15 else '#0076b0'}"/>
<path d="M24 145L189 151V172L33 165L24 156Z" fill="#7dca2b"/>
<path d="M196 123L237 125V145L196 143Z" fill="#0076b0"/><path d="M196 151L237 153V174L196 172Z" fill="#7dca2b"/>
<path d="M217 126L213 134H217L215 142L222 131H218Z" fill="#d4eee0"/>
<path d="M217 156V167M212 161H222" stroke="#d4eead" stroke-width="3"/>
<path d="M20 124V138M20 146V158M49 140V147" stroke="#77935d" stroke-width="1.5"/>
{txt(256,146,str(energy),31,width=32)}{txt(256,175,'100',31,width=32)}
<path d="M366 125L416 155V211L366 242L315 211V155Z" fill="url(#glass)" stroke="#62815b" stroke-width="2"/>
<path d="M316 148L366 118L419 148M419 201V220L366 251" fill="none" stroke="#62815b" stroke-width="1.7"/>
<use href="#{mode}" x="342" y="162" width="48" height="65" color="{GREEN}"/>
<g id="ammo"><path d="M132 13L150 0L349 8L366 26V119L144 108L132 96Z" fill="url(#glass)"/>
<path d="M349 8L366 26V119M132 49L366 61M278 6V113" fill="none" stroke="#58704d" stroke-width="1.7"/>
<g transform="matrix(1 .045 0 1 145 0)">{txt(4,39,'<tspan opacity=".3">0</tspan>21' if weapon=='pistol' else '<tspan opacity=".3">00</tspan>0',35,width=51)}{txt(89,30,'78' if weapon=='pistol' else '0',27,width=27 if weapon=='pistol' else 13)}</g>'''
    if weapon=='pistol':
        p+='<path d="M311 20Q311 9 315 8Q319 9 319 20V52H311Z" fill="#719671"/>'
    elif weapon=='fists':
        p+='<path d="M291 31C285 10 317 31 324 24C340 9 355 33 339 36C327 39 304 15 295 24C283 38 304 43 320 28" fill="none" stroke="#719671" stroke-width="3"/>'
    p+='</g>'
    if swap:
        p+=f'''<g id="weapon-change-strip" transform="translate(130 -121)"><path d="M0 15L20 0H226V62H0Z" fill="url(#glass)"/>
<path d="M224 6V59" stroke="{GREEN}" stroke-width="3"/>
<use href="#strength" x="133" y="4" width="43" height="51" color="{GREEN}"/>
<path d="M158 78H206V91H186L196 115H179L170 92H158Z" fill="{GREEN}"/></g>'''
    return p+'</g>'

def battery():
    p='<g id="night-vision-battery" data-simulated-charge="0.63">'
    # Empty cells at left, remaining charge toward right, matching supplied frames.
    for i in range(4):
        x=1490+i*58
        part=max(0,min(1,.63*4-(3-i)))
        p+=f'<path d="M{x} 83H{x+54}V97H{x}Z" fill="#6c6b25" opacity=".7"/>'
        if part:
            start=x+54*(1-part)
            p+=f'<path d="M{start} 83H{x+54}V97H{start}Z" fill="#f3f649"/>'
    return p+'</g>'

def binoculars(target=None):
    p=f'''<g id="binoculars" fill="none" stroke="{GREEN}">
<path d="M255 0L325 74Q960 105 1590 74L1660 0M300 1080L330 1050Q960 1026 1590 1050L1620 1080" opacity=".55" stroke-width="2"/>
<path d="M465 310L495 269L605 278L647 310Q960 330 1273 310L1315 278L1425 269L1455 310M466 808L495 850L605 842L647 806L770 800V815Q960 795 1150 815V800L1273 806L1315 842L1425 850L1455 808" opacity=".7" stroke-width="1.8"/>
<path d="M587 340L620 310L685 316M1235 316L1300 310L1333 340M587 774L620 805L685 799M1235 799L1300 805L1333 774" opacity=".55"/>
<path d="M932 535L940 527H952M968 527H980L988 535M932 567L940 575H952M968 575H980L988 567" stroke-width="2" opacity=".7"/>
<path d="M539 395L525 408V420H539V759H525V771L539 784M1381 395L1395 408V420H1381V759H1395V771L1381 784" fill="{GREEN}" stroke-width="1"/>
<path d="M423 550H503V573H423Z M1418 550H1498V573H1418Z" fill="#719671" fill-opacity=".18"/>
</g>{txt(445,542,'ZOOM',19,width=36)}{txt(450,569,'X02',20,width=28)}{txt(1439,542,'MODE',19,width=37)}{txt(1443,569,'VIS',20,width=27)}
{txt(829,281,'X 128  Z −304',21,width=114)}{txt(1007,281,'Y 72',21,width=60)}{txt(918,859,'12.70 M',29,width=87)}'''
    for x,side in [(558,1),(1363,-1)]:
        for i in range(39):
            y=291+i*14
            p+=f'<path d="M{x} {y}h{12 if i%5==0 else 8}" stroke="{GREEN}" stroke-width="3" opacity=".85"/>'
        for i in range(8):
            p+=txt(x+19 if side==1 else x-37,323+i*70,str(30-10*i),18,width=26)
    if target:
        # Explicit graphical placeholder, not a game model or verified mob highlighting.
        c='#f45d51' if target=='red' else '#edd37b'
        p+=f'<g id="simulated-target-outline"><path d="M935 392L922 416L925 452L901 471L877 489L863 552L897 568L921 550L915 609L895 677L906 715L936 713L952 635L972 635L985 713L1015 714L1026 680L1003 602L1003 534L1030 566L1050 544L1028 489L994 465L983 451L988 419L972 393Z" fill="{c}" fill-opacity=".12" stroke="{c}" stroke-width="2.5"/></g>'
    return p

states={
 'normal':{}, 'fists-rest':{'weapon':'fists'}, 'weapon-switch':{'weapon':'fists','swap':True},
 'critical':{'energy':12,'mode':'cloak'}, 'night-vision':{'night':True,'weapon':'fists'},
 'threat':{'visibility':'detected','energy':73}, 'lost-orange':{'visibility':'lost-orange','energy':73},
 'lost-yellow':{'visibility':'lost-yellow','energy':73},
 'binocular-study':{'visor':True,'target':'yellow','visibility':'lost-yellow'},
 'night-binoculars':{'visor':True,'night':True},
}
for state,opts in states.items():
    night=opts.get('night',False); visor=opts.get('visor',False)
    svg=f'''<svg xmlns="http://www.w3.org/2000/svg" width="1920" height="1080" viewBox="0 0 1920 1080"><title>Maquette V3 — {state}</title><desc>Design hors jeu. Données, scène, ennemi et états simulés; aucune intégration Minecraft. Coordonnées adaptées à Minecraft.</desc>{defs}
<g {'filter="url(#nv)"' if night else ''}><image href="{bg}" width="1920" height="1080"/><rect width="1920" height="1080" fill="#06111b" opacity=".25"/></g>'''
    if night or visor: svg+='<rect width="1920" height="1080" fill="url(#scan)"/><rect width="1920" height="1080" fill="url(#vignette)"/>'
    svg+='<path d="M125 87L137 73H183M1735 73H1778L1792 87" fill="none" stroke="#719671" stroke-width="1.8" opacity=".45"/>'
    svg+=navigation(opts.get('visibility','calm'))+status(opts.get('energy',100),opts.get('mode','armor'),'binoculars' if visor else opts.get('weapon','pistol'),opts.get('swap',False))
    if visor: svg+=binoculars(opts.get('target'))
    elif opts.get('weapon','pistol')=='pistol': svg+='<circle cx="960" cy="540" r="13" fill="none" stroke="#719671"/><path d="M960 520V532M960 548V560M940 540H952M968 540H980" stroke="#719671"/>'
    if night: svg+=battery()
    (ROOT/f'hud-{state}.svg').write_text(svg+'</svg>',encoding='utf-8')
(ROOT/'simulated-states.json').write_text(json.dumps({'version':3,'scope':'Design only, all telemetry simulated','states':states,'timing':{'lossToYellowMs':500,'source':'User observation; not independently measured','pulseAndShrink':'Proposed design durations, not measured'}},ensure_ascii=False,indent=2),encoding='utf-8')
print(f'Generated {len(states)} editable V3 studies.')
