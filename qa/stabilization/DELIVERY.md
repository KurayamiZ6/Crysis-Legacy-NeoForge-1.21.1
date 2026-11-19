# Livraison de stabilisation — 19 novembre 2026

Branche : feature/nanosuit-1-classic. Base : e769ad6. Aucune fusion dans main, aucune installation dans un profil personnel.

## Commits de production

| Commit | Objet |
| --- | --- |
| 20bf446 | Équipement complet homogène obligatoire et nettoyage des états invalides. |
| d28b3a0 | Convention de munitions de mêlée et compteurs vides pendant les jumelles N1. |
| 7e1628d | Première restauration audio depuis le mod original, remplacée ensuite car voix féminine. |
| d3cd0ca | Préservation du comportement historique du viseur N2 dans l'inventaire. |
| 630860f | Éclairage réel N1 sans potion, teinte jaune-verte, vignettes limitées aux coins. |
| 01b6e2f | Voix masculine Crysis 2 reconnue et validée par l'utilisateur pour l'armure N2. |
| 3072726 | Colonne d'alerte extérieure gauche de la minicarte. |

Les rapports et preuves sont enregistrés par le commit documentaire suivant ces changements. Arbre de travail vérifié propre après ce commit.

## JAR final

build/deliverables/crysis-legacy-n1-stabilized-3072726.jar

SHA-256 : `8afdd0464d5f8ffee037ff379109818f0e7029f967f3f7c1dc82c00b5aefd1e5`.

Copie identique au JAR dev compilé utilisé pour stability-final-placement et stability-threat-placement. Contrôle du contenu : nouvelle lightmap présente, shader et voix identiques aux ressources finales, aucune sonde local/crysisqa dans le JAR livré. Compilation offline réussie dans build/stability-final-build.log. Vérification des deux voix dans le JAR : verify_armor_audio.py PASS.

## Preuves et limites

- RESULTS.md : reproduction avant/après, matrice d'équipement, états invalides, cooldowns et tests non exécutés.
- MELEE-HUD.md : reconnaissance et convention de mêlée.
- COMPATIBILITY.md : matrice N1/N2, versions exactes, lumière, shaders, modèles, IA et réserves OpenGL.
- evidence/gallery.html : dix captures réelles sélectionnées, dont nouvelle position de l'alerte.
- evidence/compatibility-observations.txt : marqueurs des sondes, empreintes des JAR et erreurs graphiques conservées.

Les contrôles ciblés effectués passent ; l'intégralité des combinaisons de mods, mort/réapparition, reconnexion disque et maintien physique du radial sous shader n'a pas été testée. Les erreurs OpenGL déjà présentes dans le banc Iris ne sont pas corrigées ni occultées. Ces limites empêchent d'annoncer une compatibilité universelle.
