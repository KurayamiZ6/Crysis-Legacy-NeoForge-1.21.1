> Mise à jour du 19 novembre 2026 : voir [l’itération 3](ITERATION-3.md) et [la galerie](index.html), fondées sur les douze nouvelles captures utilisateur. Les observations ci-dessous sont historiques.

# HUD Nanosuit 1 — référence et conception

18 novembre 2026 · branche `feature/nanosuit-1-classic` · référence `6f4dc9a`.

**Mise à jour graphique :** voir `ITERATION-2.md` et `iteration-comparison.png` pour les alignements corrigés et la jauge de menace élargie. La nouvelle tentative de capture du Remastered échoue dans l’outil même en fenêtré ; elle ne remplace pas les observations historiques ci-dessous.

**Design hors jeu, sans changement de production.** Quatre maquettes principales et une étude provisoire des jumelles sont livrées en SVG éditables et PNG. Les données affichées sont simulées. La Nanosuit 2, les mécaniques, le menu radial, les sons et les shaders existants restent inchangés. Aucun build ni lancement Minecraft nécessaire à cette étude ; les captures Minecraft déjà disponibles ont été réutilisées.

## Références et niveau de preuve

| Source | Apport | Limite |
|---|---|---|
| Capture utilisateur `references/user-remastered-full.png`, 2559 × 1596 | Géométrie réelle du HUD Remastered : barres, badge, munitions sans arme, carte et menace au repos | Menu radial ouvert ; une seule orientation, un seul instant. Le badge Force reste distinct du survol Vitesse du menu. |
| `C:/Users/nicol/Desktop/Projet/Crysis Legacy/Doc crysis 1.pdf`, pages 1 et 5–6 | Rouge à 15 %, annonce à 25 %, batterie indépendante en quatre éléments, filtre vert, autonomie/recharge documentées, zoom x2/x4 | Observations de l’utilisateur ; pas des mesures refaites pendant cette session. Les tuiles internes d’images du PDF ne doivent pas être prises pour des captures complètes. |
| `C:/Users/nicol/Downloads/Crysis_Manual_Englisch.pdf`, pages PDF 11–13 et 15, imprimées 10–12 et 14 | Inventaire du HUD, distinction risque de détection / alerte ennemie, légende radar, jumelles et batterie | Manuel du jeu original, pas une preuve que tous les détails du Remastered sont identiques. |
| `references/minecraft-current.png` | Capture réelle réutilisée de `qa/radial-menu/evidence-feedback/activation-armor-late.png` | Campagne antérieure au HEAD actuel, chemin HUD non modifié par le dernier correctif de visibilité du casque. Ce n’est pas une nouvelle campagne de compatibilité. |
| `references/remastered-classic-settings.png` et `remastered-video-settings.png` | Captures directes prises pendant cette mission | Réglages seulement ; pas une capture du HUD en partie. |

### Observation directe du Remastered

Lancement de `D:/Games/CrysisRemastered/Bin64/CrysisRemastered.exe`, puis validation initiale par l’utilisateur car la touche Entrée automatique n’avait pas fait progresser l’écran d’accueil. Navigation des paramètres par contrôle de fenêtre : **mode nanocombinaison classique coché**, difficulté **Normal**, **2560 × 1600**, **sans bordures**, Display1, 60 Hz affichés ; thème **ATH militaire vert**. Aucun réglage modifié. Aucune échelle HUD distincte visible dans les pages consultées. Les captures de fenêtre sont renvoyées en 1707 × 1067 : elles ne représentent pas des pixels de rendu natifs 1:1.

Le processus s’est terminé pendant le chargement de **Contact**, avant le HUD. Dernières lignes utiles conservées dans `references/remastered-load-tail.txt` : chargement de `default_01_quicksave.CSF`, puis génération d’une table de caractères. **Cause non démontrée** ; aucun diagnostic AMD ou autre n’est déduit de cette seule fermeture. Pas de relances répétées ni de modification de pilote.

La copie intégrale initiale du profil et des sauvegardes est conservée dans `build/hud-classic-reference/personal-profile-before/`. Trois copies de test `default_hud_reference.*` ont été créées, mais l’interface Reprendre a chargé `default_01_quicksave.CSF` ; elles ne sont donc pas annoncées comme utilisées. Aucune progression, sauvegarde manuelle ou déplacement n’a été effectué. Les trois copies, restées identiques, ont été retirées. Le seul fichier personnel original modifié au lancement, `Profiles/default/profile.xml`, a été restauré après fermeture. Les empreintes finales des **18 fichiers initiaux** sont comparées dans `references/preservation-check.json`.

