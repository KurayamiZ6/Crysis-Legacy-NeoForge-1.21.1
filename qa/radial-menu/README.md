# Menu radial N1 — banc ciblé

Le banc utilise une **copie** de `Cloak Integration Test`, les options et dépendances
du lancement graphique existant. Il ne modifie aucun profil Modrinth.
Le mod de test ne doit jamais être livré dans le JAR Crysis.

## Reconstruction des textures

1. `python qa/radial-menu/export_assets.py` : extrait les quatre symboles V3
   inchangés et écrit onze SVG en couches dans les ressources du menu.
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

Pour rejouer uniquement les captures du menu et le contrôle de sa flèche :

```text
--jvm-arg=-Dradialqa.visualonly=true
```

Les options `--probe`, `--probe-mod-id`, `--width`, `--height` et `--jvm-arg`
ajoutées au lanceur sont facultatives ; sa campagne graphique historique garde
ses valeurs par défaut.

## Paramètres livrés

La touche « Menu radial Nanosuit 1 (maintenir) » est **non attribuée** par défaut.
Le F7 utilisé par le banc n'est affecté que dans l'instance isolée. V est déjà
affecté au NanoVisor : ne pas le réutiliser pour ce test.
Le fichier client `nanosuit1-radial-client.toml` règle :

| Paramètre | Défaut |
|---|---:|
| `scale` | 1 |
| `backgroundAlpha` | 0,30 |
| `hoverBackgroundAlpha` | 0,48 |
| `hexAlpha` | 0,62 |
| `selectionFlashSeconds` | 0,50 s |
| `hoverFlashIntensity` | 0,65 |
| `selectedPanelTravel` | 16 unités graphiques, environ 26 px à 1080p |

Le flash reste unique à l'entrée d'un secteur. Son intensité décroît selon
`(1 - t / durée)²`. Le fond et le contour reviennent au vert et le motif de fond
disparaît après le flash. Seule l'icône survolée reste jaune stable, quel que soit
le mode réellement actif. Le panneau rejoint son décalage extérieur en 0,18 s et revient
lorsqu'il perd le survol. La flèche suit l'angle exact de la souris et conserve
son orientation entre ouvertures, en mémoire uniquement.
Le mode actif du serveur n'impose pas de couleur à une icône non survolée.
Vitesse est rapproché au repos (rayon 74 unités) ; sélectionné, il rejoint son
ancienne position (rayon 90). Les quatre panneaux ont la même course de 16 unités.
L'anneau reste indépendant : 36°/s, horloge monotone, tour en 10 secondes.

L'ancien paramètre `hoverFlashSeconds` n'est plus lu : une configuration générée
avec 0,30 s ne doit pas imposer involontairement cet ancien défaut. Vérifier
`selectionFlashSeconds = 0.5` dans la configuration de l'instance utilisée.
Aucune configuration de profil personnel n'est écrasée par le banc.
`selectedPanelTravel` remplace également l'ancien réglage `selectedPanelOffset`
dont le défaut de 2,5 unités ne correspond plus à la demande utilisateur.

Le radial réactive temporairement le contexte de ses sept touches de déplacement
et lit les touches réellement affectées. La logique de déplacement reste celle
de Minecraft ; les contextes originaux sont restaurés à la fermeture. Le menu
capture les attaques, l'utilisation et la caméra. Les options de marche/course
et l'accroupissement basculant restent gérés sans répétition de bascule par frame.

## Transitions audiovisuelles N1

Le flash de mode est déclenché par une différence de mode dans le paquet de
synchronisation serveur existant. Armure bleu, Force rouge, Vitesse jaune :
0,50 s, alpha maximal 32/255. Camouflage aucun flash. Les retours de dépense
énergétique N1 gardent leur indication de force HUD mais n'amorcent plus ce flash.
La branche N2 conserve ses 0,45 s, alpha 64/255 et son bleu historique.

Les sources et vérifications audio sont consignées dans
`classic-transition-sources.json`, `classic-voice-verification.json` et
`classic-transition-audio-checks.json`.

```powershell
python qa/radial-menu/export_classic_transitions.py `
  --bank '../Original games sounds/Crysis Sound, VO & Music/Sounds/interface/interface_suit.fsb' `
  --audacity 'C:/Program Files/Audacity'
python qa/radial-menu/verify_transition_audio.py
python qa/radial-menu/verify_classic_voices.py
```

Les bibliothèques Audacity déjà installées fournissent les décodeurs/encodeurs.
L'extraction ne modifie jamais la banque originale. Le décodeur IMA est comparé
à celui de libsndfile et les flux Ogg sont relus pour vérifier leur intégrité.
La correspondance des voix est contrôlée sur les échantillons décodés, avec
alignement du délai d'un bloc MPEG si nécessaire. Cela ne remplace pas l'écoute :
le rôle de désactivation attribué au second effet de camouflage reste à confirmer
auditivement. Aucun son de navigation au survol n'est ajouté.

### Remplacement approuvé du camouflage

L'effet d'activation et la voix proviennent désormais du **Remastered installé**.
L'effet utilisateur validé est `interface_combined.fsb::cloak_activate`.
La table `languages/ui_dialog_recording_list.xml` associe exactement « Cloak
engaged » à `suit/male/suit_modification_engaged`. La voix et l'effet ont été
confirmés par l'utilisateur. Voir `remastered-cloak-*.json` et
`cloak-voice-dialog-proof.json` pour les empreintes et la preuve de correspondance.
L'extraction FSB3 historique préserve maintenant l'activation Remastered approuvée.

```powershell
python qa/radial-menu/import_cloak_audio.py --game-root 'D:/Games/CrysisRemastered' `
  --output src/main/resources/assets/crysismod/sounds/classic/cloak_activation.ogg `
  --manifest qa/radial-menu/remastered-cloak-activation.json
```

Pour les voix vérifiées dans le Remastered, le script de comparaison utilise
`CRYSIS_REMASTERED_DIR` s'il est défini (sinon l'installation locale ci-dessus).
L'importeur peut également convertir un MP2 précisément identifié avec `--mpeg`,
`--output`, `--manifest` et son identifiant d'archive `--origin`.

Pour les nouveaux scénarios, ajouter `--jvm-arg=-Dradialqa.feedbackonly=true`.
Le scénario inclut la marche avant/pendant/après ouverture, le déplacement latéral,
le saut, l'accroupissement, le sprint et le blocage attaque/utilisation.
Ajouter `--jvm-arg=-Dradialqa.feedbackvisualonly=true` pour capturer uniquement
le dessin réel de l'écran de production, maintenu dans une enveloppe de test
qui remplace son contrôle de maintien. Cette variante n'utilise pas de frappe
système et ne valide pas les entrées physiques. Les phases attendent leur durée
en temps réel en plus du nombre de ticks, pour résister au rattrapage de ticks.
