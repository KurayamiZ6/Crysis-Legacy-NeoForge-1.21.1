# Validation des transitions multi-Nanosuit

## Référence et périmètre

La mission part de `6cffe6ac91ee9f94d5ccb52f854de79401101084` sur `refactor/multi-nanosuit`. Le JAR de référence est `build/multi-suit-reference/candidate.jar`, SHA-256 `FF06F30592250D907EC665B5E58FC5C40DAB84D677F363DD40CBA35BCB3EA918`. Le [rapport de refactorisation](RAPPORT-MULTI-NANOSUIT.md) contient l’audit détaillé, les résultats historiques et les captures. Cette campagne porte sur la sélection du profil et la continuité de l’état joueur.

## Sélection, propriété et synchronisation des états

| État | Source actuelle | Conséquence pour les transitions |
| --- | --- | --- |
| Génération équipée | `NanosuitArmorUtil` identifie les quatre objets par emplacement. `NanosuitGeneration.fullSet` exige une génération commune, puis `NanosuitProfile.forGeneration` cherche une règle active. | Une pièce manquante ou un set mixte refuse les pouvoirs de set. La Nanovision peut fonctionner avec le casque seul selon la configuration. Les quatre objets historiques restent associés à `LEGACY_CURRENT`. |
| Règles de gameplay | `NanosuitProfile` compose les contrôles et paramètres par domaine. Les valeurs actuelles sont partagées par tous les porteurs du profil. | Les règles pourraient être propres à la génération. Les objets portent l’identité et la présentation. Un futur module pourrait appartenir à une pièce. |
| Énergie, mode et panne | `NanosuitEnergy`, attachement NeoForge du joueur, persisté avec `Energy`, `Mode`, `RegenDelayTicks` et `RecoveryTicks`. À zéro, les pouvoirs sont verrouillés jusqu’à 100 %. | Le retrait et la remise d’une pièce réutilisent la même réserve. Un profil n’obtient aucune réserve supplémentaire. Sans set, la régénération historique suit son propre barème et le délai déjà acquis. |
| Cooldowns, actions en cours et soin | `NanosuitPlayerHandler.PlayerRuntimeState`, indexé par UUID. Échéances par tick pour Armure/Camouflage, saut et sprint/nage. Charge, agrippement, soin, rétention des mouvements et visibilité appliquée sont temporaires. | Un changement d’équipement conserve les échéances. Le retrait d’une pièce coupe au tick serveur les pouvoirs du set, puis le soin recommence son délai au rééquipement. Le reset de cycle de vie au portail, à la reconnexion et au respawn est distinct de la permutation de profil. |
| Client propriétaire | `NanosuitSyncManager` et `SyncNanosuitDataPacket` en v4 transmettent énergie, mode, boost prêt et verrou. Les retours de pouvoir et les impulsions de saut et d’agrippement ont leurs paquets existants. | Les capacités sont validées côté serveur. Le client déduit les contrôles disponibles à partir de l’équipement et reçoit les changements d’état. Il ne reçoit ni échéances ni génération de la part du paquet d’énergie. |
| Observateurs | Équipement et invisibilité vanilla, effets et déplacement de l’entité. Le client propriétaire reçoit seul les paquets d’énergie et de commandes. | Les pièces doivent suffire pour résoudre le modèle à distance. Un effet propre à un nouveau mode que les métadonnées ne distinguent pas nécessitera une synchronisation de cet effet. La consommation d’énergie individuelle n’a pas à être diffusée. |

## Risques sur la référence