| Comportement original | État de vérification pendant cette mission |
|---|---|
| Mode classique / résolution / thème | **OBSERVÉ**, captures directes |
| Forme et placement du HUD au repos | **RÉFÉRENCE STATIQUE** fournie par l’utilisateur |
| Barre rouge à 15 %, batterie jaune hors activation pendant recharge | **DOCUMENTÉ PDF utilisateur**, pas réobservé |
| Recherche / détection / combat, montée et décroissance de l’alerte | **NON OBSERVÉ** ; distinction des deux jauges documentée par le manuel |
| Rotation de carte, compassage en mouvement | **NON OBSERVÉ EN MOUVEMENT** ; cap visible statiquement |
| Animations de baisse/recharge, clignotement, délais/fondu du HUD | **NON MESURÉ** ; aucune durée inventée |
| NV 60 s / recharge 15 s | **DOCUMENTÉ**, valeurs logiques existantes lues dans le code ; pas chronométré dans le Remastered |
| Jumelles, incréments de zoom et marquage en partie | **NON OBSERVÉ** directement ; PDF et implémentation existante consultés |

## Mesures et fidélité

Mesures manuelles sur la capture utilisateur 2559 × 1596, tolérance environ ±4 px aux contours et davantage pour les fonds translucides. Les boîtes englobantes sont des **repères de conception**, pas des mesures de textures sources. Le HUD est incliné : une boîte rectangulaire ne décrit pas à elle seule sa perspective.

| Élément | Boîte source x / y / largeur / hauteur, px environ | x/W ; y/H ; largeur/W ; hauteur/H | Proposition 1920 × 1080 |
|---|---|---|---|
| Carte avec bandeau de cap | 221 / 1196 / 344 / 271 | 8,64 % ; 74,94 % ; 13,44 % ; 16,98 % | 166 / 809 / 232 / 185, cadre recalé en itération 2 |
| Hexagone de risque | 171 / 1385 / 109 / 133 | 6,68 % ; 86,78 % ; 4,26 % ; 8,33 % | 130 / 940 / 71 / 79, contour et triangle renforcés |
| Groupe santé/énergie hors badge | 1970 / 1375 / 309 / 104 | 76,98 % ; 86,15 % ; 12,08 % ; 6,52 % | 1477 / 933 / 231 / 66 |
| Badge de mode | 2280 / 1395 / 115 / 122 | 89,10 % ; 87,41 % ; 4,49 % ; 7,64 % | 1713 / 945 / 72 / 87 |
| Bloc de munitions | 2103 / 1279 / 229 / 120 | 82,18 % ; 80,14 % ; 8,95 % ; 7,52 % | 1570 / 852 / 152 / 83 |
| Batterie nocturne | Autre image, PDF utilisateur p. 5 | Haut droit, quatre sections jaunes ; pas mesurée dans la même résolution | Enveloppe 1585 / 57 / 202 / 27, **placement provisoire** |
| Jumelles | Pas de capture originale complète exploitable | **Non mesuré** | Étude secondaire explicitement provisoire |

La proposition adapte les ancrages aux coins et conserve approximativement l’échelle verticale des composants, plutôt que d’étirer tout le HUD 16:10 vers 16:9. Les proportions ne sont donc pas toutes strictement identiques en pourcentage de largeur. Pour l’intégration : un seul facteur d’échelle issu de la hauteur, marges sûres ajustables et conversion explicite en coordonnées GUI Minecraft. La politique exacte ultrawide/GUI scale reste à valider.

Repris : bleu de l’énergie, vert de la santé, contour militaire vert atténué, panneaux sombres translucides, angles et découpes polygonales, badge hexagonal, symboles existants du menu déjà validé. Le badge montre le **mode réellement actif**, pas le secteur survolé du menu radial. Les ressources du menu ne sont pas modifiées.

Approximations restantes : police locale **Bahnschrift** à la place de la fonte du jeu, intensité des lueurs/transparences, perspective exacte, batterie en haut à droite et jumelles. Le terrain Minecraft existant est présenté ici désaturé pour l’étude ; **aucun moteur de carte satellite n’a été créé**. La grille basse du panneau de munitions est conservée visuellement mais aucune information inconnue n’y est inventée.

