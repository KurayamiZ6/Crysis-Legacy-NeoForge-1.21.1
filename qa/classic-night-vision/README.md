# Banc de vision nocturne classique

Ce banc utilise Java 21 et le profil NeoForge isolé sous `build/legacy-functional-qa`. Il en équipe la Nanosuit 1 et utilise le raccourci de vision existant. La batterie est observée sur le vrai serveur intégré jusqu’à épuisement puis recharge complète. Le banc vérifie l’indépendance de l’énergie principale et la réception de l’arrêt automatique par le client. Il équipe ensuite la Nanosuit 2 et contrôle la disponibilité de sa Nanovision.

```powershell
./qa/classic-night-vision/run.ps1 -RunName candidate `
    -ModJar build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar
```

Le journal et les captures se trouvent sous `build/classic-night-vision-qa/<nom>/`. Les deux images avant/après sont à vérifier visuellement. Le jeu doit rester actif pendant toute la décharge et la recharge.

En cas de défaut de la fenêtre de démarrage NeoForge, `-NoEarlyWindow` désactive cette fenêtre dans le profil isolé. `-Dedicated` exécute les mêmes règles d’équipement et de batterie sur un serveur dédié avec un joueur factice. Ce banc appelle les ticks du gestionnaire directement, sans client ni rendu.

Le compteur de batterie peut aussi être vérifié hors jeu avec `java -cp build/classes/java/main qa/classic-night-vision/ClassicNightVisionBatteryQa.java`.
