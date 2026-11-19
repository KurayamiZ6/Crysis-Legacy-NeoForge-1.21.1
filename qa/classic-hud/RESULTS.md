# Première intégration du HUD N1 — 19 novembre 2026

Branche `feature/nanosuit-1-classic`. Référence avant modification : `6f4dc9a`.

## Résultat et périmètre

Le panneau N1 remplace les anciennes divisions énergétiques et le libellé de mode. Il affiche santé normalisée au maximum réel, énergie synchronisée et icône du mode confirmé. Les formes proviennent des couches SVG V3, avec chiffres rasterisés depuis la même police de substitution Bahnschrift. La batterie jaune indépendante utilise sa charge serveur existante ; segments horizontaux, charge restante vers la droite, affichage maintenu pendant sa recharge.

Le panneau est appelé uniquement pour une combinaison complète N1, dans le point de rendu commun au compositeur et au secours historique. Le pourcentage énergétique indépendant de `NanosuitPerspectiveDebugOverlay` est également supprimé pour N1 afin d’éviter un doublon. Le chemin N2 conserve son ancien rendu.

La barre devient rouge à **15 % inclus**, y compris l’icône éclair à zéro. Le seuil de **25 %** conserve l’alerte existante, sans nouvelle bannière ni modification sonore : la référence ne justifie pas de rendre toute la barre rouge à 25 %. Les limites sont évaluées sur l’énergie brute, pas sur le chiffre arrondi. La sonde observe le message existant à 25 %. Le test n’est pas une validation acoustique.

Les barres suivent directement les données, sans retarder le rendu ni modifier les règles de recharge. Les cœurs, l’absorption, la faim, l’armure, l’expérience et la hotbar vanilla restent disponibles. Aucun panneau de munitions n’est ajouté ; la minicarte existante n’est pas redessinée ni déplacée.

## Tests exécutés

Java 21 : compilation de référence puis compilation du candidat **PASS**. Dernier `gradlew build --offline` : succès, avertissements de dépréciation préexistants ; Gradle indique `test NO-SOURCE`. Les tests exécutés séparément ne sont donc pas présentés comme une suite JUnit.

- **12/12 PASS** : mathématiques de présentation, limites 25/15, énergie fractionnaire positive, zéro, dépassement, maximum de santé modifié, charge de batterie et valeurs invalides.
- **14 captures finales** du client Minecraft NeoForge 21.1.265, Azul Java 21, Sodium 0.8.13. Le journal confirme `NVIDIA GeForce RTX 3070 Laptop GPU`, OpenGL 4.6 NVIDIA 621.01.
- **13/13 PASS** : contrôles de pixels sur ces captures ; mesures détaillées dans `evidence/manifest.json`.

| Scénario | Résultat | Observation effective |
|---|---|---|
| N1 100 % | PASS | Énergie serveur/client 100 ; barre bleue pleine, santé 100, icône Armure |
| N1 25 % | PASS | Énergie 25 ; rapport de pixels actifs 26,1 % du plein, tolérance de rasterisation ; alerte existante conservée |
| N1 15 % | PASS | Énergie 15 ; remplissage rouge mesuré 15,6 % |
| N1 0 % | PASS | Énergie 0, aucun remplissage ; éclair rouge visible |
| Recharge N1 réelle | PASS | 54,000023 points observés ; pixels actifs 54,7 % ; pas de recharge simulée dans le renderer |
| Santé à moitié | PASS | 10/20 points réels, affichage 50, remplissage 50,3 % |
| Vision nocturne active | PASS | Charge synchronisée 557/1200 ; batterie visible et alignée |
| Recharge batterie | PASS | Vision désactivée, charge synchronisée 744/1200 ; batterie encore visible |
| Batterie pleine | PASS | Pas de jauge persistante à 1200 lorsque désactivée |
| N2 équipée | PASS ciblé | Ancien HUD énergétique et modes conservés ; aucun panneau/batterie N1 |
| Pièce de combinaison retirée | PASS | Absence du panneau N1 et de sa batterie |
| GUI 2 et GUI 3, 1920×1080 | PASS | 1611 contre 1613 pixels bleus dans la même zone physique ; taille stable |
| Modes Force, Vitesse, Camouflage | PASS visuel ciblé | Trois captures supplémentaires, icônes issues des symboles V3 ; mode client synchronisé journalisé |

Les captures utilisent le framebuffer réel via `Screenshot.grab`, après les autres écouteurs GUI et après vidage des commandes de dessin. Le mod QA prépare les valeurs sur le serveur intégré d’une copie du monde. Ces préparations de scène ne sont pas livrées dans le mod. Le monde, la configuration et les mods du profil personnel ne sont pas modifiés.

