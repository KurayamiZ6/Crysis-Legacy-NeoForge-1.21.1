# Banc des deux équipements Nanosuit

Lancer depuis le projet NeoForge avec PowerShell 7, Java 21 et les fichiers de lancement historiques dans `build/legacy-functional-qa` et `build/moddev`. Le banc utilise le JAR de production et les objets inscrits au registre. Il copie la sauvegarde créée avec l’ancien JAR lors du banc de cycle de vie. Choisir un nom de rejeu inédit.

```powershell
./qa/equipment/run.ps1 -RunName candidate `
    -ModJar build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar
```

`-OldWorld` permet de fournir une autre sauvegarde isolée, équipée des quatre pièces historiques et créée avec le mod de référence. La sauvegarde fournie reste en lecture seule pour le banc.

Le premier processus ouvre la copie de l’ancien monde. Le banc équipe de vrais objets dans les emplacements d’armure, appelle les points d’entrée serveur des pouvoirs et observe les états client transmis par le jeu. Une partie de la scène utilise le camouflage autorisé par les deux profils pendant un échange des quatre pièces dans le même tick. Une autre remplace une pièce avant de compléter le set et laisse passer le tick qui réconcilie le set mixte.

Le second processus rouvre la même copie du monde et vérifie la Nanosuit 2 équipée, l’usure de ses quatre pièces, les quatre objets historiques conservés dans l’inventaire et la réserve d’énergie du joueur. Les journaux et la provenance du JAR et du monde sont dans `build/equipment-qa/<nom>/`. Les quatre captures sans shader sont dans `game/screenshots/`. Le banc vérifie leur présence. Leur contenu demande une inspection visuelle. Ajouter `-RenderOnly` avec un nom de rejeu inédit pour reprendre uniquement les captures.
