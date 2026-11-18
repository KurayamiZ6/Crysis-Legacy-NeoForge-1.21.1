# Intégration du menu radial N1 — 18 novembre 2026

Branche : `feature/nanosuit-1-classic`. Référence avant travail :
`a1ba810680445d5e16d2ab313000c9c618725ae6` (V3 déjà présente).
Le dernier retour utilisateur remplace la pulsation périodique initialement
demandée par **un flash hexagonal unique à chaque entrée sur un secteur**.

## Résultat livré

- Maintien d'une nouvelle touche configurable, non attribuée par défaut.
- Vitesse en haut, Force à droite, Camouflage en bas, Armure à gauche.
- Zone centrale neutre ; relâchement sans choix ou Échap conserve le mode.
- Le survol ne change pas le mode. Le relâchement envoie au plus une demande
  existante, seulement si le choix diffère du mode confirmé.
- Icône ambrée seule : mode confirmé par le serveur. Contour et fond ambrés :
  survol. Le flash jaune et le motif hexagonal s'estompent en **0,30 s**,
  sans répétition tant que le même secteur reste survolé.
- Anneau seul en rotation : 36°/s, période 10 s, horloge monotone indépendante
  des ticks et du nombre d'images. Le pointeur reste indépendant de cette rotation.
- Curseur et caméra gérés par l'écran Minecraft ; réticule masqué uniquement
  pendant l'ouverture du radial. Aucun remaniement du HUD énergétique.

Les quatre symboles V3 sont extraits sans retouche, puis rasterisés en couches
RGBA. La désactivation du test de profondeur pendant les dessins évite que les
rectangles transparents d'une texture ne cachent les couches suivantes.
Le rendu utilise les textures GUI habituelles, aucun nouveau shader.

## Dimensions

Facteur global par défaut : **1,0**. Échelle interne :
`hauteur GUI × 0,35 / 235`. Les panneaux latéraux et inférieur utilisent
uniformément 0,95 ; aucune déformation anisotrope.

La nouvelle référence mesure **2559 × 1596 px**. L'enveloppe du radial y fait
environ **530 × 560 px**, soit 35 % de la hauteur. À hauteur normalisée 1080 px :
environ **359 × 379 px**. Dans Minecraft : environ **364 × 382 px**, aux échelles
GUI 2 et 3 ; l'anneau mesure environ **120 px** de diamètre, comme la référence
normalisée. Les mesures sont approximatives aux contours et lueurs près.
Le centrage reprend le réticule ; le ratio d'écran différent ne déforme pas l'art.

[Comparaison visuelle](evidence/reference-comparison.png) : deux recadrages,
à la même hauteur d'écran normalisée. Les fonds et états actifs diffèrent.
Ce rapprochement de proportions ne constitue pas une identité graphique parfaite.

## Validation exécutée

JDK Oracle 21 pour compiler ; Azul Java 21 `javaw.exe` pour le client ;
NeoForge 21.1.265 et Sodium 0.8.13. Monde et configuration copiés dans
`build/mode-overlay-qa/`. Aucun profil personnel modifié.

| Scénario | Résultat |
|---|---|
| Compilation du JAR après le flash unique | PASS |
| Touche initialement non attribuée et quatre secteurs / zone centrale | PASS |
| Ouverture et captures réelles 1920 × 1080, GUI 2 et 3 | PASS |
| Survol Force / Vitesse sans changement de mode | PASS |
| Validation au relâchement des quatre modes, confirmation serveur + client | PASS |
| Un seul changement serveur par sélection ; même mode sans changement | PASS |
| Relâchement au centre ; annulation Échap | PASS |
| Caméra immobile pendant le déplacement du curseur ; souris reprise après fermeture | PASS |
| Retrait du casque ; N1 → N2 pendant l'ouverture ; remplacement par un autre écran | PASS |
| Demande réelle de camouflage à énergie 5, recharge différée dans le banc | PASS : refus, Armure conservée des deux côtés |
| Serveur : énergie insuffisante, équipement incomplet, demande pendant cooldown | PASS : refus |
| N2 : radial inaccessible ; raccourci Armure active → inactive → active | PASS |
| Flash : début, extinction et réentrée sur Vitesse | PASS |
| Maintien du même survol après extinction, sans réamorçage | PASS |
| Rotation : formule à 2,5 / 5 / 10 s et captures séparées de 2,509 s | PASS |
| Déconnexion, touche souris et scancode, conflit avec une autre touche affectée | NOT TESTED en jeu |
| Shaders, TACZ, Punchy, EMF/ETF, Fresh Animations, multijoueur distant | NOT TESTED lors de cette mission |

