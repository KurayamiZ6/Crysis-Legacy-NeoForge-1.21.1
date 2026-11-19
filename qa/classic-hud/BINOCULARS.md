# Jumelles N1 : intégration et validation ciblée

Branche `feature/nanosuit-1-classic`. Base : `8e9e5e0`, qui contient séparément la transition nocturne N1 demandée auparavant.

## Fonctionnement livré

Avec une N1 complète, le raccourci existant du NanoVisor ouvre les jumelles classiques. **V par défaut**, ou la touche déjà choisie par le joueur ; B n’est pas imposée, car elle sert déjà à la nanovision dans le mod. N2 conserve ses commandes et son NanoVisor actuels.

**Molette vers le haut : ×4 ; vers le bas : ×2.** L’événement est intercepté uniquement lorsque les jumelles N1 sont réellement ouvertes ; la molette retrouve son comportement habituel autrement. Un raccourci de grossissement N1 supplémentaire est configurable, **non attribué par défaut**. La touche C du zoom historique reste disponible pour N2 et n’est pas réaffectée : TACZ utilise déjà C pour ramper.

La projection est calculée par `2 atan(tan(FOV/2)/grossissement)` sur le FOV courant, après ses variations normales. Le multiplicateur de zoom du NanoVisor historique est neutralisé pour le seul chemin N1. Pendant l’animation, le grossissement rejoint ou quitte sa valeur avec le déploiement. Il n’est jamais multiplié une seconde fois par le zoom historique.

Une visée TACZ active ou en cours de transition ferme les jumelles et interdit leur activation ; l’adaptateur optionnel utilise les méthodes publiques de l’opérateur client TACZ 1.2.0. Ainsi l’ADS et les jumelles ne se cumulent pas. L’utilisation/attaque normales ferment aussi la présentation ; les commandes d’arme ne sont pas bloquées. Les mains sont masquées via `RenderHandEvent` pendant la vue optique, puis rendues normalement. Aucun fichier de TACZ ou du modpack personnel n’est modifié.

## Animation et données

`ClassicBinocularAnimation` applique exactement les règles corrigées :

1. Ouverture depuis 0.
2. Appui pendant ouverture : saut à 100 %, puis fermeture.
3. Appui pendant fermeture : nouvelle ouverture depuis 0.
4. Chaque appui en file est traité, y compris plusieurs pendant le même tick.

Durée 450 ms, easing smoothstep. Elle reste provisoire : les captures originales ne permettent pas une mesure fiable et le jeu officiel n’a pas été relancé pour cette mission.

Les polygones, découpes, réticule, cases et crochets sont exportés des sources V3 ; les deux traits supérieurs permanents sont **exclus** des ressources des jumelles. Textes rendus par un atlas Bahnschrift dérivé de la maquette, sans embarquer le fichier de police. Les graduations se révèlent verticalement depuis le centre, pendant l’apparition des textes. L’habillage suit la couleur N1 configurable. Le HUD principal, la minicarte et les menaces ne sont pas modifiés.

- X/Z/Y : position réelle du joueur.
- Graduations : inclinaison interpolée du regard du joueur, signe opposé au pitch Minecraft ; valeurs réfléchies à ±90°. Valide pour la caméra de première personne standard utilisée dans les tests. Les caméras tierces qui désolidarisent le regard du joueur restent à tester.
- Distance : réutilisation du raycast existant bloc/entité de `NanoVisorHudOverlay`, avec ses portées configurées et l’occlusion par les blocs. Affichage de deux décimales ; `— M` (tiret dans l’atlas) sans impact valide. Le calcul part des yeux du joueur, sans promesse d’une mesure exacte depuis une caméra détachée.
- ZOOM : grossissement réellement appliqué, ×2 ou ×4.
- MODE / VIS : libellé conservé, sans nouvelle fonction inventée.

F1 masque l’interface tout en laissant le grossissement actif, comme une commande de masquage HUD. L’ouverture d’un autre écran, la mort ou le retrait de N1 réinitialisent les jumelles et rendent le zoom. La fermeture visuelle n’entretient pas artificiellement l’état actif envoyé au serveur. Les paquets existants sont réutilisés ; aucune modification de protocole ou de coût énergétique.

## Validation effectuée

Java 21, NeoForge 21.1.265, banc isolé RTX 3070/Sodium.

