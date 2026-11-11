# Banc intégré de transition Nanosuit

Le lanceur PowerShell 7 utilise Java 21 et le profil NeoForge isolé des essais précédents. Depuis la racine du projet, compiler d’abord le JAR habituel et préparer les arguments ModDev de lancement client. `-LaunchFixture` désigne un dossier contenant `classpath.txt` (un chemin absolu par ligne), `program.txt` (arguments DevLaunch avec chargement d’un monde isolé) et `template` (copie vierge du profil avec ce monde). Le dossier historique `build/legacy-functional-qa` possède cette structure.

```powershell
./qa/transition/run.ps1 -Profile reference -SourceRef 6cffe6a -AllowFailures `
    -ModJar build/multi-suit-reference/candidate.jar
./qa/transition/run.ps1 -Profile candidate `
    -ModJar build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar
```

Définir `JAVA_HOME` ou passer `-JavaHome` pour sélectionner le JDK. Utiliser un nom de profil neuf à chaque rejeu. Sur Windows, le serveur intégré requiert l’accès à la boucle locale. Le lanceur écrit le hash du JAR d’entrée et les résultats sous `build/transition-qa/<profil>/`. Chercher `^TRANSITION_QA_` dans `transition.log`. `-AllowFailures` laisse terminer un test différentiel sur la référence même quand des assertions échouent.

Le lanceur crée une copie jetable du JAR d’entrée avec deux classes de test recompilées. Le profil `QA_SHARED` partage les règles et objets de règles historiques tout en ayant une identité distincte. `QA_RESTRICTED` refuse les modes et capacités optionnels et utilise des timings artificiels de panne et de sprint. Ces valeurs servent uniquement à déceler les changements de règle en cours d’action.

Le test change le résultat du résolveur par UUID, depuis le thread serveur intégré. Son client partage la mémoire du résolveur de test. Les relevés d’énergie, de mode et de verrou proviennent du paquet existant et les relevés de visibilité, santé et attributs des mises à jour d’entité. Le relevé client n’établit pas qu’un identifiant de génération serait transmis sur le réseau. Les pièces historiques restent équipées lors des changements simulés, puis le casque est réellement retiré pour vérifier l’arrêt du soin passif. Les tests de charge et d’agrippement utilisent les handlers serveur réels et observent leurs champs privés sans les modifier.
