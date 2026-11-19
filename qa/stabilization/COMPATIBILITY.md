# Bilan graphique et compatibilités — 19 novembre 2026

Complète RESULTS.md (défauts A/B) et MELEE-HUD.md. Toutes les exécutions ci-dessous utilisent des copies isolées du banc RTX 3070. Aucun profil personnel, réglage externe ou code d'IA n'est modifié.

## Résultats ciblés

| Périmètre | N1 | N2 | Preuve |
| --- | --- | --- | --- |
| Équipement et interruption des états | PASS | PASS | Matrice des 81 équipements et 21 scénarios, RESULTS.md. |
| Viseur N2 dans un inventaire | — | PASS | stability-n2-screen : reste actif ; retrait d'une pièce annule le viseur, FOV normal. |
| Vision nocturne sans shader, lait et vraie potion indépendante | PASS | PASS contrôle | night-light-vanilla : 18 états, failures=0. |
| Iris 1.8.14-beta.1 + Complementary Reimagined r5.10 | PASS ciblé | PASS ciblé | night-light-complementary, stability-combined-final : 18 puis 21 états, failures=0. Réserves OpenGL ci-dessous. |
| Fresh Animations 1.11.2 / EMF 3.3.12 / ETF 7.2.7 | PASS ciblé | PASS ciblé | night-light-fresh puis stability-combined-final ; modèles EMF réellement actifs pour vache, squelette et zombie. |
| Zombie Awareness 1.13.2 / CoroUtil 1.3.8 | PASS ciblé | PASS ciblé | stability-zombie-awareness : 13 états naturels, failures=0. |
| Ensemble Iris/Complementary/FA/EMF/ETF/ZA/Coro/TACZ 1.2.0 | PASS ciblé | PASS ciblé | stability-combined-final (21 états visuels), stability-za-combined (13 états de poursuite naturelle), failures=0. |
| Jumelles avec arme TACZ | PASS ciblé | — | ×2, ×4 effectif, NV simultanée, compteur masqué, retrait rendant le FOV normal. |
| Pack existant Complementary r5.9.3 + EuphoriaPatches 1.10.5 | PASS limité | PASS limité | stability-euphoria : obscurité, entrée/NV N1, contrôle et thermique N2, modèles EMF actifs, failures=0. |
| Patcher Euphoria 1.11.1-r5.10 | NOT TESTED | NOT TESTED | Présent dans le profil personnel ; non installé dans le banc. Le test du pack déjà extrait n'est pas celui de cette version. |
| Radial maintenu physiquement avec shaders, mort/réapparition, reconnexion disque, nage complète | NOT TESTED | NOT TESTED | Les fixtures d'interruption ne couvrent pas ces parcours. |

Les scènes NightLightQa figent volontairement les mobs pour comparer l'image. ZombieAwarenessQa conserve leur IA normale : acquisition spontanée après ouverture d'une paroi, camouflage, perte de cible, observation, rupture, plusieurs ennemis et retrait d'équipement. Aucun setTarget forcé, aucun NoAI dans ce second parcours.

En session combinée, un zombie N2 conserve temporairement une navigation sans cible : cela ne démontre pas un état exposé de « recherche de dernière position connue ». Aucun nouvel état précis de suspicion ni nouvelle IA n'est inventé. La jauge peut refléter la dernière synchronisation pendant les dix ticks d'échantillonnage ; la proximité seule n'est pas une preuve de détection. Le parcours valide les poursuites, pas toutes les conséquences des dégâts en combat.

## Éclairage N1 et vignettes

L'ancien filtre relevait seulement les pixels déjà rendus. Le nouveau chemin active la normalisation lumineuse de LightTexture et fournit l'intensité existante à GameRenderer.getNightVisionScale. Iris interroge déjà cette fonction pour son uniform nightVision : aucune dépendance Iris obligatoire et aucun mixin vers son implémentation.

Pas d'effet de potion ajouté au joueur. Le lait ne désactive pas la N1 ; une vraie potion reste utilisable après extinction N1. Hors N1 l'intensité supplémentaire vaut zéro. La transition existante pilote lumière et filtre ; les variations rendent la lightmap à recalculer, sortie comprise. Autonomie, recharge, sons de vision et thermique N2 inchangés.