Captures du flash Force : intensité **0,9982 à 0,00027 s**, puis **0 à 0,3503 s**.
Entrée sur Vitesse : **0,9647 à 0,00535 s**. La limite logique à 0,30 s est
également vérifiée. Le motif ne reste pas affiché après le flash.

La campagne `radial-verified` contient **43 PASS et un faux échec du banc** :
l'assertion N2 attendait Armure active, alors qu'elle l'était déjà avant l'appui
et devait donc se désactiver. L'assertion a été corrigée pour tenir compte de
l'état initial. Seul ce chemin a été rejoué : `radial-n2-shortcut`, **8 PASS,
0 FAIL**, dont désactivation puis réactivation N2. Aucune correction de gameplay.

Autres ajustements du banc, sans modification du mod : AWT forcé headless par
Minecraft (entrée Windows déplacée dans un processus auxiliaire), attente de
disponibilité de ce processus avant les touches, adaptation AZERTY ; ajout d'un
délai de recharge dans le scénario à 5 pour ne pas laisser l'énergie remonter
au-dessus du seuil pendant l'attente. Les essais interrompus ou invalides ne
sont pas comptés comme des validations.

## Preuves et portée

- [Galerie des onze captures originales](evidence/index.html).
- [Journal ciblé principal](evidence/radial-verified.txt).
- [Rejeu ciblé N2](evidence/radial-n2-shortcut.txt).
- [Dimensions et empreinte du JAR](evidence/manifest.json).
- Journaux complets conservés sous `build/mode-overlay-qa/`.

Les images originales proviennent de `Screenshot.grab` du framebuffer Minecraft.
Seule l'image de comparaison est recadrée. La rotation a été observée sur deux
instants ; sa période exacte est vérifiée par la formule et le test logique,
pas par une vidéo chronométrée de dix secondes.

Aucune modification des coûts, cooldowns, contrôles serveur, données sauvegardées,
identifiants, modèles, sons ou effets de combinaison N2. Le protocole reste **6**
et le choix passe par `ToggleNanosuitModePacket`. Les tests d'intégrations
graphiques historiques n'ont pas été répétés ; aucune compatibilité nouvelle
n'est affirmée sur cette seule campagne.

## Fichiers de cette livraison

Dans `src/main/java/io/github/painworks/crysismod/` :

- `CrysisMod.java` : enregistrement du fichier de configuration client du radial.
- `nanosuit/client/NanosuitKeyMappings.java` : touche, ouverture N1, neutralisation
  des raccourcis directs et du réticule seulement pendant cet écran.
- `nanosuit/client/radial/Nanosuit1RadialScreen.java` : affichage et interaction.
- `nanosuit/client/radial/RadialLayout.java` : sélection, échelle et horloges.
- `nanosuit/config/NanosuitRadialConfig.java` : paramètres de présentation.

Ressources : `assets/crysismod/lang/{en_us,fr_fr}.json` et
`assets/crysismod/textures/gui/nanosuit1_radial/` : neuf PNG et leurs neuf SVG
(`green-fill`, `amber-fill`, `flash-fill`, `green-frame`, `amber-frame`, `hex`,
`icons`, `ring`, `pointer`).

Validation : `qa/mode-overlay/run.py` (options facultatives), les trois sources
de banc/export dans `qa/radial-menu/`, son README, ce rapport et ses preuves.

JAR livré : `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`.
SHA-256 : `ffb321f2ddc04961f3d128c02ea1fbb7268f3a3c2facb6965dd3c68b055446ee`.
La version précédente reste en récupération sous `build/radial-menu/pre-radial.jar`.

Finitions possibles après retour utilisateur : appréciation du flash en mouvement
et avec son shaderpack habituel, lissage des icônes à de faibles résolutions,
validation des affectations souris. Ne pas en déduire un besoin de retoucher les
textures validées ou le gameplay avant ce retour.
