// Usage: node render-assets.cjs <playwright-module> <chrome.exe> <asset-directory>
const { chromium } = require(process.argv[2]);
const fs = require('fs');
const path = require('path');
const { pathToFileURL } = require('url');
(async () => {
    const browser = await chromium.launch({ executablePath: process.argv[3], headless: true });
    for (const file of fs.readdirSync(process.argv[4]).filter(f => f.endsWith('.svg'))) {
        const source = fs.readFileSync(path.join(process.argv[4], file), 'utf8');
        const [, w, h] = source.match(/width="(\d+)" height="(\d+)"/);
        const page = await browser.newPage({viewport: {width: +w, height: +h}, deviceScaleFactor: 1});
        await page.goto(pathToFileURL(path.join(process.argv[4], file)).href);
        await page.screenshot({path: path.join(process.argv[4], file.replace('.svg', '.png')), omitBackground: true});
        await page.close();
    }
    await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
