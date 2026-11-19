> Spécification corrigée : [SPEC-CORRECTIONS.md](SPEC-CORRECTIONS.md). Le prototype réversible des jumelles est historique et ne doit pas servir de machine à états pour l’intégration.

# Étude HUD Nanosuit 1 — V3

Conception hors jeu, données simulées. Aucune intégration Minecraft.

- [Galerie actuelle](index.html) : dix états du HUD.
- [Jumelles animées](binocular-animation.html) : ouverture/fermeture réversible et télémétrie simulée réglable.
- [Correspondance Minecraft](BINOCULARS.md) : données existantes et adaptation proposée.
- [Rapport V3](ITERATION-3.md) : formes, références et trois canaux de menace.
- [Comparaison](comparison-v3.png) et [vue d’ensemble](overview-v3.png).
- [Rapport initial](RAPPORT.md) : inventaire technique historique ; lire les mises à jour V3.
- `references/user-2026-11-19/` : seize captures utilisateur indexées par deux manifestes ; originaux de récupération dans `build/hud-classic-reference/user-originals-2026-11-19/`.
- `v2/` : exports historiques de la précédente proposition.

Sources éditables : dix fichiers `hud-*.svg`, `generate-v3.py`, `make-binocular-demo.py` et HTML. Les générateurs historiques V2 sont conservés ; utiliser les commandes V3 ci-dessous.

Régénérer depuis ce dossier avec Python/Pillow et Node/Playwright/Chrome :

```text
python generate-v3.py
python make-binocular-demo.py
node render.cjs <module-playwright> <chrome.exe>
python make-comparisons-v3.py
node check-binocular-demo.cjs <module-playwright> <chrome.exe>
```

Les planches comparatives doivent exister avant le contrôle des liens de galerie. Les résultats actuels figurent dans `preview-check.json` et `binocular-preview-check.json`. Ils valident les maquettes dans un navigateur, pas le jeu.
