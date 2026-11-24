# Crysis Legacy — Nanosuit Cradle & Survival

**Spécification de conception v1.0 — 24 novembre 2026**  
**Plateforme :** Minecraft 1.21.1 / NeoForge / Java 21  
**Destination recommandée :** `docs/design/NANOSUIT_CRADLE_SPECIFICATION.md`

## 1. Objet et statut

Cette spécification définit la progression Survival liée aux Nanosuits et les fondations de la Nanosuit Cradle. Elle remplace les anciennes propositions de simple station d'équipement. Elle est la référence fonctionnelle des missions Codex successives, mais n'autorise pas leur exécution simultanée.

Les règles explicitement retenues dans la discussion sont indiquées comme exigences. Les détails techniques recommandés sont des choix de mise en œuvre à vérifier contre le code réel. Les paramètres d'équilibrage et propositions non encore approuvées sont signalés comme tels. Les adaptations du lore ne constituent pas des affirmations de canon universel.

### Principes directeurs

- La Nanosuit est une combinaison intégrée à son porteur, pas une armure conventionnelle interchangeable depuis l'inventaire.
- La N1 dépend de l'infrastructure humaine ; la N2 devient autosuffisante grâce à sa technologie biologique et Ceph.
- Le joueur fabrique d'abord les pièces N1 puis les intègre via la Cradle. La progression normale N1 → N2 transforme sa combinaison, plutôt que de fabriquer une seconde armure indépendante.
- Les Nanosuits extraites restent transmissibles et réinstallables.
- Aucune potion, aucun enchantement vanilla détourné (Binding, Mending, Silk Touch, etc.) pour implémenter les mécaniques propres au mod.
- Préserver les HUD, capacités, intégrations et données existants ; toute évolution de gameplay doit être spécifiquement autorisée par la phase concernée.
- Les modèles et animations ne doivent pas piloter la logique serveur. Développer et vérifier les fonctions en jeu avant de consacrer du temps à leur présentation définitive.

## 2. Installation physique

| Composant | Encombrement de référence | Fonction |
|---|---|---|
| Nanosuit Cradle | 2 × 1 × 1 blocs, horizontale | Support, stockage des quatre pièces, logement de batterie, accueil du joueur |
| Robotic Arm Assembly | 2 × 1 × 1 blocs, au-dessus | Six bras, assemblage indépendant |
| Espace libre | Une couche de blocs entre le support et les bras | Installation du joueur et mouvement des bras |
| Cradle Controller | 1 bloc | Commandes, état de la machine, énergie, ressources |

Dimensions et ancrages des modèles définitifs à vérifier en jeu ; ne pas produire de modèle artistique provisoire coûteux. Les bras forment un seul assemblage indépendant de la Cradle. Une Cradle seule peut servir de présentoir : les pièces sont visibles au fur et à mesure de leur dépôt, sans bras, batterie ou Controller. Les interventions actives nécessitent les trois composants et une batterie suffisamment chargée.

### Placement et liaison

- La Cradle est un multibloc horizontal orientable. Placer ses deux parties ensemble ou refuser le placement entier si l'espace manque.
- Recommandation technique : une seule BlockEntity maîtresse détient les données ; la seconde partie redirige vers elle.
- Le Controller se connecte automatiquement à la Cradle valide la plus proche dans un rayon de cinq blocs à son placement. Conserver une liaison stable plutôt que la recalculer en permanence. Prévoir une reconnexion si la cible est détruite ; règles d'égalité de distance et de contrôleurs multiples à définir de façon déterministe.
- Les bras ne sont valides que s'ils sont alignés correctement au-dessus du support, avec l'espace requis. Ne pas connecter les bras d'une installation voisine par erreur.
- Les bras sont repliés au repos, déployés quand une procédure est effectivement prête et sélectionnée, puis animés pendant l'intervention. Power Off/Cancel ramène la machine à un état de repos lorsque l'annulation est autorisée.

## 3. Stockage et interactions

