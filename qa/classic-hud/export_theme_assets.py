"""Create neutral tint masks without touching original default-palette textures."""
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/crysismod/textures/gui'
for folder,names in [('classic_hud',['panel','health','digits','armor','strength','speed','cloak']),('nanosuit1_radial',['green-fill','green-frame','ring'])]:
    out=ROOT/folder/'themed';out.mkdir(exist_ok=True)
    for name in names:
        im=Image.open(ROOT/folder/f'{name}.png').convert('RGBA')
        themed=[];fixed=[]
        for r,g,b,a in im.getdata():
            # Blue panels are functional energy information, not interface chrome.
            preserve=name=='panel' and b>g and max(r,g,b)>40
            if preserve:
                themed.append((0,0,0,0));fixed.append((r,g,b,a))
            else:
                level=round(min(1,g/150)*255)
                if name=='panel' and max(r,g,b)<40:level=round(max(r,g,b)/255*.6*255)
                themed.append((level,level,level,a));fixed.append((0,0,0,0))
        output=Image.new('RGBA',im.size);output.putdata(themed);output.save(out/f'{name}.png')
        if name=='panel':
            output.putdata(fixed);output.save(ROOT/folder/'panel-fixed.png')
print('Generated neutral masks; original green palette remains byte-for-byte unchanged.')