- Compilation : **PASS**.
- Tests déterministes : **13/13 PASS**, incluant les appuis rapprochés/même timestamp et les rapports de projection.
- Tests graphiques/logiques : **31/31 PASS**, mesures dans `binocular-evidence/manifest.json`.
- Campagne principale `classic-binocular-verified` : ouverture/fermeture, interruptions, rafale de trois commandes, ×2/×4, raycasts, coordonnées, inclinaison, F1, autre écran, retrait N1, N2 et TACZ Glock 17.
- Campagne ciblée `classic-binocular-optic-verified` : M4A1 + viseur holographique **552**, ouverture, ADS, refus pendant ADS, retour aux jumelles et ×4. Inspection visuelle du viseur en ADS : réticule dans l’optique, sans interface des jumelles superposée. Ce n’est pas une validation de tous les viseurs/shaders.
- Capture sans TACZ à **1280×720**, `classic-binocular-clip` : 34 frames réels successifs. Le GIF conserve les intervalles relevés et n’interpole pas de faux frames.

| Scénario | Résultat observé |
|---|---|
| Projection ×2 | Rapport tangent mesuré 2, au FOV de base courant |
| Projection ×4 | Rapport tangent mesuré 4, y compris arme TACZ tenue |
| Bloc | 7,50 m |
| Entité devant le bloc | 4,75 m |
| Bloc masquant l’entité | 2,50 m |
| Ciel sans impact | −1 côté calcul, tiret côté affichage |
| Position | X 0,5 / Y 110 / Z 0,5 sur la scène contrôlée |
| Inclinaison | −90° vers le ciel, graduations de signe opposé |
| Fermeture pendant ouverture | Progression partielle suivie d’une fermeture depuis une valeur proche de 1 |
| Réouverture pendant fermeture | Nouvelle progression basse, puis arrivée à 1 |
| Trois clics en file | Fermeture finale effective |
| Autre écran / retrait N1 | État fermé et inactif |
| F1 | Pas d’overlay optique, état ouvert conservé |
| N2 | Ancien NanoVisor actif, machine à états N1 fermée ; inspection de la capture |
| ADS TACZ / nouvel appui | Jumelles inactives dans les deux cas |
| Retour après ADS | Jumelles ouvertes, puis grossissement ×4 effectif |

Les commandes sont injectées dans les files Minecraft et les API locales de la copie QA, sans frappes physiques. F1 utilise directement `hideGui` ; l’ADS maintient l’opérateur TACZ dans son état de visée. Les relevés `lastBaseFov/lastWorldFov` concernent le dernier calcul N1 effectué : ils ne servent pas à affirmer le FOV N2 ou celui d’une caméra inactive. Le retour normal est aussi contrôlé par les captures et l’absence d’application du chemin N1.

Le candidat des mesures et le JAR final ont des classes et ressources identiques, hormis une sérialisation équivalente des deux fichiers de traduction. Cette équivalence est vérifiée entrée par entrée ; le clip et la campagne optique utilisent directement le JAR final. Les deux empreintes sont conservées.

## Incidents de sonde corrigés

- La première commande était envoyée avant synchronisation complète de l’équipement ; téléportation/immobilisation de scène et attente de contexte ont été corrigées.
- La pause du serveur intégré bloquait la suite de la sonde ; elle ferme désormais l’écran avant d’attendre le tick serveur suivant.
- Les cases optiques étaient trop opaques et masquaient le texte : rétablissement explicite du blend après les dessins GUI et désactivation temporaire du test de profondeur pour les sprites transparents.
- Premier essai d’accessoire TACZ : création sans `HolderLookup.Provider`, rejetée par l’API. Sonde corrigée avec les registres du monde, puis scénario optique rejoué avec succès. Ce crash de préparation n’est pas un crash du mod livré.

## Limites restantes

Police de substitution, durée 450 ms et vignette légère encore approximatives. Pas de comparaison chronométrée avec Crysis Remastered. Pas de campagne Iris/Complementary, Fresh Animations/Punchy, tous les gunpacks/scopes, tir/rechargement exhaustifs, caméra détachée ou mode troisième personne. La combinaison jumelles + transition verte est conservée par l’ordre de composition existant mais n’a pas fait l’objet d’une nouvelle séquence dédiée ici. Les anciens avertissements de shaders d’armure du banc restent distincts de ces tests.

## Fichiers concernés

Production : deux nouvelles classes `ClassicBinocularAnimation` et `ClassicBinoculars`, branches N1 dans `NanoVisorState`, `NanoVisorZoomHandler`, `NanoVisorHudOverlay`, nouveau raccourci optionnel dans `NanoVisorKeyMappings`, traductions en/fr et trois SVG/PNG dans `textures/gui/classic_binoculars/`. Aucun changement du HUD principal, des cartes ou de l’énergie serveur.

Tests/export/preuves : `ClassicBinocularAnimationQa.java`, `ClassicBinocularVisualQa.java`, `export_binocular_assets.py`, `check_binoculars.py`, ce rapport et `binocular-evidence/`.

JAR : `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`. Séquence et captures : `binocular-evidence/index.html`, `minecraft-binoculars.gif`, `overview.png`.