- **Mode interdit conservé.** La demande d’activation consulte les contrôles du profil, alors que le tick, l’absorption et le camouflage supposent qu’un set complet suffit une fois le mode actif. Un passage direct peut donc maintenir l’invisibilité, l’absorption ou le drain. Le retour au premier profil peut ensuite rétablir un pouvoir resté actif.
- **Récupération accélérable.** Le compteur sauvegardé représente un avancement et le tick de référence l’interprète selon la combinaison portée à cet instant. Une autre durée peut avancer la charge, voire atteindre immédiatement 100 %. L’origine de la panne est absente du NBT.
- **Charge et mouvement hérités.** Le Power Jump vérifie l’instance des règles de saut. Deux profils partageant cette instance peuvent reprendre la même charge. L’agrippement vérifie un set complet et peut continuer avec un profil qui refuse le saut.
- **Boost et échéance raccourcis.** Le cooldown sprint/nage est calculé à la fin du boost avec les règles du profil alors équipé. Une génération à cooldown plus court peut donc raccourcir une action commencée avec l’autre. Un boost prêt peut aussi être transporté vers un profil compatible.
- **Cycle de vie.** Le code historique remet les échéances temporaires à zéro au changement de dimension et à la reconnexion. La panne appartient à l’attachement persistant. Un portail ou une reconnexion doit être traité comme une décision distincte avant d’activer de vraies générations.

## Garde-fous

- `NanosuitPlayerHandler` annule un mode interdit avant sa dépense d’énergie. L’absorption d’Armure et les règles serveur de Camouflage consultent immédiatement le profil courant, y compris pour un impact avant le prochain tick du joueur. La visibilité et les effets sont mis à jour dans le tick normal. Un mode compatible des deux côtés reste actif selon la règle actuelle.
- La charge de saut et l’agrippement appartiennent au profil qui les a commencés. Un changement de profil annule la demande ou le guidage, même si les règles sont partagées. L’impulsion déjà appliquée, la protection de chute acquise par le saut payé et le cooldown en cours gardent leur effet historique.
- L’autorisation du sprint et le boost actif appartiennent à leur profil de départ. Un changement de profil coupe le boost, retire le bonus de nage et calcule le cooldown de sortie avec les règles qui s’appliquaient au début de l’action. Le Power Melee résout déjà le profil à chaque impact.
- La récupération garde provisoirement la cadence historique commune. Un échange ne peut ainsi pas interpréter autrement le compteur NBT, même après rechargement du monde. Les futures cadences distinctes demanderont une décision sur leur représentation persistante.

## Vérification du JAR de production

Le build Gradle hors ligne réussit sous Java 21. Le JAR a pour SHA-256 `E86E95CF3BDA8438FFE764278879039567E1FB6DFB310D74396D51218A2747B2`. La comparaison des 798 entrées de chaque archive identifie six classes modifiées dans les deux handlers de cette mission. Le registre, les ressources, les classes de persistance et les paquets v4 sont identiques octet pour octet. Le banc `qa/NanosuitArchitectureQa.java` passe et confirme que les identités futures n’ont aucun profil actif dans le build normal.

Les bancs Minecraft historiques passent aussi sur le JAR normal. Les 35 traces de l’énergie, de la panne, du ciblage, du Power Swim et de la mêlée sont identiques à la référence, avec Zombie Awareness et CoroUtil chargés. Les 29 assertions du Power Jump concordent, dont la demande client et l’exécution serveur à quatre ticks, le coût de 10, le cooldown à sept et huit ticks et les rebords. Le délai jusqu’à l’impulsion mesuré sur l’horloge client varie d’un tick, comme le délai audio documenté pendant la refactorisation. Le banc de commandes passe ses neuf assertions et confirme le premier appui de sprint vanilla, le second appui boosté, le cooldown et le raccourci de saut remappé. Les preuves sont sous `build/legacy-functional-qa/multi-suit-transition-{core,controls}/console.log` et `build/consolidation-jump-qa/multi-suit-transition-legacy/console.log`.

## Matrice de validation

Le [banc de transition](qa/transition/README.md) charge une copie isolée du JAR. Un profil de test partage les règles historiques et un second interdit les pouvoirs tout en proposant des durées artificielles. Le thread serveur sélectionne le profil par UUID. Le propriétaire reçoit les vrais paquets d’état v4 et les métadonnées d’entité. Les deux côtés logiques du client intégré partagent le résolveur de test, ce qui ne démontre pas un transport de l’identité de génération. Les pièces historiques restent équipées, sauf pour le retrait réel du casque pendant le test du soin.

