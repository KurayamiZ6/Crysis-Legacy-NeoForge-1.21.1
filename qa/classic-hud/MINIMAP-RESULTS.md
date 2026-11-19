# Minicarte N1 — 19 novembre 2026

Base : 944201f. Carte fonctionnelle sur le cache de tuiles déjà présent : mêmes budgets de prélèvement et de mise à jour, uniquement chunks déjà chargés pour N1. Le rendu n'interroge aucun chunk. Texture de terrain monochrome dérivée des vraies couleurs, restaurée pour N2. Aucun moteur cartographique supplémentaire.

Le cadre, le masque polygonal, le cône et la flèche proviennent de la géométrie V3. La carte tourne, le joueur regarde vers le haut ; la boussole suit le yaw réel. Portée stable en blocs malgré l'échelle GUI. Le thème et `hud.classic.scale` s'appliquent à l'habillage. Le terrain n'est pas une image de référence.

## Objectif explicite

`/crysiswaypoint follow <x> <z>` suit un point dans la dimension courante ; `/crysiswaypoint clear` l'efface. Point de session seulement, supprimé à la reconnexion, caché hors de sa dimension et hors du polygone. Aucun point par défaut, aucune entité transformée en objectif. Import des waypoints tiers non implémenté faute d'accès validé à une sélection explicite.

## Vérification graphique

Deux exécutions isolées sur RTX 3070, fenêtre 1920×1080, Sodium 0.8.13 : `build/mode-overlay-qa/classic-map-base/` et `classic-map-diorama/`. Source reproductible : `ClassicMapVisualQa.java`. Captures framebuffer, données de position et objectif inscrites dans `console.log`.

Cas : sans objectif, objectif choisi, déplacement de 20 blocs, quatre orientations, objectif effacé, GUI 4 puis 3, F1, combinaison incomplète, N2. Les captures montrent du terrain distinct, sa rotation, le point seulement lorsqu'il est suivi, et l'ancien panneau N2. Les interactions de dimension/reconnexion sont protégées par le contexte de la source de waypoint ; validation graphique additionnelle à réaliser lors du test des menaces.

Compilation : `gradlew build --offline`, réussie. Pas de dépendance obligatoire à une carte tierce.

## Limites

Carte de surface, précision et zones non échantillonnées héritées du cache existant ; pas de carte de grotte ou de chargement forcé pour remplir les bords. Le point hors cadre est omis. Pas encore d'ennemis ni de Threat Indicator à cette étape. La typo de boussole utilise l'atlas étroit existant ; elle reste une approximation de la police propriétaire.
