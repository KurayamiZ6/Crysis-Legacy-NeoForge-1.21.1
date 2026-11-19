# Corrections de référence — 19 novembre 2026

Ces observations de l’utilisateur remplacent la spécification réversible de `BINOCULARS.md` et la démonstration historique `binocular-animation.html`. Elles concernent uniquement N1. Ne pas réutiliser ce prototype tel quel pour l’intégration.

## Jumelles : machine à états future

- Fermé → appui B : ouverture depuis 0 %.
- Pendant l’ouverture → appui B : saut immédiat à l’état entièrement ouvert, puis début de fermeture depuis 100 %.
- Pendant la fermeture → appui B : interruption et nouvelle ouverture depuis 0 %, sans conservation de progression.
- Ouvert → appui B : fermeture depuis 100 %.

Traiter chaque appui dans l’ordre, même dans la même image ; ne pas fusionner plusieurs appuis en une seule inversion. Durée 450 ms provisoire, à mesurer. Critères futurs : appui unique, fermeture pendant ouverture, réouverture pendant fermeture, rafales d’appuis, résultats identiques à fréquence d’images différente. La fermeture ne prolonge pas une capacité serveur.

## Vision nocturne verte

Future interpolation douce d’intensité sur environ 1 seconde à l’activation et à la désactivation. Durée/courbe non mesurées. Réutiliser le traitement N1 existant avec une intensité uniforme plutôt que créer un second shader. En interruption, repartir de l’intensité courante pour éviter un saut ; batterie vide déclenche la décroissance visuelle sans prolonger la consommation ni l’autorisation serveur. Tester batterie vide, réactivation rapide, retrait d’équipement, changement N1/N2 et déconnexion. Aucun changement de cette transition pendant la première intégration du HUD.

## Vignette et batterie

La vignette très sombre du prototype est trop étroite : ne conserver qu’un léger assombrissement des coins. Le shader vert N1 actuel n’applique aucune vignette ; ne pas lui ajouter le masque sombre du prototype. Les quatre segments jaunes de batterie doivent être alignés horizontalement. La représentation de charge reste indépendante de l’énergie de combinaison.
