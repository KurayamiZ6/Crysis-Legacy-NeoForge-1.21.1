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
  ['references/fire-auto.png', 'Nouvelle référence · tir automatique'],
  [qa + '/classic-hud/evidence-final/fire-burst.png', 'Minecraft · BURST, capture QA archivée · 1920 × 1080', [1669, 878, 89, 59]],
  [0, 0, 360, 179], 'Maquette FIRE-01 · quatre symboles à échelle commune');

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
      opacity="${alpha}" fill="none" stroke="#c0efa6" stroke-width="2.9" stroke-linejoin="round"/>`;
  }).join('');
}


const slope = 'matrix(1 .045 0 1 0 -6.75)';
const projectile = 'M-3 12V-8L-2 -13L0 -16L2 -13L3 -8V12Z';
function shell(x, y) {
  return `<path d="M-22 -6H-18V-2H-22ZM-22 2H-18V6H-22ZM-15 -5H-10V5H-15ZM-8 -5H12V5H-8ZM13 -5H21V5H13Z"
    transform="translate(${x} ${y})" fill="#95b193"/>`;
}
function fireRounds(mode, x, y) {
  const offsets = { SEMI: [0], BURST: [-10, 0, 10], AUTO: [-20, -10, 0, 10, 20] }[mode];
  return offsets.map(offset => `<path d="${projectile}" fill="#95b193"
    transform="translate(${x + offset} ${y})"/>`).join('');
}
function block({ join, polygonal, wide, shotgun }) {
  const plate = join
    ? `<g transform="${slope}">
         <path d="M132 13L150 0H349L366 17V51H132Z M132 56H366V107H145L132 94Z" fill="#0b1116" opacity=".72"/>
         <path d="M278 1V51M278 56V106M349 0L366 17V107L418 139" fill="none" stroke="#719671" stroke-width="1.8" opacity=".83"/>
         <path d="M8 122L25 107H354L306 140V182H20L8 170Z" fill="#090f15" opacity=".81"/>
         <path d="M8 170L20 182H306" fill="none" stroke="#62815b" stroke-width="1.8"/>
       </g>
       <path d="M366 126L416 156V211L366 242L315 211V156Z" fill="#090f15" stroke="#62815b" stroke-width="2"/>
       <path d="M419 201V220L366 251" fill="none" stroke="#62815b" stroke-width="1.7"/>`
    : `<image href="${assets}/classic_weapon/frame.svg" width="440" height="125" opacity=".65"/>
       <image href="${assets}/classic_hud/panel.png" y="100" width="440" height="155"/>`;
  const energy = wide ? '25' : '100';
  const health = wide ? '50' : '100';
  const clip = polygonal ? 'type' : 'geometry';
  const load = wide ? '123' : shotgun ? '7' : '31';
  const reserve = wide ? '128' : shotgun ? '40' : '43';
  const throwable = wide ? '12' : shotgun ? '4' : '5';
  // The atlas stays in HUD-02. TYPE-01 separately proposes the heavier glyphs.
  const endNumber = (value, y) => number(value, 266 - value.length * 27 * .49 / 2, y, 27, polygonal);
  return `<rect x="-10" y="-11" width="450" height="263" fill="#172026"/>
    ${plate}
    <defs>
      <clipPath id="${clip}-energy"><rect x="${wide ? 146 : 16}" y="100" width="173" height="155"/></clipPath>
      <clipPath id="${clip}-health"><rect x="${wide ? 102.5 : 16}" y="100" width="173" height="155"/></clipPath>
    </defs>
    <image href="${assets}/classic_hud/energy.png" y="100" width="440" height="155" clip-path="url(#${clip}-energy)"/>
    <image href="${assets}/classic_hud/health.png" y="100" width="440" height="155" clip-path="url(#${clip}-health)"/>
    ${join ? `<g transform="${slope}">
      <path d="M196 121H237V140H196Z" fill="#0076b0"/><path d="M196 149H237V169H196Z" fill="#7dca2b"/>
      <path d="M217 124L213 132H217L215 139L222 129H218Z" fill="#d4eee0"/>
      <path d="M217 153V164M212 158H222" stroke="#d4eead" stroke-width="3"/>
      <path d="M20 131V140M20 159V167M49 138V146M192 139V146M192 167V174" fill="none" stroke="#77935d" stroke-width="1.5"/>
    </g>` : ''}
    <image href="${assets}/classic_hud/armor.png" y="100" width="440" height="155"/>
    <g transform="${slope}">
      ${join ? `<image href="${assets}/classic_weapon/shell-semi.svg" x="292" y="4" width="60" height="48" opacity=".6"/>`
        : shotgun ? shell(322, 26) : fireRounds('AUTO', 322, 26)}
      ${number(load, 155 + load.length * 35 * .49 / 2, 25, 35, polygonal)}
      ${endNumber(reserve, 21)}${endNumber(throwable, 73)}
      <image href="${assets}/classic_weapon/throwable-frag.svg" x="302" y="55" width="40" height="40" opacity=".6"/>
      ${number(energy, 272, 132, 25, polygonal)}${number(health, 272, 160, 25, polygonal)}
    </g>`;
}
geometry.innerHTML = block({ join: true, polygonal: false, wide: false, shotgun: true });
const sample = document.getElementById('sample');
function updateType() {
  type.innerHTML = block({ join: false, polygonal: true, wide: sample.value === 'wide', shotgun: sample.value === 'shotgun' });
}
sample.addEventListener('change', updateType);
updateType();

fire.innerHTML = ['SEMI', 'BURST', 'AUTO', 'CARTOUCHE'].map((label, index) => {
  return `<g transform="translate(${index * 90} 0)">
    <path d="M7 51L18 42H73L84 53V110H7Z" fill="#0d151b" stroke="#719671"/>
    ${label === 'CARTOUCHE' ? shell(45, 78) : fireRounds(label, 45, 78)}
    <text x="45" y="139" text-anchor="middle" fill="#c6d8c9" font-size="12" font-family="system-ui">${label}</text>
  </g>`;
}).join('');
const shellReference = capture('references/shotgun-shell.png', 'Nouvelle référence · cartouche et grenade');
document.getElementById('fire-grid').children[0].append(...shellReference.children);

const malfunction = mountComparison('malfunction',
  ['references/malfunction.png', 'Remastered · état de panne observé'],
  [rightHud, 'Minecraft · HUD N1 en fonctionnement normal', [0, 797, 1920, 283]],
  [0, 0, 1100, 225], 'MALF-01 · démonstration animée sur fond de maquette');
malfunction.classList.add('malfunction-preview');
malfunction.innerHTML = `
  <defs>
    <clipPath id="jammed-map"><path d="M52 56L218 46V171L200 187H52Z"/></clipPath>
    <filter id="terrain-grain"><feTurbulence type="fractalNoise" baseFrequency=".071 .22" numOctaves="2" seed="7"/>
      <feColorMatrix type="matrix" values="0 .63 0 0 .18  0 .29 0 0 .16  0 .31 0 0 .16  0 0 0 1 0"/>
    </filter>
    <pattern id="jammed-lines" width="168" height="19" patternUnits="userSpaceOnUse">
      <path d="M0 2H168M17 10H124M76 13H167" stroke="#f58f95" stroke-width="2" opacity=".65"/>
    </pattern>
  </defs>
  <path d="M0 179L156 132L317 147L540 88L740 135L913 115L1100 175V225H0Z" fill="#33414b"/>
  <g fill="none" stroke="#eb727a">
    <path d="M47 31L57 23H86M1009 23H1038L1053 31" opacity=".65"/>
    <path d="M52 137V48L65 35L218 29M218 46V171L200 187H77" stroke-width="1.4"/>
    <path d="M15 155L40 142L67 156V182L40 199L15 184Z" fill="#20262d"/>
  </g>
  <g fill="#f2767e" font-family="system-ui" font-weight="700" font-size="12">
    <text x="70" y="46">E</text><text x="126" y="42">SE</text><text x="189" y="38">S</text>
  </g>
  <g clip-path="url(#jammed-map)">
    <rect x="52" y="46" width="166" height="141" filter="url(#terrain-grain)" opacity=".63"/>
    <path d="M55 162L108 122L128 132L157 89L219 67" stroke="#e27982" stroke-width="5" opacity=".33" fill="none"/>
    <rect class="interference" x="52" y="46" width="166" height="141" fill="url(#jammed-lines)" opacity=".38"/>
    <path d="M135 123L142 116L149 123M142 61V104M142 132V181" stroke="#f4a4aa" opacity=".68" fill="none"/>
    <path d="M56 59V154" stroke="#ed5965" stroke-width="5" stroke-dasharray="19 4"/>
  </g>
  <path d="M40 155V175M40 180V186" stroke="#f15c65" stroke-width="3.5"/>
  <g transform="translate(810 55) skewY(2.575)">
    <path d="M61 0H207L221 12V38H61ZM61 43H221V81H61Z" fill="#1e242a" opacity=".82"/>
    <path d="M159 2V38M159 43V81M207 0L221 12V81L246 100" fill="none" stroke="#eb727a"/>
    <g class="pulse" fill="#f15c65" font-family="system-ui" font-size="10.5" font-weight="800">
      <text x="69" y="24">MALFUNCTION</text><text x="69" y="66">MALFUNCTION</text>
    </g>
    <path d="M0 86H191V142H8L0 134Z" fill="#1b2328" opacity=".88"/>
    <path d="M0 134L8 142H191" fill="none" stroke="#eb727a"/>
    <path d="M9 96H155V110H9ZM9 119H155V133H9Z" fill="#812f37" opacity=".2"/>
    <path d="M207 106L230 92L254 106V131L230 147L207 132Z" fill="#1b2328" stroke="#eb727a"/>
    <path d="M254 136V143L231 157" fill="none" stroke="#eb727a"/>
    <path d="M221 108H239L236 124L230 130L224 124Z" fill="#eb727a"/>
  </g>`;
const replay = document.getElementById('replay-malfunction');
function playMalfunction() {
  malfunction.classList.remove('play');
  malfunction.getBoundingClientRect();
  malfunction.classList.add('play');
}
replay.addEventListener('click', playMalfunction);

const evidence = [
  ['Coup par coup · projectile vertical', ref + '/01-pistol.jpg', qa + '/classic-hud/evidence-final/fire-semi.png'],
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
