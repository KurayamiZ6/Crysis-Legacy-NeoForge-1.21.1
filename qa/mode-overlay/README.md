# Captures des modes Nanosuit

Le lanceur copie le monde et la configuration du banc graphique fonctionnel dans un nouveau dossier sous `build/mode-overlay-qa/`. Il ajoute une sonde locale qui équipe les combinaisons dans cette copie, sélectionne les modes et capture trois instants par vue. Les 24 captures couvrent Force/Vitesse N1 et Armure N1/N2 en première et troisième personne. Le client se ferme après les captures. Ces préparations de scène appartiennent uniquement à la sonde QA.

Prérequis

- Python 3.10 ou ultérieur.
- Le runtime Java 21 déjà validé sur la machine et un JDK 21 pour compiler la sonde.
- Les fichiers de lancement générés dans `build/moddev/` et `build/legacy-functional-qa/`.
- Une copie du monde `Cloak Integration Test`, `options.txt` et `config/` dans le dossier passé à `--template-game`.
- Le JAR du mod et le JAR Sodium local correspondant au banc validé.

Depuis la racine du dépôt, remplacer les paramètres entre chevrons par les chemins locaux

```text
python qa/mode-overlay/run.py --run-name candidat --mod-jar <mod.jar> --java-home <runtime-java21> --compiler-home <jdk21> --sodium <sodium.jar> --template-game <copie-du-banc>
```

Pour Iris, ajouter `--extra-mod <iris.jar> --shader-pack <shaderpack.zip>`. Les mods supplémentaires peuvent être indiqués avec plusieurs options `--extra-mod`.

Le lanceur conserve les paramètres VM et graphiques du banc existant. Les seuls réglages ajoutés avec `--shader-pack` activent ce pack dans la nouvelle copie. Aucun monde, runtime ou réglage personnel n'est inclus dans Git. Le nom d'exécution doit être nouveau.

Les preuves se trouvent dans `console.log`, `stderr.log`, `input-sha256.txt` et `game/screenshots/`. Attendre `MODE_OVERLAY_QA_COMPLETE captures=24` et vérifier les images. Ce marqueur confirme la prise des captures, leur appréciation visuelle reste nécessaire.

Le test `OverlayTintQa.java` vérifie séparément la conservation de l'alpha et de l'intensité, les canaux rouge/jaune et le retour de la texture N2 originale. Il se compile avec les classes du mod et le classpath du banc, puis se lance avec la classe `io.github.painworks.crysismod.client.nanogeo.OverlayTintQa`.
