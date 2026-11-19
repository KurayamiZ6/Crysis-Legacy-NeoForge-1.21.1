# Ligne inférieure des munitions — correction de référence

Précision du joueur, 19 novembre 2026 : le compartiment inférieur du panneau de munitions Crysis 1 affiche les **objets à lancer** (grenades, flash, incendiaires, etc.). Il ne constitue pas un emplacement décoratif vide ni une seconde réserve de balles.

Le manuel fourni le confirme également : `references/manual-hud-page11.png`, annotation « Number of grenades » pointant vers cette ligne.

Conserver le compartiment V3 et ses séparations. Lorsqu'une source compatible permettra de connaître l'objet à lancer sélectionné et sa quantité, y afficher son icône et le nombre réel. Distinguer la sélection de l'objet, son stock et les munitions d'une arme. Ne pas choisir arbitrairement une grenade simplement présente dans l'inventaire, ni inventer une commande de lancer. En l'absence de source ou de sélection fiable, laisser les cellules vides.

## Version actuellement étudiée

TACZ NeoForge 1.21.1 **1.2.0**, pack par défaut : présence d'un contexte d'animation `ThrowableAnimationStateContext`, mais aucune API publique d'objet à lancer sélectionné ni objet grenade à main identifié dans les classes/ressources ciblées. Ce contexte expose un objet courant, son nombre et ses ticks d'utilisation ; ce n'est pas un registre global de grenade choisie par le joueur.

La ressource `40mm_grenade` est une munition de lance-grenades : **elle ne doit pas alimenter automatiquement la ligne des objets à lancer**. Les munitions de ces armes restent dans leurs compteurs d'armes.

Aucun adaptateur de grenade tiers testé ou annoncé. L'affichage de la ligne inférieure reste donc réservé, sans faux compteur. Une future intégration devra valider la source de sélection et de quantité du mod concerné, son icône, les changements de type et l'épuisement du stock, sans modifier le gameplay ou ses raccourcis. Cette précision s'applique aussi aux maquettes existantes, même lorsqu'elles montrent une ligne vide.