La référence donne 24 réussites et huit échecs. Le JAR corrigé passe les **32 assertions**. Les journaux comparables sont `build/transition-qa/{reference,candidate}/transition.log`.

| Nº | Transition | Référence | Après correction, serveur et client |
| --- | --- | --- | --- |
| 1 | Profil à 100 % | PASS | Même réserve à 100 et mode neutre sur les deux côtés. |
| 2 | Énergie partielle | PASS | Réserve à 44 conservée à délai de recharge contrôlé. Aucun remplissage attribuable à l’échange. |
| 3 | Panne et charge partielle | FAIL | Le profil court faisait sauter la panne dans la référence. Le compteur progresse désormais au rythme historique et les deux côtés restent verrouillés pendant la recharge jusqu’à 100. |
| 4 | Cooldown Armure/Camouflage | PASS | Refus à 19 ticks et acceptation à 20 après aller-retour de profil, décision côté serveur. |
| 5 | Camouflage actif | FAIL | Le mode interdit passe à NONE et l’invisibilité disparaît côté serveur et propriétaire. Les deux états concordent après réception du paquet. |
| 6 | Armure active | FAIL | Le mode et l’effet Absorption sont retirés sur les deux côtés. La règle d’absorption refuse aussi un profil incompatible avant le tick de nettoyage. |
| 7 | Préparation du Power Jump | FAIL | Un profil différent avec exactement les mêmes règles ne reprend plus la charge. Aucun débit ou déplacement induit sur le serveur ou le client. |
| 8 | Agrippement engagé | FAIL | Le guidage réel a été amorcé, puis son plan est annulé. L’énergie reste à 90 sur les deux côtés, après le coût du saut déjà parti. |
| 9 | Sprint et nage | FAIL partiel | La référence coupe les bonus, mais le profil de destination raccourcit le cooldown de sprint. Le correctif retire les bonus, conserve le délai d’origine et refuse le sprint à 19 ticks, puis l’autorise à 20. La vitesse de nage observée revient de 1,4 à 1,0 côté serveur et client. |
| 10 | Retrait pendant le soin | PASS | Retrait réel du casque, annulation du délai de soin. Au rééquipement, le premier point de vie arrive après un nouveau délai, sans crédit anticipé. La santé concorde sur le client. |
| 11 | Retour au profil précédent | PASS | Identité historique résolue, énergie conservée et accès à nouveau soumis au cooldown et au verrou. La mêlée d’un profil qui l’interdit reste vanilla côté serveur. |
| 12 | Dimension, fermeture et réouverture | PASS avec limite historique | Aller-retour réel Nether/Overworld et sauvegarde sur disque. Le verrou subsiste au changement de dimension, puis le même compteur est relu à la connexion. Le client reçoit le verrou dès le chargement et revient à 100 % avec le serveur. Le cooldown de mode est conservé par l’échange, puis réinitialisé au changement de dimension, comme sur la référence. |

Le niveau d’énergie reçu peut refléter le tick précédent pendant une consommation ou une charge, comme dans les tests historiques. Le verdict porte sur les conditions de décision côté serveur et la convergence des états répliqués.

Le [lanceur du cycle de vie](qa/transition-lifecycle/run.ps1) réutilise le monde isolé pour deux processus successifs. Exemple avec un nouveau nom de rejeu après avoir lancé le banc de transition.

```powershell
./qa/transition-lifecycle/run.ps1 -RunName candidate-replay -UseTransitionOverlay `
    -ModJar build/transition-qa/candidate/mods/crysis-legacy-transition-overlay.jar
