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
| Extensions Fresh et modpack principal complet | NOT TESTED | Les essais ciblent le pack de base et une sélection de dépendances dans des profils isolés. |

Pour les lignes de la session combinée, les images numérotées 01 à 14 et le journal se trouvent sous `run/qa-consolidated-render/screenshots/` et `run/qa-consolidated-render/console.log`. Le journal confirme le chargement d’Euphoria à la ligne 89. Les captures 01, 02 et 10 montrent la pose et la silhouette Fresh avant et pendant la nanovision.

### Anomalies résiduelles

- **Gravité moyenne, OpenGL.** Les incompatibilités de formats de profondeur se répètent sous pack actif. Aucun blocage ni perte d’occlusion observé dans les scènes testées. La fréquence brute dépend du nombre d’images de la session et ne mesure pas une variation de performances.
- **Gravité faible, lisibilité.** Les réticules des 552 et EXP3 sont sombres et peu contrastés sur un mob thermique sous Euphoria, comme dans les essais antérieurs avec Iris. Ils restent centrés et la fenêtre du viseur reste transparente.
- **Incident historique.** Le crash du carter thermique TaCZ avec des attributs de sommet manquants n’a pas été reproduit dans le montage combiné. L’ancien journal figure dans `compat-retest.log` à la ligne 622.

Les validations de rendu permettent de poursuivre le déplacement du coût du Power Jump. Les anomalies graphiques relevées restent des sujets distincts de cette règle de gameplay.
