# Finitions radial et transitions N1 — 18 novembre 2026

Branche : `feature/nanosuit-1-classic`. Référence : `a79007b`.
Aucune modification du gameplay, des coûts, cooldowns, modèles, fibres lumineuses,
données sauvegardées ou paquets réseau. Protocole **6** conservé.

## Corrections

- Flèche orientée par `atan2` sur les coordonnées précises de la souris, sans
  arrondi aux quatre modes. Angle mémorisé entre ouvertures, sans sauvegarde disque.
  Sélection par quadrants et zone centrale neutre conservées.
- Flash de sélection **0,50 s** sur fond, contour et hexagones. Icône jaune stable.
  Le fond et le motif ambrés restent présents au survol après extinction du flash.
  Changer de secteur réamorce l'animation ; rester dessus ne la relance pas.
- Panneau et contenu se décalent de **2,5 unités**, environ **4 px en 1080p**,
  vers l'extérieur. Interpolation fluide de 0,18 s ; retour après désélection.
  L'anneau reste indépendant, un tour en 10 s.
- Flash de mode N1 uniquement après différence de mode confirmée par la
  synchronisation serveur : Armure bleu `73BAFF`, Force rouge `FF4B42`, Vitesse
  jaune `FFD44A`. Durée 0,20 s, alpha maximal 32/255. Camouflage aucun flash.
- Les retours de puissance N1 n'amorcent plus le flash de consommation. Leur
  indicateur Force HUD est conservé. Les paramètres N2 restent 0,45 s,
  alpha 64/255, bleu `73BAFF`.

Le paramètre `selectionFlashSeconds = 0.5` remplace `hoverFlashSeconds` :
l'ancien défaut généré à 0,3 s ne doit pas neutraliser la correction.
Vérifier cette nouvelle clé dans l'instance utilisée ; aucune configuration
personnelle n'a été écrasée. `selectedPanelOffset` permet d'ajuster le déplacement.

## Sons associés aux transitions réellement reçues

Les voix existantes ne sont pas réencodées. Comparaison de leurs échantillons
avec les MP2 anglais Crysis 1 : corrélation de 0,983 à 0,995 après alignement.
La voix de camouflage correspond à **`suit_full_cloak_on.mp2`** ; ce nom exact
est vérifié, sans prétendre avoir validé sa formulation à l'écoute.

| Transition N1 | Voix conservée | Effet ajouté |
|---|---|---|
| Vers Armure | `suit_maximum_armor.mp2` | Aucun effet Armure |
| Vers Force | `suit_maximum_strength.mp2` | Aucun effet Force |
| Vers Vitesse | `suit_maximum_speed.mp2` | `suit_speed_activate_07_good.wa` |
| Entrée Camouflage | `suit_full_cloak_on.mp2` | `cloak_chameleon_activate_12.wa` |
| Sortie Camouflage | Pas d'annonce « off » ajoutée | `suit_cloak_chameleon_101_r.wav` |

Les noms d'effets viennent des en-têtes FSB3, limités à 30 caractères, de
`Crysis Sound, VO & Music/Sounds/interface/interface_suit.fsb`. L'association du
dernier effet à la **désactivation est inférée** des deux effets disponibles :
**elle reste à confirmer à l'écoute**, ainsi que l'équilibre sonore final.
Les trois nouveaux événements `classic_*_activation/deactivation` ont un gain
ressource de 0,6, combiné au gain existant des transitions. Aucun son au survol.
Sortir du camouflage vers Armure joue naturellement l'effet de sortie du
camouflage puis la voix Armure, sans effet d'activation Armure supplémentaire.

La conversion IMA ADPCM est comparée à un décodeur WAV IMA indépendant,
libsndfile : corrélation pratiquement **1,0** sur les trois sons. Flux Ogg relus,
durées/canaux/fréquences exacts. Cela vérifie leur intégrité, pas leur rôle auditif.
Sources, empreintes et résultats : `classic-transition-sources.json`,
`classic-transition-audio-checks.json`, `classic-voice-verification.json`.

