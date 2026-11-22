const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { pathToFileURL } = require("node:url");
const { chromium } = require(process.env.PLAYWRIGHT_MODULE ||
  path.resolve(path.dirname(process.execPath), "../node_modules/playwright"));

const root = path.resolve(__dirname, "../..");
const proof = path.join(root, "build/n2-hud-live/cone");
const spriteSize = 32;
const spriteExtent = 4;

async function run() {
  const browser = await chromium.launch({
    executablePath: process.env.CHROME_BIN || "C:/Program Files/Google/Chrome/Application/chrome.exe"
  });
  try {
    const page = await browser.newPage({ viewport: { width: 1920, height: 1080 }, reducedMotion: "no-preference" });
    await page.goto(pathToFileURL(path.join(__dirname, "index.html")).href);
    await page.addStyleTag({ content: `
      html,body,main,.review{width:1920px!important;margin:0!important;padding:0!important;display:block!important}
      header,aside,.gallery{display:none!important}
      #view{width:1920px;height:1080px;border:0;border-radius:0}
    ` });
    fs.mkdirSync(proof, { recursive: true });
    for (const [aim, name] of [[false, "hanche"], [true, "visee"]]) {
      await page.evaluate(aim => {
        aimInput.checked = aim;
        aimInput.dispatchEvent(new Event("change"));
      }, aim);
      await page.waitForFunction(aim => aimFraction(performance.now()) === Number(aim), aim);
      await page.locator("#view").screenshot({ path: path.join(proof, "maquette-" + name + ".png") });
    }
    const result = await page.evaluate(({ spriteSize, spriteExtent }) => {
      const paint = drawViewCone;
      let pivot;
      drawViewCone = (ctx, x, y) => { pivot = [x, y, ctx.canvas.width, ctx.canvas.height]; };
      try { drawRadar(liveRadar, 0, false); }
      finally { drawViewCone = paint; }

      const [x, y, width, height] = pivot;
      const measure = aim => {
        const points = [];
        const probe = { beginPath() {}, fill() {}, arc: (x, y, radius) => points.push({ x, y, radius }) };
        paint(probe, x, y, aim);
        const [first, second] = points;
        const distance = p => Math.hypot(p.x - x, p.y - y);
        return {
          first, color: probe.fillStyle, length: distance(first), spacing: distance(second) - distance(first),
          angle: Math.atan2(Math.abs(first.x - x), y - first.y) * 180 / Math.PI
        };
      };
      const hip = measure(0);
      const aim = measure(1);
      aimStart = 0;
      aimTarget = 1;
      aimChanged = 0;
      const duration = Array.from({ length: 1000 }, (_, ms) => ms).find(ms => aimFraction(ms) === 1);
      const quarter = aimFraction(duration / 4);

      const canvas = document.createElement("canvas");
      canvas.width = canvas.height = spriteSize;
      const ctx = canvas.getContext("2d");
      const scale = spriteSize / spriteExtent;
      ctx.setTransform(scale, 0, 0, scale,
        spriteSize / 2 - hip.first.x * scale, spriteSize / 2 - hip.first.y * scale);
      paint(ctx, x, y, 0);
      return { data: canvas.toDataURL("image/png").split(",")[1], width, height,
        pivotX: x / width, pivotY: y / height, hip, aim, duration, quarter };
    }, { spriteSize, spriteExtent });
    // Guard the projection and easing used by Nanosuit2Cone against authoring changes.
    const rounded = value => Number(value.toFixed(4));
    assert.deepEqual([result.width, result.height, result.pivotX, result.pivotY,
      ...[result.hip.angle, result.aim.angle, result.hip.length, result.hip.spacing].map(rounded),
      result.duration, result.quarter], [440, 258, 0.49, 0.61, 27, 12, 17, 9.5, 240, 0.15625]);
    assert.equal(result.hip.first.radius, 1.65);
    assert.equal(result.hip.color, "rgb(186 243 220 / .77)");
    fs.writeFileSync(path.join(root, "src/main/resources/assets/crysismod/textures/gui/nanosuit2/radar-cone-dot.png"),
      Buffer.from(result.data, "base64"));
  } finally {
    await browser.close();
  }
}

run().catch(error => { console.error(error); process.exitCode = 1; });
