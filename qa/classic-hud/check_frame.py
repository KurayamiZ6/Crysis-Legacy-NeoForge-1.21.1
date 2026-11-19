"""Permanent N1 frame checks against real Minecraft captures and V3 stroke resources."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,hashlib,shutil
ROOT=Path(__file__).resolve().parents[2]
RUN=ROOT/'build/mode-overlay-qa/classic-hud-frame-final'
OUT=Path(__file__).resolve().parent/'frame-evidence';OUT.mkdir(exist_ok=True)
log=(RUN/'console.log').read_text(errors='replace')
assert 'CLASSIC_HUD_COMPLETE captures=17' in log
checks=[]
def check(name,ok,detail):checks.append({'name':name,'result':'PASS' if ok else 'FAIL','detail':detail})
def contrast(im,x0,x1):
    values=[]
    for x in range(x0,x1):
        backdrop=(im.getpixel((x,68))[2]+im.getpixel((x,79))[2])/2
        # Over a dark visor vignette, the same translucent stroke brightens the backdrop.
        values.append(max(abs(backdrop-im.getpixel((x,y))[2]) for y in range(71,76)))
    return sum(values)/len(values)
present=['n1-100','n1-strength','n1-speed','n1-cloak','nv-active','nv-recharge','n1-gui2','n1-restored','n1-visor']
absent=['n1-f1','no-suit']
for name in present+absent:
    im=Image.open(RUN/f'game/screenshots/{name}.png').convert('RGB')
    scores=[contrast(im,140,180),contrast(im,1738,1774)]
    check(name+' left/right',all(x>3 for x in scores) if name in present else all(abs(x)<2 for x in scores),scores)
    shutil.copy2(RUN/f'game/screenshots/{name}.png',OUT/f'{name}.png')
shutil.copy2(RUN/'game/screenshots/n2.png',OUT/'n2.png')
assets=ROOT/'src/main/resources/assets/crysismod/textures/gui/classic_hud'
for name in ['frame-left','frame-right']:
    alpha=Image.open(assets/f'{name}.png').getchannel('A').getextrema()[1]
    check(name+' original opacity',abs(alpha-round(.45*255))<=1,alpha)
    original=Image.open(assets/f'{name}.png').getchannel('A')
    neutral=Image.open(assets/f'themed/{name}.png').getchannel('A')
    check(name+' theme preserves alpha',original.tobytes()==neutral.tobytes(),'RGBA alpha comparison')
source=(ROOT/'src/main/java/io/github/painworks/crysismod/nanosuit/client/ClassicNanosuitHud.java').read_text()
check('One draw entry only',source.count('renderPermanentFrame(')==2,'one callback call plus one method declaration')
manifest={'scope':'Real RTX Minecraft captures; visibility flags and visor state set by isolated QA probe','checks':checks,'n2':'Inspected visually against previous N2 captures; no pixel absence assertion over its existing armor overlay','jarSha256':hashlib.sha256((RUN/'game/mods/crysis-legacy-port.jar').read_bytes()).hexdigest(),'telemetry':[x for x in log.splitlines() if x.startswith('CLASSIC_HUD_CAPTURE')],'renderer':[x for x in log.splitlines() if 'OpenGL Renderer:' in x]}
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
try:font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',19)
except OSError:font=ImageFont.load_default()
plate=Image.new('RGB',(1200,880),'#101719');draw=ImageDraw.Draw(plate)
for i,name in enumerate(present+absent+['n2']):
    x=i%3*400;y=i//3*220
    im=Image.open(OUT/f'{name}.png').convert('RGB');im.thumbnail((392,190))
    plate.paste(im,(x+4,y+28));draw.text((x+8,y+3),name,fill='#c4d8c0',font=font)
plate.save(OUT/'overview.png')
(OUT/'index.html').write_text('<!doctype html><meta charset="utf-8"><title>Habillage permanent N1</title><style>body{background:#101719;color:#ddd;font:17px system-ui}img{max-width:100%}</style><h1>Traits permanents N1 — captures Minecraft</h1><a href="manifest.json">Contrôles et provenance</a><img src="overview.png">'+''.join(f'<h2>{n}</h2><img src="{n}.png">' for n in present+absent+['n2']),encoding='utf-8')
print(f'{sum(c["result"]=="PASS" for c in checks)}/{len(checks)} PASS')
for c in checks:
    if c['result']=='FAIL':print(c)
if any(c['result']=='FAIL' for c in checks):raise SystemExit(1)
