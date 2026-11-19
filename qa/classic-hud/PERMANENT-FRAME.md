# Habillage permanent N1 — correction de visibilité

Référence `da783e6`, branche `feature/nanosuit-1-classic`.

Les deux traits supérieurs appartiennent à l’habillage permanent. Export exact du tracé V3 `M125 87L137 73H183M1735 73H1778L1792 87`, trait 1,8 et opacité 45 %. Deux petits SVG locaux évitent une texture plein écran inutile ; leurs PNG RGBA et masques de thème préservent l’alpha. Les proportions suivent la hauteur de l’écran et l’échelle N1, avec ancrage aux deux bords supérieurs. La couleur personnalisable précédente reste applicable.

## Organisation du rendu

`ClassicNanosuitHud.onPermanentFrame` est une passe GUI permanente, appelée une seule fois en fin de GUI. Elle vérifie combinaison complète N1, HUD actif et interface non masquée. Aucune condition de mode, de vision nocturne ou de viseur. Ni la batterie, ni la vision nocturne, ni le viseur ne dessinent ces ressources.

La première campagne a révélé que la vignette du NanoVisor historique assombrissait un dessin placé au niveau de la hotbar. La passe définitive est placée après les overlays conditionnels, à priorité LOW ; la sonde capture à LOWEST. Cela conserve l’opacité et la couleur propres des traits avec le viseur actif. Le renderer du panneau principal et les chemins N2 ne changent pas. Pas de nouvelle interface de jumelles ou de minicarte.

## Validation ciblée

- Compilation Java 21 : PASS.
- Banc isolé NeoForge/Sodium sur RTX 3070 : campagne finale `classic-hud-frame-final`, 17 captures, arrêt normal.
- Présence des deux traits en Armure, Force, Vitesse, Camouflage, vision nocturne active, recharge de batterie, GUI 2, retour après masquage et NanoVisor existant actif.
- Absence avec combinaison incomplète et interface masquée ; le test règle directement le même état `hideGui` que F1, sans simuler une frappe physique.
- N2 : comparaison visuelle ciblée, ancien HUD et habillage d’armure conservés ; aucune revendication sur une campagne complète des compatibilités graphiques.
- Vérification des alphas exportés, de leur conservation dans les masques de thème et d’un unique point d’appel.

Résultat final : **16/16 contrôles PASS**, 17 captures terminées ; le SHA-256 du JAR courant correspond au candidat capturé.

Le premier calcul de contraste supposait un trait toujours plus sombre que le décor. Avec la passe finale au-dessus de la vignette du viseur, le trait éclaircit au contraire ce fond sombre : le critère utilise donc l’écart absolu. La capture montre bien les deux traits, et cette correction concerne uniquement le test d’image.

Résultats mesurés, SHA du JAR et captures réelles : `frame-evidence/manifest.json` et `frame-evidence/index.html`. `check_frame.py` contrôle la présence des traits contre le ciel local des scènes QA ; ce n’est pas un détecteur générique utilisable sur n’importe quel décor. La présence N2 n’est pas déterminée par ce test de contraste, car son effet d’armure comporte déjà des motifs aux coins.

Le viseur existant est activé directement par la sonde pour tester la composition graphique. Aucun changement de ses entrées ou de son gameplay. Les futures jumelles N1 devront réutiliser cette couche permanente sans la redessiner.

Fichiers concernés : `ClassicNanosuitHud.java`, exporteurs d’assets et de masques, six ressources frame (deux SVG, deux PNG d’origine et deux PNG de thème), extension optionnelle de la sonde et vérificateur/preuves. Le vérificateur de thème exclut ces nouvelles ressources de sa comparaison avec la référence historique qui ne les contient pas.

Aucun changement de gameplay, de son, de protocole ou de HUD N2. Les rapports des missions précédentes restent des preuves historiques avec leurs propres empreintes. Le JAR courant est `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`.
