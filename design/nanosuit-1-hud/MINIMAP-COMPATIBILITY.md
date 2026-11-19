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
