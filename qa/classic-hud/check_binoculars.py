"""Assertions and evidence from actual Minecraft binocular sessions."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
from zipfile import ZipFile
import re,json,hashlib,math,shutil
ROOT=Path(__file__).resolve().parents[2]
RUN=ROOT/'build/mode-overlay-qa/classic-binocular-verified'
CLIP=ROOT/'build/mode-overlay-qa/classic-binocular-clip'
OPTIC=ROOT/'build/mode-overlay-qa/classic-binocular-optic-verified'
OUT=Path(__file__).resolve().parent/'binocular-evidence';OUT.mkdir(exist_ok=True)
def read(run):
 log=(run/'console.log').read_text(errors='replace');rows={}
 for line in log.splitlines():
  m=re.search(r'BINOCULAR_CAPTURE (\S+) ms=(\d+) state=(\w+) p=([0-9.E-]+) active=(true|false) zoom=(\d+) baseFov=([0-9.E-]+) finalFov=([0-9.E-]+) distance=([0-9.E-]+) pos=\(([^)]+)\) pitch=([0-9.E-]+)',line)
  if m:rows[m[1]]={'ms':int(m[2]),'state':m[3],'p':float(m[4]),'active':m[5]=='true','zoom':int(m[6]),'baseFov':float(m[7]),'finalFov':float(m[8]),'distance':float(m[9]),'position':m[10],'pitch':float(m[11])}
 return log,rows
log,rows=read(RUN);assert 'BINOCULAR_QA_COMPLETE' in log
checks=[]
def check(n,ok,detail=None):checks.append({'name':n,'result':'PASS' if ok else 'FAIL','observed':detail})
def zoomRatio(r):return math.tan(math.radians(r['baseFov'])/2)/math.tan(math.radians(r['finalFov'])/2)
for file,want in [('opening-500.png',2),('zoom4-500.png',4),('tacz-hip-1000.png',2),('tacz-zoom4-600.png',4)]:
 r=rows[file];ratio=zoomRatio(r);check(file+' real projection',r['active'] and abs(ratio-want)<.03,ratio)
check('Normal closing intermediate',0<rows['closing-150.png']['p']<1 and not rows['closing-150.png']['active'],rows['closing-150.png'])
check('Normal closed',rows['closing-500.png']['p']==0)
check('Opening interrupted from partial',rows['rapid-open-120.png']['p']<.7 and rows['rapid-close-80.png']['p']>.8)
check('Closing interrupted restart low',rows['rapid-reopen-150.png']['p']<.7 and rows['rapid-reopen-150.png']['active'])
check('Reopening reaches full',rows['rapid-reopen-500.png']['p']==1)
check('Triple queued click closes',not rows['triple-click-500.png']['active'] and rows['triple-click-500.png']['p']==0)
for file,want in [('opening-500.png',7.5),('entity-600.png',4.75),('occluded-150.png',2.5),('sky-150.png',-1)]:check(file+' raycast',abs(rows[file]['distance']-want)<.02,rows[file]['distance'])
check('Real position',rows['opening-500.png']['position']=='0.5, 110.0, 0.5',rows['opening-500.png']['position'])
check('Real sky inclination',rows['sky-150.png']['pitch']==-90)
for name in ['screen-150.png','removed-200.png','tacz-ads-600.png','tacz-refuse-400.png']:check(name+' inactive',not rows[name]['active'],rows[name]['state'])
check('Return from ADS',rows['tacz-return-1000.png']['active'])
check('Scroll consumed only as binocular control',log.count('BINOCULAR_SCROLL consumed=true')==2)
check('N2 old visor active',rows['n2-500.png']['active'] and rows['n2-500.png']['state']=='CLOSED')
# F1 hides the optical overlay, without changing the logical open state.
im=Image.open(RUN/'game/screenshots/f1-150.png').convert('RGB')
light=sum(r>150 and g>190 and b>120 and g>r for r,g,b in im.crop((420,530,505,575)).getdata())
check('F1 overlay absent',light<5 and rows['f1-150.png']['active'],light)
opticLog,optic=read(OPTIC);assert 'BINOCULAR_QA_COMPLETE' in opticLog
for file,want in [('tacz-hip-1000.png',2),('tacz-zoom4-600.png',4)]:check('552 '+file,optic[file]['active'] and abs(zoomRatio(optic[file])-want)<.03,zoomRatio(optic[file]))
for file in ['tacz-ads-600.png','tacz-refuse-400.png']:check('552 '+file,not optic[file]['active'])
check('552 return',optic['tacz-return-1000.png']['active'])
clipLog=(CLIP/'console.log').read_text(errors='replace');assert 'BINOCULAR_CLIP_COMPLETE' in clipLog
frames=re.findall(r'BINOCULAR_CLIP (\S+) ms=(\d+) phase=(\d+)',clipLog)
check('Without TACZ and second resolution',len(frames)>=20 and Image.open(CLIP/'game/screenshots'/frames[0][0]).size==(1280,720),len(frames))
gif=[];times=[]
for i,(file,ms,phase) in enumerate(frames):
 frame=Image.open(CLIP/'game/screenshots'/file).convert('RGB');frame.thumbnail((960,540));gif.append(frame)
 times.append(max(40,min(800,int(frames[i+1][1])-int(ms))) if i+1<len(frames) else 450)
gif[0].save(OUT/'minecraft-binoculars.gif',save_all=True,append_images=gif[1:],duration=times,loop=0,optimize=False)
for file in rows:shutil.copy2(RUN/'game/screenshots'/file,OUT/file)
for file in optic:shutil.copy2(OPTIC/'game/screenshots'/file,OUT/('optic-'+file))
final=ROOT/'build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar'
with ZipFile(RUN/'game/mods/crysis-legacy-port.jar') as a,ZipFile(final) as b:
 changed=[n for n in a.namelist() if a.read(n)!=b.read(n)]
 equivalent=all(n in ['assets/crysismod/lang/en_us.json','assets/crysismod/lang/fr_fr.json'] and json.loads(a.read(n))==json.loads(b.read(n)) for n in changed)
 check('Final JAR core matches measured candidate',equivalent,{'onlyEquivalentLocaleFormatting':changed})
manifest={'scope':'Actual Minecraft frames, controls queued by isolated QA mod, not physical input automation','measurements':rows,'opticMeasurements':optic,'checks':checks,'clipFrames':frames,'finalJarSha256':hashlib.sha256(final.read_bytes()).hexdigest(),'measuredJarSha256':hashlib.sha256((RUN/'game/mods/crysis-legacy-port.jar').read_bytes()).hexdigest()}
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
try:font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',20)
except OSError:font=ImageFont.load_default()
plate=Image.new('RGB',(1440,840),'#101719');draw=ImageDraw.Draw(plate)
names=['opening-500.png','zoom4-500.png','closing-150.png','rapid-open-120.png','rapid-close-80.png','rapid-reopen-150.png','entity-600.png','occluded-150.png','sky-150.png']
for i,n in enumerate(names):
 frame=Image.open(OUT/n);frame.thumbnail((472,250));x=i%3*480;y=i//3*280;plate.paste(frame,(x,y+27));draw.text((x+6,y+3),n,fill='#d5e5cc',font=font)
plate.save(OUT/'overview.png')
(OUT/'index.html').write_text('<!doctype html><meta charset="utf-8"><title>Jumelles N1 — preuves Minecraft</title><style>body{background:#101719;color:#ddd;font:17px system-ui}img{max-width:100%}</style><h1>Jumelles N1 : captures réelles</h1><p>Molette pour ×2/×4 ; raccourci du viseur existant pour ouvrir/fermer. Scènes préparées dans une copie isolée. Le GIF contient des frames Minecraft successifs avec leurs intervalles mesurés, sans interpolation artificielle.</p><a href="manifest.json">Mesures et contrôles</a><img src="minecraft-binoculars.gif"><img src="overview.png">'+''.join(f'<h2>{n}</h2><img src="{n}">' for n in rows)+''.join(f'<h2>Optique 552 : {n}</h2><img src="optic-{n}">' for n in optic),encoding='utf-8')
print(f'{sum(c["result"]=="PASS" for c in checks)}/{len(checks)} PASS; {len(frames)} real clip frames')
for c in checks:
 if c['result']=='FAIL':print(c)
if any(c['result']=='FAIL' for c in checks):raise SystemExit(1)
