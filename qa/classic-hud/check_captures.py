"""Pixel checks of actual Minecraft screenshots; no mocked renderer."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,re,shutil,hashlib
ROOT=Path(__file__).resolve().parents[2]
RUN=ROOT/'build/mode-overlay-qa/classic-hud-final'
BEFORE=ROOT/'build/mode-overlay-qa/classic-hud-before'
OUT=Path(__file__).resolve().parent/'evidence'
OUT.mkdir(exist_ok=True)
log=(RUN/'console.log').read_text(errors='replace')
assert 'CLASSIC_HUD_COMPLETE captures=14' in log,'Capture session incomplete'
records=[]
for line in log.splitlines():
    if line.startswith('CLASSIC_HUD_CAPTURE '):records.append(line)
images={}
for f in sorted((RUN/'game/screenshots').glob('*.png')):
    shutil.copy2(f,OUT/f.name);images[f.stem]=Image.open(f).convert('RGB')
for name in ['n1-100','n1-25','n1-15','n1-zero','n2','no-suit']:
    shutil.copy2(BEFORE/f'game/screenshots/{name}.png',OUT/f'before-{name}.png')
checks=[]
def check(name,ok,detail):
    checks.append({'name':name,'result':'PASS' if ok else 'FAIL','detail':detail})
def count(name,box,criterion):return sum(criterion(*px) for px in images[name].crop(box).getdata())
blue=lambda r,g,b:r<35 and g>70 and b>110 and b>g*1.2
red=lambda r,g,b:r>130 and g<110 and b<100
green=lambda r,g,b:g>120 and g>r*1.3 and b<90
yellow=lambda r,g,b:r>170 and g>165 and b<120
box=(1498,955,1619,982)
full=count('n1-100',box,blue)
check('Full energy fill',full>900,full)
for name,want,predicate in [('n1-25',.25,blue),('n1-15',.15,red),('n1-zero',0,red)]:
    ratio=count(name,box,predicate)/full
    check(name+' fill',abs(ratio-want)<.04,{'observedRatio':ratio,'expected':want})
ratio=count('n1-recharge',box,blue)/full
check('Actual recharge visible',.1<ratio<.95,ratio)
health=(1498,980,1619,1006)
ratio=count('health-half',health,green)/count('n1-100',health,green)
check('Real health 50 percent',abs(ratio-.5)<.06,ratio)
battery=(1480,70,1730,100)
check('NV active battery visible',count('nv-active',battery,yellow)>800,count('nv-active',battery,yellow))
check('NV recharging battery visible',count('nv-recharge',battery,yellow)>800,count('nv-recharge',battery,yellow))
check('Battery hidden after full charge',count('n1-100',battery,yellow)==0,count('n1-100',battery,yellow))
check('Battery hidden for N2',count('n2',battery,yellow)==0,count('n2',battery,yellow))
check('Battery hidden without complete suit',count('no-suit',battery,yellow)==0,count('no-suit',battery,yellow))
guiFull=count('n1-gui2',box,blue)
check('Energy size stable GUI 2 vs 3',abs(guiFull-full)<full*.07,{'gui2':guiFull,'gui3':full})
check('N1 panel absent without suit',count('no-suit',box,blue)==0,count('no-suit',box,blue))
try:font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',20)
except OSError:font=ImageFont.load_default()
plate=Image.new('RGB',(1500,990),'#101719');draw=ImageDraw.Draw(plate)
names=['n1-100','n1-25','n1-15','n1-zero','n1-recharge','health-half','nv-active','nv-recharge','n2','no-suit','n1-gui2','n1-strength']
for i,name in enumerate(names):
    x=i%3*500;y=i//3*245
    draw.text((x+8,y+8),name,fill='#d2e5c9',font=font)
    im=images[name].copy();im.thumbnail((490,215));plate.paste(im,(x+5,y+35))
plate.save(OUT/'overview.png')
plate=Image.new('RGB',(1440,900),'#101719');draw=ImageDraw.Draw(plate)
for i,(label,im) in enumerate([('Crysis Remastered — référence utilisateur',Image.open(ROOT/'design/nanosuit-1-hud/references/user-2026-11-19/detail-status.jpg')),('Minecraft — avant',Image.open(OUT/'before-n1-100.png').crop((1400,900,1820,1080))),('Minecraft — N1 intégrée',images['n1-100'].crop((1455,940,1800,1065)))]):
    draw.text((15,i*300+10),label,fill='#d2e5c9',font=font)
    im=im.convert('RGB');im.thumbnail((1100,245));plate.paste(im,(20,i*300+40))
plate.save(OUT/'comparison.png')
for name in ['n1-100','n1-15','nv-active','nv-recharge','n2','no-suit','n1-gui2','health-half']:
    images[name].crop((1455,940,1800,1065)).resize((690,250)).save(OUT/f'detail-{name}.png')
manifest={'scope':'Real Minecraft screenshots, scene controlled by isolated QA mod','referenceHead':'6f4dc9a','observedClientTelemetry':records,'pixelChecks':checks,'jarSha256':hashlib.sha256((RUN/'game/mods/crysis-legacy-port.jar').read_bytes()).hexdigest(),'rendererEvidence':[s for s in (RUN/'game/logs/latest.log').read_text(errors='replace').splitlines() if 'OpenGL Renderer:' in s or 'OpenGL Version:' in s]}
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
(OUT/'index.html').write_text('<!doctype html><meta charset="utf-8"><title>HUD N1 — captures réelles</title><style>body{background:#101719;color:#d2e5c9;font:17px system-ui}img{max-width:100%}section{margin:30px}</style><h1>HUD N1 : captures Minecraft RTX</h1><p>Scène préparée dans une copie isolée, données synchronisées réelles. Aucun HUD de minicarte ou d’armes ajouté.</p><a href="manifest.json">Mesures et provenance</a><img src="comparison.png"><img src="overview.png">'+''.join(f'<section><h2>{name}</h2><a href="{name}.png"><img src="{name}.png"></a></section>' for name in images),encoding='utf-8')
print(json.dumps(checks,ensure_ascii=False,indent=2))
if any(c['result']=='FAIL' for c in checks):raise SystemExit(1)