La Cradle stocke les quatre véritables ItemStacks correspondant aux emplacements casque, torse, jambes et bottes. Elle accepte des pièces N1 ou des pièces N2 récupérées, y compris un ensemble incomplet. Une combinaison complète et homogène est requise pour une intégration.

- Clic droit avec une pièce de Nanosuit non intégrée : déposer la pièce dans son emplacement correspondant ; ne pas écraser une pièce déjà présente.
- Ne pas accepter une pièce d'une autre génération si des pièces sont déjà déposées, afin de conserver un ensemble homogène. Ce refus est une décision technique recommandée à valider en jeu.
- Prévoir une récupération explicite des pièces sans Controller ni alimentation. Geste provisoire recommandé pour le premier prototype : accroupi + clic droit à main vide, récupération de la dernière pièce présente selon un ordre déterministe ; interaction par emplacement visuel à préciser lors du modèle définitif.
- Ne pas créer de copies d'objets pour la présentation 3D. Les modèles affichés représentent les ItemStacks réellement stockés.
- Persistance après sauvegarde, rechargement, déchargement de chunk et redémarrage serveur.
- Casser l'une des parties libère les objets de la Cradle exactement une fois et retire l'autre partie ; pas de drops multiples ou de BlockEntity orpheline. Comportement des explosions et protections de blocs à vérifier ultérieurement.
- Une procédure active verrouille les pièces, les ressources et la batterie. Hors procédure, pas de propriété permanente imposée à la machine.
- Le logement de batterie est indépendant du stockage des pièces et des ressources d'évolution.

## 4. Controller : états et commandes

Le Controller observe la machine, pas l'équipement du joueur qui utilise l'écran. Le joueur devant le Controller peut être différent de celui qui s'allonge sur la Cradle. Les vérifications physiques du porteur ont lieu à l'entrée dans la Cradle puis avant la validation serveur finale.

| Commande | Conditions machine | Conditions porteur |
|---|---|---|
| Integration | Ensemble N1 ou N2 complet, bras présents, énergie suffisante | N1 : Undersuit complète ; N2 : sans armure incompatible, Undersuit facultative |
| Extraction | Aucun ensemble stocké, bras présents, énergie suffisante | N1 ou N2 intégrée |
| Maintenance | Aucun ensemble stocké, bras présents, énergie suffisante | N1 intégrée, usée |
| Evolution | Aucun ensemble stocké, ressources complètes, bras présents, énergie suffisante | N1 intégrée |
| Customization | Réservé pour une phase ultérieure | À définir |
| Cancel / Power Off | Disponible avant validation irréversible | Sans objet |

L'écran présente les quatre emplacements de la combinaison déposée, la présence des bras, l'état de la liaison, le type et la charge de la batterie, les coûts des procédures, ainsi que les ressources d'évolution lorsqu'elles sont nécessaires. Dès qu'une pièce est déposée, les autres procédures actives incompatibles sont bloquées ; récupération des pièces et opérations d'annulation restent possibles. Une combinaison incomplète ne peut pas être intégrée.

États recommandés : OFF, STANDBY, PREPARING/WAITING_FOR_RESOURCES, ARMED/WAITING_FOR_OPERATOR, RUNNING, COMPLETED, ABORTED. Une procédure doit être validée côté serveur ; une sélection sur le Controller seule ne démarre pas le transfert. L'évolution possède une étape distincte de préparation des matériaux avant armement.

Le détail de l'interface graphique n'est pas encore validé. Pas de nouvelle maquette exhaustive avant fonctionnement réel.

## 5. CryNet Undersuit

Quatre pièces équipables manuellement dans les slots d'armure vanilla. Aspect textile appliqué sur le joueur, sans armure 3D complexe ; protection très faible ou nulle ; durabilité au plus égale au cuir, valeur de travail proposée : 50 % de celle du cuir. Aucune capacité Nanosuit.

