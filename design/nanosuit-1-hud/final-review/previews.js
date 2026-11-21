const assets = '../../../src/main/resources/assets/crysismod/textures/gui';
const glyphAtlas = assets + '/classic_binoculars/glyphs.png';

function glyphs(value, x, baseline, height, advance, opacity = 1) {
  return [...value].map((character, index) => {
    if (character === ' ') return '';
    const offset = (character.charCodeAt(0) - 32) * 32;
    return `<svg x="${x + index * advance}" y="${baseline - height}" width="${advance}" height="${height * 1.25}"
      viewBox="${offset} 0 32 80" preserveAspectRatio="none" opacity="${opacity}" overflow="hidden">
      <image href="${glyphAtlas}" width="1888" height="80" filter="url(#glyph-green)"/>
    </svg>`;
  }).join('');
}

function updateAmmo() {
  const proposed = document.querySelector('#ammo-layout').value === 'proposal';
  const reserve = document.querySelector('#ammo-count').value;
  const reserveX = proposed ? 262 - reserve.length * 13.5 : 234;
  document.querySelector('#ammo').innerHTML = `
    <rect x="112" y="-20" width="279" height="108" fill="#141c23"/>
    <image href="${assets}/classic_weapon/frame.svg" width="440" height="125" filter="url(#frame-green)"/>
    <g transform="matrix(1 .045 0 1 0 0)">
      ${glyphs('00', 149, 39, proposed ? 42 : 35, 17, .3)}
      ${glyphs('7', 183, 39, proposed ? 42 : 35, 17)}
      ${glyphs(reserve, reserveX, 30, 27, 13.5)}
    </g>
    <image href="${assets}/classic_weapon/shell-semi.svg" x="294" y="7" width="60" height="48" filter="url(#frame-green)"/>`;
}

function updateOptic() {
  const proposed = document.querySelector('#optic-layout').value === 'proposal';
  const showItem = document.querySelector('#item-label').checked;
  document.querySelector('#optic').innerHTML = `
    <rect x="755" y="730" width="410" height="158" fill="#141c23"/>
    <image href="${assets}/classic_binoculars/frame.svg" width="1920" height="1080" filter="url(#frame-green)"/>
    ${glyphs('11.00 M', 960 - 7 * 14.5 / 2, proposed ? 797 : 859, 29, 14.5)}
    ${showItem ? '<text x="960" y="869" text-anchor="middle" font-family="monospace" font-weight="bold" font-size="27" fill="#dedede">Glock 17</text>' : ''}`;
}

const svgNS = 'http://www.w3.org/2000/svg';
for (const figure of document.querySelectorAll('.capture')) {
  const { image, size, crop } = figure.dataset;
  const [width, height] = size.split(' ');
  const link = document.createElement('a');
  link.href = image;
  link.className = 'image-link';
  link.setAttribute('aria-label', 'Ouvrir la capture originale');
  const svg = document.createElementNS(svgNS, 'svg');
  svg.setAttribute('viewBox', crop);
  svg.setAttribute('class', 'frame');
  const raster = document.createElementNS(svgNS, 'image');
  raster.setAttribute('href', image);
  raster.setAttribute('width', width);
  raster.setAttribute('height', height);
  svg.append(raster);
  link.append(svg);
  figure.prepend(link);
}

const decisions = [...document.querySelectorAll('.decision')];
for (const select of decisions) {
  select.add(new Option('Choisir…', ''));
  for (const value of ['ACCEPTER', 'REFUSER', 'À MODIFIER', 'REPORTER']) select.add(new Option(value));
  select.addEventListener('change', () => {
    document.querySelector('#reply').value = decisions
      .filter(choice => choice.value)
      .map(choice => choice.dataset.id + ' — ' + choice.value).join('\n');
  });
}
for (const id of ['ammo-layout', 'ammo-count']) document.getElementById(id).addEventListener('change', updateAmmo);
for (const id of ['optic-layout', 'item-label']) document.getElementById(id).addEventListener('change', updateOptic);
updateAmmo();
updateOptic();
