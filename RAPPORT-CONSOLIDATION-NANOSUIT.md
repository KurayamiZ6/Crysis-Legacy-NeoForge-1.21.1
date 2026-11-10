# Consolidation Nanosuit et validation du port NeoForge 1.21.1

## Référence Git

Le dossier de travail était propre. `main` pointe sur `39738b19b64837568b4fd84ffb35487d3c8e5c1d`. Le commit `e5bf6ee7ea49663227a7bfdf6ce0a78aeb3d6734` est son descendant direct et constitue la tête de `migration/nanosuit-legacy-current`. Le commit `3865d57f9860c08ac93c118cc2f10af5ec793391`, tête de `fix/nanovision-fresh-iris`, a directement pour parent le commit de migration. Les deux relations ont été vérifiées avec `git merge-base --is-ancestor`.

La branche `consolidation/nanosuit-legacy-current` a été créée sur `3865d57`. Les commits de migration et de rendu restent identifiables séparément dans son historique. Les trois branches d’origine conservent leurs références.

## Validation graphique

Les essais utilisent Minecraft 1.21.1, NeoForge 21.1.265 et le JAR consolidé avant migration du Power Jump. Son SHA-256 est `9C0127BF7996BFD3672EF420874E53F470E72EE99458DD90980DD12CA16DBC9F`. Le JAR a été figé dans `build/consolidation-reference/crysis-pre-power-jump.jar`. Les profils de test sont isolés du modpack principal.

Les chemins de preuve ci-dessous sont relatifs au dossier NeoForge. `PASS` qualifie uniquement la scène ou le comportement indiqué.

| Configuration et scénario | Résultat | Preuve |
| --- | --- | --- |
| Sans Iris, Fresh Animations 1.11.2 + EMF 3.3.12 + ETF 7.2.7 | PASS | A/B Fresh sur la même scène. La vache, le zombie et le squelette conservent les couleurs thermiques et leurs silhouettes Fresh. `run/qa-fresh-thermal-fixed/screenshots/02-fresh-off-thermal-on.png`, `04-fresh-on-thermal-on.png`, journal ligne 521. |
| Iris 1.8.14-beta.1 et Sodium 0.8.13, pack désactivé, mêmes Fresh/EMF/ETF | PASS | Les trois entités et la pioche restent thermiques, puis un mur masque la vache. `run/qa-fresh-thermal-iris-fixed/screenshots/05-fresh-on-thermal-on-shader-off.png`, `06-fresh-on-occlusion-shader-off.png`, journal ligne 1899. |
| Iris et Complementary Reimagined r5.9.3, mêmes Fresh/EMF/ETF | PASS | Les trois entités animées sont thermiques, 612 sommets acceptés. La vache derrière un mur n’apparaît plus. `run/qa-fresh-thermal-iris-fixed/screenshots/04-fresh-on-thermal-on.png`, `07-fresh-on-occlusion-shader-on.png`, journal ligne 1750. |
| Pile complète sous Complementary r5.9.3 + Euphoria Patches 1.10.5 | PASS | Fresh/EMF/ETF, Iris/Sodium, TaCZ 1.2.0 et Punchy 3.2 chargés. La vache, le zombie et le squelette sont thermiques, le bras animé et la pioche le sont aussi. La vache disparaît derrière le mur. Captures 02–03, journal lignes 797, 845 et 1095. |
| Même pile, pack de shaders désactivé | PASS | Mêmes trois silhouettes thermiques, bras et pioche visibles en thermique, occlusion du mur conservée. Captures 10–11, journal lignes 3571, 3588 et 3593. |
| ACOG, 552 et EXP3 TaCZ 1.2.0 en visée complète sous Euphoria | PASS | Six passages ADS dans la session, dont trois sous pack. À `aim=1,00`, ouverture dégagée et réticule centré pour chaque viseur, zoom ACOG. Contraste des holographiques signalé ci-dessous. Captures 04–06, journal lignes 1603, 2294 et 3051. |
| ACOG, 552 et EXP3 TaCZ 1.2.0 en visée complète, pack désactivé | PASS | Même visibilité dans les ouvertures et centrage. Les réticules 552 et EXP3 retrouvent leur couleur. Captures 12–14, journal lignes 3598, 3602 et 3606. |
| Camouflage avec Punchy, pile complète | PASS | Bras et pioche réfractés en première personne avec et sans pack. Silhouette de la Nanosuit réfractée en troisième personne sous Euphoria. Captures 07–09, journal lignes 3457, 3527 et 3560. |
| Absence de crash du rendu dans la session combinée | PASS | Fin normale, aucune occurrence de `Missing elements in vertex` dans cette session. Journal ligne 3608 et arrêt du serveur intégré. |
| OpenGL sans erreur de profondeur sous Euphoria | FAIL | 999 occurrences de `GL_INVALID_OPERATION: Depth formats do not match` dans cette session, jusqu’à la ligne 3539 avant la phase pack désactivé. L’erreur était déjà présente sur la référence, y compris dans le profil avec Complementary seul. |
| Camouflage en ADS TaCZ, camouflage en troisième personne pack désactivé | NOT TESTED | Le scénario de camouflage combiné a utilisé une pioche. La séquence sans pack n’a repris que la première personne. |
| Locomotion des mobs, extensions Fresh et modpack principal complet | NOT TESTED | Le banc utilise trois entités statiques avec des poses Fresh et une sélection de dépendances dans des profils isolés. |

Pour les lignes de la session combinée, les images numérotées 01 à 14 et le journal se trouvent sous `run/qa-consolidated-render/screenshots/` et `run/qa-consolidated-render/console.log`. Le journal confirme le chargement d’Euphoria à la ligne 89. Les captures 01, 02 et 10 montrent la pose et la silhouette Fresh avant et pendant la nanovision.

