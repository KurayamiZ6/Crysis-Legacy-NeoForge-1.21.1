"""Assemble preview sheets from the rendered mockups (Pillow)."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parent
FONT='C:/Windows/Fonts/segoeui.ttf'
def label(draw,xy,value,size=22,color='#dce4de'):
    draw.text(xy,value,font=ImageFont.truetype(FONT,size),fill=color)

normal=Image.open(ROOT/'hud-normal.png').convert('RGB')
normal.crop((1465,842,1805,1045)).save(ROOT/'previews/proposed-status.png')
normal.crop((115,790,410,1040)).save(ROOT/'previews/proposed-navigation.png')

sheet=Image.new('RGB',(1920,1250),'#101719'); d=ImageDraw.Draw(sheet)
label(d,(30,18),'NANOSUIT 1 / Maquettes hors jeu / Données simulées',28)
for i,(state,title) in enumerate([
    ('normal','Normal / Armure / 100 %'),('critical','Énergie basse / Camouflage / 12 %'),
    ('night-vision','Vision nocturne / Batterie indépendante'),('threat','Menaces / Deux indications simulées')]):
    x=(i%2)*960;y=70+(i//2)*590
    label(d,(x+20,y),title)
    im=Image.open(ROOT/f'hud-{state}.png').convert('RGB').resize((960,540),Image.Resampling.LANCZOS)
    sheet.paste(im,(x,y+35))
sheet.save(ROOT/'overview.png')

comp=Image.new('RGB',(1800,940),'#101719');d=ImageDraw.Draw(comp)
label(d,(25,15),'Référence Remastered / Minecraft actuel / Proposition Nanosuit 1',28)
for row,kind in enumerate(['status','navigation']):
    for col,(title,file) in enumerate([
        ('Référence utilisateur',f'references/reference-{kind}.png'),
        ('HUD Minecraft existant',f'references/current-{kind}.png'),
        ('Maquette N1 (hors jeu)',f'previews/proposed-{kind}.png')]):
        x=col*600;y=85+row*425
        label(d,(x+22,y),title)
        im=Image.open(ROOT/file).convert('RGB');im.thumbnail((550,325),Image.Resampling.LANCZOS)
        comp.paste(im,(x+(600-im.width)//2,y+50+(325-im.height)//2))
label(d,(25,910),'Agrandissements de lecture : échelles différentes. Voir les proportions mesurées dans RAPPORT.md.',17,'#b5c1b8')
comp.save(ROOT/'comparison.png')
for row,kind in enumerate(['status','navigation']):
    for col,(title,file) in enumerate([
        ('Référence Remastered',f'references/reference-{kind}.png'),
        ('Première maquette',f'references/v1-proposed-{kind}.png'),
        ('Itération 2',f'previews/proposed-{kind}.png')]):
        x=col*600;y=85+row*425
        d.rectangle((x,y,x+599,y+423),fill='#101719')
        label(d,(x+22,y),title)
        im=Image.open(ROOT/file).convert('RGB');im.thumbnail((550,325),Image.Resampling.LANCZOS)
        comp.paste(im,(x+(600-im.width)//2,y+50+(325-im.height)//2))
d.rectangle((0,0,1799,65),fill='#101719')
label(d,(25,15),'Alignements et lisibilité / Référence, première maquette, itération 2',28)
comp.save(ROOT/'iteration-comparison.png')
print('Created overview, component comparisons and detail crops.')
