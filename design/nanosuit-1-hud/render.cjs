// Design preview only. Usage: node render.cjs <playwright-module> <chrome.exe>
const { chromium } = require(process.argv[2]);
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
(async () => {
  const browser = await chromium.launch({executablePath:process.argv[3], headless:true});
  const page = await browser.newPage({viewport:{width:1920,height:1080},deviceScaleFactor:1});
  const errors=[];
  page.on('pageerror',e=>errors.push(String(e)));
  for(const state of ['normal','critical','night-vision','threat','binocular-study','fists-rest','weapon-switch','lost-orange','lost-yellow','night-binoculars']){
    await page.goto(pathToFileURL(path.join(__dirname,`hud-${state}.svg`)).href);
    await page.screenshot({path:path.join(__dirname,`hud-${state}.png`)});
  }
  await page.setViewportSize({width:1440,height:1000});
  await page.goto(pathToFileURL(path.join(__dirname,'index.html')).href);
  const missing = await page.locator('a[href],img[src]').evaluateAll(nodes => nodes.map(n=>n.getAttribute('href')||n.getAttribute('src')).filter(Boolean));
  for(const name of missing) if(!name.startsWith('#')&&!fs.existsSync(path.join(__dirname,name))) errors.push('Missing '+name);
  for(const state of ['normal','critical','night-vision','threat','fists-rest','weapon-switch','lost-orange','lost-yellow','binocular-study','night-binoculars']){
    await page.locator(`button[data-state="${state}"]`).click();
    if(await page.locator('#hero').getAttribute('src')!==`hud-${state}.png`) errors.push('State '+state);
    if(await page.locator(`button[data-state="${state}"]`).getAttribute('aria-pressed')!=='true') errors.push('Selection '+state);
  }
  await page.locator('#lost').click();
  if(await page.locator('#visibility-triangle').getAttribute('color')!=='#ff670c')errors.push('Immediate orange');
  await page.waitForTimeout(550);
  if(await page.locator('#visibility-triangle').getAttribute('color')!=='#e8d653')errors.push('Yellow after 500ms');
  await page.locator('#detected').click();
  if(!await page.locator('#visibility-triangle').evaluate(n=>n.classList.contains('pulse')))errors.push('Red pulse restart');
  await page.screenshot({path:path.join(__dirname,'gallery-v3.png'),fullPage:true});
  await browser.close();
  fs.writeFileSync(path.join(__dirname,'preview-check.json'),JSON.stringify({scope:'Design previews only; no Minecraft runtime test',svgPreviews:10,galleryStates:10,threatDemo:"orange immediately, yellow after 500ms, red restart",missingLinksOrScriptErrors:errors},null,2));
  if(errors.length) throw Error(errors.join('\n'));
  console.log('PASS: 10 SVG previews, 10 gallery states and local links.');
})().catch(e=>{console.error(e);process.exit(1);});