### Anomalies résiduelles

- **Gravité moyenne, OpenGL.** Les incompatibilités de formats de profondeur se répètent sous pack actif. Aucun blocage ni perte d’occlusion observé dans les scènes testées. La fréquence brute dépend du nombre d’images de la session et ne mesure pas une variation de performances.
- **Gravité faible, lisibilité.** Les réticules des 552 et EXP3 sont sombres et peu contrastés sur un mob thermique sous Euphoria, comme dans les essais antérieurs avec Iris. Ils restent centrés et la fenêtre du viseur reste transparente.
- **Incident historique.** Le crash du carter thermique TaCZ avec des attributs de sommet manquants n’a pas été reproduit dans le montage combiné. L’ancien journal figure dans `compat-retest.log` à la ligne 622.

Les validations de rendu permettent de poursuivre le déplacement du coût du Power Jump. Les anomalies graphiques relevées restent des sujets distincts de cette règle de gameplay.

### Classes du correctif de rendu consolidé

`ThermalExternalModelCompatibility` reconnaît et déplie les types de rendu Iris par son interface optionnelle. `ThermalMarkerCompatibilityRenderer` utilise ensuite le type interne pour filtrer le flux et récupérer la texture et le format des modèles lors du rejeu thermique. Le correctif est apporté par `3865d57`.

## Migration du coût du Power Jump

`NanosuitProfile` expose deux règles dans `LEGACY_CURRENT` : délai commun de bascule Armure/Camouflage de 20 ticks et coût du Power Jump de 10 points. Le profil couvre la seule combinaison historique éligible.

`NanosuitPlayerHandler.enhancedJumpEnergyCost()` lit désormais la valeur du profil. Avant, la méthode calculait deux segments de HUD à 5 points chacun. La méthode publique reste le point commun de lecture. `EnhancedJumpClientState` l’utilise indirectement via `canChargeEnhancedJump` avant la demande de saut, et le serveur l’utilise pour autoriser la charge et débiter le lancement. Les conditions de charge, le seuil de quatre ticks, le délai de huit ticks, l’impulsion, le guidage de rebord et le découpage du HUD conservent leur code.

Le build Gradle hors ligne a réussi. SHA-256 après migration : `D6B484303245567491F370DD34EC78C24A0ADA15379149B6ED1252A835A42BB8`. Les deux JARs contiennent 779 entrées de fichiers. La comparaison des empreintes des entrées ne relève que le profil, le handler et ses quatre classes internes recompilées. Le désassemblage normalisé confirme que la seule méthode modifiée du handler est le lecteur du coût et que les instructions de ses classes internes sont identiques. Les classes du registre d’objets, de sérialisation NBT et de réseau sont identiques octet pour octet, comme le rendu et les ressources. Le protocole reste en version 4. Les preuves de comparaison se trouvent sous `build/consolidation-reference/`.

## Comparaison avant/après en jeu

Même banc dans les deux profils, SHA-256 `8CAEA31E83DF1C34894F625EE1E3C7F6ADA8D40CC8B7258A727AD6524FDA7ECE`. Les journaux complets sont `build/consolidation-jump-qa/before/console.log` et `build/consolidation-jump-qa/after/console.log`. Les deux se terminent par `POWER_JUMP_QA_COMPLETE failures=0`. Le comparateur `build/consolidation-jump-qa/compare.ps1` confirme 29 assertions passées de chaque côté.

| Scénario | Avant | Après | Preuve dans le journal après |
| --- | --- | --- | --- |
| Appui bref, saut vanilla et aucun débit | PASS | PASS | Ligne 245, énergie 100 et aucun son de Power Jump. |
| Maintien de quatre ticks, confirmation serveur et coût exact | PASS | PASS | Lignes 315–318 et 382–383, demande client et lancement serveur au tick 4, énergie 90 de chaque côté. |
| Énergie insuffisante et panne | PASS | PASS | Lignes 363–372, aucun débit à 9 points. La charge est aussi refusée avec le verrou actif et 50 points. |
| Cooldown du Power Jump | PASS | PASS | Lignes 386–387, charge refusée au tick 7 et acceptée au tick 8. Le banc neutralise la condition de contact au sol pour isoler le cooldown. |
| Rebord libre de quatre et cinq blocs, rebord obstrué | PASS | PASS | Lignes 339, 350 et 361. Les coordonnées d’atterrissage varient légèrement entre exécutions, avec les mêmes résultats de franchissement. |
| Relâchement, retrait et remise du casque | PASS | PASS | Lignes 317 et 374–381, aucune seconde impulsion, aucune charge différée et aucun débit lors du retrait. |
| Armure et Camouflage | PASS | PASS | Lignes 393 et 404–406, activation du camouflage, refus de bascule à 19 ticks puis Armure à 20. |
| Serveur dédié distant | NOT TESTED | NOT TESTED | La synchronisation a été mesurée avec le client et le serveur intégré dans les deux profils. |

### Suite de la migration

Les règles encore locales incluent le coût et le multiplicateur du Power Melee, les coûts du camouflage en combat, les drains et la recharge, les règles du sprint et de la nage, ainsi que les délais et le mouvement du Power Jump. Les chemins graphiques restent indépendants du profil.

La prochaine règle proposée est **le coût de 15 points par impact réussi du Power Melee**. Ce coût est borné à une méthode commune au poing et à la crosse. La règle peut donc être déplacée en gardant le multiplicateur et les sons dans leur code actuel. Avant ce déplacement, figer les cas de coup refusé, d’énergie à la limite et de plusieurs cibles TaCZ. Le risque principal est de modifier le moment du débit ou sa granularité par impact, ce qui changerait le comportement lorsqu’une frappe touche plusieurs cibles.
