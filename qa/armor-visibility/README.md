# Casque sans plastron — correction de visibilité

Référence : `1975b60`, branche `feature/nanosuit-1-classic`.

Cause : `PlayerHeadRenderHandler.onRenderPlayerPre` cachait la tête **et** le
buste/bras/manches dès qu'un casque Nanosuit était présent. Le casque seul
supprimait donc de la géométrie qui n'était pas remplacée par un plastron.

Correction ciblée dans **un seul fichier de production** :
`src/main/java/io/github/painworks/crysismod/nanosuit/client/PlayerHeadRenderHandler.java`.
Le casque cache la tête et sa couche extérieure ; seul le plastron Nanosuit
cache le torse, la veste, les bras et les manches. La sauvegarde/restauration
des indicateurs après rendu et le chemin du camouflage restent inchangés.

## Validation

Compilation Java 21 : PASS. Dans Minecraft NeoForge 21.1.265 avec Sodium,
profil de test isolé copié depuis le banc graphique existant :

- **26 PASS, 0 FAIL** : appels réels Pre/Post du gestionnaire sur le modèle du
  joueur, deux générations, casque seul, plastron seul, les deux, associations
  avec armure diamant, absence d'armure, générations mélangées et parties déjà
  masquées avant rendu. Vérification de la restauration exacte et des jambes
  qui ne doivent pas être modifiées.
- **7 captures réelles en troisième personne** : casque seul, plastron seul,
  casque + plastron pour chaque génération, puis retrait des équipements.
- Les captures casque seul N1 et plastron seul N2 ont été inspectées visuellement.
  Le buste/bras du skin et la tête sans casque apparaissent respectivement.
- Camouflage, première personne Punchy/TACZ et shaders : **NOT TESTED** durant
  cette correction. Le menu radial, les sons et les règles de gameplay ne changent pas.

Les résultats et captures sont dans `evidence/`. La compilation et le test
utilisent la copie de travail ; aucun modpack personnel n'a été modifié.

## Rejouer

Depuis la racine du dépôt, avec les chemins locaux adaptés et un nom de campagne neuf :

```powershell
python qa/mode-overlay/run.py --run-name armor-visibility-local `
  --mod-jar build/radial-menu-build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar `
  --java-home '<Azul JRE 21>' --compiler-home '<JDK 21>' --sodium '<Sodium 1.21.1.jar>' `
  --probe qa/armor-visibility/ArmorVisibilityQa.java --probe-mod-id crysisarmorvisibilityqa `
  --width 1920 --height 1080
```

Le banc ne simule aucune frappe et s'arrête après les captures. Le mod de test
n'est pas inclus dans le JAR livré, disponible sous
`build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`.
Son empreinte est conservée dans `evidence/manifest.json`.