Les planches comparatives montrent la différence entre : référence Remastered, énergie segmentée actuelle avec nom de mode, proposition à deux barres et badge. Le fond des maquettes est un **recadrage et réencadrement** d’une capture QA sans ses anciens widgets ; ce n’est pas une capture F1 ni un test en jeu du futur HUD. La hotbar et les indicateurs de survie sont volontairement traités comme une décision distincte, pas effacés du mod.

## Inventaire ciblé : existant et manquant

Les fichiers ci-dessous sont relatifs au dossier de dépôt `C:/Users/nicol/Desktop/Projet/Crysis Legacy/Crysis Legacy NeoForge 1.21.1/`. Les chemins abrégés `nanosuit/`, `client/`, `nanovision/` et `nanovisor/` désignent les sous-dossiers réels de `src/main/java/io/github/painworks/crysismod/`.

| Composant | Aspect original et emplacement | Données / état actuel du mod | Travail nécessaire et limites |
|---|---|---|---|
| Santé / énergie | Bas droit, deux barres pleines inclinées ; bleu au-dessus, vert au-dessous ; valeurs numériques | `nanosuit/client/NanosuitHudOverlay.java:178` : énergie 0–100 via `INanosuitEnergy`, 20 segments hérités. Santé encore affichée par Minecraft ; pas de barre classique dans ce rendu. | Nouveau dessin N1 seulement ; santé via `getHealth()/getMaxHealth()` existants, énergie déjà synchronisée. Décider absorption, vies maximales modifiées et indicateurs vanilla ; aucun besoin de nouvelle mécanique de santé. |
| Mode actif | Hexagone à droite des barres ; pictogramme militaire | `NanosuitHudOverlay.java:237`/`:244` : N1 affiche un libellé ; `NanosuitMode` et profil déjà disponibles. Icônes validées dans `textures/gui/nanosuit1_radial/icons.svg`. | Badge N1 qui réutilise les quatre symboles, état NONE et équipement incomplet définis ; pas de logique de sélection ni de paquet supplémentaires. |
| Énergie critique | Barre rouge à 15 % d’après PDF ; annonce à 25 % | `NanosuitHudOverlay.java:218` et `:734` : teinte rouge N1 déjà présente. `nanosuit/handler/NanosuitPlayerHandler.java:602` : signaux sonores 25/15, réarmement 27/17. | Réutiliser énergie et seuils existants ; aucun nouveau son. Ne pas inventer une bannière à 25 % ni une fréquence de clignotement sans observation. |
| Carte / navigation | Bas gauche ; cadre à coins coupés, bande de cap au-dessus, joueur centré et marqueurs | `client/minimap/NanoMinimapManager.java:137`/`:347`, `NanoMinimapVisibility.java`, `render/NanoMinimapRenderer.java:37`, `marker/NanoMinimapMarkerLayer.java:58`. Terrain, rotation, joueur, cibles, marquages, scans espacés et cache déjà disponibles. | Habillage N1, compas à partir du yaw, palette monochrome et masque polygonal à étudier autour du rendu existant. Pas de dépendance à Xaero/JourneyMap. Aucune mission/objectif de campagne Minecraft n’existe à afficher. |
| Menaces | Hexagone de détection accolé en bas gauche et jauge d’alerte sur le bord gauche de la carte | `client/minimap/render/ThreatGauge.java:29` : 8 segments sur le côté droit si place, 4 niveaux, expiration 60 ticks. `nanosuit/threat/NanosuitThreatTracker.java:42` : échantillon 10 ticks, rayon 32, mémoire de recherche 60, renvoi 40. | Relocalisation graphique possible avec données actuelles. **Deux variables indépendantes absentes** : ne pas présenter deux jauges identiques comme fidèles. De vrais états de recherche des mods ne sont pas universellement accessibles. |
| Vision nocturne | Filtre vert et batterie jaune indépendante en haut droit, visible pendant utilisation/recharge | `nanovision/ClassicNightVisionBattery.java` : 1200 ticks, recharge +4/tick ; `nanovision/server/ClassicNightVisionServerState.java` : serveur, synchronisation tous les 10 ticks et transitions. `client/nanovision/ClassicNightVisionClient.java:112` : jauge rectangle 62 × 5 GUI, masquée pleine et désactivée. | Adapter le dessin de la jauge N1 et éventuellement lisser uniquement la présentation entre paquets. Le modèle est **déjà serveur**, contrairement à de vieux rapports sur une batterie client. Ne pas recréer la batterie ni toucher au filtre. |
| Munitions / TACZ | Compteurs et silhouettes de cartouches dans un bloc au-dessus des barres ; état dépendant de l’arme | `nanosuit/client/NanosuitTaczHudOverlay.java:78`, `:255` et `:305` : pont optionnel par réflexion, chargeur courant, réserve, icône ; suppression actuelle de tous les overlays d’espace `tacz` quand activée. | Nouvelle présentation N1 sur ce pont, état vide sans TACZ ; différencier nombre de cartouches, chargeurs, chambre et munitions créatives. **Ne pas convertir une réserve en chargeurs par simple division**. Grenades, cadence/mode de tir non fournis par ce record. |
| Jumelles / zoom | Relevé de distance et zoom x2/x4 documentés ; géométrie précise insuffisamment référencée | `nanovisor/client/NanoVisorHudOverlay.java:111`/`:287` : texture, vignette, réticule, distance bloc/entité, nom et énergie. `NanoVisorZoomHandler.java:33`/`:80` : FOV et progression de zoom. `NanoVisorClientMarkedTargets.java` + paquets : marquages déjà présents. | Habillage N1 séparé, réutiliser raycast/zoom/marquages ; conversion FOV vers « x2/x4 » à définir précisément. Pas de reproduction certifiée des surbrillances IA originales à partir des seuls contours existants. |

