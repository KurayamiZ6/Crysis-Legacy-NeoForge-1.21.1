# Référence et validation de LEGACY_CURRENT

## Périmètre

Minecraft 1.21.1, NeoForge 21.1.265, Java 21. Les clients ont utilisé des copies de mondes et de profils de test. Le JAR empaqueté de référence a été figé avant toute modification, avec le SHA-256 `AE0208931C412C84751060B1340CD6C16E7982D9DD42AE60BF5C9A107A2AC647`. La compilation Gradle hors ligne du port de référence a réussi. Les chemins ci-dessous sont relatifs au dossier NeoForge.

## Référence fonctionnelle

| Scénario | Résultat de référence | Mesure |
| --- | --- | --- |
| Camouflage sans TaCZ | Réussi | Le zombie acquiert d’abord la cible, la perd et ne la réacquiert pas pendant le cloak. L’invisibilité est appliquée sans potion, puis retirée, et la cible est reprise. |
| Camouflage avec TaCZ | Réussi | Un dégât environnemental laisse le mode actif. Un dégât ennemi met l’énergie à zéro, coupe le mode et déclenche le verrou. |
| Tir sans silencieux | Réussi | Un projectile, énergie 100 → 80, mode désactivé. |
| Tir avec silencieux | Réussi | Un projectile, énergie 100 → 93, camouflage maintenu. |
| Predator Bow `sfms:tb23` | Réussi | Projectile à 25 dégâts de base et 37,5 avec le set complet. Aucun prélèvement, en mode neutre comme en camouflage. |
| Poings | Réussi | 1 point de dégât et 0 dépense sans boost. 2 points et 15 d’énergie avec le set complet. |
| Crosse TaCZ M4A1 | Réussi | 2 dégâts sans boost, 4 pour 15 d’énergie avec le boost. Avec une énergie insuffisante ou une cible invulnérable, aucune dépense. Deux cibles éligibles prennent 4 chacune et coûtent 30 au total. Le banc appelle le même point d’entrée serveur que le paquet de mêlée TaCZ. |
| Power Jump | Réussi pour le mouvement et les collisions. Premier critère audio strict en échec. | Saut court préservé, lancement sans charge rejeté, maintien sur commande réassignée, coût 10, hauteur d’impulsion identique au relevé précédent. Les rebords libres de quatre et cinq blocs sont franchis et le rebord obstrué est refusé. Le premier banc reçoit le son après 5 ticks client et le compare à tort au seuil de charge de 4. |
| Power Sprint | Réussi | Premier appui vanilla, maintien du sprint après relâchement, boost au second appui, servo audible, aucun redémarrage pendant le cooldown ni au changement de mode avec touche maintenue. |
| Power Swim | Réussi côté serveur | La nage immergée sans boost conserve l’énergie à 100 et l’attribut à 1,0. L’état de sprint immergé donne 1,4 et consomme de l’énergie, puis le retour à l’état normal rétablit 1,0. Le banc fournit l’état de nage au serveur. L’entrée clavier et l’audio de la propulsion ne font pas partie de cette mesure. |
| Énergie et cooldown | Réussi | Set incomplet refusé. Bascule Armure/Camouflage refusée à 19 ticks et acceptée à 20. Alerte critique, arrêt, cinq impulsions, charge et restauration détectés. Mode bloqué à 0, après expiration du cooldown et à 21,95122 %. Première bascule à 100 % acceptée, suivie d’un nouveau délai de 20 ticks. |
| Punchy 3.2 et cloak | Réussi dans les scènes capturées | Bras animé et pioche réfractés en première personne, silhouette en troisième personne, sous Complementary + Euphoria et pack désactivé. |
| TaCZ, Iris et Complementary | Réussi dans les scènes capturées | TaCZ 1.2.0, Iris 1.8.14-beta.1, Complementary Reimagined r5.9.3 et Euphoria Patches 1.10.5. 552 et EXP3 rejoués hors visée. ACOG en visée complète, carter thermique, réticule rouge centré et ouverture dégagée avec et sans pack actif. |
| Fresh Animations 1.11.2 | Échec préexistant dans le montage rejoué | Avec EMF 3.3.12 et ETF 7.2.7 chargés, le zombie, le squelette et la vache de contrôle restent gris alors que l’arme est thermique. La vache reste grise avec le pack Iris désactivé. Cette observation ne départage pas les causes possibles sans comparaison contrôlée. |
| Ancien crash TaCZ + Fresh Animations | Échec historique, non reproduit sur la référence | Les nouveaux scénarios ont rejoué le carter thermique de l’ACOG en visée avec et sans shader, Fresh, Punchy, EMF et ETF chargés, sans l’exception de sommets. Des erreurs OpenGL de formats de profondeur sont encore présentes avec le pack actif. |

### Incident de rendu

Dans `compat-retest.log:622`, le crash part du rendu de la main de SimpleBedrockModel, puis du rendu du carter TaCZ via `crysisscopefix$renderThermalPart` et `renderScope`. Le couple de consommateurs `VertexMultiConsumer$Double` reçoit un flux qui laisse au moins un tampon avec un sommet dépourvu des attributs Color, UV0, UV1, UV2 et Normal. Le prochain sommet déclenche l’exception dans `BufferBuilder`. Cette trace identifie le chemin fautif. Elle ne démontre pas que Fresh Animations est le déclencheur ni que l’absence du crash dans les nouvelles scènes couvre toutes les configurations.

### Journaux de référence

