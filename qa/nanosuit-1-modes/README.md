# Banc intégré des modes Nanosuit 1

Le banc réutilise les arguments de lancement `build/legacy-functional-qa`. Il copie une sauvegarde créée avec l’ancien JAR, démarre NeoForge avec le JAR candidat, puis redémarre sur la même copie. La sauvegarde d’origine reste en lecture seule. Le mod de test reste dans le profil isolé.

Les valeurs Nanosuit 1 de référence à 20 TPS sont de 0,025 point/tick immobile, 0,05625 allongé, 0,15 accroupi en mouvement, 0,375 à la marche, 0,75 à la course et 2,25 en sprint renforcé. Dans un monde existant, consulter `serverconfig/nanosuit-server.toml`, sous `[nanosuit1.cloak]` et `[nanosuit1.speed]`. Une valeur déjà écrite dans ce fichier prévaut sur le défaut du JAR. Vérifier aussi `defaultconfigs/nanosuit-server.toml` si le profil en fournit un pour les nouveaux mondes.

```powershell
./qa/nanosuit-1-modes/run.ps1 -RunName candidate `
    -ModJar build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar
```

Les contrôles passent par les pièces enregistrées, le handler de sélection, les dégâts et le saut réels. Le sprint et la nage utilisent l’état de déplacement vanilla côté serveur sans envoyer de demande de second appui. Le client est observé après réplication des modes, effets et attributs. Le banc capture aussi les libellés Force et Vitesse du HUD. Les tests de raccourcis physiques et de serveur dédié sont distincts. Les preuves sont sous `build/nanosuit-1-modes-qa/<nom>/`.
