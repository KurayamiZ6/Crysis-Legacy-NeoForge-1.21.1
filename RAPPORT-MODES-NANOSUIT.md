# Compatibilités des équipements et spécification des modes

## Référence

Branche `refactor/multi-nanosuit`, référence `c8258de`. JAR testé `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`, SHA-256 `A93BADF95030FA02E3904709BE2288FD6227B3C3696A34CA587A878E2EEBC456`. Les deux équipements partagent les règles historiques. Le [rapport d’équipement](RAPPORT-EQUIPEMENT-NANOSUIT.md) couvre le registre, la sauvegarde, les transitions et le serveur dédié. La [matrice de refactorisation](RAPPORT-MULTI-NANOSUIT.md) décrit les paramètres, les déclencheurs et les tests de chaque mécanisme actuel. Le présent document propose une spécification. Ses pistes restent soumises à validation.

Le manuel PC de Crysis présente une sélection parmi Armure, Force, Vitesse et Camouflage. Il décrit une marche accélérée sans débit en Vitesse, un sprint coûteux, la dépense de Force lors des actions, l’absorption de dégâts par la réserve en Armure et un Camouflage qui coûte davantage en mouvement. Il décrit aussi un tir qui vide la réserve sous Camouflage, un appareil respiratoire temporaire et une vision nocturne à réserve séparée. Ces points fournissent une référence de conception pour la Nanosuit 1. [Manuel PC Electronic Arts, pages 10–11](https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/17300/manuals/manual_english.pdf?t=1736347865)

## Matrice des essais de compatibilité

### TaCZ 1.2.0

Le banc réutilise le profil isolé TaCZ et le gunpack Mobius. Il équipe quatre objets du registre et vérifie le profil résolu pour chaque génération. Il déclenche les appels de tir et de crosse de TaCZ, contrôle les projectiles créés et mesure les dégâts aux cibles dans le monde. Les 17 assertions passent pour chaque génération. Les 16 relevés de gameplay concordent. Le journal est `build/legacy-functional-qa/equipment-compat-tacz/console.log`. Les sources et le JAR du banc adapté se trouvent sous `build/equipment-compat-tacz/`.

| Essai | Nanosuit 1 | Nanosuit 2 | Résultat observé |
| --- | --- | --- | --- |
| M4A1 sous Camouflage, sans silencieux | PASS | PASS | Tir réussi, un projectile, énergie 100 → 80 et mode NONE. |
| M4A1 sous Camouflage, avec silencieux | PASS | PASS | Tir réussi, un projectile, énergie 100 → 93 et mode CLOAK. |
| Predator Bow `sfms:tb23` | PASS | PASS | Dégâts du projectile 25 → 37,5 avec le set, en mode neutre et en Camouflage. Le tir camouflé laisse 100 et CLOAK. |
| Power Melee à la crosse | PASS | PASS | Dégâts 2 → 4 pour 15 points, seuil insuffisant à 14 et cible invulnérable sans débit. Deux cibles prennent chacune 4 pour un débit total de 30. |
| Poings et rupture après dégâts | PASS | PASS | Poing 1 → 2 pour 15 points. La source environnementale testée conserve le mode. Un coup de mob met l’énergie à 0, neutralise le mode et verrouille la combinaison. |

La campagne couvre les appels gameplay réels et leur résultat serveur. La saisie clavier TaCZ et les animations de rechargement ne sont pas exercées dans ce banc.

### Punchy, Fresh Animations et shaders

Le [banc de rendu](qa/equipment-compat/render/README.md) réutilise la scène et les réglages de consolidation. Il vérifie le vrai profil équipé, la caméra et les états actifs au moment de chaque capture. Configuration testée avec Punchy 3.2, Fresh Animations 1.11.2, EMF 3.3.12, ETF 7.2.7, Iris 1.8.14-beta.1, Sodium 0.8.13, Complementary Reimagined r5.9.3, Euphoria Patches 1.10.5 et TaCZ 1.2.0.

