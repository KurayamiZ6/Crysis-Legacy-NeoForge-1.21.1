# Stabilisation — 19 novembre 2026

Base : e769ad6, branche feature/nanosuit-1-classic, arbre initial propre.

## Défauts reproduits avant correction

`stability-before`, sur le JAR de base, dans le banc isolé RTX 3070 :

- Sans combinaison et avec casque seul, V ouvre l'ancien viseur : actif=true et FOV réel mesuré par GameRenderer ~28–32°, contre ~75° normal dans cette scène.
- Après retrait du plastron N1, le contrôleur classique ferme bien ses jumelles, mais un nouvel appui sur V passe dans le contrôleur historique non protégé et réactive le viseur sans combinaison.
- Matrice serveur des 81 possibilités (vide/N1/N2 sur chaque emplacement) : 2 combinaisons homogènes complètes, 79 invalides. Avec la configuration historique « casque suffisant », 26 équipements invalides permettent la vision nocturne N1 et 26 permettent la Nanovision N2. Les autres points d'entrée vérifiés refusent déjà les équipements invalides.
- Le retrait classique immédiat, N1→N2, N2→N1, écran et dimension ne reproduisent pas de persistance du contrôleur classique lui-même dans ce parcours. La fuite effectivement démontrée vient surtout du chemin historique activable après son nettoyage ; ne pas prétendre avoir reproduit tous les symptômes possibles rapportés par l'utilisateur.

## Corrections

`NanosuitArmorUtil.getUsableProfile` centralise la règle vivant/non spectateur/combinaison complète et homogène. Le choix casque seul des anciennes configurations ne contourne plus cette règle, côté client comme serveur. Anciennes clés conservées pour lecture, options devenues sans effet retirées des deux écrans de configuration. Aucun fichier personnel édité.

Le viseur historique vérifie désormais son équipement et ses conditions d'utilisation ; les jumelles classiques conservent leurs règles N1. Le zoom historique revient immédiatement à 1 lorsque son contexte devient invalide. Les requêtes de raccourcis Armure/Camouflage et Sprint sont filtrées côté client ; les handlers serveur restent autoritaires. Mode et dépense d'énergie serveur vérifient également le joueur vivant/non spectateur.

`NanosuitClientContext` annule les états temporaires appartenant à l'ancien contexte : animation de jumelles, grossissement, charge/suppression de déplacement du saut, flash/activité de puissance et filtres devenus invalides. Changement de joueur/profil/monde et fermeture sur écran sont traités sans changer les touches vanilla. Les clics du viseur de l'ancien contexte sont consommés ; une touche de viseur encore maintenue doit être relâchée. Un radial devenu invalide est fermé par le chemin Screen normal, qui restitue ses contextes clavier et la souris.

La batterie n'est pas remise artificiellement à 100 %. Ni énergie sauvegardée, ni délai de recharge, ni cooldown serveur ni verrou de panne ne sont réinitialisés par le nettoyage client. Une vision toujours légitime dans le même profil n'est pas désactivée arbitrairement à l'ouverture d'un inventaire. Les comportements des modes N2 ne sont pas réécrits.

## Vérification A/B réelle

Source reproductible : EquipmentStateQa.java, avec qa/mode-overlay/run.py. Sodium 0.8.13, NeoForge 21.1.265, Minecraft 1.21.1 ; aucun shaderpack pour cette phase.

| Exécution | Résultat |
| --- | --- |
| stability-before | Matrice : deux groupes de fuite (26 N1 / 26 N2). Client : 3 défauts reproduits sur 14 scénarios. |
| stability-after | Même parcours : serverFailures=0, clientFailures=0 ; FOV revenu à ~75°, multiplicateur historique exactement 1 après invalidation. |
| stability-edges-valid | Matrice étendue : 81 équipements, aucun refus manquant ; cas positifs N1 et N2 ; mêlée invalide refusée, panne et cooldown N2 conservés. 21 scénarios client : aucune erreur. |

Le parcours étendu interrompt des ouvertures encore en cours (progression journalisée), puis une fermeture, contrôle les passages N1/N2 et équipement mixte, dimension et autre écran, active réellement la vision nocturne puis retire une pièce (intensité exactement 0), active la thermique N2 puis passe en équipement mixte (OFF). Contrôle de fermeture d'un radial injecté invalide avant son tick normal, avec restitution du contexte clavier et capture de souris ; ce dernier contrôle n'est pas un essai de maintien physique d'une touche.

Les tests serveur appellent les fonctions autoritaires utilisées par les paquets, pas uniquement des prédicats de rendu. Les inventaires, modes, positions et délais de la fixture sont préparés uniquement dans la copie de test. Les 81 cas ne constituent pas 81 essais de déplacement physique en eau : Power Swim est vérifié par lecture de son chemin existant de suppression d'attribut, pas par une matrice de nage réelle.

Mort/réapparition et reconnexion disque complète : nettoyage existant examiné et garde vivant ajoutée, mais pas de nouvelle simulation complète de ces deux parcours à ce stade. Ne pas les annoncer PASS par analogie. Les cooldowns testés ici sont ceux du retrait/remise d'équipement et de requêtes refusées, pas un nouveau contrat de persistance à travers tous les événements de vie.

Compilation offline réussie : build/stability-build.log. Preuves brutes : build/mode-overlay-qa/stability-before/, stability-after/, stability-edges-valid/. Captures choisies et observations conservées dans evidence/.

## Compatibilités (état de cette phase)

Versions ciblées réellement trouvées : Iris 1.8.14-beta.1, Complementary Reimagined r5.10, Euphoria Patcher 1.11.1-r5.10, EMF 3.3.12, ETF 7.2.7, Fresh Animations 1.11.2, Zombie Awareness 1.13.2 et CoroUtil 1.3.8. TACZ 1.2.0 disponible dans libs.

À ce point : Complementary/Iris, Fresh Animations/EMF/ETF, IA spontanée Zombie Awareness et session combinée = NOT TESTED dans cette campagne. Les validations antérieures ne sont pas transformées en PASS sur ce nouveau JAR. Les rapports de phases suivantes complèteront cette section seulement après des essais réels.