- N1 : Undersuit complète requise au moment où le joueur tente de s'allonger. En son absence, la Cradle refuse l'entrée, même si la N1 est complète et la procédure programmée.
- Lors de l'intégration N1, l'Undersuit disparaît des slots et est remplacée par la N1. Conserver en interne les ItemStacks/données de l'Undersuit, sans seconde copie accessible, pour les restituer lors de l'extraction N1 avec leur usure originale.
- N2 : Undersuit facultative. Si présente, elle est assimilée et n'est jamais restaurée lors d'une extraction. L'évolution N1 → N2 assimile également l'Undersuit cachée.
- Ne pas détruire d'autres armures : refuser l'entrée ou demander de libérer les emplacements incompatibles. La gestion d'une Undersuit partielle lors d'une intégration N2 reste à préciser ; proposition conservatrice : refuser les équipements ambigus.

## 6. Combinaison intégrée : identité et persistance

- En Survival et Hardcore, l'intégration et le retrait passent obligatoirement par la Cradle. Impossible de retirer, échanger, jeter ou remplacer manuellement une pièce intégrée.
- Après une mort ordinaire, le joueur réapparaît avec sa Nanosuit ; elle n'est pas incluse dans les drops. Aucun doublon avec `keepInventory` ou d'autres mécanismes.
- Préserver la liaison après déconnexion et changement de dimension.
- Prévoir des restrictions serveur, y compris les chemins d'équipement hors inventaire (distributeurs, interactions de mods lorsqu'elles sont accessibles).
- Conserver un mode Créatif / outils de développement autorisant des essais sans infrastructure ; définir les transitions Créatif ↔ Survival et la migration des anciennes sauvegardes avant activation des restrictions globales.
- Recommandation : identité persistante unique de l'ensemble, indépendante de la génération, associée au porteur tant que la combinaison est intégrée. Préserver son identité lors de l'évolution. Définir une source de vérité serveur unique et un transfert exclusif entre joueur et machine ; éviter un stockage en double dans les données joueur et les ItemStacks.
- Une combinaison extraite peut être réinstallée sur n'importe quel joueur admissible. Les améliorations N2, le solde de Nano Catalyst et la personnalisation suivent la combinaison : proposition de conception retenue pour assurer la cohérence des transferts, à confirmer avant l'implémentation des améliorations.
- Ne pas réinitialiser gratuitement l'énergie, les cooldowns, les verrous de panne ou autres données gameplay lors d'un transfert. Examiner les règles actuelles avant de modifier leur persistance.

## 7. Interventions et immersion

1. Le Controller sélectionne une procédure ; la machine vérifie ses prérequis.
2. Lorsque la procédure est prête, les bras se déploient.
3. Le joueur interagit avec la Cradle ; le serveur vérifie son éligibilité réelle.
4. Le joueur s'allonge et la procédure démarre, avec mouvements limités et caméra en première personne par défaut. F5 reste utilisable ; une liberté raisonnable de regard est souhaitée.
5. Les bras exécutent une animation commune (six bras), adaptée visuellement à l'opération ; les pièces apparaissent/disparaissent aux instants appropriés.
6. Le serveur valide le résultat et applique la transaction. HUD et capacités suivent le profil réellement équipé ; sons et effets correspondent à la procédure.
7. Le joueur est libéré ; la machine retourne au repos et les bras se replient.

L'ambiance recherchée rappelle les interventions et Skinning Labs de Crysis 2/3, particulièrement inquiétantes en première personne. Pas de passage automatique en troisième personne. Durées, mouvements, sonorisation et effets précis à définir avec le modèle. Utiliser un prototype fonctionnel avant la production artistique.

### Sécurité des transactions

- Une seule procédure et un seul porteur à la fois par installation ; interdire l'accès concurrent aux ressources verrouillées.
- Revalider les conditions et la présence des composants avant la validation finale.
- Transférer les quatre pièces, leurs données et les ressources ensemble, une seule fois. L'animation ne peut pas créer ou supprimer les objets indépendamment de l'état serveur.
- Une interruption avant le point de validation doit laisser les objets dans leur état initial et libérer le joueur ; traiter déconnexion, destruction, déchargement, arrêt du serveur et mort externe.
- Prévoir une réservation d'énergie au démarrage ou un mécanisme équivalent ; facturation au point de validation recommandé. Une extraction N2 effectivement échouée doit consommer son énergie. Coût des autres interruptions à définir.
- Tester les interruptions et sauvegardes autour du point de validation ; une transaction mémoire seule ne garantit pas l'atomicité après crash. Concevoir la reprise en fonction des mécanismes persistants réellement disponibles.

