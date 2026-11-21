const root = '../../..';
const qa = root + '/qa';
const ref = '../references/user-2026-11-19';
const assets = root + '/src/main/resources/assets/crysismod/textures/gui';
const rightHud = qa + '/stabilization/evidence/hud-refinements/map-default-final.png';
const statusRef = ref + '/detail-status.jpg';
const svgNS = 'http://www.w3.org/2000/svg';

function capture(file, caption, crop) {
  const figure = document.createElement('figure');
  const link = document.createElement('a');
  link.href = file;
  link.className = 'shot';
  link.setAttribute('aria-label', 'Ouvrir la capture complète');
  const image = new Image();
  image.alt = caption;
  if (crop) {
    const svg = document.createElementNS(svgNS, 'svg');
    svg.setAttribute('viewBox', crop.join(' '));
    svg.style.setProperty('--ratio', crop[2] / crop[3]);
    const raster = document.createElementNS(svgNS, 'image');
    image.addEventListener('load', () => {
      raster.setAttribute('width', image.naturalWidth);
      raster.setAttribute('height', image.naturalHeight);
      raster.setAttribute('href', file);
    });
    svg.append(raster);
    link.append(svg);
  } else {
    image.loading = 'lazy';
    link.append(image);
  }
  image.src = file;
  const label = document.createElement('figcaption');
  label.textContent = caption;
  figure.append(link, label);
  return figure;
}

function mountComparison(id, reference, current, viewBox, notes) {
  const grid = document.getElementById(id + '-grid');
  grid.append(capture(...reference), capture(...current));
  const figure = document.createElement('figure');
  const svg = document.createElementNS(svgNS, 'svg');
  svg.setAttribute('viewBox', viewBox.join(' '));
  svg.setAttribute('class', 'mock');
  svg.setAttribute('aria-label', notes);
  svg.style.setProperty('--ratio', viewBox[2] / viewBox[3]);
  const caption = document.createElement('figcaption');
  caption.textContent = notes;
  figure.append(svg, caption);
  grid.append(figure);
  return svg;
}

const fullReference = [statusRef, 'Remastered · panneau de droite, extrait fourni', [0, 0, 475, 256]];
const fullCurrent = [rightHud, 'Minecraft · dernier rendu N1, SPAS-12 · 1920 × 1080', [1470, 868, 329, 182]];
const viewBox = [-10, -11, 450, 263];
const geometry = mountComparison('geometry', fullReference, fullCurrent, viewBox, 'Maquette HUD-02 · proposition de raccord, chiffres de démonstration');
const type = mountComparison('type', fullReference, fullCurrent, viewBox, 'Maquette TYPE-01 · cadre actuel, chiffres polygonaux originaux');
const fire = mountComparison('fire',
  [ref + '/01-pistol.jpg', 'Remastered · SEMI documenté · 2538 × 1506', [2206, 1240, 139, 92]],
  [qa + '/classic-hud/evidence-final/fire-burst.png', 'Minecraft · BURST, capture QA archivée · 1920 × 1080', [1669, 878, 89, 59]],
  [0, 0, 270, 179], 'Maquette FIRE-01 · formes présentées avec leur nom pour la comparaison');

const digitPaths = {
  0: 'M2 1H8L9 2V18L8 19H2L1 18V2Z',
  1: 'M2 5L5 1H6V19', 2: 'M1 4V2L2 1H8L9 2V8L1 17V19H9',
  3: 'M1 1H8L9 2V8L7 10L9 12V18L8 19H1M4 10H7',
  4: 'M1 1V11H9M8 1V19', 5: 'M9 1H1V9H7L9 11V18L8 19H1',
  6: 'M9 1H3L1 3V18L2 19H8L9 18V11L8 10H1',
  7: 'M1 1H9V4L4 19', 8: 'M2 1H8L9 2V8L7 10L9 12V18L8 19H2L1 18V12L3 10L1 8V2ZM3 10H7',
  9: 'M9 10H2L1 9V2L2 1H8L9 2V17L7 19H1'
};

