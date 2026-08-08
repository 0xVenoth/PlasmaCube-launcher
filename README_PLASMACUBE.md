# PlasmaCube Launcher

Application Android du serveur Cobblemon **PlasmaCube** — fork de
[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher) (GPLv3).

Au premier lancement, l'application télécharge et installe automatiquement le
[modpack client PlasmaCube](https://github.com/0xVenoth/PlasmaCube-modpack)
(Minecraft 1.21.1, Fabric, Cobblemon 1.7.3), crée le profil de jeu et le
sélectionne. Un compte Microsoft possédant Minecraft Java Edition reste requis.

## Modifications par rapport à PojavLauncher

- Rebranding (nom, icône, identifiant d'application `fr.plasmacube.launcher`)
- `PlasmaCubeInstaller` : téléchargement + extraction du modpack au premier
  lancement, création du profil Fabric (`app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/plasmacube/`)
- JRE 21 embarqué dans les assets (OpenJDK, GPLv2+CE)
- Profil et options par défaut adaptés au serveur PlasmaCube

## Licence

GPLv3, comme le projet d'origine — voir `LICENSE`. Tout le mérite du launcher
revient à l'équipe PojavLauncher.
