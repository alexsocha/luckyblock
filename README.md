# Lucky Block

![build](https://github.com/alexsocha/luckyblock/workflows/build/badge.svg)

<img src="img/icon_yellow.png" width="110px" align="left" style="margin-top: -5px">

Originally created in 2015, the Lucky Block is a mod for Minecraft which has since gained over 5 million users. The mod adds a new block to the game which produces random outcomes, as well as items such as the lucky sword, bow and potion. Additionally, the mod can be customized with hundreds of community add-ons.

<br>
<div align="center">
<img src="img/artwork.png" width="80%" style="
    display: block;
    margin-top: 20px;
    margin-bottom: 20px;
    border-radius: 8px;
">
</div>
<br>

## Resources

<img src="img/luckyblock_3d.png" width="180px" align="right" vspace="20px">

- <a href="https://www.luckyblockmod.com/">Download Lucky Block</a>
- <a href="https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1292247-lucky-block-mod-drops-items-spawns-mobs-structures">Minecraft Forum</a>
- <a href="https://www.planetminecraft.com/mod/lucky-block/">Plane Minecraft</a>

## Installing
1. Download and install <a href="https://files.minecraftforge.net/">Minecraft Forge</a>, then run Minecraft at least once.
2. Download the Lucky Block and put it into your <a href="https://minecraft.gamepedia.com/.minecraft">.minecraft</a>/mods/</a> folder.
3. Run Minecraft and enjoy!

## Add-ons
<img src="img/addons_3d.png" width="400px">

### Installing
1. Install the Lucky Block and run Minecraft at least once.
2. Put add-ons into your <a href="https://minecraft.gamepedia.com/.minecraft">.minecraft</a>/addons/lucky_block/</a> folder.
3. Run Minecraft and enjoy!

## Development

### Update

Edit the version constants at the top of  `build.gradle`, based on:

- [Forge versions](http://files.minecraftforge.net/)
- Mapping versions: [Forge repo](https://github.com/MinecraftForge/MinecraftForge) > `build.gradle` > `ext` > `MAPPING_VERSION`

### Develop

To use IntelliJ, ensure that the Gradle plugin is enabled, and import the directory as a Gradle project.

- `./gradlew tasks`: View all available tasks.
- `./gradlew luckyBuild`: Build the project, and create a distributable jar file in `build/dist/{version}`.
- `./gradlew luckyClient`: Run a Minecraft client.
- `./gradlew luckyServer`: Run a Minecraft server.

### Deploy

1. Clean the `build/dist` directory.
2. Run `./gradlew luckyBuild`.
3. Run `./scripts/deploy.sh`.

## Copyright
Copyright © 2015-2021 Alex Socha. All Rights Reserved.

By submitting a pull request, you agree to transfer all rights and ownership to the copyright holder.
