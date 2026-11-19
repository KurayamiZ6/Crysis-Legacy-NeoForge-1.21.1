# HUD N1 — livraison des trois étapes, 19 novembre 2026

Branche `feature/nanosuit-1-classic`. Base constatée propre : `944201f`, jumelles déjà intégrées. Aucun audit général, sous-agent ou changement d'instance personnelle. Aucune fusion dans main.

## Commits

- `e6fad50` : minicarte N1 fonctionnelle sur le cache de terrain existant, objectif explicite, coexistence Diorama ciblée.
- `28ff658` : Threat Indicator N1 fondé sur le ciblage réel, présentation distincte et règles N2 conservées.
- Commit suivant : panneau TACZ N1, finitions et livraison de ce rapport (son identifiant est donné dans la réponse de livraison).

## État réellement intégré

Santé/énergie/mode confirmé, batterie nocturne, coins permanents, menu radial et jumelles existaient en jeu à la base. Ils ne sont pas assimilés à de simples maquettes. La carte et l'indicateur sont désormais réels ; les limites détaillées sont consignées dans `MINIMAP-RESULTS.md`, `THREAT-RESULTS.md` et `design/nanosuit-1-hud/MINIMAP-COMPATIBILITY.md`.

Le panneau de munitions N1 reprend les contours V3, leur inclinaison et l'ancrage du panneau énergétique validé. Compteur principal à trois chiffres minimum, zéros initiaux atténués, réserve plus petite, icône de balle. Pas de troncature des grandes valeurs. Symbole d'infini réservé à une réserve réellement illimitée. Bande transitoire lors du passage entre armes TACZ reconnues, avec leurs vraies silhouettes et la dernière silhouette connue.

Complément demandé pendant la validation : lecture du vrai sélecteur TACZ. SEMI affiche une balle ; AUTO plusieurs balles ; BURST un groupe souligné. UNKNOWN n'est pas présenté comme SEMI. Les nouvelles variantes réutilisent le tracé de balle existant. La forme exacte du repère BURST reste une approximation à comparer au Remastered, qui n'a pas été rouvert pour cette variante ; ne pas l'annoncer comme observée dans le jeu officiel.

La **ligne inférieure est celle des objets à lancer**, selon la nouvelle précision de l'utilisateur. Elle reste vide sans sélection et stock fiables ; ce n'est pas un emplacement décoratif ou une réserve de balles. Aucun compteur fictif de grenades ajouté. Voir `design/nanosuit-1-hud/THROWABLES.md` pour l'étude ciblée TACZ 1.2.0 et la distinction avec les munitions 40 mm.

## Données TACZ et repli

Adaptateur optionnel par API publique via réflexion, sans import de classes TACZ dans la compilation. Magazine + balle chambrée (selon le type de culasse), cartouches compatibles, boîtes de munitions et réserve dummy explicite. Créatif/boîte créative = réserve infinie. Rafraîchissement borné à 50 ms ; aucun parcours d'entités. Toute API manquante ou lecture invalide laisse la place au panneau d'origine, sans substituer zéro à une donnée inconnue.

Pour N1, annulation uniquement de `tacz:tac_gun_hud_overlay` quand le remplacement peut être rendu et que les options existantes l'autorisent. Les couches chauffe, éliminations, interactions et vitrines ne sont pas annulées. Les armes à chauffe ou à alimentation directe dans l'inventaire ne sont pas remplacées : leur présentation spéciale reste native. Autres armes/mods non pris en charge : aucun compteur déduit arbitrairement.

Quand les jumelles sont visibles, les cellules d'arme restent vides comme la référence. Mains vides : cadre sans fausses munitions. Le `000 / 0 / ∞` des poings Crysis n'est pas présenté comme un stock réel Minecraft. N2 conserve le chemin d'affichage et la politique TACZ antérieurs, y compris leur masquage historique de toutes les couches TACZ ; ce comportement n'a pas été étendu à N1.

## Typographie et cohérence

Réutilisation de l'atlas raster étroit existant, issu des ressources éditables Bahnschrift, sans distribution d'un fichier de police officiel. Largeurs et positions des chiffres fixées, contraste rapproché des compteurs principaux, pente commune au panneau. Les nouvelles silhouettes, contours, fonds sombres et compteurs suivent `hud.classic.color`, l'échelle suit `hud.classic.scale`. Alertes et menaces rouges/orange/jaunes restent distinctes. Aucun nouveau contour supérieur ajouté dans les jumelles.

## Validation ciblée

Compilation finale : `gradlew build --offline --console=plain`, réussie, journal `build/classic-final-build.log`. Contrôle des espaces Git effectué. Les preuves sont des captures du framebuffer Minecraft RTX 3070, pas des maquettes.

