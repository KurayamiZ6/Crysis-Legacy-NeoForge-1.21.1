# Finitions du HUD N1 — 20 novembre 2026

Base : 5361dd6, branche feature/nanosuit-1-classic. Références : les six nouvelles captures fournies dans le chat (Warhead : panneaux, carte, roquette, fumigène, lance-grenades et repères de joueur). Ce travail ne change pas les armes, dégâts, règles d'énergie, détection ou IA.

## Carte et transparences

La couleur hud.classic.color et les préréglages existants teintent désormais aussi le terrain. Les pixels restent ceux de la carte réelle du monde : conversion de luminance puis teinte, sans remplacer le terrain par une image. Avec #719671, l'algorithme de palette précédent est conservé exactement. Changer de thème recolore les pixels déjà disponibles ; aucun nouveau chargement de chunks pour cette opération. Le passage N2 restaure la palette normale à partir des pixels source non modifiés.

Deux traits verticaux d'un pixel de référence encadrent le joueur : au-dessus, de la limite supérieure du terrain jusqu'à l'approche du chevron ; en dessous, jusqu'au bas de la carte. Ils suivent le thème avec une faible opacité. La position, la boussole, le waypoint et la barre de menace ne sont pas déplacés.

Les flèches ennemies passent de 8×8 à environ 11×10 unités, avec un léger éclaircissement et deux halos faibles. Les teintes de menace sont conservées, indépendantes du thème. Le cœur a une opacité de 85/255 (~33 %), les halos 20/255 et 8/255 : les flèches restent transparentes, un peu moins que le cône du joueur.

Le cône garde sa teinte pâle et son opacité de 18 %. Son angle d'ouverture est conservé, mais ses côtés sont prolongés jusqu'au bord supérieur réel du terrain, juste sous la boussole. Le bord supérieur du cône suit la pente de la carte. Le chevron du joueur ne change pas de position. L'état de mélange est rétabli après le rendu des traits pour que le cône et les halos ne deviennent pas opaques.

## Santé et énergie

Les jauges ont maintenant leur origine à droite : à 50 ou 25 %, le remplissage reste collé au côté des symboles et s'étend vers la gauche quand la valeur augmente. Les pistes et remplissages ont une extrémité gauche biseautée, à l'intérieur du cadre existant ; le cadre général n'est pas redessiné.

Le thème ne recolore plus la santé : remplissage vert #7DCA2B, piste et symbole de santé préservés. L'énergie reste bleue #0076B0 et son alerte rouge existante reste fonctionnelle. Les nombres et autres éléments d'habillage continuent de suivre le thème. Les fractions restent calculées à partir des données réelles du joueur et de la combinaison. La découpe est arrondie à un pixel GUI, comme le rendu existant.

## Familles de munitions et grenades

Nouvelles sources SVG éditables et textures neutres : cartouche de fusil à pompe, roquette, projectile de lance-grenades, grenade à fragmentation, fumigène, flash et incendiaire. Les silhouettes sont une première approximation des captures de faible résolution, à affiner avec les meilleures références annoncées par l'utilisateur.

Les données publiques TACZ 1.2.0 déterminent les icônes de munitions. Reconnaissances par défaut : tacz:12g → shell, tacz:rpg_rocket → rocket, tacz:40mm → grenade_round. La catégorie publique shotgun permet aussi la cartouche. Les variantes SEMI/AUTO/BURST conservent l'information du sélecteur ; une famille inconnue garde l'icône générique. Les compteurs et réserves continuent de provenir de TACZ. Un fusil illisible par cet adaptateur garde son HUD natif, même si des grenades sont présentes.

Pour ajouter une famille d'un autre gunpack dans nanosuit-client.toml :

```toml
[hud.classic]
ammoIcons = ["tacz:12g|shell", "tacz:rpg_rocket|rocket", "tacz:40mm|grenade_round"]
throwableItems = ["autre_mod:grenade|frag", "autre_mod:fumigene|smoke"]
```

Les deux identifiants autre_mod ci-dessus sont des exemples à remplacer par des objets réellement installés, pas une compatibilité annoncée. La liste throwableItems est vide par défaut. Une autre possibilité est d'ajouter les objets aux tags d'items crysismod:hud/throwables/frag, /smoke, /flash et /incendiary via un datapack.

