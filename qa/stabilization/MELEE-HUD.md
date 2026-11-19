# Compteur des armes de mêlée

Ajout explicitement demandé pendant la stabilisation : convention Crysis `000 / 0 / ∞` pour les poings et armes de mêlée. Il s'agit d'une convention d'affichage, pas d'un inventaire de munitions réel. La ligne des objets à lancer reste indépendante et vide sans source fiable.

Reconnaissance : mains vides, épées, haches, masses, tridents ; catégories NeoForge de mêlée/lance/masse ; sinon attribut positif ATTACK_DAMAGE en main principale. Les armes à projectiles et les catégories à distance sont exclues. Un objet TACZ identifié par IGun est exclu, même s'il expose des attributs de mêlée : un fusil non pris en charge conserve son HUD natif.

Pendant les jumelles, aucune valeur de munitions ni symbole de tir n'est dessiné, et le compteur TACZ d'origine est masqué par sa couche précise lorsque le remplacement N1 est autorisé. La N2 conserve son rendu.

Test réel `build/mode-overlay-qa/melee-hud/`, RTX 3070, sans TACZ : poings, épée, hache, masse, trident, bâton portant un composant de dégâts de mêlée, bloc, arc, arbalète, jumelles avec épée et N2. `MELEE_HUD_COMPLETE failures=0`. Le bâton à composant vérifie le contrat générique des attributs ; ce n'est pas une validation de tous les mods d'armes. Aucun mod précis d'armes de mêlée ajouté ou annoncé compatible sans essai.

Captures sélectionnées dans evidence/melee-fists.png et melee-binoculars.png. Compilation réussie dans build/melee-hud-build.log.
