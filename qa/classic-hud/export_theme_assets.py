"""Create neutral tint masks without touching original default-palette textures."""
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/crysismod/textures/gui'
for folder,names in [('classic_hud',['armor','strength','speed','cloak','frame-left','frame-right']),('nanosuit1_radial',['green-fill','green-frame','ring'])]:
    out=ROOT/folder/'themed';out.mkdir(exist_ok=True)
    for name in names:
        im=Image.open(ROOT/folder/f'{name}.png').convert('RGBA')
        themed=[]
        for r,g,b,a in im.getdata():
            level=round(min(1,g/150)*255)
            themed.append((level,level,level,a))
        output=Image.new('RGBA',im.size);output.putdata(themed);output.save(out/f'{name}.png')
print('Generated neutral masks; original green palette remains byte-for-byte unchanged.')
