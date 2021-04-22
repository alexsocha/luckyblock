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
- <a href="https://www.planetminecraft.com/mod/lucky-block/">Planet Minecraft</a>

## Installing
1. Download and install <a href="https://files.minecraftforge.net/">Minecraft Forge</a>, then run Minecraft at least once.
2. Download the Lucky Block and put it into your <a href="https://minecraft.gamepedia.com/.minecraft">.minecraft</a>/mods/</a> folder.
3. Run Minecraft and enjoy!

## Add-ons
<img src="img/addons_3d.png" width="400px">

### Installing
1. Install the Lucky Block and run Minecraft at least once.
2. Put add-ons into your <a href="https://minecraft.gamepedia.com/.minecraft">.minecraft</a>/addons/lucky/</a> folder.
3. Run Minecraft and enjoy!

## Development

Update the version constants in `gradle.properties`.

To use IntelliJ, ensure that the Gradle plugin is enabled, and import the directory as a Gradle project.

- `./gradlew tasks`: View all available tasks.
- `./gradles clean`: Clean the build folder. Always run this after updating the version number.
- `./gradlew luckyBuild`: Build the project, and create a distributable jar file in `build/dist/{version}`.
- `./gradlew luckyClient`: Run a Minecraft client.
- `./gradlew luckyServer`: Run a Minecraft server.

### Hot reloading

1. Download the Java 8 [DCEVM](https://dcevm.github.io/) binary.
2. `sudo java -jar DCEVM-8uXXX-installer.jar`
3. Open your Java home directory, and select "Replace by DCEVM".
4. In IntelliJ, you may need to ignore the bedrock module: `Preferences > Build, Execution, Deployment > Compiler > Excludes` and add `~/bedrock`.
5. Start the `runClient` task in debug mode.
6. Edit the code. Some useful debug values are can be found in `common/src/commonMain/kotlin/drop/DropEvaluator`.
7. Use the IntelliJ build button to reload the entire project, or Shift-Cmd-F9 to reload the current file.

### Minecraft server

- Start the `runServer` task for the first time.
- Accept `run/eula.txt`.
- Edit `run/server.properties` with the following:
    - `gamemode=creative`
    - `online-mode=false`
- Start the `runServer` again.
- Optionally, run `/op <player>`.
- Start a Mincecraft client, and add a server with the address `:25565`.


### Deploy

1. Run `./gradlew build`.
2. Run `./scripts/deploy.sh`.

## Copyright
Copyright © 2015-2021 Alex Socha. All Rights Reserved.

By submitting a pull request, you agree to transfer all rights and ownership to the copyright holder.