| Scène | Nanosuit 1 | Nanosuit 2 | Observation commune |
| --- | --- | --- | --- |
| Punchy en première personne | PASS | PASS | Bras d’armure visible pendant le mouvement de pioche. |
| Nanovision en première personne | PASS | PASS | Bras, outil et entités colorés. Les traces Fresh/EMF acceptent 612 sommets sur trois entités dans chaque mesure et ne signalent aucun sommet non sûr. |
| Occlusion Fresh/EMF/ETF | PASS | PASS | La vache apparaît en thermique à découvert et disparaît derrière le mur. Le zombie et le squelette restent visibles. |
| Nanovision en troisième personne | PASS | PASS | La silhouette équipée et les entités visibles sont colorées. |
| Nanovision et viseur holographique TaCZ 552 | PASS | PASS | Visée à 1,00. La fenêtre laisse voir la cible thermique. |
| Nanovision et Camouflage simultanés en première personne | PASS | PASS | Le Camouflage reste actif et le thermique garde la priorité visuelle sur le bras et l’outil. |
| Camouflage seul en première personne | PASS | PASS | Réfraction sur le bras et l’objet tenu. |
| Camouflage seul en troisième personne | PASS | PASS | Silhouette réfractée du corps équipé. |

Les 16 captures ont été examinées. Le client termine normalement. Journal `build/equipment-compat-render/c8258de/console.log`, traces EMF aux lignes 1071 et 3790, marqueur de fin à la ligne 6198. Captures `build/equipment-compat-render/c8258de/screenshots/`.

### Anomalies préexistantes et couverture

| Catégorie | Résultat |
| --- | --- |
| Régression observée | Aucune sur les scènes et mesures couvertes. |
| OpenGL préexistant | `GL_INVALID_OPERATION … Depth formats do not match`, 1 864 occurrences dans ce journal. La même signature figure 1 095 fois dans `run/qa-multi-suit-render/console.log`. Les séquences et le nombre d’images diffèrent, donc les totaux ne mesurent pas une variation de fréquence. Aucun `Missing elements in vertex` ni crash de rendu observé. |
| Alerte de performance préexistante | Punchy signale un callback hors de son budget sur le candidat et sur la référence. Les traces de ce banc ne mesurent pas la performance. |
| Hors rejeu ciblé | Autres viseurs, rendu des armes TaCZ sous Camouflage, rendu sans shader pour ces scènes, autres packs et extensions Fresh, Camouflage et Nanovision combinés en troisième personne, modpack principal complet. Le rendu standard en première et troisième personne a été validé pour les deux sets dans la campagne d’équipement. |

## Matrice fonctionnelle

La colonne Nanosuit 2 constitue le contrat à préserver. Les valeurs chiffrées y décrivent le comportement actuel ou ses valeurs par défaut. La colonne Nanosuit 1 est une proposition. Le plan ne fixe aucune nouvelle valeur.

| Mode Nanosuit 1 | Protection active | Camouflage | Saut et mêlée renforcés | Sprint et nage renforcés |
| --- | --- | --- | --- | --- |
| Armure | Oui | — | — | — |
| Force | — | — | Oui | — |
| Vitesse | — | — | — | Sprint, nage à valider |
| Camouflage | — | Oui | — | — |

Chaque « Oui » reste conditionné par l’équipement, l’énergie et les gardes propres à l’action. Les aides de visière et de respiration suivent leur propre règle. Sur la Nanosuit 2, les pouvoirs contextuels restent éligibles avec un mode Armure ou Camouflage actif.

### Sélection, énergie et mobilité

