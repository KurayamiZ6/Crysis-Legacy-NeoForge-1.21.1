# Les quatre modes de la Nanosuit 1

La branche `feature/nanosuit-1-modes` part de `5f95723`. Le [rapport des modes](RAPPORT-MODES-NANOSUIT.md) établit les règles de départ. Le JAR de référence `build/nanosuit-modes-regression/baseline.jar` porte le SHA-256 `A93BADF95030FA02E3904709BE2288FD6227B3C3696A34CA587A878E2EEBC456`.

## Utilisation et règles

La Nanosuit 1 complète commence en **Armure**, sauf lorsqu’une sauvegarde fournit un mode valide. Dans **Options → Contrôles**, attribuer une touche à **Mode suivant de la Nanosuit 1**. Elle parcourt Armure → Force → Vitesse → Camouflage. Elle est libre par défaut pour laisser le choix d’une touche compatible avec les autres mods. Z et X donnent un accès direct à Armure et Camouflage. Le HUD affiche le mode confirmé par le serveur et le verrou en cas de panne.

| Mode | Comportement de la Nanosuit 1 |
| --- | --- |
| Armure | Protection et absorption existantes. Débit à l’impact et délai de régénération serveur après absorption. Aucun entretien énergétique au repos. Quitter Armure retire sa protection active. |
| Force | Power Jump sur la touche vanilla et Power Melee aux poings ou à la crosse TaCZ. Coûts de 10 et 15 points, charge de saut de quatre ticks et cooldown de huit ticks repris de l’existant. Quitter Force annule une charge et un guidage d’agrippement. L’impulsion et la protection de chute déjà acquises par un saut payé conservent leur effet. |
| Vitesse | Bonus de déplacement normal distinct du boost énergétique. Le sprint vanilla en cours suffit à lancer le boost. La nage sprintée sous l’eau réutilise le bonus ×1,40 et partage le cooldown de sortie de 20 ticks. Quitter Vitesse coupe le boost et le modificateur de marche. |
| Camouflage | Réutilise la discrétion, les perceptions des mobs, les sons, la réfraction et les interactions de tir actuelles. Le tir qui rompt le Camouflage peut laisser le mode neutre en attendant la prochaine sélection. |

Les sélections sont exclusives et partagent le cooldown existant de 20 ticks. Resélectionner le mode courant n’arme pas une nouvelle échéance. Un set incomplet ou mixte ne dispose d’aucun mode de set. La panne bloque les sélections et les pouvoirs jusqu’à 100 %. Une Nanosuit 1 complète revient alors en Armure. La réserve et les échéances acquises appartiennent au joueur. Un échange n’ajoute pas d’énergie. Une Nanosuit 2 peut reprendre un mode Armure ou Camouflage compatible lors d’un échange direct. Force ou Vitesse ne lui sont pas transférés.

La Nanosuit 2 conserve la bascule Z/X, le saut chargé contextuel, le sprint sur un deuxième appui, la nage renforcée, la mêlée à l’impact et les interactions TaCZ existantes. Les aides Nanovision et NanoVisor restent sur les règles communes existantes.

## Paramètres provisoires de la Nanosuit 1

| Clé de configuration serveur | Valeur initiale | Justification |
| --- | --- | --- |
| `nanosuit1.speed.walkBonus` | `0.10` | Multiplicateur additionnel ×1,10 pendant le déplacement normal en Vitesse. Il se compose avec le bonus passif historique. |
| `nanosuit1.speed.sprintDrainPerTick` | `0.75` point/tick | Reprend le débit de sprint historique en mode neutre. Le sprint et la nage Nanosuit 1 le partagent. |
| `nanosuit1.speed.sprintSpeedAmplifier` | `1` | Reprend l’amplificateur vanilla historique, affiché Vitesse II. |
| `energy.regenDelayAfterDrainTicks` | `30` ticks par défaut | Délai existant réutilisé après absorption en Armure Nanosuit 1. |

Armure, Force et Vitesse hors boost suivent la régénération commune après délai. Sans autre débit, le bilan par défaut est de 1,15 point/tick à l’arrêt et 0,95 en déplacement, via le barème de base. Cela évite qu’Armure par défaut vide la réserve d’un joueur immobile. Les paramètres propres à Vitesse laissent indépendants les réglages de la Nanosuit 2. Le test d’autorisation de Force peut aussi servir de garde au futur recul sans nouveau mécanisme aujourd’hui.