function number(value, x, y, height, polygonal, dimLeading = false) {
  const advance = height * .49;
  if (!polygonal) {
    return [...value].map((digit, i) => {
      const offset = (digit.charCodeAt(0) - 32) * 32;
      const alpha = dimLeading && digit === '0' && i < value.search(/[^0]/) ? .32 : 1;
      const left = x + (i - value.length / 2) * advance;
      return `<svg x="${left}" y="${y - height / 2}" width="${advance}" height="${height * 1.25}"
        viewBox="${offset} 0 32 80" preserveAspectRatio="none" opacity="${alpha}">
        <image href="${assets}/classic_binoculars/glyphs.png" width="1888" height="80"/>
      </svg>`;
    }).join('');
  }
  return [...value].map((digit, i) => {
    const left = x + (i - value.length / 2) * advance;
    const alpha = dimLeading && digit === '0' && i < value.search(/[^0]/) ? .32 : 1;
    return `<path d="${digitPaths[digit]}" transform="translate(${left} ${y - height / 2}) scale(${advance / 12} ${height / 20})"
      opacity="${alpha}" fill="none" stroke="#c0efa6" stroke-width="2.1" stroke-linejoin="round"/>`;
  }).join('');
}

function block({ join, polygonal, wide }) {
  const plate = join
    ? `<path d="M132 14L150 2L349 11L366 28V115L145 105L132 94Z" fill="#0b1116" opacity=".91"/>
       <g fill="none" stroke="#719671" stroke-width="1.8">
         <path d="M149 2L349 11L366 28V115L418 148"/>
         <path d="M132 50L366 60M132 97L366 107M278 8V103" opacity=".63"/>
       </g>
       <path d="M8 122L25 107L354 122L306 151V197L20 183L8 171Z" fill="#090f15" stroke="#58704d" stroke-width="1.6"/>
       <path d="M366 126L416 156V211L366 242L315 211V156Z" fill="#090f15" stroke="#62815b" stroke-width="2"/> `
    : `<image href="${assets}/classic_weapon/frame.svg" width="440" height="125" opacity=".65"/>
       <image href="${assets}/classic_hud/panel.png" y="100" width="440" height="155"/>`;
  const energy = wide ? '25' : '100';
  const health = wide ? '50' : '100';
  const clip = polygonal ? 'type' : 'geometry';
  return `<rect x="-10" y="-11" width="450" height="263" fill="#172026"/>
    ${plate}
    <defs>
      <clipPath id="${clip}-energy"><rect x="${wide ? 146 : 16}" y="100" width="173" height="155"/></clipPath>
      <clipPath id="${clip}-health"><rect x="${wide ? 102.5 : 16}" y="100" width="173" height="155"/></clipPath>
    </defs>
    <image href="${assets}/classic_hud/energy.png" y="100" width="440" height="155" clip-path="url(#${clip}-energy)"/>
    <image href="${assets}/classic_hud/health.png" y="100" width="440" height="155" clip-path="url(#${clip}-health)"/>
    ${join ? '<path d="M196 123L237 125V145L196 143Z" fill="#0076b0"/><path d="M196 151L237 153V174L196 172Z" fill="#7dca2b"/><path d="M217 127L213 134H217L215 142L222 131H218Z" fill="#d4eee0"/><path d="M217 156V167M212 161H222" stroke="#d4eead" stroke-width="3"/>' : ''}
    <image href="${assets}/classic_hud/armor.png" y="100" width="440" height="155"/>
    <image href="${assets}/classic_weapon/shell-semi.svg" x="294" y="8" width="60" height="48" opacity=".6"/>
    ${number(wide ? '123' : '007', polygonal ? 177 : 175, 29, 35, polygonal, true)}
    ${number(wide ? '128' : '29', polygonal ? 245 : 247, 27, 27, polygonal)}
    ${polygonal ? number(wide ? '12' : '8', 245, 77, 27, true) +
      `<image href="${assets}/classic_weapon/throwable-frag.svg" x="298" y="65" width="40" height="40" opacity=".6"/>` : ''}
    ${number(energy, 272, 137, 25, polygonal)}${number(health, 272, 165, 25, polygonal)}`;
}

