# Itération 2 — alignements et menace

18 novembre 2026. Conception uniquement ; mêmes branche et HEAD `feature/nanosuit-1-classic` / `6f4dc9a`. Aucun fichier de production modifié.

## Corrections graphiques

- **Munitions** : espacement accru entre chargeur courant et réserve, chiffres plus étroits et mieux proportionnés, premier zéro atténué ; placement des deux tailles de caractères sur une même ligne supérieure inclinée, plutôt que deux positions isolées. Cartouches replacées dans le panneau, sans dépasser sa découpe supérieure droite.
- **Boussole** : lettres descendues d’environ 11 px dans leur bandeau et alignées sur une seule base inclinée de pente −0,055 ; caractères condensés, plus proches de la référence. Enveloppe de carte ajustée à la mesure de la capture : haut à 809, bas à 994 au lieu de 798–1001, sur toile 1920 × 1080.
- **Threat Indicator** : rail d’alerte de 12 px, quatre segments de 8 px de large au lieu du trait de 3 px ; fond contrasté, segments vides visibles, contour de l’hexagone renforcé et triangle agrandi. Au calme, seule la section inférieure est allumée ; à forte alerte, les quatre sections sont allumées en rouge. Ces données restent simulées.
- **Valeurs santé/énergie** : chiffres légèrement plus hauts et condensés pour se rapprocher de la source, sans déplacement des panneaux ou changement des valeurs.

`iteration-comparison.png` compare explicitement référence / première maquette / nouvelle itération. Les deux gros plans V1 sont conservés dans `references/v1-proposed-*.png`. La galerie, les cinq SVG, les PNG et les planches ont été régénérés.

## Nouvelle tentative Remastered

Le jeu a été relancé depuis son installation existante et sa fenêtre a été retrouvée. L’outil de contrôle renvoie toutefois **`foreground window did not report a process id`** avant de fournir une capture. L’utilisateur a ensuite placé le jeu en **fenêtré** et demandé un nouvel essai : nouvelle sélection de la fenêtre, puis tentative d’activation/capture, même erreur.

Ce résultat prouve un blocage de l’outil de capture pendant cette session ; **il ne prouve ni un crash du jeu ni une cause AMD**. Le jeu a été laissé ouvert. Aucun changement de pilote, de réglage Windows ou du mod n’a été effectué. Le passage en fenêtré a été réalisé par l’utilisateur et n’a pas été annulé.

Les corrections sont donc comparées aux captures Remastered déjà fournies, pas à une nouvelle capture en partie. Les animations et l’orientation en mouvement restent à vérifier. La géométrie de la réserve avec une arme équipée reste approximative : la référence principale du panneau de munitions montre le joueur sans arme, et ne suffit pas à établir tous les états TACZ.

Une copie initiale des fichiers personnels de cette tentative est conservée dans `build/hud-classic-reference/retry-02/personal-profile-before/`. Elle n’est pas restaurée sur la session ouverte ni sur les changements volontaires de l’utilisateur. Le fichier `references/preservation-check.json` documente la **première étude**, pas cette nouvelle session.

## Validation

Rendu des cinq SVG dans Chrome, inspection des gros plans, quatre états de galerie et liens locaux. Aucun test Minecraft ni nouvelle validation de compatibilité. L’intégration du HUD reste suspendue jusqu’à validation graphique.