## 8. Nanosuit 1 : usure et maintenance

La N1 conserve une durabilité. L'usure augmente proportionnellement les coûts énergétiques jusqu'à un seuil critique où aucune fonctionnalité Nanosuit nécessitant un système opérationnel ne peut être activée. Les pièces ne se détruisent pas à ce seuil et restent liées au joueur.

Proposition d'équilibrage initiale (non définitive) : usure normalisée `w` de 0 à 1, multiplicateur `1 + w`, seuil critique à 100 %. Exemple : 50 % d'usure = coûts ×1,5. Mesure globale à partir des quatre pièces ; une pièce à la limite critique suffit à désactiver les capacités. Méthode exacte d'agrégation, sources/vitesse d'usure, comportement des capacités passives et de la batterie indépendante de vision nocturne à définir avant cette phase.

Ne pas modifier les taux de recharge ou cooldowns existants sans décision explicite. Le joueur ne doit pas être définitivement bloqué faute de pouvoirs ; la Cradle doit rester utilisable pour réparer une N1 inopérante.

La maintenance restaure la durabilité. Pas de coût matériel supplémentaire : seul le coût énergétique de la machine intervient, proportionnel à l'usure. Calibration et réparation peuvent être regroupées en une commande Maintenance. La N1 ne se répare pas via enchantements vanilla.

## 9. Nanosuit 2 : autosuffisance et extraction

- Pas de statistique de durabilité N2 ; son autoréparation est implicite. Aucun Mending ni mécanisme périodique inutile.
- L'extraction N1 est sans danger. Restaurer l'Undersuit et transférer la combinaison dans une Cradle vide.
- L'extraction N2 présente par défaut 50 % de risque de mort (tirage unique serveur).
- Succès : retirer la liaison, transférer la N2 entière dans la Cradle, ne pas restituer d'Undersuit, laisser le joueur à 0,5 cœur au plus (ne jamais le soigner artificiellement).
- Échec fatal : la N2 reste intégrée au joueur, aucune pièce n'est transférée dans la machine, le joueur meurt. En Survival normal il réapparaît avec sa N2 ; les règles Hardcore de mort continuent de s'appliquer. Consommer l'énergie de la procédure.
- Avertissement explicite avant évolution, installation d'une N2 existante et extraction ; le consentement doit être celui du porteur, non uniquement de l'utilisateur du Controller.
- Risque mortel désactivable et probabilité configurable côté serveur. Avec risque désactivé, extraction réussie, réduction de santé conservée par défaut. Interaction avec Totem et autres mécanismes de prévention de mort à définir ; une extraction échouée ne devient pas réussie parce qu'une mort a été évitée.

## 10. Évolution N1 → N2

Évolution irréversible de la même combinaison, avec conservation de son identité. La progression normale ne demande pas de fabriquer une seconde N2 complète. La N2 existante extraite reste cependant réinstallable.

Ressources prévues (noms de travail, pas de recettes ni quantités définitives) :

| Ressource | Provenance |
|---|---|
| Advanced CryFibril | Coffres / installations CELL |
| Ceph Technology Fragments | Butin des Ceph Stage 1 (inspiration Crysis / Warhead) |
| Ceph Neural Core | Composant rare de boss Ceph |

Le Controller indique les matériaux requis ; le joueur les dépose sur la Cradle par clic droit. Le Controller actualise les quantités réellement stockées. La commande de lancement ne s'arme que lorsque les ressources et l'énergie nécessaires sont disponibles. Le porteur N1 s'allonge, la machine transforme la N1 en N2 au point de validation, consomme les matériaux et l'énergie, assimile l'Undersuit et active les systèmes N2. Le remplacement des ItemStacks doit préserver les données qui appartiennent à la combinaison ; définir la migration précise de ses données avant cette phase.