| Fonction | Nanosuit 2 · comportement actuel à conserver | Nanosuit 1 · proposition et décisions |
| --- | --- | --- |
| Équipement | Set homogène complet pour les pouvoirs. Règle de casque configurable pour la Nanovision. | Même résolution. Les quatre IDs historiques restent ceux de la Nanosuit 1. Un set mixte ou incomplet reste sans mode de set. |
| Modes manuels | Z et X basculent Armure et Camouflage. Ils sont exclusifs entre eux. NONE est disponible. | Armure, Force, Vitesse et Camouflage sont quatre choix manuels exclusifs. La sélection répétée pourrait être idempotente. Décider du mode initial, du rôle de NONE et du comportement à la fin d’une panne. |
| Cooldown de sélection | 20 ticks partagés Armure/Camouflage après une transition effective. Le set et le verrou sont validés par le serveur. | Étendre l’autorité et les garde-fous existants aux quatre modes. Valider la durée, le refus d’une sélection pendant une panne et le sort du cooldown à la dimension, reconnexion et mort. |
| Réserve, délai, régénération | Réserve joueur 0–100. Le barème dépend des actions, des délais et de la config. Les débits ponctuels réutilisent le délai existant. Le créatif suit son régime historique. | Garder la même réserve et la logique commune. Établir le débit par action et la régénération dans chaque mode, y compris à l’arrêt, en mouvement et en créatif. |
| Panne, charge et alertes | À 0, neutralisation et verrou jusqu’à 100. Chronologie commune, charge dès t81 et restauration à t122 du compteur. Alertes et gains historiques. | Conserver la chronologie commune au premier lot. Si une cadence spécifique est approuvée plus tard, figer la règle à la mise en panne et persister sa référence ou son échéance. |
| Soin passif | Avec set, énergie positive, hors panne, premier soin après délai de 80 ticks puis 1 HP par 60 ticks, sans débit. | Décider si le soin est commun, réservé à Armure ou soumis à une autre condition. Préciser le délai lors d’un changement de mode. |
| Vitesse passive et marche | Modificateur historique de mouvement, par défaut +15 %, présent même sans set. | Viser une marche renforcée en mode Vitesse et gratuite. Décider comment composer ce bonus avec l’attribut historique sans le modifier globalement, le doubler ou le laisser après sortie de Vitesse. |
| Power Sprint | Second appui sur la touche sprint vanilla pendant un sprint en cours. Débit par défaut de 0,75 point/tick en NONE. Le mode actif peut appliquer son propre barème. Cooldown de sortie de 20 ticks partagé avec la nage. | Autoriser le boost seulement en Vitesse. Choisir entre le sprint vanilla déclencheur direct dans ce mode et le double appui actuel. Conserver le sprint normal dans les autres modes. |
| Power Swim | Nage sprintée sous l’eau, contextualisée sans second appui. Attribut ×1,40 par défaut et cooldown partagé. | Option à valider sous Vitesse. Décider du sprint sous l’eau, du barème et de la transition terre/eau. Retirer l’attribut dès la perte d’autorisation. |
| Respiration | Réserve d’air rétablie sous l’eau avec set complet, y compris en panne. | Fonction d’assistance indépendante du mode. Décider si la durée illimitée de Minecraft est conservée ou si une réserve d’air distincte devient nécessaire. |
| Power Jump | Charge sur la touche de saut vanilla, maintien 4 ticks, délai 8, coût 10. Appui bref, charge annulable et validation serveur. | Réserver le saut renforcé à Force. Choisir maintien ou impulsion directe dans ce mode. En dehors de Force, préserver le saut vanilla sans retard de charge. |
| Impulsion et agrippement | Impulsion serveur, guidage aux rebords avec collisions, synchronisation, protection de chute acquise après lancement. | Réutiliser l’impulsion et les collisions. Annuler charge et guidage si Force n’est plus autorisé. Décider de la protection après une impulsion déjà payée et du lien éventuel avec Armure. |
| Retour de mobilité | Boucle et effet de sprint/nage, son du saut, flash et indicateur de force contextuel. | Distinguer mode sélectionné et capacité effectivement déclenchée. Valider les sons de sélection et le maintien des retours de mouvement. |

### Combat, camouflage et interactions

