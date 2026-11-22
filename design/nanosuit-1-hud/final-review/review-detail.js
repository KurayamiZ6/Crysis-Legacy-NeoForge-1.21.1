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
const fullCurrent = [rightHud, 'Minecraft · avant HUD-02, SPAS-12 · 1920 × 1080', [1470, 868, 329, 182]];
const viewBox = [-10, -11, 450, 263];
const geometry = mountComparison('geometry', fullReference, fullCurrent, viewBox, 'Maquette HUD-02 · nouveau cadre, valeurs de démonstration');
const type = mountComparison('type', fullReference, fullCurrent, viewBox, 'Maquette TYPE-01 · nouveau cadre et chiffres intégrés, valeurs de démonstration');
const fire = mountComparison('fire',
  ['references/fire-auto.png', 'Nouvelle référence · tir automatique'],
  [qa + '/classic-hud/evidence-final/fire-burst.png', 'Minecraft · BURST, capture QA archivée · 1920 × 1080', [1669, 878, 89, 59]],
  [0, 0, 360, 179], 'Maquette FIRE-01 · quatre symboles à échelle commune');

function digits(value, right, top, height, maxWidth, filter, minimum = 0) {
  const digits = value.padStart(minimum, '0');
  height = Math.min(height, maxWidth / (digits.length * .49));
  const advance = height * .49;
  return [...digits].map((digit, i) => `<svg x="${right + (i - digits.length) * advance}" y="${top}"
    width="${advance}" height="${height}" viewBox="${Number(digit) * 16} 0 16 24" preserveAspectRatio="none"
    opacity="${i < digits.length - value.length ? .30 : 1}">
    <image href="${assets}/classic_hud/digits.png" width="160" height="24" filter="url(#${filter})"/>
  </svg>`).join('');
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
function mixWithWhite(rgb, amount) {
  const channels = [1, 3, 5].map(offset => parseInt(rgb.slice(offset, offset + 2), 16));
  return '#' + channels.map(value => Math.round(value + (255 - value) * amount).toString(16).padStart(2, '0')).join('');
}
function tint(id, color) {
  return `<filter id="${id}" color-interpolation-filters="sRGB"><feFlood flood-color="${color}"/>
    <feComposite in2="SourceAlpha" operator="in"/></filter>`;
}

function chrome(prefix, mode, art = 'panel') {
  return `<image href="${assets}/classic_weapon/frame.png" width="440" height="125"/>
    <image href="${assets}/classic_weapon/frame-outline.png" width="440" height="125" filter="url(#${prefix}-accent)"/>
    <image href="${assets}/classic_hud/${art}.png" y="100" width="440" height="155"/>
    <image href="${assets}/classic_hud/panel-outline.png" y="100" width="440" height="155" filter="url(#${prefix}-outline)"/>
    <image href="${assets}/classic_hud/${mode}.png" y="100" width="440" height="155" filter="url(#${prefix}-accent)"/>`;
}

function block({ id, wide, shotgun, mode = 'armor', theme = '#719671' }) {
  const energy = wide ? '25' : '100';
  const health = wide ? '50' : '100';
  const load = wide ? '123' : shotgun ? '7' : '31';
  const reserve = wide ? '128' : shotgun ? '40' : '43';
  const throwable = wide ? '12' : shotgun ? '4' : '5';
  const otherText = theme === '#719671' ? '#c0efa6' : theme;
  return `<rect x="-10" y="-11" width="450" height="263" fill="#172026"/>
    <defs>
      ${tint(id + '-accent', theme)}
      ${tint(id + '-outline', theme === '#719671' ? '#62815b' : theme)}
      ${tint(id + '-text', otherText)}
      ${tint(id + '-loaded', mixWithWhite(otherText, .23))}
      <clipPath id="${id}-energy"><rect x="${189 - 173 * Number(energy) / 100}" y="100" width="173" height="155"/></clipPath>
      <clipPath id="${id}-health"><rect x="${189 - 173 * Number(health) / 100}" y="100" width="173" height="155"/></clipPath>
    </defs>
    ${chrome(id, mode)}
    <image href="${assets}/classic_hud/energy.png" y="100" width="440" height="155" clip-path="url(#${id}-energy)"/>
    <image href="${assets}/classic_hud/health.png" y="100" width="440" height="155" clip-path="url(#${id}-health)"/>
    <g transform="${slope}">
      <image href="${assets}/classic_weapon/${shotgun ? 'shell-semi' : 'auto'}.png" x="292" y="1" width="60" height="48" filter="url(#${id}-accent)"/>
      ${digits(load, 211, 4, 39, 73, id + '-loaded', 3)}
      ${digits(reserve, 274, 2, 29, 59, id + '-text')}
      ${digits(throwable, 274, 62, 39, 125, id + '-text')}
      <image href="${assets}/classic_weapon/throwable-frag.png" x="302" y="61" width="40" height="40" filter="url(#${id}-accent)"/>
      ${digits(energy, 292, 113, 28, 49, id + '-text')}${digits(health, 292, 142, 28, 49, id + '-text')}
    </g>`;
}

geometry.innerHTML = block({ id: 'geometry', wide: false, shotgun: true });
const sample = document.getElementById('sample');
const themeSample = document.getElementById('type-theme');
const suitMode = document.getElementById('type-mode');
function updateType() {
  type.innerHTML = block({ id: 'type', wide: sample.value === 'wide', shotgun: sample.value === 'shotgun',
    theme: themeSample.value, mode: suitMode.value });
}
sample.addEventListener('change', updateType);
themeSample.addEventListener('input', updateType);
suitMode.addEventListener('change', updateType);
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
    <clipPath id="jammed-map"><path d="M52 56L218 46V171L200 187L76 193V152L52 139Z"/></clipPath>
    <filter id="terrain-grain"><feTurbulence type="fractalNoise" baseFrequency=".071 .22" numOctaves="2" seed="7"/>
      <feColorMatrix type="matrix" values="0 .63 0 0 .18  0 .29 0 0 .16  0 .31 0 0 .16  0 0 0 1 0"/>
    </filter>
    <pattern id="crt-scanlines" width="2" height="3" patternUnits="userSpaceOnUse">
      <path d="M0 1H2" stroke="#201319" opacity=".65"/>
    </pattern>
    <pattern id="jammed-lines" width="168" height="23" patternUnits="userSpaceOnUse">
      <path d="M0 2H168M17 10H124M76 16H167" stroke="var(--hud-theme)" stroke-width="2" opacity=".65"/>
    </pattern>
  </defs>
  <path d="M0 179L156 132L317 147L540 88L740 135L913 115L1100 175V225H0Z" fill="#33414b"/>
  <path d="M52 56V48L65 35L199 27L218 46Z" fill="#202020" opacity=".88"/>
  <g fill="none" stroke="var(--hud-theme)">
    <path d="M47 31L57 23H86M1009 23H1038L1053 31" opacity=".65"/>
    <path d="M52 139V48L65 35M218 171L200 187L76 193" stroke-width="1.4"/>
    <path d="M15 155L40 142L67 156V182L40 199L15 184Z" fill="#202020" fill-opacity=".8"/>
  </g>
  <g fill="var(--hud-theme)" font-family="system-ui" font-weight="700" font-size="12">
    <text x="70" y="49">E</text><text x="126" y="45">SE</text><text x="189" y="41">S</text>
  </g>
  <g clip-path="url(#jammed-map)">
    <g class="malfunction-terrain">
      <rect x="52" y="46" width="166" height="147" fill="#4e3c40"/>
      <path d="M52 46H218V74L166 88L139 66L108 79L89 119L52 136ZM52 169L98 140L133 145L166 124L218 119V187H52Z" fill="#87615e"/>
      <path d="M52 156L88 138L109 103L136 90L166 99L218 74" fill="none" stroke="#26292d" stroke-width="17"/>
      <path d="M52 156L88 138L109 103L136 90L166 99L218 74" fill="none" stroke="#b59891" stroke-width="2" opacity=".6"/>
      <rect x="52" y="46" width="166" height="147" filter="url(#terrain-grain)" opacity=".3"/>
      <rect x="52" y="46" width="166" height="147" fill="var(--hud-theme)" opacity=".15"/>
    </g>

    <rect class="interference" x="52" y="46" width="166" height="147" fill="url(#jammed-lines)" opacity=".38"/>
    <rect x="52" y="46" width="166" height="147" fill="url(#crt-scanlines)" opacity=".55"/>
    <path class="scan-beam" d="M52 49H218V54H52Z" fill="var(--hud-theme)" opacity=".22"/>
    <path class="player-marker" d="M135 123L142 116L149 123M142 61V104M142 132V181" stroke="#f4a4aa" opacity=".68" fill="none"/>
    <path d="M56 59V154" stroke="var(--hud-theme)" stroke-width="5" stroke-dasharray="19 4"/>
  </g>
  <path d="M40 155V175M40 180V186" stroke="var(--hud-theme)" stroke-width="3.5"/>
  <g transform="translate(815 60) scale(.64)">
    <defs>${tint('fault-accent', 'var(--hud-theme)')}${tint('fault-outline', 'var(--hud-theme)')}</defs>
    ${chrome('fault', 'armor', 'panel-malfunction')}
    <g transform="${slope}">
      <g class="pulse" fill="var(--hud-theme)" font-family="Bahnschrift, Arial, sans-serif" font-size="25" font-weight="700">
        <text x="150" y="34" textLength="110" lengthAdjust="spacingAndGlyphs">MALFUNCTION</text>
        <text x="150" y="89" textLength="110" lengthAdjust="spacingAndGlyphs">MALFUNCTION</text>
      </g>
    </g>
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
