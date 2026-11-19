# Threat Indicator N1 — 19 novembre 2026

## Données et limites

Le serveur échantillonne toutes les 10 ticks dans le rayon existant de 32 blocs. N1 : cible directe ou mémoire ATTACK_TARGET attribuée au joueur + ligne de vue et règles de camouflage existantes = DETECTED. Cible sans vue, mémoire ANGRY_AT ou agression récente attribuée au joueur = SEARCHING. Aucun mob simplement proche n'est interprété comme ayant détecté le joueur. Les règles N2 restent dans leur branche d'origine, y compris leur suspicion de proximité.

Le maintien de SEARCHING pendant 60 ticks après une alerte est une **estimation d'alerte récente**, héritée du système, pas une preuve qu'un mob recherche sa dernière position connue. Les états de suspicion propres à Crysis n'existent pas pour tous les mobs Minecraft. Aucun comportement d'IA ajouté.

Zombie Awareness 1.13.2 / CoroUtil 1.3.8 : les champs déjà exploités par le mod renseignent une investigation, mais sans l'attribuer de manière fiable à ce joueur. N1 ne convertit donc pas ces investigations de bruit/odeur/lumière en détection du joueur. Les cibles réelles des mobs restent utilisables. Pas de prétention à reproduire toute la machine de suspicion ZA.

## Présentation

Triangle V3 dans l'hexagone inférieur gauche : petit/thème au repos, rouge agrandi pulsant en détection, orange fixe durant 0,5 s après la perte, jaune puis rétrécissement sur 0,5 s. Barre de quatre segments larges à gauche du terrain. Couleurs d'alerte fixes malgré un thème personnalisé. Dessin séparé du calcul, sans recherche d'entités par frame. Données reçues bornées dans le temps et au joueur/monde ; historique graphique effacé au changement de contexte.

Le paquet existant ne transmet qu'un état agrégé : **pas de flèches ennemies colorées individuelles inventées**. Elles nécessiteraient des données par entité. L'indicateur accompagne notre minicarte N1 et disparaît lorsque celle-ci est désactivée ou F1 actif. N2 conserve son indicateur à huit segments.

## Tests réels

Source : `ClassicThreatVisualQa.java`. Banc isolé RTX 3070, 1920×1080, Sodium 0.8.13.

| Scénario | Vanilla puis ZA présent |
| --- | --- |
| Aucun ennemi / zombie proche sans cible | CALM / CALM |
| Zombie ciblant le joueur avec vue | DETECTED |
| Camouflage à 7 blocs | Cible réellement effacée par le système existant, SEARCHING résiduel |
| Reprise de cible | DETECTED |
| Cible retirée | SEARCHING, captures orange puis jaune |
| Après expiration | CALM |
| Deux zombies ciblant le joueur | DETECTED |
| Passage au Nether | CALM, objectif de l'Overworld caché |
| Retrait du plastron | Pas de HUD N1 |
| N2 équipée dans la nouvelle dimension | Ancien HUD et indicateur, CALM |

Les zombies de la fixture ont leur IA désactivée pour isoler leur cible et la perte de cible ; le ciblage, les règles de camouflage et les paquets testés sont réels. Ce test ne démontre pas les transitions spontanées de recherche de tous les mobs ou la perception sensorielle complète ZA.

Exécutions : `build/mode-overlay-qa/classic-threat-vanilla/`, puis `classic-threat-za/`. La seconde contient la version graphique finale (priorité de rendu corrigée pour que la capture soit prise après l'indicateur). Chaque log contient les douze états et THREAT_QA_COMPLETE. Compilation offline réussie. Aucun coût énergétique, son, IA ni règle de camouflage modifié.
