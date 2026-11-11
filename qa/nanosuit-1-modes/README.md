# Banc intégré des modes Nanosuit 1

Le banc réutilise les arguments de lancement `build/legacy-functional-qa`. Il copie une sauvegarde créée avec l’ancien JAR, démarre NeoForge avec le JAR candidat, puis redémarre sur la même copie. La sauvegarde d’origine reste en lecture seule. Le mod de test reste dans le profil isolé.

```powershell
./qa/nanosuit-1-modes/run.ps1 -RunName candidate `
    -ModJar build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar
```

Les contrôles passent par les pièces enregistrées, le handler de sélection, les dégâts et le saut réels. Le sprint et la nage utilisent l’état de déplacement vanilla côté serveur sans envoyer de demande de second appui. Le client est observé après réplication des modes, effets et attributs. Le banc capture aussi les libellés Force et Vitesse du HUD. Les tests de raccourcis physiques et de serveur dédié sont distincts. Les preuves sont sous `build/nanosuit-1-modes-qa/<nom>/`.
