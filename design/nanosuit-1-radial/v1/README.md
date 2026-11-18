# Maquette du menu radial Nanosuit 1

Travail graphique préparé sur la branche `feature/nanosuit-1-classic`, HEAD initial `c887fd7`. Aucun fichier du mod n'est modifié.

## Sources et limites de fidélité

Le manuel fourni `Crysis_Manual_Englisch.pdf`, page PDF 15 / page imprimée 14, montre explicitement le menu ouvert. Son schéma de 263 × 271 pixels est conservé dans `assets/reference-menu.png`. Il confirme les panneaux polygonaux, l'anneau central interrompu, les quatre pictogrammes, leurs positions et une sélection ambrée sur fond vert militaire. Il comporte aussi un accès aux armes.

Le document utilisateur `Doc crysis 1.pdf` contient des captures du HUD et des bras en Armure/Force, qui donnent du contexte à l'interface. Les ressources du dépôt examinées comprennent les textures du HUD existant. Aucun atlas du menu radial original n'a été trouvé dans ces ressources ciblées.

Les formes et pictogrammes sont redessinés en vecteurs depuis le schéma. Leur faible résolution empêche une reproduction au pixel près. La police du menu original, son échelle exacte à l'écran et ses animations de survol ne sont pas établies par ces images. Le schéma n'a aucun libellé typographique sur les panneaux.

## Choix de la maquette

| Élément | Référence | Adaptation proposée |
|---|---|---|
| Positions | Vitesse en haut, Force à droite, Camouflage en bas à droite, Armure à gauche | Positions conservées |
| Accès aux armes | Cinquième panneau en bas à gauche | Emplacement laissé libre pour garder les quatre modes demandés |
| Sélection | Icône ambrée dans le schéma | Ambre sur l'icône, le contour et le chevron du secteur survolé |
| Mode courant | Non établi séparément dans le schéma | Armure avec liseré pâle, conservé tant que le choix n'est pas validé |
| Transparence | Schéma imprimé sur fond blanc | Panneaux noirs à 86 %, anneau translucide, légère atténuation du fond |
| Nom du mode | Absent du schéma | Un seul libellé sous le radial pendant le survol |
| Police | Non identifiable | Barlow Condensed si disponible, repli local Bahnschrift puis Arial Narrow |
| Taille | Non déterminée en situation réelle | Environ 330 × 310 pixels sur une composition 1280 × 720 |

L'ambre indique la sélection dans le menu, comme sur la référence. Les quatre noms internes sont ARMOR, STRENGTH, SPEED et CLOAK. Les états Force/Vitesse représentent le choix en attente, validé au relâchement. Le menu neutre conserve Armure comme mode courant.

Le fond contextuel provient de la capture réelle `n1-armor-fps-1.png` du banc graphique. Les trois PNG livrés sont des rendus Chrome des SVG de conception. Ils représentent une composition graphique. Le HUD visible fait partie de la capture de fond et n'a pas été modifié dans le mod. Son réticule sert de repère central dans cette maquette.

## Fichiers éditables

- `index.html` — galerie locale avec trois états consultables.
- `neutral.svg`, `strength.svg`, `speed.svg` — compositions complètes, chaque SVG contient le fond et les tracés éditables.
- `radial-neutral.svg`, `radial-strength.svg`, `radial-speed.svg` — éléments graphiques seuls sur fond transparent.
- `neutral.png`, `strength.png`, `speed.png` — aperçus 1280 × 720 contrôlés visuellement.

Les groupes `armor`, `strength`, `speed`, `cloak`, les symboles de pictogrammes et le groupe `radial-menu` restent éditables dans un éditeur SVG. Ouvrir `index.html` pour comparer les états, ou ouvrir un SVG directement.

## Intégration NeoForge ultérieure, après validation

1. Ajouter une touche configurable non attribuée par défaut. Aucune affectation au clic de molette.
2. À l'ouverture, mémoriser le mode courant et afficher les ressources vectorielles exportées en textures adaptées aux échelles GUI. Garder les tracés SVG comme sources graphiques.
3. Séparer la zone morte centrale, les quatre secteurs et la zone libre. La zone libre ne sélectionne aucun mode. La géométrie proposée conserve le trou correspondant à l'ancien panneau des armes.
4. Afficher le secteur survolé avec les mêmes ressources graphiques. Un petit seuil de déplacement évitera les sélections involontaires au centre.
5. Au relâchement, envoyer uniquement le mode effectivement sélectionné par le chemin serveur de demande existant. Sans sélection, conserver le mode précédent. Garder les validations de combinaison, d'énergie et de cooldown côté serveur.
6. Vérifier ensuite les échelles GUI, les mouvements de souris, le maintien/relâchement d'une touche réassignée et le comportement en cas de fermeture forcée. La Nanosuit 2 conserve ses commandes existantes.

Ces étapes décrivent une proposition technique. Aucun écran Minecraft, raccourci, paquet, shader ou comportement de jeu n'a été implémenté pendant cette mission.
