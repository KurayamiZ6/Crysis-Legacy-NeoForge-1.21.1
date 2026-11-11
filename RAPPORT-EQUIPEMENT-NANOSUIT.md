# Deux générations de Nanosuit équipables

La mission part de `29ae988` sur `refactor/multi-nanosuit`. Le [rapport de transition](RAPPORT-TRANSITIONS-NANOSUIT.md) contient les résultats du banc précédent et la limite historique des cooldowns lors d’un changement de dimension. Le JAR de départ est archivé sous `build/equipment-reference/baseline-29ae988.jar` avec le SHA-256 `E86E95CF3BDA8438FFE764278879039567E1FB6DFB310D74396D51218A2747B2`.

## Équipement et résolution

| Emplacement | Nanosuit 1, objet historique | Nanosuit 2, nouvel objet |
| --- | --- | --- |
| Tête | `crysismod:nanogeo_nanosuit_helmet` | `crysismod:nanosuit_2_helmet` |
| Torse | `crysismod:nanogeo_nanosuit_chestplate` | `crysismod:nanosuit_2_chestplate` |
| Jambes | `crysismod:nanogeo_nanosuit_leggings` | `crysismod:nanosuit_2_leggings` |
| Pieds | `crysismod:nanogeo_nanosuit_boots` | `crysismod:nanosuit_2_boots` |

Les quatre objets historiques résolvent `NANOSUIT_1` et conservent leurs identifiants et leur définition d’armure. Les objets `nanosuit_2_*` résolvent `NANOSUIT_2` et se trouvent dans le même onglet créatif. Les noms distinguent les générations. Les deux profils partagent les règles de `LEGACY_CURRENT`, le modèle NanoGeo et les textures. Le registre Minecraft et la synchronisation vanilla des emplacements donnent à chaque client l’identité des pièces. Le protocole Crysis v4 continue de transmettre l’état gameplay au propriétaire.

Chaque emplacement valide sa pièce. Les pouvoirs de set exigent quatre pièces d’une même génération. Les pièces isolées restent visibles et la règle historique de Nanovision par casque reste gouvernée par sa configuration. La réserve, le mode, la panne et les cooldowns restent associés au joueur.

Le remplacement du set entier dans un même tick permet au Camouflage compatible de rester actif. Si le serveur observe un set incomplet ou mixte pendant un remplacement pièce par pièce, il applique la coupure historique. Les actions temporaires comme la charge de saut ou le boost restent liées au profil qui les a déclenchées.

## Contrats vérifiés par le build

Le build Gradle hors ligne sous Java 21 réussit. Le banc `qa/NanosuitArchitectureQa.java` vérifie les deux identités et l’identité des règles partagées avec `LEGACY_CURRENT`. Le JAR candidat porte le SHA-256 `A93BADF95030FA02E3904709BE2288FD6227B3C3696A34CA587A878E2EEBC456`.

La comparaison des archives passe de 798 à 802 entrées. Les quatre modèles d’inventaire ajoutés héritent des modèles historiques. Les deux traductions et les classes de registre, d’identité et de sélection de rendu sont les autres différences. Les textures, la géométrie, les shaders, les sons, les recettes, la sérialisation NBT, les paquets v4 et les classes des handlers de gameplay sont identiques octet pour octet.

## Validation en client intégré

Le [banc d’équipement](qa/equipment/README.md) utilise les objets du registre et les emplacements d’armure réels. Il appelle les handlers côté serveur et observe les états reçus par le client. Le candidat passe 30 assertions en jeu, puis sept à la réouverture. Les journaux sont sous `build/equipment-qa/candidate/{play,reopen}.log`.

