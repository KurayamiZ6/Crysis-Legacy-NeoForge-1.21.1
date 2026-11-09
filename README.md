# Crysis Legacy sur NeoForge 1.21.1

Port expérimental du mod reconstruit. Il cible Minecraft 1.21.1, NeoForge 21.1.265 et Java 21. Les quatre pièces de la Nanosuit conservent leurs identifiants `crysismod:nanogeo_nanosuit_*`.

## Construction et essai local

Depuis ce dossier, avec un JDK 21 et les dépendances déjà en cache sur cette machine :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
.\gradlew.bat build --offline
.\gradlew.bat runClient --offline
```

L’archive est créée dans `build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar`. Le client de développement utilise `run/client`, avec sa propre sauvegarde et ses propres réglages. Les dépendances locales utilisées sont Cloth Config 15.0.140 et TaCZ NeoForge 1.2.0. Elles sont fournies séparément dans `libs`.

## État du port

- L’énergie et le mode de la Nanosuit sont enregistrés avec un attachement NeoForge. Les dix messages d’origine ont été adaptés au réseau NeoForge.
- Le rendu, l’interface, les registres et les événements ont été adaptés aux API 1.21.1. Les cibles des mixins utilisées au démarrage ont été corrigées lors des essais client.
- Les tags thermiques suivent les chemins 1.21.1, les références des ressources ont été contrôlées et les 37 programmes GLSL ont été compilés et assemblés sur la machine de test.
- Dans un monde créatif neuf, le client de développement a affiché la Nanosuit complète en troisième personne, le bras en première personne, le HUD d’énergie et la minicarte. Le camouflage, le mode armure avec son halo, le NanoVisor avec mesure de distance et la nanovision thermique ont été activés à l’écran.
- Le correctif de lunette TaCZ fait partie de `crysismod`. Les réglages de rendu se trouvent dans l’écran de configuration du mod principal.
- Avec le JAR TaCZ 1.2.0 fourni, le client a traversé le rendu natif de la lunette et capturé l’optique animée en nanovision après la correction du tampon partagé. Le journal `run/qa-final/logs/latest.log` enregistre ces deux passages ainsi que la poursuite de la session. Le comportement avec un pack de shaders externe reste à tester.
- Le camouflage pilote la visibilité et la détection séparément. Un mob peut repérer le joueur camouflé à 1,5 bloc à l’arrêt ou accroupi, 4 blocs en marche et 9 en sprint, sous réserve des règles de perception propres au mob. Infliger des dégâts rend le joueur repérable jusqu’à 12 blocs pendant deux secondes. En dehors de la portée de détection, la cible et les souvenirs de combat associés sont oubliés. Les pas accroupis sont silencieux et ne produisent pas de vibration de pas.
- Dans le monde isolé `Cloak Integration Test`, le serveur a vérifié directement la cible des zombies. Le premier ciblait le joueur sans camouflage, puis l’a perdu après activation. Le second n’a pas ciblé le joueur camouflé avant de subir un coup. Aucun effet d’invisibilité ne figurait dans les effets actifs du joueur. Les distances en marche et en sprint n’ont pas été mesurées dans ce scénario. Les résultats sont consignés sous `CLOAK_QA` dans `run/qa-final/logs/latest.log`.
- Avec TaCZ 1.1.8-hotfix-r6, le rendu thermique d’un M4A1 équipé d’une lunette ACOG a été vérifié après séparation des tampons de rendu physique et thermique. Avec TaCZ 1.2.0, le client de test a chargé les raccords actualisés et utilisé le pont de rendu thermique de l’arme. La visée ACOG avec la nanovision active reste à vérifier avec la 1.2.0.

Les essais ont été réalisés avec le client de développement Gradle et son serveur intégré. L’installation du JAR dans un lanceur normal, le serveur dédié, la migration de l’énergie depuis Forge, le passage en visée avec l’ACOG, les packs de shaders externes et les autres versions de TaCZ restent à vérifier.

Le journal `client-port-4.log` contient les essais de la Nanosuit et le diagnostic du premier essai TaCZ. `client-port-5.log` couvre le second essai TaCZ après correction.

## Compatibilité des modèles et animations

Le camouflage réfracte le décor à partir de la surface visible la plus proche et ajoute un léger reflet bleu animé. La profondeur du décor masque la silhouette en troisième personne. Dans `run/qa-cloak-visual`, les captures `refraction-02` à `refraction-10` couvrent le bras Punchy à main vide, les outils, le joueur avec un obstacle devant lui, le M4A1 avec ACOG en visée et le retour au rendu normal. La nanovision reprend son rendu thermique avec le camouflage actif en première et troisième personne. La visée conserve le zoom et le carter de l’ACOG, avec le réticule projeté masqué pendant la réfraction. Le journal `client-cloak-refraction-verified.log` couvre ce test.

La réfraction fonctionne sans Iris et sous Iris 1.8.14-beta.1 avec Complementary Reimagined r5.9.3, avec ou sans Euphoria Patches 1.10.5. Les tests ont couvert la première et la troisième personne, l’occlusion derrière un bloc et la priorité de la nanovision. Le repli invisible reste disponible quand la composition n’est pas compatible. Les captures sont dans `run/qa-cloak-iris/screenshots`.

La boucle `nanovision_ambience` de Crysis 3 peut se superposer à celle du camouflage. Les effets ponctuels sont à 80 % de leur niveau d’origine. Les curseurs de **Crysis Legacy → Nanosuit audio** modulent ces niveaux.

Un dégât réellement infligé par une entité non alliée coupe le camouflage et déclenche la panne à 0 %. Le test a confirmé que les dégâts environnementaux laissent le camouflage actif. Pour un tir TaCZ confirmé, une arme sans silencieux coupe le camouflage et coûte 20 %, une arme silencieuse coûte 7 % et maintient le mode tant qu’il reste de l’énergie. Le Predator Bow `sfms:tb23` du pack MS-Mobius n’ajoute aucun coût au tir et conserve le camouflage. Avec la tenue complète, ses projectiles passent de 25 à 37,5 points de dégâts dans le test, en mode neutre comme en camouflage. Le journal final est `client-cloak-iris-final.log`.

Le test dans `run/compat-retest` a chargé Fresh Animations 1.11.2 avec EMF 3.3.12 et ETF 7.2.7, Punchy 3.2 et TaCZ 1.2.0. La nanovision a coloré le zombie et le squelette de Fresh Animations. La capture de secours utilise la géométrie fournie par le moteur de rendu et se limite aux 64 entités visibles les plus proches. Si une géométrie ne peut pas être capturée, son rendu reste neutre en nanovision.

Avec Punchy, le bras de la Nanosuit suit la pose animée fournie par son API. Le test a affiché le bras thermique avec une pioche, puis l’effet du mode armure. Les captures `qa-02` à `qa-04` se trouvent dans `run/compat-retest/screenshots`. Les extensions de Fresh Animations du profil principal n’ont pas été testées individuellement.

## Jauge de menace

La jauge suit la position de la minicarte et synthétise les mobs proches. Vert signifie qu’aucun indice de menace n’a été détecté. Jaune indique un hostile proche et en ligne de vue qui ne cible encore personne. Orange indique une poursuite sans ligne de vue, une mémoire de combat, ou une recherche récente dans le secteur. Rouge exige que le mob cible réellement ce joueur et conserve la ligne de vue. Après une poursuite, l’état orange reste affiché jusqu’à trois secondes. Le [manuel de Crysis 3](https://eaassets-a.akamaihd.net/eahelp/manuals/crysis-3-manuals_PC.pdf) décrit les états vert, orange et rouge.

Le serveur évalue les mobs dans un rayon maximal de 32 blocs tous les dix ticks et synchronise le niveau au changement. Avec Zombie Awareness 1.13.2, la jauge lit aussi le dernier déplacement d’enquête du mob si son chemin est encore actif et que le mob est proche. Ce signal indique une recherche dans le secteur, sans attribuer au joueur le bruit, l’odeur ou la lumière qui l’a déclenchée. La jauge ne modifie pas les décisions de l’IA.

Le test `client-power-jump.log` confirme la séquence vert, jaune, rouge, orange derrière un mur, puis vert. Les captures `threat-red.png` et `threat-orange.png` se trouvent dans `run/qa-power-jump/screenshots`. Le déclenchement d’une enquête de Zombie Awareness par un bruit ou une lumière n’a pas été testé dans ce scénario.

## Sources audio de la Nanosuit

Les onze fichiers Ogg Vorbis proviennent de la collection locale `Original games sounds`. Les pistes FSB ont été extraites par leur nom et les fichiers convertis ont été entièrement redécodés pour vérifier leur format et leur durée.

| Son dans le mod | Jeu et source |
| --- | --- |
| Voix du camouflage | Crysis 2, `Languages/English/dialog/suit_male/suit_voice_stealth_mode.mp2` |
| Voix du mode armure | Crysis, `Languages/English/dialog/suit/male/suit_maximum_armor.mp2` |
| Voix de la nanovision | Crysis 2, `Languages/English/dialog/suit_male/suit_voice_nanovision_enabled.mp2` |
| Activation et sortie du mode armure | Crysis 2, `Sounds/interface/interface_suit_static.fsb` → `armor_activate`, `suit_armor_off` |
| Activation et sortie de la nanovision | Crysis 2, même banque → `nanovision_select`, `nanovision_deselect` |
| Saut de la Nanosuit | Crysis 3, `C3/Sounds/interface/interface_perks.fsb` → `verticality_jump` |
| Activation, sortie et ambiance du camouflage | Crysis 2, `Sounds/interface/interface_suit_static.fsb` → `cloak_activate`, `suit_stealth_deselect`, `suit_cloak_loop` |

La boucle de camouflage de quatre secondes est locale au joueur et suit le volume de la catégorie Joueurs. Elle cesse à la sortie du mode, à la mort, au retrait d’une pièce ou à la déconnexion.

Les gains de base des ambiances de camouflage et d’armure ont été réduits de 30 %. La nanovision ajoute une boucle locale indépendante, issue de la piste `nanovision_ambience` de Crysis 3 dans `C3/Sounds/interface/interface_suit_static.fsb`. Elle peut jouer avec l’ambiance de camouflage. Le curseur Ambiances reste commun à ces pistes.

Le super saut utilise la touche **Saut** de Minecraft, maintenue 4 ticks au sol (environ 0,2 s). Un appui bref produit le saut normal au relâchement quand le super saut est disponible. En visant un rebord proche, l’aide relève le joueur puis le guide sur le dessus si la trajectoire est dégagée. Les déplacements restent soumis aux collisions du monde.

Le journal `client-mantle.log` confirme les six ticks sur la touche G réassignée, un atterrissage stable sur un rebord de cinq blocs et le refus d’un rebord obstrué. L’essai à quatre blocs s’est arrêté légèrement sous le dessus. Le JAR final augmente la marge de relèvement. Son build a réussi, mais deux plantages du pilote graphique AMD au démarrage du client ont empêché de rejouer cet essai sur le JAR final. Le protocole 4 requiert le même JAR côté client et serveur.

Le flash bleu démarre à 25 % d’opacité et s’estompe en 450 ms. Il se déclenche lors d’un usage de force et à l’activation du camouflage ou de la nanovision. L’icône **SPRINT** d’origine signale la dépense de sprint/nage et s’allume brièvement lors d’un saut ou d’une frappe renforcée. Le poing utilise `player_punch_strength` de la banque `interface_suit_static.fsb` de Crysis 3. La crosse utilise les trois variantes `hit_feedback_melee_01–03` de `physics_bullets_impacts_static.fsb`, des impacts de mêlée génériques.

Les boucles d’armure, de camouflage et de propulsion sont calibrées sur la nanovision, puis rehaussées de 6 dB par rapport à elle. La boucle `suit_speed_run_loop` de la banque `interface_suit_static.fsb` de Crysis 2 accompagne le sprint renforcé terrestre confirmé par le serveur. Elle est indépendante des sons de pas.

Sous Iris avec un pack de shaders actif, l’activation de la nanovision n’affiche pas le flash bleu plein écran. Le test `client-holo-iris.log` a confirmé ce repli avec Complementary Reimagined et Euphoria Patches, ainsi que le flash sans pack. Avec TaCZ 1.2.0, le test a aussi confirmé que les ouvertures des viseurs 552 et EXP3 restent transparentes avec leur réticule centré. Le réticule holographique passe en gris dans la scène thermique Iris. L’ACOG conserve son ouverture, son zoom et son réticule rouge. Les captures `holo-01` à `holo-08` sont dans `run/qa-cloak-iris/screenshots`.

Pour activer le sprint renforcé, démarre le sprint normal, relâche **Sprint** en continuant d’avancer, puis appuie à nouveau sur la même touche pendant que le joueur sprinte encore. Après l’arrêt du boost ou de la propulsion, la Nanosuit attend 20 ticks avant d’en autoriser un nouveau. Le sprint normal reste disponible. Le test `client-sprint-controls.log` a confirmé ce geste avec **Y** réassigné, la lecture du servo, le maintien de **Saut** à quatre ticks avec **G** et les états de la jauge intégrée à la minicarte.

La tenue complète soigne un point de vie après quatre secondes sans dégât, puis toutes les trois secondes. Le soin exige de l’énergie disponible et s’interrompt pendant la panne, sans consommer d’énergie ni de nourriture. Le test `client-sprint-recovery.log` a confirmé les soins et leurs conditions.

### Sons de déplacement

Les pas utilisent des variantes de marche et de course de Crysis 2 sur la pierre, la terre, l’herbe, le gravier, le métal, le sable et le bois. Le tissu réutilise les échantillons de marche de sa propre banque. Le mode armure rend la marche plus grave et utilise les bottes d’armure en sprint. Le camouflage atténue les pas debout et les supprime en position accroupie. Les surfaces avec un son sans équivalent gardent le son de Minecraft ou du mod qui fournit le bloc. Les impacts de chute suffisamment forts utilisent un bruit d’équipement de Nanosuit. Les déclencheurs de vibrations des pas restent inchangés en dehors des pas accroupis en camouflage.

Les 50 échantillons sont issus des banques de Crysis 2 dans `Sounds/physics_foleys`. Les sons `footstep_*`, `foostep_*` et `armor_boot_run_0[1–3]` proviennent de `physics_foleys_footsteps_static.fsb`. Les deux variantes `nanosuit_gear_land_01` et `nanosuit_gear_land_03` proviennent de `physics_foleys_gear_static_sp.fsb`. Tous les Ogg Vorbis ont été redécodés et comparés à leur source sur le nombre d’échantillons, la fréquence et le nombre de canaux.

### Recycleur et retours sonores

La combinaison complète maintient la réserve d’air à son maximum lorsque les yeux du joueur sont sous l’eau. Ce passif fonctionne dans tous les modes, même avec une réserve d’énergie vide. Il ne consomme pas d’énergie. Retirer une pièce rétablit la respiration normale de Minecraft.

Les transitions de plongée et de remontée ont été retirées. La propulsion sous-marine utilise la boucle `endurance_hydro_thruster_loop` de Crysis 3 lorsque le bonus de nage est actif. Le mode armure utilise sa propre ambiance.

Dans le test en survie consigné dans `client-cloak-strong-audio-verified.log`, la réserve est passée de 55 à 300 sous l’eau avec les quatre pièces en mode neutre, à énergie constante. Elle est redescendue à 276 après retrait du casque.

### Cycle d’énergie

À 10 % d’énergie ou moins, la combinaison joue `energy_critical` de Crysis 3. À 0 %, elle se verrouille et joue `suit_disabled_state_initial`, puis cinq impulsions `suit_disabled_state_loop` espacées d’environ une demi-seconde. Le volume des impulsions passe de 70 à 30 %. La charge commence à 4,05 secondes et dure 2,05 secondes en ticks de jeu, soit la durée de `energy_charge` augmentée d’environ une demi-seconde. `energy_restored` signale le retour à 100 % et la fin du verrouillage. Les changements de mode armure et camouflage sont espacés d’au moins une seconde.

La mêlée renforcée double les dégâts du coup principal à mains vides ou d’une frappe de mêlée native TaCZ et consomme 15 % d’énergie lorsqu’elle touche. La propulsion accroît la vitesse de nage de 40 % et utilise la dépense de sprint existante. Le NanoVisor joue les pistes `binocular_on`, `binocular_off`, `binocular_zoom_in` et `binocular_zoom_out` de Crysis 3. Les niveaux sont réglables par catégorie dans **Crysis Legacy → Nanosuit audio** et enregistrés dans `nanosuit-client.toml`.

Le test isolé `client-energy-polish.log` a confirmé le délai de bascule, les cinq impulsions et leurs volumes, les 41 ticks de recharge, le refus du changement de mode à 21,95 % et son acceptation à 100 %. Le poing renforcé est passé de 1 à 2 points de dégâts et a coûté 15 % d’énergie. Le coup de crosse, la nage accélérée et les sons du viseur restent à essayer en jeu. Le protocole réseau a changé et requiert le même JAR sur le client et le serveur.

Zombie Awareness 1.13.2 et CoroUtil 1.3.8 ont chargé dans le client de test isolé. Avec le camouflage actif, une note de bloc musical a créé une trace sonore de Zombie Awareness. Le zombie ne s'est pas approché du point de mesure dans la fenêtre de l'essai. Le code de Zombie Awareness calcule et poursuit les sources de lumière indépendamment du ciblage direct d'un joueur. Ce déplacement vers la lumière reste à vérifier en jeu.
