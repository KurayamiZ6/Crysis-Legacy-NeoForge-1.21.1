# HUD classique — itération 3, 19 novembre 2026

Conception uniquement. Branche `feature/nanosuit-1-classic`, HEAD de départ `6f4dc9a`. Aucun changement Java, ressource de jeu, HUD N2, menu radial, shader, son ou protocole réseau. Aucun lancement du jeu nécessaire pour cette itération.

## Références et provenance

Les douze captures sont fournies par l’utilisateur, et non prises par Codex. `references/user-2026-11-19/manifest.json` indexe les sources, dimensions et SHA-256. Les originaux sont conservés dans `build/hud-classic-reference/user-originals-2026-11-19/`, les aperçus JPEG dans le dossier de conception. La V2 est conservée dans `v2/`.

Ces références remplacent les réserves des rapports antérieurs sur l’absence de vues exploitables des jumelles, des poings et des états d’alerte. Les difficultés historiques de contrôle du jeu ne constituent pas une analyse de son HUD.

## Corrections graphiques

| Élément | Observation de référence | Nouvelle maquette |
|---|---|---|
| Vie/énergie | Barres inclinées, coin gauche découpé, aile supérieure rejoignant le badge | Panneau retracé sur `detail-status.jpg`, cases éclair/croix et chiffres repositionnés |
| Pistolet | `021`, réserve `78`, zéro initial atténué, un symbole de balle | Variante dédiée, sans inventer la signification précise du symbole de balle |
| Poings au repos | `000`, réserve `0`, symbole infini, bras absents de l’écran | Variante dédiée sur scène sans arme ; aucune animation ajoutée |
| Changement d’arme | Petite liste transitoire au-dessus des munitions | État distinct, silhouettes provisoires des poings/pistolet ; durée non mesurée |
| Jumelles | Cadre large, deux échelles, ZOOM/X02, MODE/VIS, télémètre, HUD conservé | Cadre redessiné ; panneau munitions conservé avec case droite vide |
| Vision nocturne | Filtre vert et batterie jaune segmentée en haut à droite ; cumul avec jumelles | Deux variantes dont jumelles + vision nocturne, charge représentée vers la droite |
| Minicarte | Chevrons, cône du joueur, point vert lumineux | Joueur pâle et cône, ennemis orientés et colorés, point vert séparé |
| Menace | Barre latérale large et triangle qui change d’échelle/couleur | États repéré, perte de vue orange, jaune rétréci ; démonstration temporelle hors jeu |

Les chiffres et coordonnées sont simulés. Le télémètre utilise des mètres simulés. Les coordonnées du décor Crysis ne sont pas converties artificiellement en latitude/longitude Minecraft : la maquette propose X/Z/Y. Cette adaptation reste à valider.

## Trois informations distinctes

Ne pas dériver tous les éléments d’une seule couleur : les captures 09 et 10 montrent un contour ennemi jaune alors que la barre globale reste rouge. La capture 12 montre le triangle orange avec une barre rouge. Il faut distinguer :

1. L’alerte globale, représentée par la barre segmentée.
2. L’état de chaque ennemi, représenté sur la carte et par son contour aux jumelles.
3. La détection immédiate du joueur, représentée par le triangle.

Le lien exact entre ces données et les temporisations internes du jeu original n’est pas démontré par des images fixes. Le marqueur vert n’est pas assimilé arbitrairement à un ennemi.

## Transition décrite par l’utilisateur

Repérage actif : triangle agrandi rouge clignotant. Perte de vue : orange sans clignotement. Environ 500 ms plus tard : jaune, retour vers sa taille normale tant que le joueur reste indétecté. Cette chronologie vient de l’observation de l’utilisateur ; elle n’a pas été mesurée indépendamment. La démonstration propose une pulsation de 700 ms et une réduction de 350 ms : ces deux durées sont des paramètres de maquette, pas des mesures du jeu. Elle ne simule ni le moteur d’IA ni son comportement réel.

## Réutilisation technique et développements futurs

Réutiliser l’inventaire de `RAPPORT.md` : compositor, énergie/santé, minicarte existante, batterie N1, télémètre et intégration optionnelle TACZ. Aucun nouveau moteur cartographique ni batterie ne doit être créé. La batterie et les données de mode existent déjà ; les variantes de présentation ne sont pas encore intégrées.

La synchronisation actuelle d’une seule catégorie de menace ne suffit pas à garantir trois canaux indépendants et des états par ennemi. Une spécification des signaux serveur, de leur persistance et de leur disponibilité est nécessaire avant l’intégration ; pas de modification réseau dans cette mission. La recherche sonore Zombie Awareness ne prouve pas que le mob voit le joueur.

Pour TACZ, vérifier la disponibilité des compteurs et le sens de l’indicateur de munition avant de reproduire son comportement. La bande de changement d’arme nécessite une liste réellement disponible ; les silhouettes actuelles ne sont que des espaces graphiques. Définir aussi le traitement de la hotbar, faim, absorption et barre d’expérience, sans masquer les autres mods par défaut.

## Limites visuelles

Police Bahnschrift de substitution, icônes de modes reprises du radial existant sans le modifier. Carte et scène Minecraft illustratives ; personnage surligné représenté par une silhouette schématique explicitement simulée. Pas de résultat de shader ou de compatibilité graphique revendiqué. Courbure/perspective du HUD approchées par des polygones ; les captures montrent des variations d’échelle et de perspective. Le filtre de vision nocturne est illustratif. Le moment exact de retour au triangle calme vert reste à documenter. Le rôle des chevrons latéraux occasionnels aux jumelles n’est pas établi et n’est pas inventé.

## Validation de cette livraison

Les PNG sont rendus depuis les SVG dans Chrome ; `preview-check.json` consigne les contrôles de galerie. Aucun test Minecraft, TACZ ou IA n’est impliqué. Voir la galerie `index.html` et la comparaison `comparison-v3.png`. L’intégration reste suspendue à la validation graphique.

Régénération : `python generate-v3.py`, puis `node render.cjs <module-playwright> <chrome.exe>`. `generate.py` est le générateur historique V2, conservé pour comparaison ; utiliser le générateur V3 pour cette livraison.

## Précision utilisateur : objectif vert

Le point vert représente bien un objectif dans Crysis 1. Pour Minecraft, le réserver à un waypoint suivi facultatif, et l’omettre sans source réelle. Voir [la proposition de coexistence des minicartes](MINIMAP-COMPATIBILITY.md). Les réserves antérieures sur la signification du point vert sont levées par cette précision utilisateur.
