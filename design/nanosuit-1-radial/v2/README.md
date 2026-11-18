# Menu radial Nanosuit 1 — deuxième itération

Travail graphique sur la branche feature/nanosuit-1-classic, HEAD c887fd7. Les fichiers du mod ne sont pas modifiés. La première maquette est conservée intégralement dans v1/.

## Référence et méthode

Source principale fournie par l'utilisateur, Crysis_Manual_Englisch.pdf, page PDF 15 / page imprimée 14. Son schéma de 263 × 271 pixels est conservé dans assets/reference-menu.png.

Les quatre logos ont été réexaminés séparément à partir de recadrages du schéma. La première maquette avait inventé des détails, notamment le coureur. La V2 relève les contours des zones colorées et leurs découpes sombres directement dans ces recadrages, puis les transforme en tracés SVG. Les segments des icônes sont simplifiés à moins d'un pixel de la source pour réduire les marches d'escalier. La géométrie des panneaux de V1 est conservée, conformément aux éléments déjà validés.

Les SVG contiennent des tracés vectoriels éditables, avec les vides des motifs conservés par une règle de remplissage pair-impair. Les couleurs dominantes du schéma sont le vert #44633E et l'ambre #D78B10.

## Comparaison par mode

| Mode | Erreur de la première maquette | Correction V2 | Approximation restante |
|---|---|---|---|
| ARMOR | Bouclier arrondi et petit réseau d'hexagones réguliers détaché de sa silhouette | Contour du bouclier, raccord et alvéoles du bord inférieur gauche relevés dans la source | Petites cellules de quelques pixels et leurs nuances |
| STRENGTH | Doigts flottants et silhouette de main trop ouverte, pouce mal défini, poignet générique | Reprise du poing stylisé de la source, de ses blocs de doigts, du pouce recourbé refermé et des cellules du poignet | Le petit schéma ne permet pas de détailler davantage les replis du poing, aucun détail anatomique supplémentaire n'est ajouté |
| SPEED | Personnage en train de courir inventé à l'extrémité des chevrons | Contour des chevrons et extrémité composée de cellules géométriques, sans coureur | Chanfreins et raccords des cellules limités par leur taille de quelques pixels |
| CLOAK | Silhouette trop étroite et maillage régulier générique | Contour de la tête, épaules et pointe inférieure, découpe et dimensions variables des cellules reprises dans la source | Éclats de couleur, nuances ambrées et anticrénelage de l'image originale |

La V2 conserve les positions des quatre modes, l'anneau central et le vide de l'ancien panneau des armes. Elle retire le libellé ajouté sous le radial, le marquage distinct du mode courant, la trame décorative et la forte bordure ambrée de V1. La sélection reste indiquée par le pictogramme ambre. Le chevron de direction reprend le langage du petit indicateur de la référence.

## Fichiers livrés

- [Galerie V2](index.html), avec boutons Neutre / Force / Vitesse.
- [Comparaison des quatre icônes](comparison.png), [source SVG](comparison.svg).
- [Comparaison des menus complets](menu-comparison.png), [source SVG](menu-comparison.svg).
- [Première galerie conservée](v1/index.html).
- [Neutre](neutral.png), [Force sélectionnée](strength.png), [Vitesse sélectionnée](speed.png).
- Compositions éditables neutral.svg, strength.svg, speed.svg.
- Radials transparents radial-neutral.svg, radial-strength.svg, radial-speed.svg.
- Logos vectoriels isolés icon-armor.svg, icon-strength.svg, icon-speed.svg, icon-cloak.svg.
- Recadrages de la source dans assets/reference-*.png.

Les PNG sont des rendus réels des SVG par Chrome. Le fond contextuel reste la même capture Minecraft que pour V1, atténuée pour composer la maquette. Le HUD et le réticule visibles appartiennent à cette capture. Les planches comparatives montrent la référence raster agrandie, puis les deux versions vectorielles.

## Limites

Le schéma est petit. Les contours principaux et le motif hexagonal sont exploitables, mais les détails sous-pixel, les nuances et l'anticrénelage ne peuvent pas être reproduits exactement. Les icônes V2 utilisent une couleur unie. La police originale et les animations du menu ne sont pas documentées par cette référence statique.

La transparence en situation réelle et la taille du menu ne sont pas mesurables dans ce schéma imprimé. Le fond et l'échelle de la composition restent donc des choix de maquette. L'indicateur directionnel est repositionné pour les sélections Force et Vitesse. Le manuel le montre seulement dans une autre direction.

## Fonctionnement prévu, toujours en attente de validation

Maintenir une touche configurable, non attribuée par défaut. Déplacer la souris vers un secteur, puis relâcher pour valider. Relâcher au centre ou dans l'ancien emplacement des armes conserve le mode précédent. Aucun usage du clic de molette.

Après validation du design, l'intégration pourra réutiliser les images exportées depuis ces SVG, une zone morte centrale et le chemin serveur de demande de mode existant. Cette itération ne contient aucune intégration Minecraft.