| Scénario | Résultat |
| --- | --- |
| Sets complets Nanosuit 1 et 2 | Les quatre identifiants et le profil correspondant concordent côté serveur et client. |
| Pièce retirée ou pièces mélangées | Le profil de set complet disparaît et le serveur refuse le mode de set. |
| Échange dans les deux sens | Le serveur et le client résolvent le nouveau profil. L’échange direct pendant le Camouflage conserve l’énergie et le mode. Un tick avec un set mixte l’interrompt et le retour au set complet ne le réactive pas. |
| Panne énergétique | Le verrou survit à l’échange et à la charge partielle. Il se lève à 100 % côté serveur et client. |
| Cooldowns | Le changement de mode et la reprise du sprint restent refusés à 19 ticks et autorisés à 20. |
| Charge de Power Jump | L’échange annule la charge sans débit ni saut différé. Une nouvelle charge permet un saut payé de 10 points. |
| Sprint et nage renforcés | Le changement de tenue coupe le boost de sprint et le bonus de nage. |
| Sauvegarde et reconnexion | Une copie d’un monde enregistré avec l’ancien JAR s’ouvre avec les quatre pièces historiques équipées. Après équipement de la Nanosuit 2 et redémarrage du processus Minecraft, les quatre pièces équipées et leur usure, les quatre objets historiques rangés dans l’inventaire et les 61 points d’énergie du joueur sont préservés. Le client reçoit le même profil et la même réserve. |
| Rendu première et troisième personne | Le bras de chaque tenue en première personne et son modèle d’armure en troisième personne ont été vérifiés visuellement, sans shader. Les quatre images sont sous `build/equipment-qa/captures/game/screenshots/`. Le rejeu de capture attend une image rendue et remplace une première capture Nanosuit 1 prise trop tôt. |

## Validation sur serveur dédié

Le [banc dédié](qa/equipment-dedicated/README.md) lance NeoForge et deux processus client distincts sur la boucle locale. Le candidat passe 14 contrôles serveur, six côté porteur et six côté observateur. Les trois processus terminent avec zéro échec. Les preuves sont dans `build/equipment-dedicated-qa/candidate-observed/{server,owner,observer}/console.log`.

- **Serveur.** Les deux joueurs se connectent au serveur dédié. Le porteur équipe les deux générations et conserve ses 72 points lors du premier échange. Le Camouflage se déclenche, débite la réserve, survit à l’échange inverse direct, puis se désactive. Après un nouvel échange, le mode Armure est refusé à 19 ticks et accepté à 20, puis se désactive.
- **Porteur.** Le client reçoit les quatre objets, le bon profil, l’énergie et les modes attendus. Il observe la réserve stable à 72 avant activation et le drain après activation.
- **Observateur.** Le second client reçoit les quatre identifiants de chaque génération et résout le profil correspondant. Il observe le Camouflage sur les deux générations et le retour visible. Les captures `dedicated-equipment-{0,1,2}.png` sous `observer/screenshots/` montrent les deux sets visibles et la réfraction de la Nanosuit 2.

Les premières tentatives ont détecté une option dupliquée dans la configuration des clients du banc et une assertion qui imposait exactement quatre blocs de distance. Le test final garde les vérifications d’équipement et de visibilité pendant que la distance varie. Il capture après le rendu et centre l’observateur sur le porteur. Ces corrections concernent uniquement les bancs. Aucune régression de production n’a été observée dans les scénarios couverts.

## Préparer une réserve par combinaison

Il faudra d’abord décider ce qui appartient à la tenue et ce qui limite le joueur. Une réserve, son avancement de recharge et l’identité de la tenue devraient avoir une source unique côté serveur. La règle d’appartenance devra couvrir les pièces isolées ou mélangées, l’échange entre joueurs, la mort, la déconnexion et la copie en créatif. Le format actuel stocke l’énergie dans l’attachement du joueur. Une migration devra attribuer cette valeur une seule fois, gérer les pièces déjà en circulation et définir le sort des cooldowns acquis avant échange. Les tests devront notamment empêcher de remplir ou dupliquer la réserve par échange, copie, réparation ou rechargement d’une sauvegarde.

## Git et périmètre

Le commit `aa7b65d` contient les objets, les profils et la sélection de leur présentation. Les bancs et ce rapport sont réunis dans le commit suivant sur `refactor/multi-nanosuit`. `main` pointe toujours sur `39738b1`.

Cette campagne réutilise les précédents essais TaCZ, Fresh Animations et shaders. Les nouveaux équipements ont été vérifiés avec le rendu standard. Les combinaisons de mods facultatifs et le modpack principal complet restent hors de cette campagne.