| Exécution isolée | Cas effectivement observés |
| --- | --- |
| `classic-map-base` | Terrain réel, quatre orientations, déplacement de 20 blocs, objectif absent/suivi/effacé, GUI 4/3, F1, équipement incomplet, N2 |
| `classic-map-diorama` | Diorama 0.10.2 : couche minimap annulée avec N1 prête, restaurée avec option propre désactivée/retrait/N2 ; waypoints non interceptés |
| `classic-threat-vanilla` | Douze états de données, calme/proximité sans cible/ciblage/camouflage/perte/expiration/plusieurs/dimension/retrait/N2 |
| `classic-threat-za` | Même séquence avec ZA 1.13.2 et CoroUtil 1.3.8, plus captures finales rouge/orange/jaune |
| `classic-final-tacz` | TACZ 1.2.0 : Glock 21/78 vérifié par assertion, puis 12/78, camouflage 73, vision nocturne, jumelles, mode Vitesse, M4A1, objectif, thèmes bleu/violet, GUI 4/3, arbalète vanilla, retour au panneau natif, N2 |
| `classic-final-contrast`, puis `classic-final-clean` | Contraste final et bande de changement d'arme ; quatre captures de Vitesse/M4A1/thème bleu/violet GUI 3. La dernière masque seulement le chat et le nom temporaire d'objet dans la fixture pour faciliter la lecture |
| `classic-final-no-tacz` | Version finale sans TACZ : N1 mains vides et carte prête, F1, retrait de la combinaison, puis N2 ; pas d'erreur de dépendance |
| `classic-final-fireselector-valid` | MP5A5 : AUTO → BURST → SEMI par `IClientPlayerGunOperator.fireSelect()`, donc commande cliente et synchronisation TACZ réelles, sans imposer les trois états au serveur ; trois assertions et captures réussies, chargeur/réserve inchangés 21/78 |

Les logs FINAL_CAPTURE montrent les nombres réels lus et l'annulation du compteur TACZ seul : N1 `gunCanceled=true`, `heatCanceled=false`; repli explicite `false/false`; N2 conserve `true/true`. Les scénarios préparent les objets, positions et cibles dans une copie du monde de test ; aucune valeur de maquette n'est affichée à la place des données réelles dans le mod livré. La fixture de menaces isole les cibles de zombies sans leur IA pour distinguer preuve de ciblage et perception spontanée.

Le scénario intitulé combat démontre un ciblage réel et le compteur 12/78 préparé dans l'objet ; il ne constitue pas un test de tir/rechargement complet de TACZ. La vérification sans TACZ précède uniquement l'ajout du sélecteur (lui aussi enfermé dans l'adaptateur optionnel) ; aucun fichier de la N2 n'a changé entre ces deux étapes.

Premier essai du sélecteur (`classic-final-fireselector`) non valide : GunItemBuilder avait créé une arme avec `UNKNOWN`, détecté par l'assertion. Correction de la fixture uniquement : initialiser son premier mode autorisé depuis GunData, puis utiliser la commande normale pour les deux changements suivants. Aucune conversion automatique de UNKNOWN en AUTO ajoutée au mod. Les noms historiques des trois phases du probe sont `waypoint-rifle`, `theme-blue`, `theme-purple-gui3`, mais ce parcours spécial ne change ni thème ni waypoint ; les preuves renommées `fire-auto/burst/semi.png` indiquent les vrais états.

## Comparaison et écarts restants

Références consultées : `design/nanosuit-1-hud/references/reference-status.png`, captures utilisateur Remastered et géométrie `generate-v3.py`. Correspondances : répartition carte gauche/panneaux droite, cadre polygonal, boussole mobile, marqueur joueur avec cône, mode actif, compartiments de munitions et hiérarchie des chiffres, triangle agrandi et transitions de couleur, habillage permanent.

- Terrain Minecraft de surface, pas une image satellite Crysis ; zones inconnues et résolution héritées du cache. Pas de nouveau moteur de grottes.
- Pas d'import de waypoint tiers ni flèches ennemies colorées individuelles ; objectif natif explicite seulement. Les données réseau de menace restent agrégées.
- Diorama testé uniquement dans la version indiquée. Xaero 26.6.0 non lancé (XaeroLib absent), JourneyMap absent. Aucun masquage automatique annoncé pour eux.
- Police approchée, contours raster sensibles aux très petites échelles ; pas de promesse d'identité pixel par pixel avec la police du jeu officiel.
- Bande d'armes limitée aux armes TACZ reconnues, durée de 1 s provisoire ; pas de reproduction du sélecteur de poings ou d'un inventaire de grenades inexistant. Ligne des objets à lancer réservée à une intégration documentée ultérieure.
- Finition par rapport au commit des menaces : la barre globale reste rouge pendant l'alerte résiduelle, comme la V3 ; elle ne suit plus la couleur jaune du triangle. Les premières captures de perte de cible du dossier `evidence-threat` précèdent cette correction de couleur seule ; géométrie et machine d'états inchangées.
- Les couleurs de thème très sombres restent au choix du joueur ; les alertes gardent leur palette fixe. Scène de vision nocturne déjà existante inchangée.
- Test de présence ZA et ciblage réel, pas validation de tous ses mécanismes sensoriels. Pas de nouvelles IA ou règles de camouflage.
- Pas de nouvelle validation de tous les viseurs, packs TACZ, mods de carte ou combinaisons de shaders. Les cas non pris en charge conservent leur rendu natif.

## Fichiers de cette dernière étape

`ClassicTaczData.java`, `ClassicWeaponHud.java`, branche N1 de `NanosuitTaczHudOverlay.java`, ressources `textures/gui/classic_weapon/`, exporteur `export_weapon_assets.py`, fixture `ClassicFinalVisualQa.java`, documentation et preuves. `ClassicWaypoint.java` reçoit uniquement le nettoyage à la déconnexion pour libérer la référence de session. Aucun coût, son, animation de jumelles, shader nocturne ou gameplay changé.

`ClassicThreatIndicator.java` reçoit également la correction de couleur globale décrite ci-dessus. La compilation finale inclut le sélecteur de tir et cette couleur corrigée.

Captures conservées dans `evidence-map/`, `evidence-threat/`, `evidence-final/`. Les autres captures et journaux complets restent dans `build/mode-overlay-qa/<exécution>/`. L'artefact construit est `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`.