geometry.innerHTML = block({ join: true, polygonal: false, wide: false });
const sample = document.getElementById('sample');
function updateType() {
  type.innerHTML = block({ join: false, polygonal: true, wide: sample.value === 'wide' });
}
sample.addEventListener('change', updateType);
updateType();

const projectile = 'M-4 9V-7L-2 -14L0 -17L2 -14L4 -7V9Z';
const firingModes = [
  ['SEMI', [[0, 0, 1]]],
  ['BURST', [[-9, 1, .78], [0, -4, .9], [9, 1, .78]]],
  ['AUTO', [[-22, 3, .68], [-11, 1, .78], [0, -1, .88], [11, 1, .78], [22, 3, .68]]]
];
fire.innerHTML = firingModes.map(([label, rounds], index) => {
  const x = index * 90;
  return `<g transform="translate(${x} 0)">
    <path d="M7 51L18 42H73L84 53V110H7Z" fill="#0d151b" stroke="#719671"/>
    ${rounds.map(([dx, dy, s]) => `<path d="${projectile}" fill="#a9c3a0"
      transform="translate(${45 + dx} ${78 + dy}) scale(${s})"/>`).join('')}
    <text x="45" y="139" text-anchor="middle" fill="#c6d8c9" font-size="12" font-family="system-ui">${label}</text>
  </g>`;
}).join('');

const evidence = [
  ['Bloc droit et état critique', ref + '/08-binocular-red.jpg', qa + '/stabilization/evidence/hud-refinements/bars-empty.png'],
  ['Mode actif et coins', '../references/user-remastered-full.png', qa + '/classic-hud/frame-evidence/n1-speed.png'],
  ['Batterie et vision nocturne', ref + '/05-night-vision.jpg', qa + '/stabilization/evidence/stability-combined-final-lit.png'],
  ['Radial et sélection', '../../nanosuit-1-radial/assets/reference-menu.png', qa + '/radial-menu/evidence-feedback/feedback-strength-after-flash.png'],
  ['Carte, cône et cap', ref + '/01-pistol.jpg', rightHud],
  ['Menace active', ref + '/11-detected-red.jpg', qa + '/stabilization/evidence/sprint-contacts/classic-contacts-complementary-contacts-1.png'],
  ['Jumelles ouvertes', ref + '/04-binoculars.jpg', qa + '/stabilization/evidence/stability-combined-final-tacz-binoculars.png'],
  ['Vision nocturne avec jumelles', ref + '/06-night-binoculars.jpg', qa + '/stabilization/evidence/stability-combined-final-tacz-zoom4.png'],
  ['Transition d’arme', ref + '/03-weapon-switch.jpg', qa + '/classic-hud/evidence-final/weapon-switch.png']
];
for (const [label, original, minecraft] of evidence) {
  const detail = document.createElement('details');
  const title = document.createElement('summary');
  title.textContent = label;
  const grid = document.createElement('div');
  grid.className = 'grid';
  grid.append(capture(original, 'Remastered · capture de référence'), capture(minecraft, 'Minecraft · capture du banc, état comparable ou archivé'));
  detail.append(title, grid);
  document.getElementById('evidence').append(detail);
}

const fireDetail = document.createElement('details');
const fireSummary = document.createElement('summary');
fireSummary.textContent = 'Voir les trois états de tir du mod actuel';
const existingFire = document.createElement('div');
existingFire.className = 'grid';
for (const mode of ['semi', 'burst', 'auto']) {
  existingFire.append(capture(qa + '/classic-hud/evidence-final/fire-' + mode + '.png',
    'Minecraft · ' + mode.toUpperCase() + ' · capture archivée', [1580, 875, 170, 65]));
}
fireDetail.append(fireSummary, existingFire);
document.getElementById('fire-grid').after(fireDetail);

const decisions = [...document.querySelectorAll('[data-decision]')];
for (const select of decisions) {
  select.add(new Option('Choisir…', ''));
  for (const value of ['ACCEPTER', 'REFUSER', 'À MODIFIER', 'REPORTER']) select.add(new Option(value));
  select.addEventListener('change', () => {
    document.getElementById('reply').value = decisions
      .filter(choice => choice.value)
      .map(choice => choice.dataset.decision + ' — ' + choice.value).join('\n');
  });
}
