from pathlib import Path
root=Path('src/main/java'); changed=0
for p in root.rglob('*.java'):
    s=p.as_posix()
    if any(x in s for x in ['/nanosuit/energy/','/nanosuit/network/','/nanovision/network/','/nanovisor/network/','/client/nanovision/thermal/']) or p.name == 'NanovisionClientEvents.java': continue
    original=p.read_text(encoding='utf-8-sig'); text=original; pos=0
    while (i:=text.find('new ResourceLocation(',pos))>=0:
        j=i+len('new ResourceLocation('); depth=1; quote=None; escape=False; comma=False
        for k in range(j,len(text)):
            c=text[k]
            if quote:
                if escape: escape=False
                elif c=='\\': escape=True
                elif c==quote: quote=None
            elif c in ('"',"'"): quote=c
            elif c in '([{': depth+=1
            elif c in ')]}':
                depth-=1
                if depth==0: break
            elif c==',' and depth==1: comma=True
        else: break
        replacement='ResourceLocation.fromNamespaceAndPath(' if comma else 'ResourceLocation.parse('
        text=text[:i]+replacement+text[j:];pos=i+len(replacement);changed+=1
    if text!=original: p.write_text(text,encoding='utf-8')
print('ResourceLocation constructors migrated:',changed)