Les ressources de test peuvent exister sans recettes, mobs ou génération de donjons définitifs ; ne pas inventer un équilibrage présenté comme final.

## 11. Énergie : batteries physiques

Deux types de blocs de batterie rares, fabricables et récupérables dans les installations correspondantes. Chaque batterie conserve sa charge propre lorsqu'elle est minée, placée, transportée, installée, échangée ou récupérée. Non empilable ; charge bornée côté serveur et persistante dans le bloc et son équivalent ItemStack.

| Propriété | CELL Power Cell | Ceph Energy Cell |
|---|---|---|
| Technologie | Humaine | Extraterrestre |
| Capacité | Standard | Supérieure |
| Recharge | Lente via station CELL (solution recommandée) | Rapide via cristaux consommés |
| Alimente la Cradle | Oui | Oui |
| Surcharge de N2 | Non | Oui, bloc posé au sol |

La Cradle possède un seul logement de batterie visible à terme, pas de réserve énergétique séparée. Clic droit avec une batterie : installation si vide, échange si occupé, restitution sûre de l'ancienne batterie au joueur. Prévoir récupération volontaire à main vide (geste et hitbox définitifs à valider). Aucune perte en cas d'inventaire plein ; soit refuser l'échange, soit utiliser une restitution explicite et sûre. Verrouillage durant la procédure.

- Pas de consommation au repos, y compris lorsque la combinaison sert de présentoir. Les coûts sont associés aux interventions.
- Intégration, extraction et évolution : coûts fixes configurables ; maintenance N1 : proportionnelle à l'usure. L'évolution est prévue comme la procédure la plus coûteuse.
- Veiller à ce que chaque opération ait au moins une source d'énergie réalisable ; capacité et coût doivent être cohérents avec un seul logement de batterie.
- Capacités, charge initiale des blocs générés, coûts, vitesses et recettes restent ouverts. Pas de réseau électrique complexe ni dépendance énergétique externe imposée à ce stade.

### 11.1 Recharge CELL

Solution recommandée : une CELL Charging Station fabricable et récupérable dans les installations CELL. La batterie CELL y est installée, un signal redstone active la recharge lente. Aucun combustible ni câble supplémentaire dans la version initiale ; le signal représente la commande. Charge conservée quand le signal disparaît ou la batterie est retirée. Ne pas charger une batterie Ceph via cette station. Cette solution de recharge reste un choix de conception à confirmer lors de la phase énergétique ; l'alternative minimale est la recharge redstone directe du bloc.

### 11.2 Cristaux et recharge Ceph

- Ceph Energy Crystal : ressource énergétique consommable. Les amas récoltables non renouvelables sont relativement communs dans les donjons Ceph.
- Ceph Crystal Core : cœur / racine très rare, récupérable et déplaçable depuis les installations Ceph. Pas de génération naturelle dans les mines ou géodes terrestres ; pas de fabrication ni multiplication des cœurs.
- Un cœur peut faire pousser des cristaux avec le temps, selon un mécanisme propre au mod inspiré des géodes. Récolter les cristaux sans détruire le cœur ; culture possible dans une base du joueur. Stades, faces libres et vitesse à définir.
- Les cristaux de donjons sans cœur ne repoussent pas. Cristaux décoratifs distincts possibles ultérieurement, non obligatoires.
- Récupération avec un outil approprié, sans obligation de Silk Touch. La batterie Ceph posée au sol se recharge par clic droit avec un cristal : consommer le cristal et ajouter une quantité de charge bornée. Ne pas consommer de cristal sur une batterie pleine ; traitement du surplus à définir.
- Le design de la Ceph Energy Cell reprend les dispositifs énergétiques de Crysis 3 avec lesquels Prophet interagit pour se surcharger. Référence visuelle à établir avant création du modèle.

### 11.3 Surcharge N2