La ligne basse affiche uniquement des objets ainsi reconnus, avec le total réel de l'inventaire. Priorité au type tenu en main ou en main secondaire, puis au dernier type tenu encore disponible ; à défaut, premier type reconnu dans la hotbar, puis première catégorie disponible dans l'inventaire. Cette sélection de secours n'est pas annoncée comme le sélecteur de grenade interne d'un mod tiers. Aucun nom traduit ou fragment d'identifiant n'est deviné. Aucun compteur fictif quand aucun objet n'est reconnu. Les jumelles masquent aussi cette ligne.

Les résultats sont mis en cache 50 ms ; pas de recherche d'entités supplémentaire ni de nouvelle dépendance obligatoire. Aucun nouveau comportement de lancer ou de grenade n'est ajouté.

## Vérification

| Contrôle | Résultat |
| --- | --- |
| Compilation offline | BUILD SUCCESSFUL, build/hud-refinements-final-build.log. Gradle test NO-SOURCE ; validations ci-dessous réalisées séparément. |
| Palette d'origine | ClassicPaletteQa : 8 192 couleurs ARGB comparées à l'ancien calcul, résultat identique ; alpha conservé avec les thèmes testés. |
| hud-details-final | 12 états, HUD_DETAILS_COMPLETE failures=0 : thèmes par défaut/bleu/ambre, jauges 100/50/25/0, familles TACZ, ligne basse, GUI 3 et contrôle N2. |
| TACZ réel | SPAS-12 : 7 chargées / 29 en réserve, cartouche ; RPG-7 et M320 : 1 / 7, roquette et grenade_round ; AA-12 : cartouches + AUTO. Aucun mod de grenades à main certifié par cet essai. |
| Adaptateur grenades | Mapping uniquement dans la copie QA : boules de neige → smoke et œufs → frag. Totaux réels observés 10 puis 19 ; démontre le raccordement et le rendu, pas une compatibilité avec un mod réel de grenades. |
| Transparence | hud-map-alpha-final : 5 états sans échec après ajustement du mélange et de l'alpha ; hud-map-cone-final : 2 états finaux sans échec après prolongement du cône. Cette dernière capture, sans messages de chat, est la référence visuelle courante. |
| Pixels | check_refinements.py : 24/24 contrôles réussis. Bord droit identique à 100/50/25 %, progression vers la gauche, zéro vide, santé et énergie identiques entre thèmes, terrain teinté et cône à alpha 46/255 jusqu'en haut. |

Couleurs intérieures mesurées sur les captures : énergie (0,118,176), santé (125,202,43), identiques pour les thèmes par défaut, bleu et ambre. À 50 puis 25 %, le bord gauche avance vers la droite et le bord droit reste au même pixel. La portée et les états des contacts ennemis ne sont pas réévalués par ce travail ; les mobs de la scène graphique sont figés pour comparer le rendu.

Le premier essai hud-details préparait l'inventaire après l'équipement et effaçait donc l'armure au tout premier état ; il n'est pas utilisé comme preuve du rendu par défaut. La sonde finale corrige cet ordre et vérifie explicitement la présence de la N1. Les captures de la campagne complète précèdent les derniers ajustements du cône ; elles servent aux compteurs et aux jauges, pas à décrire la forme finale du cône. Aucun test exhaustif de nouveaux shaderpacks ou d'autres mods d'armes dans cette mission.

## Livraison

Sources graphiques et générateurs sous qa/classic-hud et assets/crysismod/textures/gui/classic_hud, classic_map, classic_weapon. Changements de présentation : ClassicNanosuitHud, ClassicMinimapRenderer, NanoMinimapTerrainTexture, ClassicHudPalette, ClassicTaczData, ClassicAmmoIcons, ClassicThrowableHud, ClassicWeaponHud et les deux listes de configuration.

Galerie : [captures sélectionnées](evidence/hud-refinements/index.html). Observations et empreintes des différentes versions de test : evidence/hud-refinements/observations.txt. JAR final : build/deliverables/crysis-legacy-hud-refinements.jar, avec fichier .sha256. Aucun code des sondes dans le JAR, aucune modification du profil personnel, aucune fusion dans main.

SHA-256 final : `f09fcfab832587a36831f9a9fce3d0a90360724ccd6e0828fdd4e26ea971905a`. Fichier identique à celui du dernier essai hud-map-cone-final. Les étapes complètes antérieures et leurs empreintes sont distinguées dans les observations pour ne pas présenter leurs anciennes captures de cône comme l'état final.
