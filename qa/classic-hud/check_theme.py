"""Check screenshots and immutable default texture palette for the N1 color option."""
from pathlib import Path
from PIL import Image
import json,hashlib,subprocess,shutil
ROOT=Path(__file__).resolve().parents[2]
RUN=ROOT/'build/mode-overlay-qa/classic-hud-theme-violet'
OUT=Path(__file__).resolve().parent/'theme-evidence';OUT.mkdir(exist_ok=True)
log=(RUN/'console.log').read_text(errors='replace')
assert 'CLASSIC_HUD_COMPLETE captures=14' in log
assert 'CLASSIC_HUD_THEME #8C64FF' in log
checks=[]
def check(name,ok,detail):checks.append({'name':name,'result':'PASS' if ok else 'FAIL','detail':detail})
def pixels(name,box,pred):
    im=Image.open(RUN/f'game/screenshots/{name}.png').convert('RGB').crop(box)
    return sum(pred(*px) for px in im.getdata())
purple=lambda r,g,b:b>120 and r>50 and b>r*1.4 and g<r
blue=lambda r,g,b:r<35 and g>70 and b>110 and b>g*1.2
red=lambda r,g,b:r>130 and g<110 and b<100
yellow=lambda r,g,b:r>170 and g>165 and b<120
box=(1490,950,1785,1050)
check('N1 purple chrome and health',pixels('n1-100',box,purple)>1500,pixels('n1-100',box,purple))
check('Energy remains blue',pixels('n1-100',(1498,955,1619,982),blue)>900,pixels('n1-100',(1498,955,1619,982),blue))
check('Critical energy remains red',pixels('n1-15',(1498,955,1645,982),red)>100,pixels('n1-15',(1498,955,1645,982),red))
check('Battery remains yellow',pixels('nv-active',(1480,70,1730,100),yellow)>800,pixels('nv-active',(1480,70,1730,100),yellow))
check('N2 no purple interface',pixels('n2',box,purple)<10,pixels('n2',box,purple))
check('Radial purple',pixels('theme-radial',(700,300,1220,820),purple)>300,pixels('theme-radial',(700,300,1220,820),purple))
check('Radial selected amber preserved',pixels('theme-radial',(1000,430,1190,650),yellow)>500,pixels('theme-radial',(1000,430,1190,650),yellow))
unchanged=[]
for folder in ['classic_hud','nanosuit1_radial']:
    for f in (ROOT/'src/main/resources/assets/crysismod/textures/gui'/folder).glob('*.png'):
        if f.name in ('panel-fixed.png','frame-left.png','frame-right.png'):continue
        rel=f.relative_to(ROOT).as_posix()
        original=subprocess.check_output(['git','show','87cb8d3:'+rel],cwd=ROOT)
        check('Default texture '+folder+'/'+f.name,original==f.read_bytes(),hashlib.sha256(original).hexdigest());unchanged.append(rel)
for name in ['n1-100','n1-15','nv-active','n2','theme-radial']:
    shutil.copy2(RUN/f'game/screenshots/{name}.png',OUT/f'{name}.png')
manifest={'scope':'Actual RTX Minecraft captures; optional configuration injected only into isolated QA client','theme':'#8C64FF','originalTextureFilesChecked':len(unchanged),'checks':checks,'jarSha256':hashlib.sha256((RUN/'game/mods/crysis-legacy-port.jar').read_bytes()).hexdigest()}
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
(OUT/'index.html').write_text('<!doctype html><meta charset="utf-8"><title>Thème N1 violet — captures réelles</title><style>body{background:#101719;color:#ddd;font:16px system-ui}img{max-width:100%}</style><h1>Thème N1 violet</h1><p>Valeur #8C64FF, uniquement dans la copie QA. Le radial est dessiné par son renderer réel, sans test des entrées clavier.</p>'+''.join(f'<h2>{n}</h2><img src="{n}.png">' for n in ['n1-100','n1-15','nv-active','n2','theme-radial']),encoding='utf-8')
print(f'{sum(c["result"]=="PASS" for c in checks)}/{len(checks)} PASS; '+str(len(unchanged))+' original textures byte-identical')
for c in checks:
    if c['result']=='FAIL':print(c)
if any(c['result']=='FAIL' for c in checks):raise SystemExit(1)