## Sauvegarde et protocole

Les identifiants d’objets, l’attachement énergie et les clés NBT `Energy`, `Mode`, `RegenDelayTicks` et `RecoveryTicks` sont conservés. `Mode` reste un nom. Les anciens noms se lisent directement et les modes incompatibles avec l’équipement sont neutralisés côté serveur. Le reset historique des états temporaires au changement de dimension ou à la connexion demeure.

Le protocole Crysis passe de **4 à 5**. Les paquets de demande et de synchronisation conservent leur structure et codent explicitement les modes en VarInt. `NONE=0`, `ARMOR=1`, `CLOAK=2`, `STRENGTH=3`, `SPEED=4`. Les codes inconnus sont rejetés. Utiliser le même JAR v5 sur client et serveur. Le porteur reçoit le mode et la réserve. Les observateurs continuent de recevoir les équipements et les effets par les mécanismes Minecraft existants, dont le drapeau d’invisibilité.

## Validation

Build Gradle hors ligne réussi sous Java 21. Le JAR testé porte le SHA-256 `7E539F11504B70CB63D472A90DB85E47D5EE574D91E283BEE945BAAAA6CE5B40`. Le banc `qa/NanosuitArchitectureQa.java` passe et couvre les noms persistés, les identifiants réseau et les permissions des deux profils.

| Scénario | Résultat et preuve |
| --- | --- |
| Armure initiale et exclusivité | PASS. Le client reçoit Armure et la réserve ne baisse pas au repos. Un impact de 6 HP débite 3 points et laisse la santé intacte. Les quatre sélections, le refus à 19 ticks, le changement à 20 et la resélection sans nouvelle échéance sont observés. Le passage en Force enlève Absorption. |
| Force | PASS. Saut chargé et coup de poing renforcé réservés au mode. Coût 10 et impulsion de saut, ×2 et 15 au poing. Armure refuse la charge et le bonus de mêlée, Vitesse refuse la charge, Force refuse le boost terrestre. Une demande de saut déjà en file est annulée au changement de mode. |
| Vitesse | PASS. Attribut de marche 0,115 → 0,1265 sans entretien. En sprint vanilla, 90 → 84 en huit ticks, sans deuxième demande. La nage passe de ×1,0 à ×1,4 et coûte 6,75 en neuf ticks. Le changement de mode retire le boost, la vitesse et le bonus de nage. |
| Camouflage, panne et échanges | PASS. Camouflage et indicateur invisibilité concordent côté serveur et client. L’échange conserve l’énergie, retire les modes incompatibles et rétablit Armure à l’entrée dans la Nanosuit 1. Les ensembles incomplets et mixtes ne résolvent aucun mode. La panne et la charge partielle résistent aux deux échanges. Armure revient à 100 %. |
| Sauvegarde | PASS. Ouverture d’un monde de référence contenant les quatre objets historiques. Après un redémarrage séparé, le serveur relit l’équipement, Force et la réserve 61. Le client reçoit les mêmes valeurs. |
| Serveur NeoForge dédié et deux clients | PASS. 18 contrôles serveur, huit côté porteur, six côté observateur. Les deux demandes v5 Force et Vitesse partent du client porteur et reviennent confirmées par le serveur. La Nanosuit 2 refuse Force. L’observateur reçoit les deux ensembles et voit le Camouflage se déclencher puis disparaître. |
| Nanosuit 2, énergie et mobilité | PASS. Les 35 traces énergie, Armure, Camouflage, nage et mêlée concordent ligne pour ligne. Les commandes remappées, le premier et le deuxième appui et le cooldown de sprint passent. Les 29 assertions de Power Jump concordent, dont charge, coût, cooldown, limites d’agrippement et obstacles. |
| TaCZ 1.2.0 | PASS. 17 assertions par génération. La Nanosuit 1 complète donne 2 dégâts et zéro débit à la crosse sous Armure, puis 4 dégâts et 15 points sous Force. Deux impacts coûtent 30. Les 17 relevés Nanosuit 2 concordent avec la campagne précédente. Les deux tenues conservent les mesures de tir silencieux/non silencieux et l’exception Predator Bow. |

