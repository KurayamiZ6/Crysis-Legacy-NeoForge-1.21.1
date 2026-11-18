# Retours utilisateur — menu, déplacement et camouflage N1

Référence : `89321b2`, branche `feature/nanosuit-1-classic`.

## Résultat

- Vitesse au repos rapproché de 90 à 74 unités du centre.
- Les **quatre** panneaux se déplacent de **16 unités**, environ 26 px en 1080p,
  vers l'extérieur. Vitesse sélectionné atteint ainsi son ancienne position.
- Flash de panneau de 0,5 s, puis fond/contour verts et disparition du motif.
  **Seule l'icône survolée** reste jaune ; le mode actif n'impose plus sa couleur.
- Marche, déplacement latéral, sprint, accroupissement et saut possibles pendant
  le menu. Lecture des raccourcis existants et activation temporaire de leur
  contexte d'entrée, restauré à la fermeture. Attaque/utilisation et caméra capturées.
- Flashs de changement de mode N1 portés à **0,5 s**, intensité et fondu conservés.
  Le flash de puissance N2 conserve sa durée de 0,45 s et sa couleur historique.

Les règles énergétiques, coûts, cooldowns, paquets, modèles et fibres ne changent
pas. L'anneau reste à un tour en 10 s et les icônes restent les mêmes ressources.

## Sons enfin identifiés

**Activation :** `Crysis Remastered/Game/sounds.pak`, banque
`sounds/interface/interface_combined.fsb`, échantillon **`cloak_activate`**.
Effet extrait à 44 100 Hz stéréo, 42 624 frames, durée 0,9665 s.
**Validé à l'écoute par l'utilisateur.**

**Voix :** `Crysis Remastered/Game/Localized/english.pak`, fichier
`languages/dialog/suit/male/suit_modification_engaged.mp2`.
La table **`languages/ui_dialog_recording_list.xml`** lui associe exactement
**« Cloak engaged »**. Il n'était donc pas trouvable par son nom avec « cloak ».
61 056 frames à 44 100 Hz stéréo, durée 1,3845 s.
**Validée à l'écoute par l'utilisateur.**

Sources et vérifications : `remastered-cloak-activation.json`,
`remastered-cloak-voice.json`, `cloak-voice-dialog-proof.json`.
Archives consultées en lecture seule ; aucun lancement du Remastered nécessaire,
aucun fichier de son installation modifié. Les voix/effets de désactivation et
les ressources N2 ne sont pas remplacés.

## Validation et portée

| Vérification | Résultat |
|---|---|
| Compilation Java 21 | PASS |
| Marche avant/pendant/après menu, déplacement latéral, crouch, sprint, saut | PASS sous vraies frappes, campagne `radial-feedback-03` |
| Accroupissement basculant sans répétition intempestive | PASS |
| Attaque et utilisation bloquées, caméra stable pendant sélection | PASS |
| Sélection d'un mode tout en marchant | PASS |
| Flashs rouge/jaune/bleu : captures initiales, intermédiaires (>0,3 s), finales (>0,55 s) | PASS |
| N2 : flash toujours bleu | PASS ; durée historique inchangée statiquement |
| Quatre panneaux de même amplitude, retour au vert, une seule icône jaune | PASS, captures `radial-feedback-render` |
| Restauration des sept contextes de déplacement à la fermeture | PASS dans la reprise visuelle |
| Trois effets Ogg : décodage, frames, fréquence, canaux et empreintes | PASS |
| Voix « Cloak engaged » : source MP2 vers Ogg | PASS, corrélation PCM 0,98867 |
| Iris/Complementary, TACZ/Punchy/EMF, serveur dédié | NOT TESTED ; chemins correspondants non modifiés |

`radial-feedback-03` a **30 contrôles fonctionnels PASS** et un contrôle de
collecte de captures FAIL : la phase pilotée par ticks pouvait se terminer avant
0,55 seconde réelle pendant le rattrapage de ticks. Ce contrôle a été corrigé
en imposant aussi une durée réelle minimale. La reprise de rendu séparée
`radial-feedback-render` donne **15 PASS, 0 FAIL**, sans refaire les scénarios
de déplacement déjà réussis. Elle utilise le dessin réel de l'écran de production
dans une enveloppe qui le maintient ouvert sans touche physique ; le réticule de
fond peut y rester visible. Les images ne sont pas une reconstruction SVG.

Les essais antérieurs ne sont pas comptés comme des réussites : un essai n'avait
pas ouvert le menu, et le premier passage de déplacement exposait l'inactivation
du contexte `IN_GAME` de Minecraft quand un écran est ouvert. Le rétablissement
temporaire et borné aux seules touches de mouvement corrige ce blocage.

Les captures des mouvements/flashs précèdent uniquement le dernier ajustement
d'indication jaune, qui a été repris séparément en rendu. Le remplacement audio
change deux ressources et leur provenance ; les événements et leur routage
confirmé par le serveur restent ceux déjà testés, sans nouveau test graphique requis.

Voir la [galerie](evidence-feedback/index.html) et les journaux sous `evidence-feedback/`. Les anciennes preuves
restent conservées dans leurs dossiers historiques et ne décrivent pas les
nouvelles consignes d'apparence. Le modpack personnel n'a pas été modifié.

## Fichiers

Production : `Nanosuit1RadialScreen.java`, `NanosuitKeyMappings.java`,
`NanosuitRadialConfig.java`, `NanosuitPowerFeedback.java` et les deux ressources
`sounds/classic/cloak_on.ogg`, `sounds/classic/cloak_activation.ogg`.
Validation/documentation : banc radial, importeur audio, vérificateurs de sources,
manifestes audio, README, présent rapport et preuves. La liste exhaustive est
dans `feedback-files.txt`. Le JAR livré reste sous
`build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar` ; empreinte dans le
manifeste des preuves. Les paramètres client sont `selectionFlashSeconds = 0.5`
et **`selectedPanelTravel = 16`**, qui remplace l'ancien `selectedPanelOffset`.

SHA-256 du JAR final :
`02709b9b7fb7a0b15aa7277e91ad2254c81d6efd60dd1f48b3ab2c837eb1d3a4`.