```

Les deux phases passent sur la référence avec l’équipement historique et sur le candidat avec les profils de test. Les journaux `build/transition-lifecycle-qa/{legacy-checked,candidate-checked}/{prepare,reopen}.log` comparent le compteur à la sortie et à la reprise. Il vaut 55 puis 55 sur la référence, et 54 puis 54 sur le candidat. Le profil de test en mémoire disparaît au redémarrage et les pièces équipées résolvent `LEGACY_CURRENT`. Le client observe le verrou dès l’écran de chargement. Un premier essai cherchait uniquement l’énergie zéro une fois l’écran fermé et ratait ce paquet. La mesure corrigée couvre aussi le chargement et la recharge partielle.

### Limites de couverture

- Le banc remplace le profil résolu. L’équipement de deux nouvelles tenues, leur identité reçue par un autre processus client et leur rendu distant restent à valider avec les vrais objets.
- Le serveur intégré exécute les handlers de gameplay et le réseau local. Le serveur dédié et un observateur distant n’ont pas été rejoués pour les profils de test.
- Le rejeu après reconnexion concerne une panne persistée. Le cooldown armé seul à la reconnexion et la mort n’ont pas fait l’objet d’un essai en jeu. La réinitialisation à la connexion apparaît dans le handler historique.
- Le rendu TaCZ, Fresh Animations et les shaders n’ont pas été rejoués. Les classes et ressources de rendu sont identiques aux archives de la campagne précédente. Le banc valide le contrôle serveur du camouflage lors de la transition. Les relevés de tir TaCZ restent ceux de la campagne précédente.
- La politique du soin lors d’un passage direct entre deux profils compatibles mais aux soins différents reste à définir. Le banc couvre le retrait du set et le retour avec les règles historiques.

## Critères pour les deux générations équipables

1. Définir les quatre modes manuels de la Nanosuit 1 et les pouvoirs contextuels de la Nanosuit 2. Fixer en particulier la continuité d’un mode autorisé par les deux profils, la politique des dégâts de chute après un saut payé, les soins et les interactions entre boost, Armure et Camouflage.
2. Réserver huit identifiants distincts, par exemple `crysismod:nanosuit_1_helmet/chestplate/leggings/boots` et `crysismod:nanosuit_2_helmet/chestplate/leggings/boots`, avec un préfixe explicite par génération dans le registre. Les objets `crysismod:nanogeo_nanosuit_*` gardent leur identité et leur profil historique. Réutiliser un modèle provisoire via le descripteur de présentation, en conservant une entrée de résolution par génération et par emplacement.
3. Exiger les quatre pièces de même génération pour les pouvoirs de set et valider les ensembles mixtes. Traiter séparément le casque si la Nanovision le permet. Tester les deux équipements avec des profils provisoires maîtrisés avant de figer leurs valeurs de gameplay.
4. Choisir si les échéances survivent au portail, à la reconnexion et à la mort. Si une récupération devient propre à la génération, définir une règle ou une échéance figée au début de la panne et la conserver sous une forme versionnée. Prévoir la lecture des sauvegardes actuelles avec la chronologie historique. Vérifier qu’un échange, un retrait ou un redémarrage ne rend jamais une capacité disponible plus tôt.
5. Rejouer l’équipement réel, les événements de cycle de vie et les états distants sur serveur dédié avec deux clients. Introduire des identifiants réseau stables si Strength et Speed ou des états visuels supplémentaires doivent être transportés. Tester la lisibilité du modèle par les observateurs et les transitions côté propriétaire.

## Fichiers de la mission

| Fichiers | Modification |
| --- | --- |
| `src/main/java/io/github/painworks/crysismod/nanosuit/handler/NanosuitPlayerHandler.java` | Garde-fous sur les modes, la récupération et les pouvoirs de déplacement. |
| `src/main/java/io/github/painworks/crysismod/nanosuit/handler/NanosuitCloak.java` | Contrôle de l’autorisation serveur du profil courant. |
| `qa/transition/TransitionQa.java`, `qa/transition/run.ps1`, `qa/transition/META-INF/neoforge.mods.toml`, `qa/transition/README.md` | Banc différentiel, lanceur isolé et mode d’emploi. |
| `qa/transition-lifecycle/LifecycleQa.java`, `qa/transition-lifecycle/run.ps1`, `qa/transition-lifecycle/META-INF/neoforge.mods.toml` | Banc de changement de dimension, fermeture et réouverture du monde. |
| `RAPPORT-TRANSITIONS-NANOSUIT.md` | Résultats, limites et plan d’activation. |

Le correctif de production est isolé dans `8ae7fb3`. La liste Git exacte est disponible avec `git diff --name-status 6cffe6a HEAD`.