- `build/legacy-functional-qa/before-core/console.log` pour le camouflage sans TaCZ, la panne, les poings et la nage.
- `build/legacy-functional-qa/before-tacz/console.log` pour les tirs, le Predator et la crosse.
- `build/legacy-functional-qa/before-jump-sprint/console.log` et `build/legacy-functional-qa/before-mantle/console.log` pour les contrôles et collisions.
- `run/legacy-render-qa/verification.log` et `run/legacy-render-qa/verification-ads.log` pour le rendu et les captures `legacy-01` à `legacy-18`.

## Rejeux ciblés de la référence

- **Power Jump, réussi.** Le banc instrumenté confirme le déclenchement local au quatrième tick, l’impulsion serveur et un seul son. La réception du son est relevée séparément. La première assertion qui assimilait l’arrivée du son au déclenchement a échoué avant la modification. Journal `build/legacy-functional-qa/before-controls-instrumented/console.log`.
- **Second client, réussi à quatre blocs.** Le premier essai a connecté l’observateur mais raté la fenêtre de cloak. Le rejeu a observé les trois états visible, camouflé puis visible, avec le drapeau distant et la sélection du rendu correspondants. Les captures 1 et 2 montrent la silhouette réfractée avec la pioche, puis le modèle opaque. La capture 0 est tombée sur un écran de chargement, bien que l’assertion d’état distant ait passé. Journal et captures sous `build/legacy-functional-qa/before-observer-guest-retry/`.
- **Non vérifiés dans cette référence.** Visée complète des 552 et EXP3 dans le montage avec Fresh. Rendu du camouflage distant avec TaCZ, Punchy et shaders simultanés. Autres extensions de Fresh Animations. Entrée réelle et son de la nage renforcée. Compatibilité sur le modpack principal.

## Modification et validation comparative

Le dossier NeoForge ne contenait aucun dépôt Git. Le dépôt local a été initialisé avec le port actuel sur `main` au commit `39738b1`. La branche de travail est `migration/nanosuit-legacy-current`. Les dépendances locales, caches, profils et journaux sont exclus de l’index local. Aucun dépôt distant n’a été configuré.

- `src/main/java/io/github/painworks/crysismod/nanosuit/NanosuitProfile.java` déclare `LEGACY_CURRENT` et le délai de 20 ticks.
- `src/main/java/io/github/painworks/crysismod/nanosuit/handler/NanosuitPlayerHandler.java` choisit ce profil à partir du test de tenue complète dans `tryToggleMode`. Les gardes, la transition de mode et l’envoi réseau gardent leur ordre.
- `RAPPORT-TESTS-LEGACY-CURRENT.md` contient les résultats et leurs limites.

Le build hors ligne de la branche a réussi. SHA-256 du JAR après modification `B51239432A0D35B48FC718D98226AB232443E06E5877E637F378B8DB093A6585`. La comparaison du contenu des deux JARs identifie uniquement la nouvelle classe de profil et la recompilation du handler et de ses quatre classes internes. Les instructions des classes internes et les méthodes du handler hors `tryToggleMode` sont identiques après normalisation des indices de constantes du bytecode. Les IDs d’objets, les données NBT, les capacités, les enregistrements de paquets du protocole 4, les ressources et les interfaces sont identiques.

Le rejeu sans TaCZ donne **35 lignes d’assertion et de mesures identiques**, en comparant séparément les traces serveur et les sons client. Les deux bancs se terminent avec zéro échec. Le set incomplet est refusé, la bascule Armure/Camouflage échoue à 19 ticks et passe à 20, la panne empêche toute bascule jusqu’à 100 %, puis une bascule réarme le même délai. Les neuf signaux audio, la perte et la reprise de cible, les poings et les mesures de nage concordent. Comparer `build/legacy-functional-qa/before-core/console.log` et `build/legacy-functional-qa/after-core/console.log`.

Les 17 lignes TaCZ sont également identiques, y compris les dégâts et coûts exacts des deux tirs M4A1, du Predator et de la mêlée à une ou deux cibles. Comparer `build/legacy-functional-qa/before-tacz/console.log` et `build/legacy-functional-qa/after-tacz/console.log`.

Les scénarios de sprint et saut instrumentés passent avant et après. Le saut est demandé au quatrième tick sur les deux JARs et atteint la même hauteur d’impulsion. Les deux rebords libres sont franchis, le rebord obstrué est refusé et le saut court ne dépense rien. La valeur d’énergie relevée à un instant du client pendant le sprint et les coordonnées finales sur les rebords varient entre exécutions. Ces mesures ne sont pas prises après un nombre fixe de ticks serveur. Les critères de commande, de coût du saut, de hauteur et de collision concordent. Journaux `before-controls-instrumented`, `after-controls-instrumented`, `before-mantle` et `after-mantle` sous `build/legacy-functional-qa/`.

Le rejeu de l’ACOG à 100 % de visée aboutit au même résultat visuel avec et sans pack actif. Aucun crash de sommets sur les deux JARs, et le défaut des mobs gris avec Fresh Animations est toujours visible. Journaux `run/legacy-render-qa/verification-ads.log` et `run/legacy-render-qa-after/verification-ads.log`. Les essais avant/après n’ont pas mis en évidence de régression du changement de cooldown.

## Prochaine étape

Isoler le défaut thermique en comparant la même scène avec et sans Fresh Animations, en gardant EMF, ETF, Iris et les options fixes. Traiter séparément l’erreur de format de profondeur et compléter les cas non vérifiés du montage de rendu avant de modifier ce chemin. Pour la migration des règles, choisir une seule nouvelle règle à extraire et lui donner un test de référence au niveau du serveur. Reporter les décisions de gameplay propres aux deux générations à l’étape qui leur est consacrée.
