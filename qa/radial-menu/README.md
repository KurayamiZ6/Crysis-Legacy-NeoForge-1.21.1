# Menu radial N1 — banc ciblé

Le banc utilise une **copie** de `Cloak Integration Test`, les options et dépendances
du lancement graphique existant. Il ne modifie aucun profil Modrinth.
Le mod de test ne doit jamais être livré dans le JAR Crysis.

## Reconstruction des textures

1. `python qa/radial-menu/export_assets.py` : extrait les quatre symboles V3
   inchangés et écrit neuf SVG en couches dans les ressources du menu.
2. `node qa/radial-menu/render-assets.cjs <module-playwright> <chrome.exe> <répertoire-des-SVG>`
   : produit les PNG RGBA à leurs dimensions natives, sur fond transparent.

Les SVG éditables sont conservés avec les PNG ; aucun dessin des icônes n'est
reconstruit par des primitives Java.

## Lancement

Depuis la racine du dépôt, avec les chemins locaux appropriés :

```powershell
python qa/mode-overlay/run.py --run-name radial-local `
  --mod-jar build/radial-menu-build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar `
  --java-home '<Azul JRE 21>' --compiler-home '<JDK 21>' --sodium '<Sodium 1.21.1.jar>' `
  --probe qa/radial-menu/RadialMenuQa.java --probe-mod-id crysisradialqa `
  --width 1920 --height 1080
```

Le nom de campagne doit être nouveau. Les bibliothèques déjà préparées dans
`build/legacy-functional-qa` et `build/moddev` sont réutilisées. Le répertoire
de sortie est `build/mode-overlay-qa/<nom>/`.

Le banc enregistre les vrais événements clavier, le menu de production, ses
paquets et les modes confirmés sur le serveur intégré. Un petit processus Java
de test séparé fournit les frappes Windows : `Minecraft.Main` force AWT headless,
ce qui interdit `Robot` dans son processus. Le processus auxiliaire confirme
son démarrage avant le début des scénarios et s'arrête avec le banc. Le clavier
AZERTY est pris en compte pour le raccourci Armure. Garder la fenêtre de test
active et ne pas utiliser le clavier pendant cette courte campagne.

Les captures sont faites par `Screenshot.grab`, à partir du framebuffer réel,
sans reconstitution du décor ou de l'interface. La réflexion dans le mod de test
ne sert qu'à dater le début du flash et à capturer le bon secteur.

Pour rejouer uniquement le contrôle du raccourci Armure N2, ajouter :

```text
--jvm-arg=-Dradialqa.n2only=true
```

Les options `--probe`, `--probe-mod-id`, `--width`, `--height` et `--jvm-arg`
ajoutées au lanceur sont facultatives ; sa campagne graphique historique garde
ses valeurs par défaut.

## Paramètres livrés

La touche « Menu radial Nanosuit 1 (maintenir) » est **non attribuée** par défaut.
Le V utilisé par le banc n'est affecté que dans l'instance isolée.
Le fichier client `nanosuit1-radial-client.toml` règle :

| Paramètre | Défaut |
|---|---:|
| `scale` | 1 |
| `backgroundAlpha` | 0,30 |
| `hoverBackgroundAlpha` | 0,48 |
| `hexAlpha` | 0,62 |
| `hoverFlashSeconds` | 0,30 s |
| `hoverFlashIntensity` | 0,65 |

Le dernier retour utilisateur remplace la pulsation périodique V3 par un flash
unique à l'entrée d'un secteur. Son intensité décroît selon `(1 - t / durée)²`.
Le motif disparaît à la fin ; le contour et l'icône restent ambrés au survol.
Une icône ambrée seule indique le mode confirmé par le serveur.
L'anneau reste indépendant : 36°/s, horloge monotone, tour en 10 secondes.
