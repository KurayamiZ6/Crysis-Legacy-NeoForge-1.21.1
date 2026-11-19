# Minicarte N1 : objectif et coexistence avec les autres mods

Décision de conception, 19 novembre 2026. Aucune intégration réalisée ou compatibilité tierce testée.

## Point vert

L’utilisateur confirme que le point vert de Crysis 1 représente un objectif. Dans Minecraft, il représentera éventuellement **un waypoint choisi par le joueur**. Sans waypoint disponible ou sélectionné, ne pas afficher de point vert fictif. Le point présent dans les maquettes est un objectif simulé ; il ne représente pas un ennemi.

Un adaptateur optionnel par mod pourra fournir identifiant, libellé, position XYZ et dimension du waypoint sélectionné. Filtrer selon le monde/serveur et la dimension, puis utiliser la projection existante de la minicarte. Ne pas convertir automatiquement toutes les balises, morts ou points enregistrés en objectifs verts. Le choix d’un point suivi doit être explicite ; la sélection exposée par chaque mod reste à vérifier. Sans adaptateur compatible, omettre le marqueur et conserver le HUD de base fonctionnel.

## Choix recommandé

**Conserver le terrain et les marqueurs de notre minicarte, avec l’habillage Crysis, et masquer uniquement l’affichage concurrent lorsqu’elle est effectivement rendue.** Réutiliser les données des waypoints quand une intégration le permet, plutôt que déplacer le rendu complet d’une autre minicarte dans notre cadre.

Ce choix limite le nombre de variantes de formes, cadrage, rotation, échelle, textes et superpositions à maîtriser pour reproduire les références. Déplacer une minicarte tierce n’assure pas son découpage dans le polygone Crysis, son style monochrome ni l’alignement de notre boussole et de nos menaces. La faisabilité et le coût exacts d’une interception de rendu restent toutefois à vérifier pour chaque version de chaque mod.

« Masquer » signifie supprimer temporairement la petite carte concurrente à l’écran, **pas désactiver le mod**, ses waypoints, sa carte plein écran ou son enregistrement du terrain. Ne pas écrire dans les configurations personnelles ni supprimer de données. Ne pas utiliser une annulation globale de tous les éléments GUI du mod.

La condition doit être celle du rendu N1 effectif : combinaison/profil approprié, HUD et minicarte autorisés, monde chargé et rendu disponible. Si notre carte est désactivée, absente ou défaillante, l’affichage tiers doit redevenir disponible. Le HUD N2 conserve sa politique actuelle : aucune nouvelle suppression implicite.

Prévoir une option de coexistence : masquage automatique du seul panneau compatible, ou conservation du panneau tiers au choix de l’utilisateur. Si aucun mécanisme de masquage fin et réversible n’est disponible, ne pas intercepter à l’aveugle ; documenter le réglage manuel propre au mod. La présence d’un mod ne prouve pas que son panneau peut être masqué de cette façon.

## État réellement constaté dans le dépôt

- `src/main/java/io/github/painworks/crysismod/client/minimap/NanoMinimapVisibility.java` conditionne déjà notre carte à l’activation de sa configuration, à la visibilité du GUI et à une combinaison complète ; cette condition n’est pas encore spécifique au futur HUD N1 et ne prouve pas qu’un rendu a effectivement abouti.
- `client/minimap/marker/NanoMinimapMarker.java` contient PLAYER, TARGET, MARKED_ENTITY et NEARBY_ENTITY. Il n’existe pas de type waypoint/objectif : TARGET est une entité visée, il ne faut pas le détourner pour un objectif cartographique.
- `client/minimap/marker/NanoMinimapMarkerLayer.java` collecte déjà les entités et marquages, et réutilise `NanoMinimapCoordinateMapper`. Ajouter plus tard une source optionnelle de waypoint au système existant, sans recréer le moteur cartographique.
- La recherche ciblée dans `src/main/java` ne trouve pas d’intégration nommée Xaero, JourneyMap ou Diorama. Aucun accès à leurs API ou couche GUI n’a été validé ici.

## Ordre d’intégration ultérieur

1. Finaliser l’habillage N1 sur le moteur de carte existant ; vérifier le rendu de secours et les règles de visibilité.
2. Relever les versions exactes installées et étudier les points d’extension disponibles pour **un seul mod à la fois**. Aucun engagement de compatibilité générique.
3. Ajouter un adaptateur optionnel pour son panneau et son waypoint suivi, sans dépendance obligatoire.
4. Tester carte active/inactive, F1, équipement retiré, N1/N2, reconnexion, changement de dimension et absence du mod ; vérifier le retour du panneau d’origine et la conservation de la carte plein écran/des waypoints.
5. Étendre aux autres mods uniquement après validation de ce premier cas. Tester plusieurs cartes installées sans supposer une source de waypoint prioritaire.

Ce document prépare l’intégration. Les mods de minicarte personnels n’ont pas été modifiés, et aucun masquage automatique n’est encore implémenté.

## Intégration effective — 19 novembre 2026

Cette section remplace l'état préparatoire ci-dessus. La minicarte N1 fonctionne sur le moteur existant. Options : `hud.classic.minimapEnabled` choisit notre carte ; `hud.classic.hideDioramaMinimap` autorise le masquage ciblé décrit ci-dessous. Aucun fichier personnel n'est modifié.

| Mod / version réellement trouvée | Étude et résultat réel |
| --- | --- |
| Diorama 0.10.2+neoforge-1.21.1 | Testé dans le banc isolé. Sa couche publique NeoForge `diorama:minimap` est annulée uniquement quand le terrain N1 est prêt. Les captures et événements démontrent son retour quand notre option est désactivée, la N1 retirée ou N2 équipée. Couche `diorama:waypoints` jamais annulée. La carte plein écran et les points sauvegardés ne sont pas modifiés ; leur interaction utilisateur n'a pas fait l'objet d'un test dédié. Aucun accès validé à un waypoint explicitement suivi : aucun import. Adaptateur limité à cette série de version. |
| Xaero's Minimap 26.6.0 NeoForge 1.21.1 | JAR désactivé présent ; dépendance XaeroLib requise mais absente des instances examinées. Pas de lancement, masquage ni import validés. Utiliser le choix explicite de carte ; pas d'interception ajoutée. World Map 1.47.0 également désactivé, non testé. |
| JourneyMap | Absent des deux dossiers de mods ciblés. Aucune compatibilité annoncée ou testée. Coexistence manuelle. |

Objectif natif facultatif : `/crysiswaypoint follow <x> <z>`, `/crysiswaypoint clear`. Session/dimension filtrées ; rien par défaut. Rapport et protocole : `qa/classic-hud/MINIMAP-RESULTS.md`. Captures et logs : `build/mode-overlay-qa/classic-map-base/` et `classic-map-diorama/`.