| Fonction | Nanosuit 2 · comportement actuel à conserver | Nanosuit 1 · proposition et décisions |
| --- | --- | --- |
| Armure passive de l’équipement | Matériau, protection, attributs et durabilité par slot, indépendants du mode. | Conserver les valeurs des objets historiques. La sélection Armure commande la protection active. |
| Protection Armure | Cœurs d’absorption et absorption des dégâts admissibles via l’énergie, 2 HP par point avec la configuration par défaut. Dégâts excédentaires transmis, exemptions de sources, régime créatif et épuisement actuels. Drain de maintien arrêt/marche/boost de 0,0625/0,083333336/0,25 point/tick. | Réutiliser le handler de dégâts. Arbitrer maintien à vide, conversion dégâts/énergie, exceptions dont chute/noyade, cœurs d’absorption et recharge en Armure. |
| Camouflage actif | Invisibilité serveur, visuel 1P/3P, potion d’invisibilité distincte. Débits arrêt/marche/boost de 0,0625/0,25/0,5 point/tick. Le drain et la discrétion utilisent les mesures de déplacement historiques. | Garder le moteur de camouflage. Définir le drain et sa variation en mouvement. Aucun boost Force/Vitesse simultané avec le mode Camouflage. |
| IA, bruit et menace | Conditions de ciblage, portées selon mouvement et bruit, abandon de cible et mémoires, bruit de combat, jauge et intégration facultative Zombie Awareness. | Garder ciblage et rendu de menace dans le premier lot. Décider ensuite de l’ajustement des portées et de ce qui se passe à la rupture. |
| Pas et atterrissage | Bruits selon surface et mouvement, étouffement des pas en camouflage, son d’atterrissage distinct. Les bruits externes restent indépendants. | Réutiliser les événements et volumes. Valider la règle de pas ou d’atterrissage éventuellement propre à Vitesse ou Force. |
| Dégâts reçus en camouflage | Un attaquant vivant déclenche la rupture et peut vider la réserve. Les dégâts environnementaux sans attaquant suivent les règles existantes. | Conserver le point d’entrée. Choisir quelles sources provoquent une rupture et le coût exact. |
| Tirs TaCZ | Tir confirmé non silencieux à 20 points et rupture. Tir silencieux à 7 points, camouflage conservé tant que la réserve le permet. | Choisir le tir qui vide et rompt le camouflage d’après le manuel, ou une exception silencieux cohérente avec le mod. Décider séparément le cas des armes vanilla. |
| Predator Bow | `sfms:tb23` exempt de coût et rupture au tir camouflé. Multiplicateur ×1,5 avec set. Bruit à l’impact géré séparément. | Décider si cette exception de la Nanosuit 2 est exclusive à sa génération. Préserver l’ID de l’arme, les points d’injection et la validation du tir confirmé. |
| Power Melee au poing | Force contextuelle à main vide. Dégâts ×2, débit de 15 par impact réussi, pas de débit sur un échec. | Réserver le bonus à Force. Valider coût, multiplicateur et armes autorisées. L’attaque vanilla reste disponible dans chaque mode. |
| Power Melee à la crosse TaCZ | Même logique que le poing, appliquée par cible touchée. Deux impacts réussis coûtent 30. | Réserver le bonus à Force si la crosse reçoit le même traitement. Garder le débit après chaque impact réussi et décider si l’éligibilité change entre deux cibles. |
| Recul, tenue de visée et objets lourds | Aucune capacité Force dédiée à ces usages dans le moteur actuel du mod. | Le manuel les rattache à Force. Les traiter comme extensions séparées après validation, avec une décision explicite sur coût, plateformes TaCZ et interactions vanilla. |

### Visières, présentation et état

| Fonction | Nanosuit 2 · comportement actuel à conserver | Nanosuit 1 · proposition et décisions |
| --- | --- | --- |
| Nanovision | Activation locale et autorisation serveur distinctes, thermique et profil de casque. Drain historique dépendant des branches de tick. Avec set complet et sans mode ou boost, le drain nominal calculé n’est pas appliqué. | Choisir si le thermique reste une aide commune ou si la Nanosuit 1 reçoit plus tard la vision nocturne et la réserve séparée du manuel. La première implémentation de Force/Vitesse peut garder le comportement auxiliaire actuel. |
| NanoVisor et zoom | Affichage, zoom, sensibilité et sons locaux. Autorisation serveur distincte pour le marquage. | Décider de l’équivalence avec les jumelles du jeu. Garder les permissions et le moteur au premier lot. |
| Marquage et assistance balistique | Marquage borné côté serveur, synchronisé au propriétaire. Aide balistique locale pour arc vanilla. | Choisir si ces capacités sont partagées. L’assistance balistique est indépendante de l’exception du Predator Bow TaCZ. |
| Minimap et menace | Carte, marqueurs et jauge de menace liés au set et au HUD. Munitions et arme TaCZ via le pont existant. | Conserver le moteur commun. Décider des éléments affichés sur le HUD Nanosuit 1. |
| HUD, commandes, raccourcis | Indicateurs historiques d’énergie, Armure, Sprint/Force contextuels et Camouflage. Z/X, saut et sprint remappables selon les chemins actuels. | Ajouter les quatre modes à la présentation locale et garder les actions distinctes. Valider touches, sélection radiale ou cycle et conflits avec V du NanoVisor et les raccourcis des mods. |
| Sons, flash et visuels de mode | Boucles et sons existants, impulsions de Force, composition thermique et réfraction en 1P/3P, ponts Punchy, TaCZ, Fresh et Iris. Le thermique a priorité dans les chemins de rendu qui le prévoient. | Réutiliser les moteurs et signaux existants. Ajouter uniquement les sons et états visuels approuvés pour Force et Vitesse. Décider quels effets sont visibles par les observateurs. |
| Modèles, poses et textures | Descripteur par pièce et moteurs communs. Même modèle provisoire pour les deux générations. | Les nouveaux modes peuvent être testés avec ce descripteur. Les modèles Blockbench relèvent d’une mission distincte. |
| État joueur et sauvegarde | `Energy`, `Mode`, `RegenDelayTicks`, `RecoveryTicks` dans l’attachement. Cooldowns et charge en mémoire. Synchronisation équipement vanilla, données Crysis v4 au propriétaire. | Garder IDs, attachement et clés. Définir la valeur de `Mode` chargée si elle est incompatible avec le set, ainsi que la politique portail/reconnexion. L’énergie par tenue reste une évolution distincte. |
| Améliorations | Aucun module actif. Préparer une extension future des règles de la Nanosuit 2, résolue côté serveur. Le profil de base doit rester mesurable en l’absence de module. | Le premier lot Nanosuit 1 porte sur la sélection et l’autorisation des capacités. |

