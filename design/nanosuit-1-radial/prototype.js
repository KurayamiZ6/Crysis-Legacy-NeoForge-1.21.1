"use strict";
const svg = document.getElementById("radial-prototype");
const radial = document.getElementById("radial-menu");
const labels = { neutral: "Neutre", armor: "Armure", strength: "Force", speed: "Vitesse", cloak: "Camouflage" };
const pointerAngles = { neutral: 0, speed: 0, strength: 90, cloak: 180, armor: -90 };
let background = "light";

function selectMode(mode) {
  svg.dataset.selected = mode;
  svg.querySelectorAll(".mode").forEach(panel => {
    const selected = panel.dataset.mode === mode;
    panel.classList.toggle("selected", selected);
    panel.setAttribute("aria-pressed", String(selected));
  });
  document.querySelectorAll("[data-select]").forEach(button => {
    button.setAttribute("aria-pressed", String(button.dataset.select === mode));
  });
  document.getElementById("direction-pointer").setAttribute("transform", "rotate(" + pointerAngles[mode] + " 132 141)");
  document.getElementById("view-status").textContent = labels[mode] + " · Fond " + (background === "light" ? "clair" : "sombre");
}

document.querySelectorAll("[data-select]").forEach(button => {
  button.addEventListener("click", () => selectMode(button.dataset.select));
});
svg.querySelectorAll(".mode").forEach(panel => {
  panel.addEventListener("keydown", event => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      selectMode(panel.dataset.mode);
    }
  });
});
svg.addEventListener("pointermove", event => {
  const point = svg.createSVGPoint();
  point.x = event.clientX;
  point.y = event.clientY;
  const local = point.matrixTransform(radial.getScreenCTM().inverse());
  const dx = local.x - 132;
  const dy = local.y - 141;
  const distance = Math.hypot(dx, dy);
  if (distance < 45 || distance > 145) {
    selectMode("neutral");
    return;
  }
  const mode = Math.abs(dx) > Math.abs(dy)
    ? (dx > 0 ? "strength" : "armor")
    : (dy > 0 ? "cloak" : "speed");
  selectMode(mode);
});
svg.addEventListener("pointerleave", () => selectMode("neutral"));
document.querySelectorAll("[data-background]").forEach(button => {
  button.addEventListener("click", () => {
    background = button.dataset.background;
    document.getElementById("context-background").setAttribute("href",
      background === "light" ? "assets/minecraft-context.png" : "assets/dark-context.jpg");
    document.querySelectorAll("[data-background]").forEach(other => {
      other.setAttribute("aria-pressed", String(other === button));
    });
    selectMode(svg.dataset.selected);
  });
});
document.getElementById("pause-ring").addEventListener("click", event => {
  if (svg.animationsPaused()) {
    svg.unpauseAnimations();
  } else {
    svg.pauseAnimations();
  }
  event.currentTarget.setAttribute("aria-pressed", String(svg.animationsPaused()));
  event.currentTarget.textContent = svg.animationsPaused() ? "Reprendre" : "Pause";
});
document.getElementById("restart-ring").addEventListener("click", () => svg.setCurrentTime(0));
selectMode("neutral");
