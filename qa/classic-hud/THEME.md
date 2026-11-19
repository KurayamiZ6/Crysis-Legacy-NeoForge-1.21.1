# Couleur personnalisable de l’interface N1

Ajout demandé pendant la première intégration du HUD. Le commit de base `87cb8d3` contient le panneau et ses preuves ; cet ajout ne modifie que la présentation et sa configuration.

Dans `config/nanosuit-client.toml`, rubrique existante `[hud.classic]` :

```toml
[hud.classic]
scale = 1.0
color = "#719671"
```

Ne pas ajouter une seconde rubrique si elle existe déjà. Exemple violet : `color = "#8C64FF"`. Une couleur hexadécimale à six chiffres est attendue. Le fichier est généré dans l’instance qui charge le mod ; aucune configuration personnelle n’a été modifiée pendant les tests.

La valeur par défaut conserve exactement la palette historique, avec ses différences de luminosité. Une autre valeur remplace les verts du panneau N1 : bordures, badge du mode, chiffres, santé et fond. Le fond utilise une version beaucoup plus sombre de la même teinte. Elle s’applique également aux éléments verts du radial N1 : cadres, fonds, icônes non sélectionnées et anneau. Les géométries, animations et opacités existantes sont conservées.

Restent indépendants : énergie bleue, alerte énergétique rouge, batterie nocturne jaune, sélection/flash ambrés du radial et pointeur blanc. Le filtre vert de vision nocturne reste un effet de vision, pas une couleur d’habillage. Aucun changement au HUD N2 ni aux composants vanilla. Les jumelles et le nouvel habillage de minicarte ne sont pas intégrés dans cette mission : leur future présentation pourra reprendre cette même valeur, sans recolorer les menaces ou les objectifs.

## Méthode

Les textures par défaut ne sont pas modifiées. Des masques neutres supplémentaires sont produits par `export_theme_assets.py`, puis teintés au rendu avec la couleur choisie. Leur luminosité distingue liserés, texte et fond. Le panneau possède une couche fixe séparée afin de conserver le bleu de l’énergie. Les teintes du radial sélectionné utilisent toujours leur chemin ambré d’origine. La couleur de dessin est rétablie après les couches pour éviter de teinter d’autres éléments du HUD.

Le réglage est lu par les renderers N1 ; pas de donnée enregistrée sur les objets, de paquet réseau ou de mécanique serveur supplémentaire. Les anciennes configurations sans cette clé utilisent la valeur par défaut.

## Validation

- Compilation Java 21 : PASS.
- Neuf tests de lecture RGB, couleurs limites et repli : PASS.
- Capture de la palette par défaut dans le banc RTX avec le nouveau code : campagne `classic-hud-theme`, quatorze états terminés. Cette première tentative n’a pas injecté la couleur de test : le caractère `#` non protégé était interprété dans le fichier d’arguments Java. Elle n’est pas utilisée comme preuve du violet.
- Campagne corrigée : `classic-hud-theme-violet`, argument Java entièrement entre guillemets. La sonde journalise la couleur effectivement appliquée. Les contrôles et captures sont dans `theme-evidence/`.
- `check_theme.py` vérifie pixels du thème, énergie bleue, alerte rouge, batterie jaune, absence de nouveau thème N2, radial, et identité binaire de toutes les textures originales. Le manifeste indique les résultats effectivement obtenus et le SHA-256 du JAR testé.
- La capture supplémentaire du radial utilise son renderer réel depuis la sonde ; elle ne constitue pas un nouveau test de maintien de touche, de déplacement ou de validation de mode.

Résultat final : **29/29 contrôles PASS**, dont 22 textures originales identiques octet par octet. La capture du radial montre également sa sélection ambrée, vérifiée par mesure de pixels. Neuf tests unitaires de palette PASS en complément.

Les preuves de la première intégration restent dans `evidence/` avec leur SHA propre. Le JAR le plus récent, incluant la couleur configurable, est `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar` ; son empreinte actualisée figure dans `theme-evidence/manifest.json`.

La validation graphique des autres mods et les futurs composants exclus restent hors périmètre.
