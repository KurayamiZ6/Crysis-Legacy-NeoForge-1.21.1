const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { pathToFileURL } = require("node:url");
const { chromium } = require(process.env.PLAYWRIGHT_MODULE ||
  path.resolve(path.dirname(process.execPath), "../node_modules/playwright"));

async function run() {
  const browser = await chromium.launch({
    executablePath: process.env.CHROME_BIN || "C:/Program Files/Google/Chrome/Application/chrome.exe"
  });
  try {
    const page = await browser.newPage({ viewport: { width: 1920, height: 1080 }, reducedMotion: "reduce" });
    await page.goto(pathToFileURL(path.join(__dirname, "index.html")).href);
    const art = await page.evaluate(() => {
      const canvas = document.createElement("canvas");
      canvas.width = 80;
      canvas.height = 40;
      const ctx = canvas.getContext("2d");
      // Tint the opaque figurative shapes and their halos together in Minecraft.
      mapArrow(ctx, 20, 20, 0, "#fff");
      ctx.fillStyle = ctx.shadowColor = "#fff";
      ctx.shadowBlur = 4;
      ctx.beginPath();
      ctx.arc(60, 20, 3.75, 0, Math.PI * 2);
      ctx.fill();
      const pixels = ctx.getImageData(0, 0, 80, 40).data;
      const alpha = (x,y) => pixels[(y*80+x)*4+3];
      return {
        png: canvas.toDataURL("image/png").split(",")[1],
        colors: [...threatPalette], cores: [alpha(20,20), alpha(60,20)],
        halos: [alpha(20,8), alpha(60,15)]
      };
    });
    assert.deepEqual(art.colors, ["#d2ffea", "#d2ffea", "#ffdf32", "#ff3932"]);
    assert.deepEqual(art.cores, [255, 255]);
    assert.ok(art.halos.every(alpha => alpha > 0 && alpha < 255));
    const root = path.resolve(__dirname, "../..");
    const textures = path.join(root, "src/main/resources/assets/crysismod/textures/gui/nanosuit2");
    fs.writeFileSync(path.join(textures, "radar-markers.png"), Buffer.from(art.png, "base64"));

    const proof = path.join(root, "build/n2-hud-live/markers");
    fs.mkdirSync(proof, { recursive: true });
    await page.addStyleTag({ content: `
      html,body,main,.review{width:1920px!important;margin:0!important;padding:0!important;display:block!important}
      header,aside,.gallery{display:none!important}
      #view{width:1920px;height:1080px;border:0;border-radius:0}
    ` });
    await page.evaluate(() => { threatInput.value = 2; threatInput.dispatchEvent(new Event("input")); });
    await page.locator("#view").screenshot({ path: path.join(proof, "maquette.png") });
  } finally {
    await browser.close();
  }
}

run().catch(error => { console.error(error); process.exitCode = 1; });
