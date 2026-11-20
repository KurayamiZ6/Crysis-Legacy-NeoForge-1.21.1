"""Measure real screenshot fills and palette invariants; no screenshot composition."""
from pathlib import Path
from PIL import Image, ImageStat
import json

ROOT = Path(__file__).resolve().parents[2]
FULL = ROOT / 'build/mode-overlay-qa/hud-details-final/game/screenshots'
MAP = ROOT / 'build/mode-overlay-qa/hud-map-alpha-final/game/screenshots'
CLEAN = ROOT / 'build/mode-overlay-qa/hud-map-cone-final/game/screenshots'
checks=[]
def check(name, passed, evidence):
    checks.append(dict(name=name,passed=bool(passed),evidence=evidence))
def blue(r,g,b):return g>80 and b>130 and b>g*1.15 and b>r+40
def green(r,g,b):return g>100 and g>r*1.2 and g>b*1.7
spans={}
for name in ['default-full','blue-full','amber-half','amber-quarter','amber-empty']:
    im=Image.open(FULL/f'{name}.png').convert('RGB')
    ranges=[]
    for y,predicate in [(970,blue),(988,green)]:
        xs=[x for x in range(1492,1616) if predicate(*im.getpixel((x,y)))]
        ranges.append([min(xs),max(xs)] if xs else [])
    spans[name]=ranges
    check(name+' fixed health green', im.getpixel((1600,988))==(125,202,43),im.getpixel((1600,988)))
    if name!='amber-empty':check(name+' fixed energy blue',im.getpixel((1600,970))==(0,118,176),im.getpixel((1600,970)))
for index,kind in enumerate(['energy','health']):
    full,half,quarter=[spans[n][index] for n in ['default-full','amber-half','amber-quarter']]
    check(kind+' fixed right edge',full[1]==half[1]==quarter[1],[full,half,quarter])
    check(kind+' grows from right toward left',full[0]<half[0]<quarter[0],[full,half,quarter])
    check(kind+' fractions within one GUI pixel',abs((half[1]-half[0]+1)/(full[1]-full[0]+1)-.5)<.04 and abs((quarter[1]-quarter[0]+1)/(full[1]-full[0]+1)-.25)<.04,[full,half,quarter])
check('zero energy has no blue fill',spans['amber-empty'][0]==[],spans['amber-empty'][0])
for name,expected in [('default-full','green'),('blue-full','blue'),('amber-half','amber')]:
    source=CLEAN/f'{name}.png'
    im=Image.open(source if source.exists() else MAP/f'{name}.png').convert('RGB');r,g,b=ImageStat.Stat(im.crop((190,860,245,895))).mean
    passed=g>r>b if expected=='green' else b>g>r if expected=='blue' else r>g>b
    check('terrain '+expected,passed,[round(r,2),round(g,2),round(b,2)])
    check(name+' final health remains green',im.getpixel((1600,988))==(125,202,43),im.getpixel((1600,988)))
player=Image.open(ROOT/'src/main/resources/assets/crysismod/textures/gui/classic_map/player.png').convert('RGBA')
check('player cone asset stays translucent',player.getpixel((232,50))[3]==46,player.getpixel((232,50)))
check('player cone reaches upper terrain edge',player.getpixel((232,16))[3]==46,player.getpixel((232,16)))
out=ROOT/'qa/stabilization/evidence/hud-refinements';out.mkdir(parents=True,exist_ok=True)
(out/'pixel-checks.json').write_text(json.dumps(checks,indent=2)+'\n',encoding='utf-8')
print(f'{sum(c["passed"] for c in checks)}/{len(checks)} pixel checks PASS')
for result in checks:
    if not result['passed']:print(result)
assert all(result['passed'] for result in checks)
