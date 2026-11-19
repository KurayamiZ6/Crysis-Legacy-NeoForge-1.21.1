# Sprint N1 et contacts ennemis — 20 novembre 2026

Base : 0e1f3e5, branche feature/nanosuit-1-classic. Travail limité aux retours sprint, marqueurs ennemis N1 et contours des jumelles. Position du Threat Indicator conservée à son emplacement rétabli. Aucun changement des coûts configurés, de l'IA ou de la thermique N2.

## Sprint

Cause : classicSprintExhausted restait verrouillé jusqu'à la fin du sprint. Le drainage terrestre était également plafonné au seuil classique de 10 %, partagé avec le camouflage. Le délai historique N2 empêchait certaines reprises rapprochées.

Correction : suppression du verrou N1. Le boost terrestre cesse à 20 % et en dessous ; il peut reprendre au-dessus du seuil sans arrêt du déplacement. Maintenir la demande de sprint consomme toujours de l'énergie sous ce seuil, jusqu'à zéro, sans donner le bonus de vitesse. Depuis 1 %, une nouvelle demande peut à nouveau vider l'énergie. Pas de verrou de panne N2 ajouté à la N1. Le passage positif → zéro déclenche le signal sonore existant DISABLED, sans lancer sa mécanique de panne/recharge. Les avertissements 25/15 % sont conservés.

Le cooldown de reprise N2 est conservé ; la N1 n'utilise plus ce délai de reprise du boost. Le seuil du camouflage reste à 10 %, la nage et les multiplicateurs/coûts configurés ne sont pas réécrits.

ClassicSprintQa utilise les ticks serveur réels, une combinaison complète et des déplacements préparés dans le monde copié. Il ne prétend pas reproduire un maintien physique de Shift : le drapeau de sprint et la vitesse de déplacement sont pilotés par la sonde. Les valeurs aux frontières sont préparées ; dans le cas final de recharge en marchant, l'énergie de reprise n'est pas injectée.

Résultats classic-sprint-walk-recharge : 11 cas, CLASSIC_SPRINT_COMPLETE failures=0, emptySounds=2. Notamment :

- 20 → 6,5 et 19 → 5,5 en six ticks : sprint toujours demandé, boost absent.
- Deux passages préparés à 1 % : zéro atteint, pas de verrou de récupération, deux événements sonores clients observés.
- Retour d'énergie en gardant le sprint actif : boost de nouveau actif sans relâchement de ce drapeau.
- Marche continue : recharge naturelle 0 → 36,00001 en trente ticks, puis sprint ramenant l'énergie à 22,500011, boost actif. Aucun arrêt du déplacement ni injection d'énergie à cette reprise.
- N2 : pas de boost automatique ; demande explicite de reprise toujours nécessaire et fonctionnelle.

Le premier essai avait deux attentes de fixture incorrectes : 30 % descendaient à 16,5 % durant la fenêtre d'observation, donc le boost devait être coupé. L'énergie préparée de ces cas a été augmentée pour vérifier une reprise restant au-dessus de 20 %. Le coût réel par tick n'a pas été changé pour faire passer le test.

## Une source par ennemi pour carte et jumelles

La minicarte classique sortait avant la couche historique de marqueurs, et le serveur ne transmettait qu'un niveau global de menace. Ajout d'un instantané N1 des contacts déjà parcourus par NanosuitThreatTracker : identifiant, UUID et niveau évalué pour ce joueur. Aucun ciblage serveur n'est deviné depuis le client.

- Vert : hostile au repos, sans preuve d'agressivité envers ce joueur.
- Rouge : cible/ATTACK_TARGET visant le joueur, ligne de vue et absence de dissimulation par le camouflage.
- Jaune : cible sans ligne de vue, mémoire de colère/agression ou courte rémanence d'une détection précédente.
- Orange pendant 0,5 s lors d'une perte du rouge, puis jaune. Même fonction de couleur pour la flèche et le contour.

La rémanence N1 existante de 60 ticks est suivie par contact. On n'ajoute pas ensuite une seconde rémanence globale : la jauge correspond au maximum des preuves conservées. Cette rémanence est une approximation d'alerte, pas une nouvelle IA de recherche. Une investigation Zombie Awareness sans attribution au joueur n'est pas annoncée comme détection. La proximité seule ne rend jamais un ennemi jaune ou rouge.

Les contacts couvrent le rayon de détection existant de 32 blocs, avec la boîte verticale existante et au plus 128 contacts les plus proches. Instantané toutes les dix ticks ; expiration client après trente ticks sans nouvelles. Aucun chargement de chunk et aucune recherche d'entités par frame. Les UUID évitent d'appliquer un contact à un identifiant réutilisé. Monde, joueur, équipement invalide et passage N2 rendent les données inactives et les nettoient.

