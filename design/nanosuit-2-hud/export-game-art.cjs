const fs=require('node:fs');
const path=require('node:path');
const {pathToFileURL}=require('node:url');
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||path.resolve(path.dirname(process.execPath),'../node_modules/playwright'));
const drawRadarRim=require('./radar-rim.cjs');
const out=path.resolve('src/main/resources/assets/crysismod/textures/gui/nanosuit2');
(async()=>{
 const browser=await chromium.launch({executablePath:process.env.CHROME_BIN||'C:/Program Files/Google/Chrome/Application/chrome.exe'});
 try{
  fs.mkdirSync(out,{recursive:true});
  const page=await browser.newPage({viewport:{width:1920,height:1080},reducedMotion:'reduce'});
  await page.goto(pathToFileURL(path.resolve('design/nanosuit-2-hud/index.html')).href);
  await page.addStyleTag({content:`
    html,body,main,.review{background:none!important;margin:0!important;padding:0!important;width:1920px!important;display:block!important}
    header,aside,.gallery{display:none!important}
    #view{border:0;border-radius:0;background:none;width:1920px;height:1080px}
    #view:before,#view:after,#view .aim{display:none!important}
    #view .hud,#view .radar{transform:none!important;filter:none!important;mask:none!important;translate:none!important}
    #view .mount,#view .radar-mount{transform:none!important}
    #view .radar-stance{transform:none!important}
    #view .ammo strong,#view .ammo b,#view .ammo .slash{visibility:hidden}
    #view .rifle,#view .shot,#view .bow{visibility:hidden}
    #view .hud[data-export="base"] :is(.energy,.modes){visibility:hidden}
    #view .hud[data-export-tone="red"] .energy i.on{background:#ff5b4c!important}
    #view .hud[data-export="energy"] :is(.ammo,.cells,.left-rail,.right-rail,.modes),#view .hud[data-export="energy"] .hud-frame:before{visibility:hidden}
    #view .hud[data-export="modes"] :is(.hud-frame,.left-rail){visibility:hidden}
    #view .hud[data-export="shots"] :is(.ammo,.energy,.left-rail,.right-rail,.modes),#view .hud[data-export="shots"] :is(.hud-frame:before,.cell:before){visibility:hidden}
    #view .hud[data-export="shots"] .shot{visibility:visible}
    #view .hud[data-export="bow"] :is(.ammo,.energy,.left-rail,.right-rail,.modes),#view .hud[data-export="bow"] :is(.hud-frame:before,.cell:before){visibility:hidden}
    #view .hud[data-export="bow"] .bow{visibility:visible}
    #view .radar[data-export] .radar-terrain,#view .radar[data-export] .compass-mark{visibility:hidden}
    #view .radar[data-export="base"] :is(.radar-stance,.radar-threat strong,.radar-threat-base){visibility:hidden}
    #view .radar[data-export="floor"] :is(.radar-map,.radar-threat,.radar-mask,.radar-status,.radar-stance){visibility:hidden}
    #view .radar[data-export="stance"] :is(.radar-grid,.radar-mask){visibility:hidden}
    #view .alert span{opacity:0}
  `});
  await page.evaluate(async()=>{await document.fonts.ready;await new Promise(requestAnimationFrame);await new Promise(requestAnimationFrame);});
  const metadata=await page.evaluate(()=>{
   const box=e=>{const r=e.getBoundingClientRect();return [r.x,r.y,r.width,r.height].map(n=>Math.round(n*1000)/1000);};
   const panel=[...document.querySelectorAll('#view .hud')][0],radar=document.querySelector('#view .radar');
   const data={};for(const [k,s] of Object.entries({panel:'.hud',energy:'.energy',armor:'.mode.armor',stealth:'.mode.stealth',
      ammo:'.ammo',gun:'.cell',loaded:'.total',reserve:'.max',slash:'.slash',radar:'.radar',grid:'.radar-grid',map:'.radar-paper',
      threat:'.radar-threat',floor:'.radar-threat-base',alert:'.alert',stance:'.radar-stance'})){
    data[k]=box(document.querySelector('#view '+s));
   }
   data.segments=[...panel.querySelectorAll('.energy i')].map(box);
   data.threatSegments=[...radar.querySelectorAll('.radar-threat i')].map(box);
   data.mask=getComputedStyle(radar.querySelector('.radar-terrain-clip')).clipPath;
   return data;
  });
  function clip(r){const [x,y,w,h]=r;return {x:Math.floor(x),y:Math.floor(y),width:Math.ceil(x+w)-Math.floor(x),height:Math.ceil(y+h)-Math.floor(y)}}
  const save=async(name,rect)=>page.screenshot({path:path.join(out,name+'.png'),clip:clip(rect),omitBackground:true});
  await page.evaluate(drawRadarRim);
  await page.evaluate(()=>liveRadar.hud.dataset.export="base");
  await page.evaluate(()=>liveRadar.meter.style.setProperty("--threat-amount",0));
  await save("radar-0",metadata.radar);
  await page.evaluate(()=>{
   liveRadar.hud.dataset.export="floor";
   liveRadar.hud.style.setProperty("--threat-ink","#fff");
  });
  await save("radar-threat-base",metadata.radar);
  await page.evaluate(()=>liveRadar.hud.style.removeProperty("--threat-ink"));
  if(process.argv.includes('--radar-only')) return;
  const digitBloom=await page.evaluate(async()=>{
   const tileWidth=112,tileHeight=144,padding=24;
   const strip=document.createElement("canvas");
   strip.width=tileWidth*10;
   strip.height=tileHeight;
   const images=await Promise.all(digitMasks.map(async mask=>{
    const image=new Image();image.src=mask.slice(4,-1);await image.decode();return image;
   }));
   for(const [digit,image] of images.entries()){
    const tile=document.createElement("canvas");
    tile.width=tileWidth;tile.height=tileHeight;
    const ctx=tile.getContext("2d");
    ctx.filter="blur(10px)";
    ctx.drawImage(image,padding,padding,64,96);
    strip.getContext("2d").drawImage(tile,digit*tileWidth,0);
   }
   return strip.toDataURL().split(",")[1];
  });
  fs.writeFileSync(path.join(out,"digits-bloom.png"),Buffer.from(digitBloom,"base64"));
  await page.evaluate(()=>{
   liveRadar.hud.dataset.export="base";
   liveRadar.hud.style.setProperty("--threat-ink","#fff");
   liveRadar.meter.style.setProperty("--threat-amount",20);
   liveRadar.hud.querySelector(".radar-threat strong").style.visibility="visible";
  });
  await save("threat-segments",metadata.threat);
  const threatLabel=await page.locator('#view .radar-threat strong').boundingBox();
  metadata.threatLabel=[threatLabel.x,threatLabel.y,threatLabel.width,threatLabel.height];
  await save("threat-label",metadata.threatLabel);
  await page.evaluate(()=>{
   liveRadar.hud.style.removeProperty("--threat-ink");
   liveRadar.hud.querySelector(".radar-threat strong").style.removeProperty("visibility");
  });
  if(process.argv.includes('--lighting-only')) return;
  await page.evaluate(()=>live.hud.dataset.export="base");
  await save('panel',metadata.panel);
  for(const [name,low,on,pulse] of [['energy-off',false,false,false],['energy-on',false,true,false],['energy-low-off',true,false,false],['energy-low-on',true,true,false],['energy-low-red',true,true,true]]){
   await page.evaluate(([low,on,pulse])=>{live.hud.dataset.export="energy";live.hud.dataset.critical=String(low);
     live.hud.dataset.pulse=String(pulse);live.hud.dataset.exportTone=pulse?"red":"";for(const segment of live.segments)segment.classList.toggle('on',on);},[low,on,pulse]);
   await save(name,metadata.energy);
  }
  for(const [name,state] of [['mode-neutral',''],['mode-armor','armor'],['mode-stealth','stealth']]){
   await page.evaluate(state=>{live.hud.dataset.export="modes";for(const tab of live.hud.querySelectorAll('.mode'))tab.classList.toggle('on',tab.classList.contains(state||'none'));},state);
   await save(name,metadata.panel);
  }
  for(const n of [1,3,4]){
   await page.evaluate(n=>{live.hud.dataset.export="shots";live.hud.querySelector(".shot").replaceChildren(...Array.from({length:n},()=>document.createElement('i')));},n);
   await save('shots-'+n,metadata.panel);
  }
  await page.evaluate(()=>live.hud.dataset.export="bow");
  await save('bow',metadata.panel);
  for(const pose of ['standing','crouching','prone']){
   await page.evaluate(pose=>{liveRadar.hud.dataset.export="stance";liveRadar.hud.querySelector('.radar-threat-base').style.removeProperty('visibility');drawStance(liveRadar,pose);},pose);
   await save('stance-'+pose,metadata.stance);
  }

  const compass=await page.evaluate(()=>{
    const glyphs='N E S W';const c=document.createElement("canvas");c.width=128;c.height=32;
    const ctx=c.getContext("2d");ctx.font='820 20px Bahnschrift, sans-serif';ctx.textAlign="center";ctx.textBaseline="middle";ctx.fillStyle="#d2ffea";ctx.shadowColor="#a7e9ce85";ctx.shadowBlur=4;
    glyphs.split(" ").forEach((g,i)=>ctx.fillText(g,i*32+16,16));return c.toDataURL().split(",")[1];
  });
  fs.writeFileSync(path.join(out,"compass.png"),Buffer.from(compass,"base64"));
  await page.evaluate(()=>{alertText.style.setProperty("opacity","1","important");});
  await save("alert-brackets",metadata.alert);
  await page.addStyleTag({content:"#view .alert::before,#view .alert::after{visibility:hidden}#view .alert span{opacity:1!important}"});
  for(const [name,message] of [["low-en","LOW ENERGY"],["low-fr","ÉNERGIE FAIBLE"],["critical-en","CRITICAL HEALTH"],["critical-fr","SANTÉ CRITIQUE"]]) {
    await page.evaluate(message=>{alertText.firstElementChild.textContent=message;},message);
    await save("alert-"+name,metadata.alert);
  }
  await page.evaluate(()=>{live.hud.removeAttribute("data-export");live.hud.querySelector(".slash").style.visibility="visible";});
  await save("slash",metadata.slash);

  const opticAssets=await page.evaluate(()=>{
    opticEnabled.checked=true;const previous=opt.fillText;opt.fillText=()=>{};
    const capture=(x,y,w,h)=>{const c=document.createElement("canvas");c.width=w;c.height=h;
      c.getContext("2d").drawImage(optic,x,y,w,h,0,0,w,h);return c.toDataURL().split(",")[1];};
    opticPitch.max=1000;opticPitch.value=1000;focus=undefined;drawOptic(performance.now());
    const assets={
      "optic-left":capture(215,175,105,190),
      "optic-right":capture(640,175,105,190),
      "optic-center":capture(430,175,100,190)
    };
    opticPitch.value=0;pointerX=480;pointerY=270;drawOptic(performance.now());
    const code=drawOptic.toString();const start=code.indexOf('  opt.save();\n  opt.translate(679,137);');
    const end=code.indexOf('  for(let i=0;i<6;i++)',start);
    opt.clearRect(0,0,960,540);eval(code.slice(start,end));opt.restore();
    assets["optic-info"]=capture(678,117,239,146);
    opt.fillText=previous;return assets;
  });
  for(const [name,png] of Object.entries(opticAssets)) fs.writeFileSync(path.join(out,name+".png"),Buffer.from(png,"base64"));
  const digits=await page.evaluate(async()=>{
    const c=document.createElement('canvas');c.width=640;c.height=96;
    const images=await Promise.all(digitMasks.map(async m=>{const img=new Image();img.src=m.slice(4,-1);await img.decode();return img;}));
    const ctx=c.getContext('2d');images.forEach((img,i)=>ctx.drawImage(img,i*64,0));return c.toDataURL().split(',')[1];
  });
  fs.writeFileSync(path.join(out,'digits.png'),Buffer.from(digits,'base64'));
  fs.mkdirSync(path.resolve("build/n2-hud-review"),{recursive:true});
  fs.writeFileSync(path.resolve("build/n2-hud-review/layout-export.json"),JSON.stringify(metadata,null,2));
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