Interaction à main vide avec une Ceph Energy Cell posée au sol par un porteur N2 complet et valide : transférer toute la charge restante dans un état temporaire Supercharged, vider la batterie sans la détruire. Durée proportionnelle à la charge absorbée (valeur d'essai proposée : 60 s à pleine charge, non définitive). Une batterie vide ou un porteur sans N2 valide ne déclenche rien. Une surcharge déjà active refuse un nouveau transfert ; pas de cumul ni cooldown artificiel initial.

Pendant la surcharge, les capacités énergétiques N2 disposent d'énergie infinie (affichage à 100 % recommandé). Préserver leurs autres règles, notamment la rupture normale du camouflage. Ne pas rendre automatiquement les armes humaines TACZ infinies ; munitions illimitées des futures armes Ceph et éventuels effets supplémentaires à spécifier après comparaison au jeu original. Ne pas ajouter d'invulnérabilité absolue sans validation ; vérifier l'effet sur le mode Armure existant.

Le serveur gère la durée et le débit de la batterie de façon indivisible. La surcharge ne doit pas survivre à la perte du profil N2 ; comportement après mort/reconnexion à préciser, recommandation : pas de prolongation exploitable. Effets de fibres, HUD, sons et transfert d'énergie à reproduire à partir de Crysis 3, dans une phase ultérieure. La boucle de culture et recharge devra être équilibrée en Survival pour éviter la surcharge permanente trop facile.

## 12. Nano Catalyst et améliorations N2

Les Ceph Stage 1 fournissent une faible quantité de Nano Catalyst, absorbable uniquement par une N2 intégrée et opérationnelle. Le catalyseur ne devient pas un objet d'inventaire classique pour les autres joueurs ; l'absorption crédite le compteur propre à la combinaison. Prévenir la collecte multiple en multijoueur et définir durée de présence au sol et quantités lors du développement des ennemis.

Le joueur utilise son menu d'améliorations personnel N2 et dépense ce crédit sans retourner à la Cradle. Liste des modules, coûts et éventuelle progression par génération Ceph à définir ultérieurement. Pas d'améliorations N1 prévues à ce stade. La personnalisation visuelle reste une intervention future de la Cradle.

## 13. Progression Survival et monde

- CryNet : installations contenant des Cradles, Controllers et bras.
- CELL : composants technologiques humains, Advanced CryFibril, batteries et station de recharge.
- Ceph : batteries, cristaux non renouvelables, cœurs rares ; fragments obtenus sur les Ceph Stage 1 ; Neural Core de boss.
- Les recettes des pièces N1, de l'Undersuit, des machines et batteries seront définies avec la progression complète. Ne pas ajouter de N2 directement fabricable comme raccourci à l'évolution.
- Ennemi, boss, génération des structures et loot final hors périmètre des fondations ; aucun drop provisoire dans les mondes Survival de l'utilisateur sans approbation.

## 14. Ordre de réalisation

Chaque phase doit avoir un résultat vérifiable, un commit ciblé et un point d'arrêt. Les phases ne sont pas des autorisations de tout développer d'un coup.

| Phase | Périmètre | Critère de sortie |
|---:|---|---|
| 0 | Spécification et branche de travail | Document versionné, base Git identifiée |
| 1 | Cradle multibloc et stockage | Placement, quatre slots, récupération, persistance, destruction sans pertes/doublons |
| 2 | Controller et états | Liaison à 5 blocs, diagnostic réel, commandes préparées sans intervention fictive |
| 3 | Batteries et logement | Charge persistante, échange, lecture par Controller |
| 4 | Recharge CELL / cristaux Ceph | Recharge fonctionnelle et croissance ciblée, pas de donjons définitifs |
| 5 | Undersuit, identité et liaison | Règles d'équipement, réapparition et migration contrôlées |
| 6 | Bras multibloc et états | Alignement, présence, repli/déploiement fonctionnels |
| 7 | Intégration/extraction | Cycle serveur complet N1/N2, y compris les deux issues N2 |
| 8 | Modèles 3D | Modèle N2 distinct et ressources de l'installation validées en jeu |
| 9 | Animations et immersion | Première personne, F5, procédures synchronisées, aucun état résiduel |
| 10 | Usure et maintenance N1 | Coûts, seuil critique, réparation sans destruction |
| 11 | Évolution N1 → N2 | Ressources et énergie consommées, identité conservée |
| 12 | Surcharge N2 | Transfert réel de charge, durée et fin correctes |
| 13 | Nano Catalyst et améliorations | Crédit N2, acquisition et persistance des modules |
| 14 | Progression Survival finale | Recettes, loot, donjons, ennemis, boss et équilibrage |

Les phases 5 et 7 peuvent nécessiter un ajustement : ne pas activer l'interdiction globale d'équipement Survival avant qu'un cycle d'intégration opérationnel et une stratégie de migration soient disponibles. Les autres fonctions peuvent être développées derrière un mode de test afin de ne pas rendre les sauvegardes existantes injouables.

## 15. Contrat de travail Codex

- Vérifier HEAD, branche et arbre Git réels ; conserver les modifications non liées. Le dernier HEAD anciennement communiqué n'est pas présumé être le HEAD actuel.
- Nouvelle branche recommandée `feature/nanosuit-cradle` issue de la version de développement actuelle validée (historiquement `feature/nanosuit-1-classic`), pas du `main` potentiellement ancien. Ne pas fusionner, pousser ou publier sans instruction explicite.
- Une mission à la fois ; pas de sous-agents, pas d'audit général, pas de recherche web ou refactorisation sans nécessité. Réutiliser les bancs QA.
- Privilégier un test de logique serveur ciblé, puis une démonstration Minecraft réelle sur le banc graphique existant. Tester le multijoueur quand les transactions partagées le requièrent. Ne pas confondre compilation et validation fonctionnelle.
- Conserver les intégrations TACZ, Zombie Awareness, Iris/Complementary et Fresh Animations/EMF/ETF. Les tests graphiques complets interviennent lorsqu'une phase modifie le rendu ou les capacités ; ne pas relancer toutes les suites pour un simple bloc.
- Préserver les identifiants et la compatibilité des sauvegardes ; documenter tout changement requis. Pas de remplacement des ressources N1/N2 déjà validées.
- Les modèles et interfaces définitifs doivent être approuvés visuellement. Préférer l'aperçu dans Minecraft tôt, plutôt que refaire un long cycle de maquette HTML déconnectée du rendu réel.
- Limiter les relances et les captures. Si un blocage survient, terminer une unité cohérente, conserver le travail et documenter précisément ce qui reste non testé. Ne pas multiplier les tentatives identiques ni promettre un quota garanti.
- Rapport court : fichiers, commit, résultat de compilation, scénarios PASS/FAIL/NOT TESTED, preuves, limites, JAR si produit. Aucune assertion de compatibilité non démontrée.

## 16. Registre des décisions restant ouvertes

1. Durabilité exacte de l'Undersuit ; sources d'usure N1, agrégation, seuil et multiplicateur ; batterie de vision nocturne et capacités passives au seuil critique.
2. Capacités des batteries, coûts des interventions, recharge CELL (station proposée), vitesse de recharge et de croissance des cristaux, traitement du surplus.
3. Durée maximale de surcharge et comportement précis de ses effets, mort/reconnexion ; comparaison Crysis 3 avant création graphique.
4. Détails des interactions de récupération, position du logement de batterie, gestion des conflits de liaison entre Controllers et contraintes exactes des modèles.
5. Gestion des interruptions et prévention de mort, coûts d'une procédure interrompue, migration des anciennes sauvegardes et passage Créatif/Survival.
6. Quantités/recettes, loot, équilibrage des ennemis et des boss ; liste des améliorations N2 et rattachement définitif du solde de Nano Catalyst.
7. Design et animations de l'installation, modèle N2 définitif, localisation des textes et interfaces.

Aucune de ces décisions d'équilibrage ne bloque la phase 1. Les choix provisoires ne doivent pas être présentés comme définitifs ou utilisés pour étendre le périmètre.
