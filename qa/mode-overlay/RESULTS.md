# Validation des couleurs N1 du 18 novembre 2026

Référence `cfa0496`, branche `feature/nanosuit-1-classic`. Banc graphique validé, Azul Java 21, Sodium 0.8.13, NVIDIA RTX 3070, NeoForge 21.1.265. Les quatre sessions utilisent la même copie initiale du monde et les mêmes réglages graphiques du banc.

## Diagnostic

La couche de fond animée, les impulsions et leur composition produisent des textures RGB cyan avec un masque alpha. Le backend par défaut est `DYNAMIC_TEXTURE_COMPAT`, avec le preset `UNSHADED_ARMOR_LAYER`. Le shader vanilla `rendertype_eyes.fsh` multiplie la texture par la couleur des sommets. Les rendus FPS et TPS n'utilisent pas exactement les mêmes passes de fond et de composition.

Une teinte jaune multiplie les canaux rouge et vert déjà inégaux du cyan, donnant un résultat vert. Une teinte rouge conserve seulement le canal rouge, faible dans beaucoup de pixels lumineux cyan. Ces zones deviennent très atténuées, laissant apparaître le matériau bleu-gris sous-jacent. Les captures de référence montrent une Force incomplète et une Vitesse verte. Une zone violette distincte n'a pas été isolée avec certitude dans ce banc réduit.

Le shader personnalisé possédait déjà une recoloration finale, mais elle intervenait après une première atténuation par les sommets. Elle ne couvre pas le backend dynamique utilisé ici.

## Correction

- Recoloration des seules textures de surimpression N1, après génération/composition des images originales. Aucun échantillonnage de la texture de base de l'armure.
- Intensité égale au maximum des trois canaux émissifs existants. La valeur de crête de chaque pixel est conservée, avec un alpha strictement identique. Reconstruction rouge `(I,0,0)` ou jaune `(I,I,0)`.
- Même générateur, centres d'impulsion, masques, paramètres, cadence et progression. Les trois caches de sortie suivent l'identité des images sources et leur révision. Ils sont libérés lors de la réinitialisation des caches.
- Le shader personnalisé reçoit une couleur de sommet neutre pour N1, avant sa recoloration finale existante.
- N2 retourne directement les textures originales. Le test logique vérifie cette identité.

## Résultats

| Scénario | Sans shaderpack | Iris + Complementary |
|---|---|---|
| Force FPS/TPS | PASS, rouge et motifs visibles | PASS, rouge et motifs visibles |
| Vitesse FPS/TPS | PASS, jaune | PASS, jaune doré après traitement du shaderpack |
| Animation | PASS, anneaux différents aux trois instants | PASS, anneaux différents aux trois instants |
| Armure N1 FPS/TPS | PASS, apparence sombre conservée | PASS, apparence sombre conservée |
| Armure N2 FPS/TPS | PASS, cyan original et anneaux | PASS, cyan original et anneaux |

Chaque session a enregistré 24 captures réelles par `Screenshot.grab` et terminé avec `MODE_OVERLAY_QA_COMPLETE captures=24`. Trois captures par vue sont espacées de 12 ticks. Les vues ont été examinées à plusieurs instants, sans fabriquer d'images.

Les sources de production compilent sous Java 21. `OverlayTintQa` réussit 1 792 cas couvrant tous les alphas avec plusieurs couleurs et contrôle le retour inchangé de la texture N2.

Shaderpack testé `ComplementaryReimagined_r5.9.3.zip`, Iris `1.8.14-beta.1+mc1.21.1`. Les avertissements de variables du shaderpack et les messages OpenGL de formats de profondeur sont également présents dans la référence. Ils restent hors périmètre. Euphoria Patches, les autres presets de rendu et les autres shaderpacks n'ont pas été rejoués. Les captures TPS montrent le joueur local de face, pas un second client.

## Preuves locales

Les chemins suivants partent de la racine du dépôt et restent exclus de Git

- `build/mode-overlay-qa/original/game/screenshots/` — avant, sans shaderpack.
- `build/mode-overlay-qa/corrected/game/screenshots/` — après, sans shaderpack.
- `build/mode-overlay-qa/original-iris/game/screenshots/` — avant, avec shaderpack.
- `build/mode-overlay-qa/corrected-iris/game/screenshots/` — après, avec shaderpack.
- Chaque dossier d'exécution contient le journal et le SHA256 du JAR utilisé.
- `build/mode-overlay-unit-qa/console.log` — test logique.
- `build/n1-mode-verified-build.log` — compilation.

Fichiers représentatifs `strength-fps-1.png`, `strength-tps-1.png`, `speed-fps-1.png`, `speed-tps-1.png`, `n2-armor-tps-1.png`. Les suffixes `0`, `1`, `2` permettent d'observer l'animation.

JAR candidat SHA256 `1400C2F7372FE5222E5CC1FF6F7AFA0DC414BB86756FD16E6F7799A1195B349B`.

## Fichiers de production

- `NanoGeoClassicOverlayTint.java` — recoloration et trois caches de textures dérivées.
- `NanoGeoArmorModeDynamicTextureCompatRenderer.java` — application aux sorties de fond, d'impulsions et de composition, puis nettoyage.
- `NanoGeoFirstPersonArmRenderer.java` — entrée neutre du shader personnalisé pour N1.
- `NanoGeoArmorModeOverlayLayer.java` — même traitement en troisième personne.

Le lanceur portable et la sonde sont dans `qa/mode-overlay/`. Les chemins de runtime, les mods locaux, les mondes, les captures et les fichiers natifs personnels ne sont pas inclus dans Git.
