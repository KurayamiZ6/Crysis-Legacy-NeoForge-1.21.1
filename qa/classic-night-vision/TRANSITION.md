# Transition verte N1

Référence `1c7ca9b`, branche `feature/nanosuit-1-classic`. Correction séparée de l’intégration ultérieure des jumelles.

Le shader vert existant reçoit un uniforme `Intensity`. Le rendu normal est mélangé avec le résultat original complet : couleur verte et amplification lumineuse suivent ainsi le même facteur. À 1, l’ancien rendu est conservé ; à 0, aucun traitement. Aucun nouveau shader, son ou cycle énergétique.

`ClassicVisionTransition` utilise le temps monotone de rendu et smoothstep sur une seconde. Un changement de cible repart de la dernière intensité affichée. La sortie continue lorsque `active` devient faux, y compris à batterie vide. Ce prolongement visuel ne modifie aucune autorisation serveur ni consommation. Le retrait d’équipement, la déconnexion et le passage à N2 réinitialisent l’effet pour éviter un filtre N1 sur une autre génération.

Java 21 : compilation PASS. Dix tests déterministes PASS : demi-transition, fins, inversions, réinitialisation et indépendance de la fréquence d’échantillonnage. Banc RTX isolé : 23 captures, six assertions runtime PASS, arrêt normal. Preuves et SHA du candidat : `transition-evidence/manifest.json` et `overview.png`.

Exemples observés : entrée intermédiaire 0,327 puis 0,702, arrivée à 1 ; sortie à 0 ; inversion depuis une intensité intermédiaire sans remise à zéro ; batterie à zéro, active=false mais intensité résiduelle 0,897, puis extinction complète. N2 à intensité 0. Le coût de capture du framebuffer et la latence des paquets décalent le temps de la sonde par rapport à l’instant d’échantillonnage du shader : les fichiers « 250/500 ms » sont des repères de capture approximatifs, pas une mesure exacte de la courbe. Les tests logiques contrôlent les temps exacts.

Le HUD permanent et la batterie apparaissent dans les captures. Les constantes 1200/300 ticks et les sources thermiques N2 ne sont pas modifiées. Leur absence de modification est vérifiée statiquement ; pas de nouvelle campagne thermique, Iris ou shaders. Courbe et durée originales Crysis encore non mesurées, smoothstep/1 s restent l’approximation demandée.

Fichiers de production : `client/nanovision/ClassicNightVisionClient.java`, `ClassicVisionTransition.java`, `shaders/program/classic_night_vision.json` et `.fsh`. Sonde et tests uniquement dans `qa/classic-night-vision/`.