## Découpage proposé, sans refactorisation générale

1. **Valider ces SVG et compléter la référence manquante.** Une session Remastered fonctionnelle ou des captures utilisateur : NV activée puis désactivée en recharge, énergie basse, jumelles, alerte ennemie. Chronométrer seulement les animations nécessaires. Ne pas retarder les barres et le badge pour des détails non bloquants de la carte.
2. **Routage N1 de présentation uniquement.** Dans `NanosuitHudMasterCompositor`, résoudre une fois le profil via `NanosuitArmorUtil.getEquippedProfile`. Diriger N1 vers un futur `ClassicNanosuitHudOverlay`, laisser N2 et LEGACY_CURRENT dans leurs chemins actuels. Même garde pour les abonnements de secours lorsque le compositeur est désactivé, afin d’éviter le double HUD. Le compositeur existant est conservé.
3. **Lot santé/énergie/mode.** Nouveau rendu N1, état en lecture seule issu des données existantes ; nouveau petit fichier de mise en page N1. Réutiliser les fonctions d’inertie si compatibles avec les nouveaux ancrages. Réserver des options N1 distinctes des réglages hérités pour préserver le placement N2.
4. **Lot batterie et munitions.** Habillage de batterie dans `ClassicNightVisionClient` sans toucher au serveur, puis fournisseur TACZ optionnel réutilisé depuis `NanosuitTaczHudOverlay`. L’adaptateur ne dessine pas un second HUD ; absence d’arme/TACZ = bloc absent, pas de faux compteur 000. Restituer le HUD TACZ si le pont échoue.
5. **Lot navigation.** Habillage classique autour de `NanoMinimapRenderer` et de son `NanoMinimapViewport` ; réutiliser manager, sampler, caches, mapper et couches de marqueurs. Séparer seulement le cadre/compas/palette du terrain. Tester d’abord le masque sans shaders, puis avec Iris ; ne pas changer globalement le stencil/FBO pour ce cadre.
6. **Lot menaces, soumis à décision.** D’abord déplacer la représentation des quatre niveaux déjà transmis. Si fidélité complète souhaitée, ajouter ultérieurement une observation serveur distincte du risque de détection et de l’alerte, sans modifier l’IA. Cela nécessite une spécification, des données et probablement un nouveau paquet/version de protocole ; aucun changement v6 dans cette mission.
7. **Lot jumelles.** Présentation N1 via le routage de `NanoVisorHudOverlay`, moteur de zoom et marquages existants. Conserver exactement l’interface N2. Référence visuelle complète requise avant géométrie finale.

**Composants partagés à conserver** : énergie et ses paquets, modes et profil équipé, santé synchronisée vanilla, batterie serveur et paquets NV, pont TACZ optionnel, état NanoVisor, cache/texture/mapper/marqueurs de la carte et sources de menace. Les classes futures proposées sont des renderers ou des données de présentation ; aucun second moteur de pouvoirs ou de cartographie.

