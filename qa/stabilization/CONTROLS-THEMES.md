# Commandes et thèmes — 20 novembre 2026

Base : 410ed10, branche feature/nanosuit-1-classic. Corrections demandées : empêcher la recharge sous demande continue de sprint N1, allonger l'appui du saut N2, employer la molette pour le zoom N2 et rendre les couleurs N1 accessibles avec des préréglages.

## Sprint N1 à zéro

Le correctif précédent ne considérait une demande de drainage que si l'énergie était positive. À zéro, il entrait donc dans la branche de recharge ; un prochain tick de sprint consommait l'énergie revenue et relançait le son.

Une demande de sprint terrestre N1 en mode Vitesse reste désormais une demande de drainage même à zéro. L'énergie reste à zéro, le bonus de vitesse reste coupé et le délai normal de recharge reste repoussé tant que ce sprint est sollicité. Le son n'est envoyé que pour un passage positif → zéro. Relâcher le sprint permet ensuite la recharge, après son délai configuré, sans devoir arrêter le déplacement. Seuil du boost à 20 %, coût par tick, camouflage, nage et règles N2 conservés.

## Saut puissant N2

Appui requis : 4 → 6 ticks (+50 %), soit 200 → 300 ms à 20 TPS. Client et serveur lisent la même règle. Coût, vitesse verticale, cooldown et timeout conservés. Le saut N1 reste instantané (holdTicks=0).

## Zoom des jumelles N2

Molette vers le haut : zoom fort ; vers le bas : zoom de base. L'événement est consommé tant que les jumelles sont actives, même lorsque le grossissement est déjà à sa limite : aucune sélection de hotbar. Après fermeture, la molette retrouve son fonctionnement normal. Les deux multiplicateurs FOV configurés, le lissage et les sons existants sont conservés.

La touche de zoom devient une bascule facultative, désassignée par défaut. Les affectations déjà enregistrées par le joueur ne sont pas effacées. L'ancienne option « maintenir pour zoomer » est retirée de l'écran ; sa clé de fichier reste compatible en lecture mais n'impose plus un maintien, qui contredirait le choix effectué à la molette. Les commandes N1 ne sont pas réécrites.

## Où changer les couleurs N1

**Mods → Crysis Legacy → Configurer → Interface N1 : couleurs et préréglages.**

- Choisir une couleur dans la palette, avec les curseurs Rouge/Vert/Bleu, ou saisir un code #RRGGBB. L'aperçu montre le liseré et un fond plus sombre.
- Saisir un nom puis « Enregistrer le préréglage », et « Appliquer » pour écrire la couleur et les préréglages dans la configuration.
- Cliquer sur le nom entre chevrons pour parcourir les préréglages, puis « Charger ». Le même nom remplace le préréglage correspondant ; « Supprimer » retire celui sélectionné.
- « Couleur d'origine » restaure #719671 sans effacer les préréglages. « Annuler » ou Échap quitte sans écrire les modifications en attente.
- Jusqu'à 16 préréglages créés dans cet éditeur, noms de 24 caractères maximum. Un code invalide désactive l'application et l'enregistrement du préréglage.

Le paramètre existait déjà dans nanosuit-client.toml, sous hud.classic.color, mais n'était pas proposé dans les écrans. Il reste au même emplacement. La liste hud.classic.presets contient les noms et couleurs. Le nouvel écran d'accueil donne aussi accès aux réglages complets existants et au rendu thermique/sons. L'écran complet du viseur possède également une catégorie « Interface Nanosuit 1 » avec un sélecteur de couleur et le chemin vers les préréglages.

Les consommateurs du thème N1 existants sont réutilisés : HUD, coins permanents, radial, jumelles et minicarte. Les couleurs d'alerte/menace et le traitement nocturne restent sémantiques. Le thème ne recolore pas le HUD N2. Les interfaces sont traduites en français et en anglais.

## Vérifications ciblées

Minecraft 1.21.1, NeoForge 21.1.265, Sodium 0.8.13, banc graphique RTX 3070 isolé ; aucun profil personnel modifié.

| Test | Résultat |
| --- | --- |
| held-sprint-jump | HOLD_JUMP_COMPLETE failures=0. Sprint maintenu 80 ticks depuis 1 % : reste à zéro, un seul événement sonore, pas de verrou de panne. |
| Relâchement puis reprise | Recharge en marchant à 48 %, puis boost actif à 41,25 %, sans nouvelle injection d'énergie. |
| Frontière serveur du saut N2 | Requête de lancement préparée : énergie 100 à 5 ticks, puis 90 à 6 ticks. La sonde contrôle le compteur de ticks pour appeler le handler autoritaire ; elle ne simule pas une frappe physique. N1 holdTicks=0 vérifié. |
| zoom-theme-final | ZOOM_THEME_COMPLETE failures=0 sur 13 étapes. |
| Molette N2 | Appel du vrai callback MouseHandler.onScroll, pas seulement d'un événement posté : hotbar reste case 3 en zoom montant, à la limite et en zoom descendant ; elle passe normalement à la case 2 une fois les jumelles fermées. Multiplicateurs observés ~0,10 / ~0,35, ceux de la copie de configuration. |
| Éditeur français | Palette/RVB/hex affichés et inspectés ; préréglage « Océan » #3388CC sauvegardé dans le TOML puis retrouvé par une nouvelle instance d'écran, chargé, réinitialisé et supprimé. |
| Annulation et validation | Couleur d'origine conservée après annulation ; application désactivée pour #GGGGGG. Préréglage conservé lors du retour à la couleur d'origine. |
| Rendu du thème | Capture réelle du HUD N1 bleu après application ; contours, fond sombre et habillage permanent inspectés. Écran à l'échelle GUI 4 et après réinitialisation de son affichage contrôlé. |

La première sonde UI attendait des ticks serveur dans un menu qui met volontairement le monde intégré en pause. Elle a été adaptée pour que ses étapes d'éditeur restent côté client. Cette première exécution n'est pas annoncée comme une réussite complète ; la sonde finale s'achève sans échec. Les avertissements antérieurs de ressources et de service d'authentification du banc sont distincts de ces contrôles.

Compilation offline réussie : build/controls-theme-final-build.log. Gradle indique test NO-SOURCE : les résultats ci-dessus viennent des sondes Minecraft. La validation mécanique précède un ajustement de fins de ligne du fichier de traduction anglais ; les classes de gameplay sont vérifiées identiques à celles du JAR final. Aucun nouveau test exhaustif des shaderpacks ou mods d'armes pendant cette correction.

Captures et observations : [evidence/controls-themes/index.html](evidence/controls-themes/index.html). JAR : build/deliverables/crysis-legacy-controls-themes.jar, avec son fichier .sha256. Aucun code des sondes inclus dans le JAR ; aucune fusion dans main.

SHA-256 : `b8430dcd5f8f9a30b3464ea3328de61255d90cff6fa71b0c4137870ffc27d771`. Commit mécanique : 20d6a59 ; commandes de zoom, éditeur et preuves dans le commit suivant. État Git vérifié après enregistrement.