## Décisions à valider avant codage

1. **Sélection et état initial.** Choisir comment sélectionner les quatre modes. Décider si Armure est choisie automatiquement, si NONE est un mode volontaire ou un état de secours et si le mode choisi revient après une panne ou une remise de tenue. Fixer la continuité d’Armure/Camouflage lors d’un échange direct entre générations et la politique des échéances au portail, à la reconnexion et à la mort.
2. **Force.** Fixer le déclenchement du saut, l’agrippement et la protection de chute. Valider poing et crosse, leurs coûts et la politique par impact. Laisser recul et interactions avec objets lourds à une spécification séparée.
3. **Vitesse.** Fixer marche et sprint, coût à l’effort, déclenchement à l’appui et éventuelle nage en Vitesse. Spécifier l’ordre d’application avec la vitesse vanilla, la config du mod et les attributs d’autres mods.
4. **Armure et Camouflage.** Fixer coût de maintien, absorption, protection de chute, régénération et soin. Pour le Camouflage, décider la rupture lors d’un tir, les exceptions silencieux et Predator Bow et les sources de dégâts.
5. **Assistance et présentation.** Décider ce que la Nanosuit 1 partage avec la Nanosuit 2 pour la respiration, la Nanovision, le NanoVisor, la carte et la menace. Valider raccourcis, quatre indicateurs, sons et effets distants de Force/Vitesse.

Pour le premier lot distinct, la proposition est de limiter les différences aux sélections manuelles et à l’autorisation des pouvoirs physiques, puis de traiter les barèmes et exceptions après validation. Les réglages et les tests de la Nanosuit 2 restent la référence de non-régression.

## Plan et fichiers

Les chemins Java sont relatifs à `src/main/java/io/github/painworks/crysismod/`.

