from pathlib import Path
import re
root=Path('src/main/java'); changed=0
for p in root.rglob('*.java'):
 s=p.as_posix()
 if any(x in s for x in ['/nanosuit/energy/','/nanosuit/network/','/nanovision/network/','/nanovisor/','/client/nanovision/thermal/']) or p.name=='NanovisionClientEvents.java': continue
 text=p.read_text(encoding='utf-8'); new=text
 # The 1.21 buffer builder starts at Tesselator.begin and finalizes with buildOrThrow.
 new=re.sub(r'BufferBuilder (\w+) = Tesselator\.getInstance\(\)\.getBuilder\(\);\s*\1\.begin\(([^;]+)\);',r'BufferBuilder \1 = Tesselator.getInstance().begin(\2);',new)
 if 'BufferBuilder' in new:
  new=re.sub(r'(\b(?:buffer|builder|vertices|vertexBuilder|buf)\s*)\.vertex\(',r'\1.addVertex(',new)
  new=new.replace('.endVertex()', '').replace('.uv(','.setUv(').replace('.color(','.setColor(')
  new=re.sub(r'(\b(?:buffer|builder|vertices|vertexBuilder|buf))\.end\(\)',r'\1.buildOrThrow()',new)
 if p.name=='ArmorModeOverlaySettingsScreen.java': new=new.replace('this.renderBackground(guiGraphics);','this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);')
 if new!=text: p.write_text(new,encoding='utf-8');changed+=1
print('Adjusted render files:',changed)