Le [banc intégré](qa/nanosuit-1-modes/README.md) passe 47 contrôles en jeu et quatre à la réouverture. Les journaux sont sous `build/nanosuit-1-modes-qa/candidate/{play,reopen}.log`. Les captures `n1-mode-strength.png` et `n1-mode-speed.png` dans `game/screenshots/` montrent le libellé du mode. Le contrôle d’exclusivité confirme la disparition de l’effet d’Armure une fois sa transition visuelle passée.

Les journaux dédiés sont sous `build/nanosuit-modes-dedicated/protocol-v5-verified/{server,owner,observer}/console.log`. Le premier passage vérifiait encore deux attentes du banc historique. Il attendait NONE en sortant d’Armure vers Force et imposait une réserve inférieure à 72 après une phase de régénération. Le banc corrigé vérifie Force et la disparition de la protection, ainsi que la réception des modes pendant les phases suivantes.

Les comparaisons Nanosuit 2 utilisent les journaux `build/legacy-functional-qa/nanosuit2-modes-{baseline,candidate}-{core,controls,jump}/console.log`. Le banc sprint relève une différence de 0,75 point sur un instantané côté client, soit un tick du débit historique. Le son de saut arrive au tick client 5 sur la référence et 4 sur le candidat. La demande de saut part au tick 4 dans les deux cas. Les positions exactes d’arrivée sur un rebord peuvent varier.

Le journal TaCZ est `build/legacy-functional-qa/nanosuit-modes-candidate-tacz/console.log`. Les tirs testés consomment 20 sans silencieux et rompent le mode, ou 7 avec silencieux et conservent le mode. Le Predator camouflé conserve sa réserve et inflige 37,5 dégâts de projectile avec set. Les bancs utilisent de vrais objets et les points d’entrée serveur. Le banc TaCZ déclenche les appels de l’opérateur d’arme et les impacts dans le monde.

### Limites de couverture

Aucune régression observée dans les scénarios exécutés. La comparaison des JARs confirme l’identité octet pour octet de 374 entrées contrôlées, notamment les modèles, textures, shaders, sons, registre d’objets, code NBT, mixins gameplay et composants Nanovision/NanoVisor. La campagne réutilise les validations graphiques de `5f95723`. Les nouveaux libellés du HUD sont vérifiés sans shader. Le nouveau raccourci de cycle n’a pas été actionné par entrée physique, les sélections réseau ont été exercées par les paquets. Un agrippement complet en Force sur la Nanosuit 1, un pair client v4 sur le serveur v5 et le modpack principal complet n’ont pas été rejoués. Le moteur d’agrippement partagé est couvert sur la Nanosuit 2.

## Décisions restant à prendre

- Ajuster en jeu le bonus de marche, la vitesse et le débit de sprint/nage, ainsi que le bilan de régénération en Armure.
- Fixer les règles de tir propres à la Nanosuit 1, dont le silencieux et le Predator Bow. Les coûts et exceptions hérités servent de référence pour ce lot.
- Valider le raccourci définitif et le futur menu radial. Le cycle actuel, les accès directs et le HUD local utilisent les contrôles et la composition existants.
- Définir la politique de cooldown après portail, reconnexion et mort avant une évolution de ces échéances. Le comportement historique y reste la référence.

## Fichiers et Git

| Commit | Fichiers |
| --- | --- |
| `7e3d5ba` | Dans `nanosuit/`, `NanosuitMode.java`, `NanosuitProfile.java`, `rules/NanosuitControls.java`, `handler/NanosuitPlayerHandler.java`, `handler/NanosuitPowerMelee.java`, `config/NanosuitServerConfig.java` et `network/{NanosuitNetwork,SyncNanosuitDataPacket,ToggleNanosuitModePacket}.java`. Test autonome `qa/NanosuitArchitectureQa.java`. |
| `4c31547` | `nanosuit/client/{NanosuitKeyMappings,NanosuitHudOverlay}.java`, `nanosuit/command/NanosuitDebugCommands.java`, ressources `assets/crysismod/lang/{en_us,fr_fr,ru_ru}.json`. |
| Validation | Ce rapport et `qa/nanosuit-1-modes/{Nanosuit1ModesQa.java,run.ps1,README.md,META-INF/neoforge.mods.toml}`. |

Les chemins Java sont sous `src/main/java/io/github/painworks/crysismod/` et les ressources de langue sous `src/main/resources/`. La liste exacte est disponible avec `git diff --name-status 5f95723 HEAD`. `main` pointe toujours sur `39738b1`.
