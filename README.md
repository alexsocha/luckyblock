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

- [Download Lucky Block](https://www.luckyblockmod.com/)
- [Minecraft Forum](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1292247-lucky-block-mod-drops-items-spawns-mobs-structures)
- [Planet Minecraft](https://www.planetminecraft.com/mod/lucky-block/)

## Install
1. Download and install [Forge](https://files.minecraftforge.net) or [Fabric](https://fabricmc.net), then run Minecraft at least once.
2. Download the Lucky Block mod and put it into your [.minecraft](https://minecraft.gamepedia.com/.minecraft)/mods/</a> folder.
3. Run Minecraft and enjoy!

## Add-ons
<img src="img/addons_3d.png" width="400px">

### Install
1. Install the Lucky Block mod and run Minecraft at least once.
2. Put add-ons into your [.minecraft](https://minecraft.gamepedia.com/.minecraft)/addons/lucky/ folder.
3. Run Minecraft and enjoy!

## Versioning

All Lucky Block releases follow the format:
```
lucky-block-{loader}@{minecraft}-{major}.{minor}
```

- `loader`: forge/fabric/bedrock
- `minecraft`: Minimum compatible Minecraft version
- `major`: Incremented on every feature/fix to the core mod.
- `minor`: Incremented on every fix to this particular `loader`/`minecraft` combo.

## Development

Dependencies are listed in `project.yaml`, and lockfiles can be updated with:

```
./gradlew dependencies --write-locks
```

To use IntelliJ, ensure that the Gradle plugin is enabled, and import the directory as a Gradle project.

- `./gradlew tasks`: View all available tasks
- `./gradlew clean`: Clean the build folder. Always run this after updating the version number.
- `./gradlew :{forge/fabric}:build`: Build the project, and create a distributable jar file in `build/dist/{version}`
- `./gradlew :jvmTemplateAddonDist`: Copy the latest template addon to `build/dist/{version}`
- `./gradlew :{forge/fabric}:runClient`: Run a Minecraft client
- `./gradlew :{forge/fabric}:runServer`: Run a Minecraft server
- `./gradlew :{forge/fabric/tools}:test --tests "*{test name regex}" --info`: Run tests

### Hot reloading

1. Download the Java 8 [DCEVM](https://dcevm.github.io/) binary
2. `sudo java -jar DCEVM-8uXXX-installer.jar`
3. Open your Java home directory, and select "Replace by DCEVM"
4. In IntelliJ, you may need to ignore the bedrock module: `Preferences > Build, Execution, Deployment > Compiler > Excludes` and add `~/bedrock`
5. Start the `runClient` task in debug mode
6. Edit the code. Some useful debug values are can be found in `common/src/commonMain/kotlin/drop/DropEvaluator`
7. Use the IntelliJ build button to reload the entire project, or Shift-Cmd-F9 to reload the current file

### Minecraft server

- Start the `runServer` task for the first time
- Accept `run/eula.txt`
- Edit `run/server.properties` with the following:
    - `gamemode=creative`
    - `online-mode=false`
- Start the `runServer` again
- Optionally, run `/op <player>`
- Start a Mincecraft client, and add a server with the address `:25565`

### Bedrock

For convenience, create a symlink to the resource and behavior packs:

```
mklink /D "C:\Users\%USERNAME%\AppData\Local\Packages\Microsoft.MinecraftUWP_8wekyb3d8bbwe\LocalState\games\com.mojang\development_resource_packs" "C:\...\bedrock\run\development_resource_packs"

mklink /D "C:\Users\%USERNAME%\AppData\Local\Packages\Microsoft.MinecraftUWP_8wekyb3d8bbwe\LocalState\games\com.mojang\development_behavior_packs" "C:\...\bedrock\run\development_behavior_packs"
```

### Bedrock tools

- `./gradlew :tools:run --args "generate-bedrock-drops --help"`
- `./gradlew :tools:run --args "nbt-to-mcstructure --help"`
- `./gradlew :tools:run --args "download-block-ids --help"`

## Copyright
Copyright © 2015-2021 Alex Socha. All Rights Reserved.

By submitting a pull request, you agree to transfer all rights and ownership to the copyright holder.
