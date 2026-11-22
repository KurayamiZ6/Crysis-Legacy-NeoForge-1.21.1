# Pilote de transposition du HUD N2

## Composant

Le pilote concerne l’hexagone qui entoure le triangle de la cible cadrée dans le viseur N2. La maquette exécute `drawOptic` dans un Canvas 2D de 960 × 540. Le tracé fermé utilise `hexagon(x,y,32)`, la couleur `#c7f7e1`, une épaisseur de 1,7 et le motif continu `setLineDash([3,3])`, avec les extrémités Canvas par défaut (`butt`).

L’ancien rendu Minecraft reconstruisait un hexagone de rayon 30, avec des traits de 0,9 et une remise à zéro du motif à chacun des six sommets. À cette échelle, le rapport longueur/épaisseur et la phase du motif produisaient des tirets fins.

## Méthode et reproductibilité

Depuis la racine du projet, exécuter `node design/nanosuit-2-hud/export-focus-outline.cjs`. Le script utilise Playwright et Chrome. Les variables facultatives `PLAYWRIGHT_MODULE` et `CHROME_BIN` permettent de désigner leurs chemins. Il charge la maquette, centre la cible et réexécute son propre tracé à 2× dans le Canvas. Il isole l’unique appel de trait pointillé et enregistre ses paramètres avec la capture de contrôle. La découpe est centrée sur le pivot et conserve exactement la résolution de sortie. Aucun pixel de scène, de triangle ou de viseur n’entre dans la texture.

- Source graphique : `design/nanosuit-2-hud/index.html`, fonctions `drawOptic`, `hexagon` et `plot`.
- Ressource : `src/main/resources/assets/crysismod/textures/gui/nanosuit2/optic-focus.png`, 144 × 128 px, RGBA transparent.
- Rendu : `Nanosuit2Optic.java` place les 72 × 64 unités logiques de la texture autour de la position projetée du triangle, avec le même facteur d’échelle que le viseur.

Le repère est un graphique fixe dont le pivot et la visibilité sont dynamiques. L’export convient à cette géométrie. Le cône appartient à la catégorie des graphiques dynamiques. Son Canvas place des cercles de rayon 1,65 à partir de 17 unités, espacés de 9,5, avec un demi-angle interpolé de 27° à 12°. Il demande le transfert de ces paramètres, de l’interpolation de visée et de la transformation du Canvas dans le viewport de la carte. Une seule texture figerait le cône. Les balayages CRT et le brouillage appartiennent à la catégorie des effets temporels.

## Validation et captures

Deux lancements du banc isolé sous Minecraft 1.21.1, NeoForge 21.1.265, Sodium 0.8.13 et RTX 3070 Laptop GPU. Même scénario, même monde de départ, fenêtre 1920 × 1080 et viseur à ×3 pour les deux captures Minecraft. La capture navigateur est aussi en 1920 × 1080, soit deux pixels par unité du Canvas. La séquence du banc a atteint ses cinq phases et le client s’est arrêté normalement. Compilation `gradlew.bat build --offline --console=plain` réussie après le changement.

Captures brutes et comparaison avec recadrage identique agrandi à 2× dans `build/n2-hud-live/hex-pilot/` :

- `navigateur.png`
- `minecraft-avant.png`
- `minecraft-apres.png`
- `comparaison.png`
- `stroke.json` pour les mesures de la source

Avec le même seuil de couleur menthe, la largeur mesurée aux sommets latéraux est de 130 px dans le navigateur, 121 px avant et 130 px après. Dans les 60 pixels centraux du côté supérieur, le motif dépasse le seuil sur trois lignes dans le navigateur et après correction, contre deux auparavant. Le côté supérieur se trouve aux lignes 483–485 dans ces deux captures, contre 487–488 avant. Les couleurs et l’anticrénelage peuvent varier aux bords. Le fond, les marqueurs et le scan évoluent séparément du contour exporté.

## À traiter ensuite

Cône de vision, autres contours indépendants, puis séquences CRT et brouillage. Le pilote livre uniquement l’hexagone de cadrage.
