"""Browser-only animation prototype. All telemetry is simulated, never connected to Minecraft."""
from pathlib import Path
import runpy, xml.etree.ElementTree as ET, json, hashlib, shutil
from PIL import Image
ROOT=Path(__file__).resolve().parent
m=runpy.run_path(str(ROOT/'generate-v3.py'))
refs=ROOT/'references/user-2026-11-19'
archive=ROOT.parents[1]/'build/hud-classic-reference/user-originals-2026-11-19'
manifest=json.loads((refs/'manifest.json').read_text(encoding='utf-8-sig'))
# Keep a separate manifest for this additional sequence; existing source records unchanged.
records=[]
for name,ident in [('opening-middle','02170b77-2c37-4f94-979d-7bc57d2e320e'),('opening-start','200de994-ede1-4a1c-8c8c-30ffab4f6c99'),('opening-later','77b5bc58-0069-44d4-9ab2-f22f084774f5'),('fully-open','ba9e943d-5e55-4a1d-83b2-7b79b556a373')]:
    source=Path('C:/Users/nicol/AppData/Local/Temp')/f'codex-clipboard-{ident}.png'
    shutil.copy2(source,archive/f'{name}.png')
    with Image.open(source) as im:
        size=im.size;im.convert('RGB').save(refs/f'{name}.jpg',quality=94)
    records.append({'name':name,'source':str(source),'sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'dimensions':size,'provenance':'User supplied; timing not measured'})
(refs/'opening-manifest.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
root=ET.fromstring('<svg>'+m['binoculars']()+'</svg>')
frame=root[0]
for el in list(frame):
    if el.attrib.get('d','').startswith('M539'): frame.remove(el)
frame=ET.tostring(frame,encoding='unicode')
scene=f'<image href="{m["bg"]}" width="1920" height="1080"/><rect width="1920" height="1080" fill="#041018" opacity=".45"/>'
svg=f'''<svg viewBox="0 0 1920 1080" xmlns="http://www.w3.org/2000/svg">{m['defs']}<defs><clipPath id="deploy-clip"><rect id="reveal" x="510" y="540" width="900" height="0"/></clipPath></defs>{scene}{m['navigation']()}{m['status'](100,'armor','binoculars')}
<g id="deploy-frame">{frame}<g fill="#c0efa6" font-family="Bahnschrift,Arial,sans-serif" font-size="21"><text id="coords" x="820" y="281"/><text id="range" x="918" y="859"/><text x="445" y="542">ZOOM</text><text id="zoom-label" x="450" y="569">X02</text><text x="1439" y="542">MODE</text><text x="1443" y="569">VIS</text></g></g>
<g clip-path="url(#deploy-clip)"><g id="ticks"/></g></svg>'''
html='''<!doctype html><html lang="fr"><meta charset="utf-8"><title>Jumelles N1 · étude animée</title><style>body{margin:0;background:#101719;color:#d5e3d6;font:16px system-ui}main{max-width:1400px;margin:auto;padding:24px}svg{width:100%;display:block}button,input{margin:8px}button{padding:10px;background:#354e37;color:white;border:1px solid #7c9870}label{display:inline-block;margin-right:18px}p{max-width:1000px;color:#bfcfc0}</style><main><h1>Jumelles · ouverture réversible</h1><p>Maquette hors jeu, données simulées. Ouverture : apparition du texte et déploiement vertical des échelles depuis le centre. Fermeture : même progression en sens inverse, y compris en cas d’interruption. Durée proposée : 450 ms, non mesurée sur les captures.</p>
<button id="open">Ouvrir</button><button id="close">Fermer</button><button id="zoom">Zoom ×2 / ×4</button><label>Inclinaison Minecraft <input id="pitch" type="range" min="-90" max="90" value="70"></label><label>X <input id="x" type="number" value="128"></label><label>Z <input id="z" type="number" value="-304"></label><label>Distance simulée <input id="distance" type="number" value="2.16" step=".01" min="0"></label><p id="phase">Fermé</p>SVG
<p>Les graduations indiquent l’élévation : −inclinaison Minecraft, avec valeurs réfléchies au-delà de ±90°. MODE / VIS reste un libellé de référence, sans fonction supplémentaire inventée. Les coordonnées et le télémètre seront branchés aux données existantes lors d’une future intégration validée.</p><a href="index.html">Galerie HUD</a>
<script>
let progress=0,target=0,last=performance.now(),zoom=2;
const $=s=>document.querySelector(s), ns='http://www.w3.org/2000/svg';
function telemetry(){
 $('#coords').textContent='X '+$('#x').value+'   Z '+$('#z').value+'   Y 72';
 $('#range').textContent=Number($('#distance').value).toFixed(2)+' M';$('#zoom-label').textContent='X0'+zoom;
 const angle=-Number($('#pitch').value);let content='';
 for(let a=-180;a<=180;a+=2){let y=550+(angle-a)*6;if(y<290||y>837)continue;let folded=Math.round(Math.asin(Math.sin(a*Math.PI/180))*180/Math.PI);for(let x of [558,1363]){content+='<path d="M'+x+' '+y+'h'+(a%10===0?16:9)+'" stroke="#719671" stroke-width="3"/>';if(a%10===0)content+='<text x="'+(x===558?578:1320)+'" y="'+(y+6)+'" fill="#c0efa6" font-family="Bahnschrift,Arial,sans-serif" font-size="19">'+folded+'</text>';}}
 for(let x of [539,1381])content+='<path d="M'+x+' 395V784" stroke="#719671" stroke-width="3"/>';
 $('#ticks').innerHTML=content;
}
function draw(){let eased=progress*progress*(3-2*progress);$('#reveal').setAttribute('y',550-280*eased);$('#reveal').setAttribute('height',560*eased);$('#deploy-frame').style.opacity=String(eased);document.documentElement.dataset.progress=progress.toFixed(3);}
function step(now){let dt=Math.min(50,now-last);last=now;progress=Math.max(0,Math.min(1,progress+Math.sign(target-progress)*Math.min(Math.abs(target-progress),dt/450)));draw();requestAnimationFrame(step);}
$('#open').onclick=()=>{target=1;$('#phase').textContent='Ouverture · durée proposée 450 ms';};$('#close').onclick=()=>{target=0;$('#phase').textContent='Fermeture · animation inversée';};$('#zoom').onclick=()=>{zoom=zoom===2?4:2;telemetry();};for(const el of document.querySelectorAll('input'))el.oninput=telemetry;telemetry();draw();requestAnimationFrame(step);
</script></main></html>'''
(ROOT/'binocular-animation.html').write_text(html.replace('SVG',svg),encoding='utf-8')