## Corrections identifiées pendant la validation

1. Première version : ancien pourcentage indépendant encore présent au-dessus du panneau N1. Correction de sa condition de rendu exclusivement pour N1 ; nouvelle campagne finale sans doublon.
2. Sonde initiale : capture trop tôt pour contenir la batterie dessinée en fin de GUI. Correction de priorité de la sonde et appel `flush()` ; les premières captures ne sont pas utilisées comme preuve de fonctionnement de la batterie.

## Comparaison avec Crysis et limites

`evidence/comparison.png` compare la référence utilisateur, l’ancien HUD Minecraft et le panneau intégré. `evidence/index.html` contient les captures avant/après, les états et le manifeste. Les contours, teintes et proportions reprennent la V3. Les chiffres restent une approximation typographique, rasterisée à haute résolution ; ni police originale ni déformation 3D dynamique de Crysis ne sont revendiquées. Le panneau est fixe à l’écran, sans nouvelle inertie.

La vignette forte était présente dans la maquette, **pas dans le shader vert N1 existant**, qui n’en contient aucune. La maquette est corrigée pour limiter l’assombrissement aux coins ; aucun shader n’est ajouté ou modifié en jeu. La capture NV finale montre le traitement vert actuel sans cet assombrissement excessif. La transition d’intensité d’environ une seconde est uniquement spécifiée pour une étape ultérieure.

Les nouvelles observations des jumelles sont consignées dans `design/nanosuit-1-hud/SPEC-CORRECTIONS.md` et remplacent explicitement le prototype réversible historique. Aucune intégration des jumelles pendant cette mission.

**NOT TESTED ici** : shaders/Iris, Fresh Animations, TACZ, interfaces de cartes tierces, plein écran sur une autre résolution, variations manuelles du nouveau facteur d’échelle, résistance aux appuis rapides des futurs composants. Le chemin de secours du compositeur est inspecté statiquement mais n’a pas fait l’objet d’une campagne client séparée. Pas de garantie générale de non-régression sur ces intégrations.

Les avertissements de shaders d’armure (uniformes absents) et de classes optionnelles TACZ/Iris absentes sont présents dans les journaux du banc réduit, avant comme après. Ils sont distincts des vérifications du HUD ; aucune correction globale entreprise. Aucun crash AMD observé dans ces sessions RTX.

## Réglage et reproduction

Nouvelle clé **`hud.classic.scale`**, défaut 1, limites 0,5–2, dans la configuration client existante. Elle agit sur panneau N1 et batterie indépendamment de `hud.scale` N2. Les positions suivent la hauteur de l’écran ; aucune valeur personnelle existante n’est écrasée.

Depuis la racine du dépôt :

```text
python qa/classic-hud/export_assets.py
node qa/radial-menu/render-assets.cjs <module-playwright> <chrome.exe> src/main/resources/assets/crysismod/textures/gui/classic_hud
gradlew build --offline
```

Pour le client, réutiliser `qa/mode-overlay/run.py` avec `--probe qa/classic-hud/ClassicHudQa.java --probe-mod-id crysisclassichudqa --width 1920 --height 1080`, un nom d’exécution neuf, les chemins Java 21/Sodium du banc et le JAR voulu. Les campagnes de cette mission sont `build/mode-overlay-qa/classic-hud-before`, `classic-hud-after` (intermédiaire) et `classic-hud-final`.

## Fichiers

Production :

- `nanosuit/client/ClassicNanosuitHud.java` : nouveau renderer des couches V3 et de la batterie.
- `nanosuit/client/ClassicHudValues.java` : calculs de présentation uniquement.
- `nanosuit/client/NanosuitHudOverlay.java` : routage N1, retour avant l’ancienne jauge.
- `nanosuit/client/NanosuitPerspectiveDebugOverlay.java` : garde N1 sur l’ancien pourcentage.
- `nanosuit/config/NanosuitClientConfig.java` : échelle indépendante N1.
- `client/nanovision/ClassicNightVisionClient.java` : nouveau dessin de batterie et garde combinaison complète pour son affichage ; traitement scène, synchronisation et batterie logique inchangés.
- `assets/crysismod/textures/gui/classic_hud/` : onze SVG éditables et onze PNG RGBA exportés.

Tests : `qa/classic-hud/` contient sonde, tests de limites, exporteur, vérificateur des captures et preuves. Conception : conservation du dossier V3 précédemment non suivi, correction de la vignette/alignement et documentation des nouvelles spécifications. Aucun changement d’identifiant, protocole v6, gameplay, coût, recharge ou son.

Le JAR final et son empreinte sont référencés dans `evidence/manifest.json`. La prochaine étape reste la validation utilisateur de ce panneau, avant toute intégration des composants exclus.