| Étape | Intervention ciblée | Fichiers et critère de sortie |
| --- | --- | --- |
| 1. Représenter et sélectionner | Ajouter Force/Vitesse au mode réellement utilisé par le serveur. Utiliser `ManualMode` existant pour exposer les modes autorisés. Isoler la sémantique Nanosuit 1 de la bascule historique. Traiter un mode incompatible dès le chargement ou l’échange et synchroniser la décision serveur. | `nanosuit/{NanosuitMode,NanosuitProfile}.java`, `nanosuit/rules/NanosuitControls.java`, `nanosuit/handler/NanosuitPlayerHandler.java`, `nanosuit/client/NanosuitKeyMappings.java`, `nanosuit/command/NanosuitDebugCommands.java`. Tester refus avec set mixte, panne et cooldown, sélection répétée, échanges dans les deux sens et modes inconnus. |
| 2. Adapter les contrats | Les deux paquets de mode utilisent actuellement `writeEnum/readEnum` par ordinal. Garder explicitement 0/1/2 pour NONE/ARMOR/CLOAK, attribuer des codes stables à Force/Vitesse et annoncer une version réseau suivante avant de les transmettre. Mettre à jour le contrôle des paquets invalides. Conserver le nom des modes historiques dans `Mode` et le repli des valeurs inconnues. Vérifier l’interface du HUD sur l’état confirmé. | `nanosuit/network/{NanosuitNetwork,ToggleNanosuitModePacket,SyncNanosuitDataPacket,NanosuitSyncManager}.java`, `nanosuit/energy/NanosuitEnergy.java`, `nanosuit/client/ClientNanosuitPacketHandlers.java`, `qa/NanosuitArchitectureQa.java`. Tester sauvegarde v4 avec les deux sets, aller-retour codec, ID inconnu, refus propre d’un pair réseau incompatible. La présente mission conserve v4. |
| 3. Autoriser Force/Vitesse | Ajouter une autorisation par capacité, profil et mode aux entrées existantes. L’utiliser lors de la charge et du lancement du saut, du tick d’agrippement, de la demande et du tick de boost et de chaque impact de mêlée. Pour la Nanosuit 2, conserver l’éligibilité contextuelle actuelle y compris sous Armure/Camouflage. Réévaluer les demandes actives dès la transition. | `nanosuit/handler/{NanosuitPlayerHandler,NanosuitSwimBoost,NanosuitPowerMelee}.java`, `nanosuit/client/EnhancedJumpClientState.java`, `nanosuit/rules/{NanosuitControls,NanosuitMobilityRules}.java`. Les mixins `PlayerPowerMeleeMixin.java` et `TaczPowerMeleeMixin.java` peuvent conserver leur point d’entrée. Tester changements pendant charge/guidage/sprint/nage et coups par cible. |
| 4. Appliquer les règles de dépense validées | Distinguer mode sélectionné et source de débit. Aujourd’hui, tout mode non NONE déclenche `MODE_ACTIVE` et le repli de `getModeDrainPerTick` utilise la valeur Armure. Introduire les cas explicites de Force et Vitesse, pour qu’un mode sélectionné sans action puisse régénérer selon la règle approuvée. Traiter les variantes Armure et Camouflage au niveau de leurs règles par profil. | `nanosuit/handler/{NanosuitPlayerHandler,NanosuitCloak}.java`, `nanosuit/rules/{NanosuitCombatRules,NanosuitEnergyRules,NanosuitMobilityRules}.java`. `mixin/nanosuit/TaczCloakGunfireMixin.java` seulement si une exception Predator ou silencieux devient propre à la génération. Tester comptabilité à énergie partielle, à zéro, au retour à 100, et mêmes valeurs Nanosuit 2 avant/après. |
| 5. Présenter les modes et valider | Ajouter une vue locale des quatre modes et les signaux retenus, sur la base du mode serveur. Déduire la génération de l’équipement. Si Force/Vitesse doivent être visibles à distance, synchroniser l’état visuel utile aux clients qui suivent l’entité, à l’arrivée et aux changements. | `nanosuit/client/{NanosuitHudOverlay,NanosuitPowerFeedback,CloakAmbientSound,ClientNanosuitPacketHandlers}.java`, `nanosuit/client/render/NanosuitArmorModeOverlayState.java` si nécessaire, `registry/ModSounds.java` et ressources HUD/langues/sons si approuvées. Réutiliser `qa/equipment/` et `qa/equipment-dedicated/`. Valider le propriétaire, l’observateur, les transitions de cycle de vie et les raccourcis remappés. |

Les étapes 1 et 2 forment une unité de transport et de persistance. Garder Force et Vitesse inactifs pour les joueurs jusqu’à ce que les règles, leurs gardes et l’indication locale soient validées. La sélection, le verrou et les débits restent autoritaires côté serveur. Chaque étape garde la Nanosuit 2 sur les mêmes instances de règles que `LEGACY_CURRENT`. La campagne de non-régression de la Nanosuit 2 doit vérifier les valeurs historiques et réutiliser les relevés existants avant d’élargir aux interactions affectées.

La mission actuelle ajoute ce rapport et le banc de rendu `qa/equipment-compat/render/` sur `refactor/multi-nanosuit`. Les résultats proviennent du JAR de production de `c8258de`. Les propositions de modes, de persistance et de protocole restent au stade de spécification.
