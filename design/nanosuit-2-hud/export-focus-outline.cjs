const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { pathToFileURL } = require("node:url");
const { chromium } = require(process.env.PLAYWRIGHT_MODULE ||
  path.resolve(path.dirname(process.execPath), "../node_modules/playwright"));

const root = path.resolve(__dirname, "../..");
const texture = path.join(root, "src/main/resources/assets/crysismod/textures/gui/nanosuit2/optic-focus.png");
const proof = path.join(root, "build/n2-hud-live/hex-pilot");
const atlas = { width: 72, height: 64, supersampling: 2 };

async function run() {
  const browser = await chromium.launch({
    executablePath: process.env.CHROME_BIN || "C:/Program Files/Google/Chrome/Application/chrome.exe"
  });
  try {
    const page = await browser.newPage({ viewport: { width: 1920, height: 1080 }, reducedMotion: "reduce" });
    await page.goto(pathToFileURL(path.join(__dirname, "index.html")).href);
    await page.addStyleTag({ content: `
      html,body,main,.review{width:1920px!important;margin:0!important;padding:0!important;display:block!important}
      header,aside,.gallery{display:none!important}
      #view{width:1920px;height:1080px;border:0;border-radius:0}
    ` });

    await page.evaluate(() => {
      opticEnabled.checked = true;
      opticPitch.value = 0;
      pointerX = contacts[0].x;
      pointerY = contacts[0].y;
    });
    fs.mkdirSync(proof, { recursive: true });
    await page.locator("#view").screenshot({ path: path.join(proof, "navigateur.png") });

    const result = await page.evaluate(({ width, height, supersampling }) => {
      // Re-execute the authored optic at output resolution and isolate its dashed stroke.
      // All other drawing is suppressed. The original page file is untouched.
      optic.width = 960 * supersampling;
      optic.height = 540 * supersampling;
      opt.scale(supersampling, supersampling);
      const stroke = opt.stroke.bind(opt);
      const markers = [];
      opt.stroke = (...args) => {
        const dash = opt.getLineDash();
        if (!dash.length) return;
        markers.push({ dash, strokeWidth: opt.lineWidth, cap: opt.lineCap, stroke: opt.strokeStyle });
        stroke(...args);
      };
      opt.fill = () => {};
      opt.fillText = () => {};
      opt.fillRect = () => {};
      opt.strokeRect = () => {};
      opt.clip = () => {};
      focus = undefined;
      drawOptic(performance.now());

      const image = document.createElement("canvas");
      image.width = width * supersampling;
      image.height = height * supersampling;
      image.getContext("2d").drawImage(optic,
        (480 - width / 2) * supersampling, (270 - height / 2) * supersampling,
        image.width, image.height, 0, 0, image.width, image.height);
      return { data: image.toDataURL("image/png").split(",")[1], markers };
    }, atlas);
    assert.equal(result.markers.length, 1, "Expected exactly one isolated dashed stroke from the authored optic");
    fs.writeFileSync(texture, Buffer.from(result.data, "base64"));
    fs.writeFileSync(path.join(proof, "stroke.json"), JSON.stringify({ ...atlas, ...result.markers[0] }, null, 2));
    console.log(texture);
  } finally {
    await browser.close();
  }
}

run().catch(error => { console.error(error); process.exitCode = 1; });