## Validation ciblée et limites

Compilation Java 21 réussie. Même JAR pour les deux campagnes :

| Campagne / contrôle | Résultat |
|---|---|
| `radial-polish-01` : menu, modes, serveur, audio et N2 | **75 PASS, 0 FAIL** |
| `radial-polish-visual` : reprises visuelles du menu avec F7 | **30 PASS, 0 FAIL** ; sous-ensemble, pas 30 cas indépendants supplémentaires |
| Flèche oblique à 63,435°, angle conservé à la réouverture | PASS |
| Frontière droite/haut, ancien panneau revenu à zéro | PASS |
| Flash : 0,0815 s → intensité 0,7005 ; 0,2559 s → 0,2383 ; 0,5535 s → 0 | PASS |
| Déplacement : 2,17 unités à 0,0815 s ; 2,5 à 0,2559 s | PASS |
| Icône opaque toujours RGB 255/196/60 aux quatre instants | PASS, contrôle des pixels des captures |
| Quatre confirmations de mode, relâchement, centre neutre, Échap | PASS |
| Survol et sélection du mode déjà actif : aucun son ni nouveau flash de mode | PASS |
| Camouflage refusé à énergie 5 : mode conservé, aucun son ni flash | PASS |
| Couleurs après activation, Camouflage sans flash, retours de puissance N1 sans nouveau flash | PASS |
| Événements sonores : Force 1 voix ; Vitesse 1 voix + 1 effet ; Camouflage 1 voix + 1 effet ; sortie vers Armure 1 effet + 1 voix | PASS, sans doublon |
| N2 : radial inaccessible, raccourci Armure, flash de puissance bleu | PASS |
| Ressources vocales et branche audio non classique N2 | Inchangées, contrôle statique |
| Écoute du résultat et choix définitif de l'effet de sortie Camouflage | **PENDING** |
| Iris/Complementary, TACZ, Punchy, Fresh Animations, serveur dédié | **NOT TESTED** pendant cette finition |

Le banc utilisait initialement V, également affecté au NanoVisor. Les captures
de transitions de cette campagne peuvent donc montrer cet autre élément du HUD.
Le conflit ne concerne pas une touche livrée : le radial est non attribué par
défaut. Le banc est corrigé pour utiliser **F7**, et seules les captures du menu
ont été rejouées, sans relancer la campagne serveur/audio. Aucun changement
produit n'a été nécessaire pour cette correction du banc.

[Galerie des captures réelles](evidence-polish/index.html), avec séquence à
plusieurs instants et images avant/après les activations. Les images originales
proviennent de `Screenshot.grab` en 1920 × 1080, GUI 2 et 3 ; seule la planche
comparative juxtapose des recadrages. Journaux ciblés et empreintes conservés
dans `evidence-polish/`. Aucun modpack personnel modifié.

## Fichiers et livraison

- Production : `Nanosuit1RadialScreen.java`, `RadialLayout.java`,
  `NanosuitRadialConfig.java`, `NanosuitPowerFeedback.java`,
  `ClientNanosuitPacketHandlers.java`, `ModSounds.java` et `sounds.json`.
- Ressources : deux couches nouvelles `flash-frame` / `hex-glow` PNG + SVG ;
  définitions SVG d'export mises à jour ; trois effets Ogg `sounds/classic/`.
  L'atlas PNG des quatre icônes est **identique octet pour octet** à la référence.
- Banc et documentation : `RadialMenuQa.java`, exporteur graphique, exporteur
  audio, deux scripts de vérification audio, README, résultats et preuves.
- Inventaire précis : [polish-files.txt](polish-files.txt).

JAR : `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`.
SHA-256 : `d8c453c8346d110dded72afc136156f98fdc504f635c4c127de2747d59b86b44`.
Précédent JAR conservé : `build/radial-menu/before-polish-a79007b.jar`.
Prochaine validation minimale : écoute et essai utilisateur du menu, sans
nouvelle refactorisation ni campagne générale de compatibilité.