Fichiers réellement concernés par ces futurs lots : `nanosuit/client/NanosuitHudMasterCompositor.java`, `NanosuitHudOverlay.java`, `NanosuitTaczHudOverlay.java`, `NanosuitHudInertia.java` si nécessaire pour les ancrages, `nanosuit/config/NanosuitClientConfig.java`, `client/nanovision/ClassicNightVisionClient.java`, `client/minimap/NanoMinimapHudOverlay.java`, `NanoMinimapManager.java` uniquement pour fournir le cadre choisi, `render/NanoMinimapRenderer.java`, `render/ThreatGauge.java`, `nanovisor/client/NanoVisorHudOverlay.java`, ainsi que les nouvelles ressources N1 et traductions. Le suivi serveur/paquet de menace ne change que dans le lot facultatif 6. Les résolveurs de profil et moteurs communs ne nécessitent pas de réécriture.

## Risques et décisions avant intégration

| Risque | Importance | Décision / garde nécessaire |
|---|---|---|
| Carte et clipping sous Iris/Complementary | Élevée | Le renderer existant utilise notamment scissor/stencil et transformations. Tester le nouveau masque et l’état OpenGL restauré ; ne pas annoncer de compatibilité à partir des SVG. |
| Deux jauges de menace issues d’un seul enum | Élevée | Choisir une approximation explicitement assumée ou développer deux mesures indépendantes. ZA signale une investigation de zone, pas nécessairement la recherche du joueur. |
| Marqueurs de mobs moddés / client hors portée | Élevée | Données limitées aux entités et chunks reçus. Ne pas promettre une carte satellite globale, ni un radar d’IA omniscient. |
| N2 affectée par une modification partagée | Élevée | Routage profil dans les chemins compositeur **et** secours ; anciens paramètres N2 inchangés. Contrôler avant/après et transition N1/N2/équipement incomplet. |
| TACZ et autres HUD d’armes | Moyenne à élevée | Réduire la suppression à ce qui est réellement remplacé ; ne pas supprimer les aides de visée, reload et indicateurs non reproduits. Pont de réflexion avec repli au HUD original en cas d’échec. Compatibilité non retestée ici. |
| Santé non standard / absorption / faim / oxygène / XP | Moyenne | Valider le pourcentage de santé et la coexistence des infos de survie. Ne pas masquer arbitrairement la hotbar, la faim ou l’absorption pour imiter une capture Crysis. |
| Punchy, EMF/ETF/Fresh Animations | Indirecte | Le dessin 2D ne doit pas repasser dans les moteurs de bras ou d’entités. Aucun changement prévu dans leurs chemins ; absence de régression à vérifier lors de l’intégration, pas certifiée dans cette étude. |
| Polices, GUI scale, ultrawide, inertie et menus | Moyenne | Police redistribuable à choisir, marges sûres, dimensions proportionnelles, réglages N1 distincts ; vérifier chat/menu/F1 et superpositions d’autres mods. |
| NV et post-traitement | Moyenne | Jauge après la scène, couleurs lisibles ; ne pas réappliquer le filtre à tout le HUD. Ne pas confondre vision nocturne N1 et thermique N2. |

Autres décisions : comportement visuel de NONE, badge à énergie nulle, affichage des munitions sans TACZ ou avec arc vanilla/Predator Bow, unité exacte de la réserve, direction fixe ou rotation de carte, politique de marquage des ennemis non repérés, affichage ou absence d’objectifs. Aucun de ces choix ne doit être implicitement transformé en règle de gameplay pendant l’intégration graphique.

## Livrables et vérification de cette étude

- `index.html` : galerie locale avec quatre états et comparaisons ; `comparison.png` : planche agrandie.
- `hud-normal`, `hud-critical`, `hud-night-vision`, `hud-threat` : chacun en `.svg` et `.png` 1920 × 1080.
- `hud-binocular-study.svg/.png` : **étude provisoire**, géométrie non validée.
- `generate.py` : source de génération ; SVG autonomes contenant les fonds et symboles ; `simulated-states.json` : provenance des valeurs.
- `references/` : captures d’origine utilisateur, captures directes des réglages, pages PDF rendues, comparaison de préservation et trace limitée du chargement.

Validation effectuée : génération des SVG, rendu réel dans Chrome, contrôle visuel des PNG, vérification des liens de galerie et des quatre états, absence de modifications dans `src/`. Pas de validation du rendu Minecraft du nouveau HUD, ni de campagne TACZ/Punchy/EMF/Iris. Les durées des animations originales restent non mesurées. **L’intégration attend la validation des maquettes.**
