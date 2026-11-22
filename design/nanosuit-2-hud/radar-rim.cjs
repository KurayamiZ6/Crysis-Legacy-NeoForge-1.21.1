module.exports = function drawRadarRim() {
  const mask = document.querySelector("#view .radar-mask");
  const css = getComputedStyle(mask);
  const w = parseFloat(css.width);
  const h = parseFloat(css.height);
  const shear = Math.tan(7 * Math.PI / 180);
  const radar = mask.closest(".radar");
  const clip = radar.querySelector(".radar-terrain-clip");
  const gap = parseFloat(getComputedStyle(radar.querySelector(".radar-status")).columnGap);
  const width = .032 * h / Math.hypot(1, shear);
  // Measure widths and offsets after the in-game shear, then undo that shear for the sprite.
  const screen = ([x, y]) => [x, y - shear * x];
  const terrain = [...getComputedStyle(clip).clipPath.matchAll(/(-?[\d.]+)px\s+(-?[\d.]+)px/g)]
    .map(([,x,y]) => screen([Number(x),Number(y)]));
  if (terrain.length !== 11) throw new Error("Unexpected terrain outline");
  const railStart = parseFloat(getComputedStyle(radar).getPropertyValue("--rail-start")) * h / 100;
  const threat = [[.90 * w, .94 * h], [.976 * w, .78 * h], [.976 * w, railStart]].map(screen);
  const edges = [];
  for (let i = 1; i < 8; i++) edges.push([terrain[i], terrain[i+1]]);
  const map = radar.querySelector(".radar-map");
  const compass = radar.querySelector(".radar-compass");
  const mapStyle = getComputedStyle(map);
  const mapWidth = map.getBoundingClientRect().width;
  const mapHeight = parseFloat(mapStyle.height);
  const cssLength = (value,size) => parseFloat(value)*(value.endsWith("%") ? size/100 : 1);
  const outline = mapStyle.clipPath.slice(8,-1).split(",").slice(0,3).map(pair => {
    const [x,y] = pair.trim().split(/\s+/);
    return screen([cssLength(x,mapWidth),cssLength(y,mapHeight)]);
  });
  if (outline.length !== 3) throw new Error("Unexpected compass outline");
  const top = compass.offsetTop;
  edges[0] = [outline[1],outline[2]];
  // Preserve the small lower-left chamfer where the two terrain edges meet.
  const nearCorner = ([x,y]) => {
    const [cx,cy] = terrain[3];
    const f = 1.5/Math.hypot(x-cx,y-cy);
    return [cx+f*(x-cx),cy+f*(y-cy)];
  };
  const bevelStart = nearCorner(terrain[2]);
  const bevelEnd = nearCorner(terrain[4]);
  edges.splice(1,2,[terrain[2],bevelStart],[bevelStart,bevelEnd],[bevelEnd,terrain[4]]);
  edges.push([threat[0],threat[1]], [threat[1],threat[2]]);
  function projectRail(distance) {
    const lines = edges.map(([[ax,ay],[bx,by]]) => {
      const dx = bx-ax, dy = by-ay;
      return [dy,-dx,dy*ax-dx*ay-distance*Math.hypot(dx,dy)];
    });
    lines.unshift([shear,1,top]);
    lines.push([shear,1,railStart]);
    return lines.slice(1).map(([a,b,c],i) => {
      const [d,e,f] = lines[i];
      const det = a*e-b*d;
      if (Math.abs(det) < 1e-6) throw new Error("Parallel adjoining radar edges");
      return [(c*e-b*f)/det,(a*f-c*d)/det];
    });
  }
  const center = projectRail(gap + width / 2);
  // Extend the stroke before clipping its tip against the compass baseline.
  const [start,next] = center;
  const extend = 2*width/Math.hypot(next[0]-start[0],next[1]-start[1]);
  const tip = [start[0]-(next[0]-start[0])*extend,start[1]-(next[1]-start[1])*extend];
  const points = [tip,...center.slice(1)].map(p => p.join(",")).join(" ");
  const a = -2*w;
  const b = 3*w;
  const cut = [[a,top-shear*a],[b,top-shear*b],[b,2*h],[a,2*h]].map(([x,y]) => x+"px "+y+"px").join(",");
  const style = document.createElement("style");
  style.textContent = "#view .radar-mask::before{display:none}";
  document.head.append(style);
  mask.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" style="position:absolute;inset:0;overflow:visible;' +
    'transform:skewY(7deg);transform-origin:0 0;clip-path:polygon('+cut+')" width="'+w+'" height="'+h+'" aria-hidden="true">' +
    '<polyline points="'+points+'" fill="none" stroke="var(--radar-rail)" stroke-width="'+width+'" stroke-linejoin="miter"/></svg>';
};
