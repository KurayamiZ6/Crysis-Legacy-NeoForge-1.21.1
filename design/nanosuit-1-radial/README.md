# Menu radial Nanosuit 1 — V3 animée

Design préparé sur feature/nanosuit-1-classic, à partir de c887fd7. La V2 est conservée dans v2/ et la V1 dans v1/. Aucun fichier du mod n'a été modifié.

## Ouvrir et utiliser

Ouvrir [index.html](index.html) dans un navigateur. L'anneau tourne dès l'ouverture.

- Déplacer la souris vers l'un des quatre panneaux ou utiliser les boutons ARMOR, STRENGTH, SPEED et CLOAK.
- Revenir au centre ou choisir Neutre pour retirer le survol.
- Alterner Clair et Sombre pour vérifier la transparence.
- Pause et Repartir à 0 servent uniquement à inspecter l'animation dans cette maquette.

Cette démonstration ne capture aucune touche Minecraft et n'envoie aucun paquet. La future touche d'ouverture reste non attribuée par défaut. Aucun clic de molette n'est utilisé.

## Références prioritaires

Les trois captures Crysis Remastered fournies pendant la mission sont conservées dans assets/

1. remastered-speed.png — disposition à quatre modes, anneau et icône Speed courante ambrée.
2. remastered-strength.png — référence du panneau actif complet, bordure lumineuse et hexagones sombres derrière le poing.
3. remastered-armor.png — décor plus clair visible à travers les panneaux et distinction entre icône courante ambrée et survol complet.

Le manuel précédemment fourni reste une référence historique du dessin. Ses cinq panneaux ne sont pas repris dans le prototype V3. Avec l'accord explicite de l'utilisateur, la disposition V3 suit les nouvelles captures en croix, Camouflage en bas. Les quatre secteurs couvrent les quatre directions. Il n'existe aucun emplacement d'armes ni cinquième secteur.

Les quatre symboles SVG validés en V2 sont réutilisés sans modifier leurs tracés. Les quatre fichiers icon-*.svg restent identiques à ceux archivés dans v2/. Les contours polygonaux sont remis à plat et alignés sur les quatre axes d'après les nouvelles captures.

## Paramètres graphiques

Ces valeurs sont des estimations visuelles choisies en comparant les captures. Elles ne sont pas extraites du moteur Crysis.

| Élément | Valeur |
|---|---|
| Fond vert normal | #12381C, opacité 0,30, soit 70 % de transparence |
| Contour normal | #93AD8A, opacité 0,78, épaisseur 1,8 unité SVG |
| Icône normale | #769675, opacité 1 |
| Fond actif ambré | #B67D13, opacité 0,48, soit 52 % de transparence |
| Contour actif | #FFDA50, opacité 1, épaisseur 2,1 unités SVG |
| Icône active | #FFC43C, opacité 1 |
| Trame hexagonale active | Trait #332409, épaisseur 2,1 unités, opacité de la couche 0,62 |
| Taille des cellules du fond | Rayon 4,8 unités, motif répété de 8,314 × 14,4 unités |
| Lueur du bord actif | #FFC943, opacité 0,28, flou 0,65 unité |
| Anneau | #77976D, opacité 0,76, rayon 34, épaisseur 6,4 unités |
| Chevron extérieur | #F1F0DA, épaisseur 2,2 unités |

Les motifs du panneau actif sont découpés par son contour et peints avant l'icône. Ils sont distincts des hexagones déjà présents dans le logo. Les trois autres panneaux gardent leur fond et leur icône verts. Aucun voile coloré n'est appliqué à l'écran entier.

La composition utilise un facteur d'échelle 1,9. Son centre est (640, 360) sur un plan de 1280 × 720. Le fond clair est la capture du banc Minecraft déjà utilisée en V2. Le fond sombre est une capture Crysis extraite du document utilisateur antérieur. Le HUD visible appartient à ces images de contexte.

## Animation et contrôles

L'animation SVG native animateTransform fait tourner uniquement le groupe ring-rotation autour du pivot local de l'anneau. Les panneaux, les icônes et le chevron ne font pas partie de ce groupe.

- Durée 10 000 ms par révolution.
- Vitesse 0,1 tour/s, soit 36 degrés/s.
- Progression linéaire, répétition indéfinie.
- Sens horaire provisoire, impossible à déterminer à partir des trois images fixes.
- Pivot local (132, 141), traduit au centre du menu.
- Les changements de sélection et d'arrière-plan ne recréent pas l'animation.

Pour une intégration future, la même phase peut être calculée avec un temps monotone écoulé et la relation angle = 360 × (temps_ms modulo 10000) / 10000. Cette formule ne dépend pas du nombre d'images affichées.

Le prototype a été vérifié dans Chrome avec les quatre sélections à la souris et le retour au centre. Les captures de rotation ont été prises à environ 0 / 2,5 / 5 / 7,5 secondes d'une animation réellement en cours. Les angles observés sont proches de 0 / 90 / 180 / 270 degrés. Le centre mesuré reste (640,000025 ; 360,000012), sans déplacement entre les quatre mesures. Les boîtes des panneaux restent identiques.

La planche [rotation-quarters.png](rotation-quarters.png) rassemble des recadrages de ces quatre captures du navigateur. Les angles y sont ceux de la matrice SVG observée, avec la précision d'une image d'affichage.

## Fichiers

- index.html — galerie générée, directement ouvrable.
- prototype-template.html, prototype.css, prototype.js — sources de la galerie et interactions de démonstration.
- generate_v3.py — génération des SVG et de la galerie à partir des symboles V2 conservés, avec Python standard.
- radial-neutral.svg et radial-{armor,strength,speed,cloak}.svg — sources SVG transparentes réellement animées.
- neutral.svg et {armor,strength,speed,cloak}.svg — compositions SVG autonomes sur le fond clair.
- neutral.png, strength.png, speed.png, armor.png, cloak.png — captures réelles du prototype.
- Les variantes *-dark.png présentent les mêmes états sur le fond sombre.
- [selection-comparison.png](selection-comparison.png) — référence Force fournie et rendu V3.
- assets/remastered-*.png — nouvelles références.
- v1/ et v2/ — versions précédentes conservées, avec leurs sources et comparaisons.

Les profils de navigateur, scripts de contrôle temporaires et fichiers intermédiaires restent hors du dossier de design et ne sont pas inclus dans Git.

## Comparaison et limites

Les fonds V2 presque noirs sont remplacés par un vert réellement translucide. La sélection V3 ajoute le contour ambré, le fond chaud et le motif hexagonal sombre visible dans la capture Force. L'anneau est isolé et animé, les quatre positions sont alignées sur le menu Remastered à quatre modes.

La trame active est reconstruite en nid d'abeille régulier. Ses nuances, sa texture et sa densité restent approximatives. Les opacités exactes du jeu ne peuvent pas être déduites de captures uniques sans connaître le décor non recouvert correspondant. Le sens de rotation reste à confirmer.

Deux références montrent une icône courante ambrée avec un panneau encore vert. Ce prototype présente l'état neutre entièrement vert et le survol complet ambré demandés dans la mission. Il ne simule pas séparément un mode courant persistant.

Les logos conservent la définition vectorielle validée en V2, issue du petit schéma du manuel. Ils n'ont pas été redessinés depuis les nouvelles images plus grandes. Leur précision et leurs petits chanfreins restent donc ceux de la V2.

L'intégration NeoForge attend une mission séparée et la validation de cette V3.
