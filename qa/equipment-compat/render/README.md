# Rejeu de rendu des deux générations

Le banc réutilise le profil isolé `run/qa-multi-suit-render` et les arguments NeoForge de `build/legacy-functional-qa`. Les mods et packs sont copiés dans un nouveau dossier. Le JAR testé est celui de production, et le banc équipe les vrais objets des deux générations. Utiliser Java 21 et un nom de rejeu inédit.

```powershell
./qa/equipment-compat/render/run.ps1 -RunName c8258de-replay `
    -ModJar build/libs/crysis-legacy-neoforge-port-1.1.1-port-dev.jar
```

Le banc vérifie le profil équipé, Fresh Animations, l’activation d’Iris, les états de Nanovision et de camouflage, la caméra et la visée du 552. Les captures sous `build/equipment-compat-render/<nom>/screenshots` servent à vérifier visuellement le bras animé, les modèles thermiques, leur occlusion, le viseur et la réfraction. Le champ `PASS` du journal correspond aux états de jeu observés pendant la capture. Les erreurs OpenGL doivent être comparées au journal du profil de référence.
