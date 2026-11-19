"""Build comparison plates from already rendered design previews."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT = Path(__file__).resolve().parent
try:
    font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',18)
except OSError:
    font=ImageFont.load_default()
canvas=Image.new('RGB',(1440,810),'#101719'); draw=ImageDraw.Draw(canvas)
states=[('normal','Pistolet'),('fists-rest','Poings au repos'),('weapon-switch','Changement d’arme'),('critical','Énergie basse'),('night-vision','Vision nocturne'),('binocular-study','Jumelles · cible simulée'),('threat','Repéré'),('lost-orange','Perte de vue'),('night-binoculars','Jumelles + vision nocturne')]
for i,(name,label) in enumerate(states):
    im=Image.open(ROOT/f'hud-{name}.png').convert('RGB');im.thumbnail((472,245))
    x=(i%3)*480;y=(i//3)*270;canvas.paste(im,(x,y+24));draw.text((x+8,y+2),label,fill='#c0d6bf',font=font)
canvas.save(ROOT/'overview-v3.png')
canvas=Image.new('RGB',(1500,690),'#101719');draw=ImageDraw.Draw(canvas)
cols=[('Références utilisateur',ROOT/'references/user-2026-11-19/detail-status.jpg',ROOT/'references/user-2026-11-19/11-detected-red.jpg'),('Maquette V2',ROOT/'v2/hud-normal.png',ROOT/'v2/hud-threat.png'),('Maquette V3',ROOT/'hud-normal.png',ROOT/'hud-threat.png')]
for i,(label,status,nav) in enumerate(cols):
    x=i*500;draw.text((x+15,15),label,fill='#d5e6cf',font=font)
    im=Image.open(status).convert('RGB')
    if i: im=im.crop((1460,820,1795,1045) if i==1 else (1480,875,1795,1060))
    im.thumbnail((480,270));canvas.paste(im,(x+(500-im.width)//2,55))
    im=Image.open(nav).convert('RGB')
    if i: im=im.crop((120,800,410,1030))
    im.thumbnail((450,310));canvas.paste(im,(x+(500-im.width)//2,365))
canvas.save(ROOT/'comparison-v3.png')
