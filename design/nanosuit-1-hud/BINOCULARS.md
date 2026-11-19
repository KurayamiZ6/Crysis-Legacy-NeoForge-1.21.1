> Spécification corrigée : [SPEC-CORRECTIONS.md](SPEC-CORRECTIONS.md). Le prototype réversible des jumelles est historique et ne doit pas servir de machine à états pour l’intégration.

# Jumelles N1 : données et animation

Complément de conception du 19 novembre 2026. Les quatre nouvelles captures montrent l’ordre d’ouverture **2 → 1 → 3 → 4**, sans horodatage permettant de mesurer sa durée. La fermeture inverse est décrite par l’utilisateur. Sources indexées dans `references/user-2026-11-19/opening-manifest.json`.

## Correspondance Minecraft proposée

| Texte | Donnée à utiliser lors de l’intégration | Limites |
|---|---|---|
| Graduations latérales | Élévation de la caméra en degrés ; signe opposé au pitch Minecraft : regarder vers le sol donne une valeur négative | Utiliser l’angle caméra effectivement rendu si une animation le décale ; interpoler pour éviter une mise à jour saccadée. La réflexion des graduations après ±90° reproduit la séquence visible autour du nadir. |
| Coordonnées supérieures | X/Z du joueur, éventuellement Y à part | Pas de latitude/longitude inventée ; dimension à distinguer si utile. Position du joueur, pas point visé. |
| Distance inférieure | Distance de l’œil à l’intersection réellement visée | Le chemin `NanoVisorHudOverlay.resolveAimTarget` calcule déjà distances bloc/entité avec obstruction. Réutiliser ce résultat, afficher `— M` sans impact valide ; 1 bloc = 1 m est la convention proposée. Deux décimales d’affichage ne garantissent pas une précision physique centimétrique. |
| ZOOM / X02, X04 | Grossissement effectif choisi et appliqué au FOV | `NanoVisorZoomHandler` possède déjà un multiplicateur de FOV courant/configuré. Ce multiplicateur n’est pas directement le nombre ×2/×4 : pour un grossissement angulaire cohérent, comparer `tan(FOV de base / 2) / tan(FOV final / 2)`. Ne pas simplement inverser le multiplicateur ni changer le zoom actuel pendant cette conception. |
| MODE / VIS | Conserver le libellé source | Signification exacte et éventuel changement de mode non établis. Ne pas le présenter comme un commutateur fonctionnel vérifié. |

Les textes ne sont donc pas destinés à être des décorations fixes. Aucun branchement au jeu n’est effectué ici : les contrôles de la démonstration manipulent exclusivement des valeurs simulées dans le navigateur. La lecture ciblée des deux classes confirme les mécanismes de distance/zoom existants, sans test en jeu ni modification de leurs sources.

## Animation proposée

Une progression unique de 0 à 1 commande l’apparition progressive des textes et l’ouverture verticale des graduations depuis leur centre, à leurs positions horizontales définitives. Il ne s’agit pas de faire glisser les deux colonnes horizontalement depuis le réticule. Les cadres et libellés restent à leur place. À la fermeture, la progression diminue ; une inversion en cours reprend à sa position courante.

Le prototype utilise **450 ms**, durée proposée faute de mesure vidéo. L’ordre des quatre captures est respecté ; leur espacement temporel n’est pas connu. Cette valeur reste à valider visuellement. Les valeurs simulées peuvent varier sans modifier la progression de l’ouverture. Le zoom du décor n’est pas simulé : le bouton montre seulement le changement du libellé.

Sources : `make-binocular-demo.py` et `binocular-animation.html`. Images réelles du prototype : `binocular-closed.png`, `binocular-opening.png`, `binocular-open.png`, `binocular-closing.png`. Contrôles navigateur consignés dans `binocular-preview-check.json`. Aucun résultat Minecraft, TACZ, Iris ou shader n’est revendiqué.

Lors de l’intégration future, utiliser le temps de rendu pour cette transition visuelle, conserver les données de télémétrie existantes et isoler la présentation N1. La fermeture visuelle ne doit pas prolonger une capacité serveur ou sa consommation. Les décisions de gameplay et le HUD N2 restent inchangés.