Pièce isolée à lumière bloc=0/ciel=0 : luminance numérique moyenne de la zone (500,200)-(1400,750), avant la dernière retouche de teinte : sans shader 8,66 éteint → 98,07 actif ; Complementary 8,99 → 79,19. Après lait actif : 98,07 / 79,20 ; après arrêt et lait : 8,66 / 8,81. Ce sont des mesures de pixels du banc, pas une promesse de luminosité universelle.

Références de couleur : design/nanosuit-1-hud/references/user-2026-11-19/05-night-vision.jpg et 06-night-binoculars.jpg. Leur vert est jaune-vert. Coefficients passés de (0,63 ; 1,04 ; 0,67) à (0,58 ; 1,04 ; 0,20). Le rendu varie encore avec matériaux et shaderpack ; ce n'est pas le traitement CryEngine complet.

La vignette laisse clairs le centre et les milieux des bords ; seuls les coins éloignés reçoivent jusqu'à 32 % d'assombrissement. Les jumelles emploient un SVG/PNG radial correspondant. Leur contribution décroît lorsque celle de la NV augmente, pour éviter le double assombrissement. stability-combined-final contient le filtre final, les jumelles seules et jumelles+NV. stability-final-placement contrôle ensuite le rendu final sans shader (failures=0).

## Corrections demandées en fin de campagne

- Barre de menace : colonne déplacée hors du terrain, sur son bord gauche (x local -15 à -3 au lieu de 3 à 15). Triangle toujours dans le cadre en bas à gauche. Taille, états et calcul inchangés ; N2 intacte. Contrôle final stability-threat-placement : 13 états, ZA_COMPLETE failures=0, capture n1-revealed inspectée avec alerte rouge hors de la carte.
- Voix armure : les événements étaient distincts, mais les fichiers N1/N2 du port contenaient presque le même enregistrement (corrélation des enveloppes 0,9977). La première restauration du JAR original du mod faisait entendre la voix féminine ; elle a été rejetée et remplacée.
- Source finale reconnue explicitement par l'utilisateur : Crysis 2/Languages/English/dialog/suit_male/suit_voice_armor_mode.mp2. Conversion via les codecs Audacity existants, sans nouvelle normalisation. Provenance dans n2-armor-voice.json. Voix N1, autres annonces et volumes inchangés. verify_armor_audio.py contrôle les fichiers et mappages du JAR ; ce n'est pas une capture loopback.
- Boussole : calcul sur yaw réel conservé ; pas de modification de ce composant pendant cette campagne.

## Réserves graphiques

Les exécutions Iris terminent mais journalisent GL_INVALID_OPERATION « Depth formats do not match ». Le même message existe dans run/qa-multi-suit-render/logs/latest.log antérieur à cette campagne. Non corrigé ici : PASS ciblé signifie réussite des états/captures contrôlés, pas une chaîne graphique sans erreur. Complementary r5.10 signale aussi des variables endFlashIntensity/endFlashFactor inconnues. Le vieux pack Euphoria ne présente pas ce même avertissement. Des avertissements de ressources/tags existent aussi sans shader. Aucun de ces messages n'est masqué.

Pas de nouveau test Xaero/JourneyMap/Diorama ni d'un mod précis d'armes de mêlée. Les scénarios sont représentatifs ; les combinaisons de réglages ne sont pas toutes testées. Voir MELEE-HUD.md pour la distinction entre convention 000/0/∞ et véritables données de munitions.

## Livraison et fichiers

Production : ClassicNightVisionClient, deux nouveaux mixins ClassicNightVisionLightmapMixin et ClassicNightVisionStrengthMixin, crysismod.mixins.json, classic_night_vision.fsh, ClassicBinoculars, vignette.svg/png, ClassicThreatIndicator et sounds/max_armor.ogg. Le correctif préservant le viseur N2 concerne NanoVisorState et NanosuitClientContext. Changements A/B et mêlée détaillés dans leurs rapports.

Banc : run.py accepte packs de ressources et shaderpacks déjà extraits dans ses copies ; NightLightQa et ZombieAwarenessQa restent hors du mod distribué. Compilation finale : build/stability-final-build.log, BUILD SUCCESSFUL. Gradle indique test NO-SOURCE : les validations viennent des sondes dans Minecraft, pas de tests unitaires imaginaires.

Captures : [galerie](evidence/gallery.html). Marqueurs, versions de JAR et messages graphiques : evidence/compatibility-observations.txt. Commits et état final : DELIVERY.md.
