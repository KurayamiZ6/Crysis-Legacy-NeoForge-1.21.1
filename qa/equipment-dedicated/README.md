# Vérification des équipements sur serveur dédié

Le lanceur PowerShell 7 réutilise le monde et le classpath du profil `build/legacy-functional-qa`, les arguments ModDev de `build/moddev` et Java 21. Il copie le JAR de production et le monde dans `build/equipment-dedicated-qa/<nom>`. Le serveur de test est lié à `127.0.0.1` et deux clients de développement s’y connectent avec des noms distincts. Choisir un nouveau nom à chaque rejeu et un port local libre.

```powershell
./qa/equipment-dedicated/run.ps1 -RunName equipment-replay `
    -ModJar build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar
```

Le test équipe les vrais objets sur le joueur porteur côté serveur. Il vérifie la conservation de la réserve à 72 lors du changement de génération, le drain du camouflage, le maintien du mode compatible lors du changement inverse, les bornes du cooldown à 19 et 20 ticks et la fin du mode Armure. Les deux clients vérifient le registre des quatre objets reçus et le profil résolu. Le porteur contrôle aussi l’état d’énergie et de mode reçu. L’observateur vérifie la visibilité et prend des captures.

Les journaux `server/console.log`, `owner/console.log` et `observer/console.log` doivent chacun annoncer `COMPLETE failures=0`. Les captures de l’observateur se trouvent sous `observer/screenshots`. Le lanceur n’emploie ni shader, ni TaCZ, ni configuration du modpack principal. Le commutateur `-Smoke` permet de vérifier uniquement le démarrage du serveur et ferme ensuite le monde jetable.