La minicarte utilise des flèches orientées, dans sa transformation et son échelle existantes. Pas de nouvel objectif vert : les marqueurs de mobs sont distincts du waypoint suivi et du joueur. Des ennemis très proches peuvent se superposer à une échelle cartographique large.

Les jumelles utilisent le contour natif de Minecraft, limité à la N1 et aux ennemis visibles connus. Aucune modification des équipes, des flags glowing serveur ou des potions. Fermeture des jumelles, F1, autre écran et retrait de combinaison retirent cette contribution. Les effets de surbrillance indépendants restent du ressort de Minecraft.

Limite du rendu : contour natif, sans reproduire le remplissage interne de CryEngine. La visibilité est échantillonnée avec les contacts (jusqu'à 0,5 s de latence) ; un ennemi entièrement caché perd son contour. Pour un ennemi partiellement visible, le contour natif peut aussi dessiner des parties occultées. Ce n'est pas un masque de silhouette par profondeur identique à Crysis.

Le protocole réseau passe de 6 à 7 pour ce nouvel instantané : client et serveur doivent utiliser la même version du mod.

## Validation graphique ciblée

ClassicContactsQa prépare explicitement un zombie au repos, un husk avec mémoire d'agression et un squelette ciblant le joueur. Ils sont figés pour comparer les images : ce n'est pas une preuve d'acquisition naturelle par l'IA.

- classic-contacts-visual sans shader : cinq états, failures=0 ; trois contours colorés inspectés.
- classic-contacts-complementary : six états, failures=0 ; vert/jaune/rouge distincts, F1 retire les contours, paroi retire celui du squelette, retrait du plastron puis N2 nettoient les contacts/jumelles. Captures après toutes les couches HUD.
- Iris 1.8.14-beta.1, Complementary Reimagined r5.10, EMF 3.3.12, ETF 7.2.7 et Fresh Animations 1.11.2 présents dans ce second essai ; capture des trois contours avec les modèles animés inspectée.

Contrôle naturel séparé avec Zombie Awareness 1.13.2 et CoroUtil 1.3.8 : ZombieAwarenessQa conserve les IA normales, sans setTarget ni NoAI. Le premier passage classic-contacts-natural-za présente une assertion en échec : l'ancienne sonde exigeait zéro cible pendant toute l'observation sous camouflage, mais un zombie s'approchait à 1,4 bloc, dans sa portée légale de détection. Cette attente a été corrigée pour refuser uniquement les cibles que NanosuitCloak.hidesFrom doit effectivement cacher. Aucun changement de portée, camouflage ou code Zombie Awareness pour obtenir ce résultat. Second passage classic-contacts-natural-valid : 13 états, ZA_COMPLETE failures=0 ; marqueurs bruts conservés avec les preuves.

Le banc Iris conserve ses avertissements antérieurs de variables Complementary et de formats de profondeur. Le succès ciblé ne signifie pas absence de tout avertissement graphique. Pas de test de tous les shaderpacks ou de tous les mobs moddés.

Compilation : build/classic-sprint-contacts-final-build.log, BUILD SUCCESSFUL. Gradle test NO-SOURCE ; résultats ci-dessus issus des sondes Minecraft. Les captures et journaux choisis sont dans evidence/sprint-contacts/.

## Fichiers

Sprint : NanosuitPlayerHandler et ClassicSprintQa. Contacts : NanosuitThreatTracker, SyncClassicThreatsPacket, NanosuitNetwork, ClassicEnemyContacts, ClassicMinimapRenderer, deux mixins client ClassicEnemyOutlineMixin/ClassicEnemyColorMixin et crysismod.mixins.json. ClassicContactsQa reste exclusivement dans qa/.

JAR livré : build/deliverables/crysis-legacy-n1-sprint-contacts.jar, accompagné de son empreinte .sha256. Client et serveur à mettre à jour ensemble en multijoueur. Aucune fusion dans main, aucune modification du profil Modrinth personnel.

SHA-256 : `478e64a1c54b32031233a34c679887e5be50ba7bf062daf1b00c62cc74020498`. Identique aux JAR des trois contrôles finaux (sprint/recharge, contours avec shaders, IA naturelle). Aucune sonde qa incluse. Commit du sprint : 0d4e219 ; contacts et preuves dans le commit suivant. [Captures sélectionnées](evidence/sprint-contacts/index.html).
