package mod.lucky.java.loader

import mod.lucky.common.LuckyBlockSettings
import mod.lucky.common.attribute.splitLines
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.dropsFromStrList
import mod.lucky.common.gameAPI
import mod.lucky.java.Addon
import mod.lucky.java.JavaGameAPI
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.javaGameAPI
import java.io.*
import java.lang.Exception
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

data class GlobalSettings(
    val checkForUpdates: Boolean = true,
)

data class LocalSettings(
    val block: LuckyBlockSettings,
)

data class MainResources(
    val globalSettings: GlobalSettings,
    val settings: LocalSettings,
    val drops: Map<String, List<WeightedDrop>>,
    val worldGenDrops: Map<String, List<WeightedDrop>>,
    val structures: Map<String, StructureResource>,
    val craftingLuckModifiers: Map<String, Int>,
)

data class AddonResources(
    val addon: Addon,
    val settings: LocalSettings,
    val drops: Map<String, List<WeightedDrop>>,
    val structures: Map<String, StructureResource>,
    val worldGenDrops: Map<String, List<WeightedDrop>>,
    val craftingLuckModifiers: Map<String, Int>,
    val blockCraftingRecipes: List<CraftingRecipe>
)

val commonConfigFiles = listOf(
    "drops.txt",
    "sword_drops.txt",
    "bow_drops.txt",
    "potion_drops.txt",
    "natural_gen.txt",
    "properties.txt",
    "structures.txt",
    "luck_crafting.txt",
)

fun getInputStream(baseDir: File, path: String): InputStream? {
    return if (baseDir.isDirectory) {
        val file = File("${baseDir}/${path}")
        if (file.exists() && !file.isDirectory) FileInputStream(file) else null
    } else {
        val file = ZipFile(baseDir)
        val entry = file.entries().asSequence().find { it.name == path }
        if (entry != null && !entry.isDirectory) file.getInputStream(entry) else null
    }
}

fun readLines(stream: InputStream): List<String> {
    val bufferedReader = BufferedReader(InputStreamReader(stream))
    return generateSequence { bufferedReader.readLine() }.toList()
}

fun getConfigDir(gameDir: File): File {
    return gameDir.resolve("config/lucky/${javaGameAPI.getModVersion()}-${javaGameAPI.getLoaderName()}")
}

fun parseDrops(lines: List<String>): List<WeightedDrop> {
    val newLines = splitLines(lines)
    return dropsFromStrList(newLines)
}

fun extractDefaultConfig(configDir: File) {
    try {
        val stream = JavaLuckyRegistry::class.java.getResourceAsStream("lucky-config.zip")
        if (stream == null) {
            gameAPI.logInfo("No default resources found. Ignore this in a dev environment.")
            return
        }
        val inputStream = ZipInputStream(stream)
        for (entry in generateSequence { inputStream.nextEntry }) {
            val dest = configDir.resolve(entry.name)
            if (!entry.isDirectory && !dest.exists()) {
                if (!dest.parentFile.exists()) dest.parentFile.mkdirs()
                dest.createNewFile()
                val outputStream = FileOutputStream(dest)
                for (dataByte in generateSequence { inputStream.read().let { if (it == -1) null else it } }) {
                    outputStream.write(dataByte)
                }
                outputStream.close()
            }
        }
        inputStream.close()
    } catch (e: Exception) {
        gameAPI.logError("Error extracting default config", e)
    }
}

fun loadMainResources(configDir: File): MainResources {
    val fileContents = commonConfigFiles.map { path ->
        val stream = getInputStream(configDir, path)
        if (stream == null) {
            gameAPI.logError("Missing resource '${path}' in ${configDir.path}")
            path to emptyList()
        } else path to readLines(stream)
    }.toMap()

    val drops = mapOf(
        JavaLuckyRegistry.blockId to "drops.txt",
        JavaLuckyRegistry.swordId to "sword_drops.txt",
        JavaLuckyRegistry.bowId to "bow_drops.txt",
        JavaLuckyRegistry.potionId to "potion_drops.txt",
    ).mapValues { (_, v) ->
        fileContents[v]?.let { parseDrops(it) } ?: emptyList()
    }

    return MainResources(
        globalSettings = parseGlobalSettings(fileContents["properties.txt"]!!),
        settings = parseLocalSettings(fileContents["properties.txt"]!!),
        drops = drops,
        worldGenDrops = readWorldGenDrops(fileContents["natural_gen.txt"]!!),
        craftingLuckModifiers = readCraftingLuckModifiers(fileContents["luck_crafting.txt"]!!),
        structures = readStructures(configDir, fileContents["structures.txt"]!!)
    )
}

fun loadAddonResources(addonFile: File): AddonResources? {
    val configFiles = commonConfigFiles + listOf("plugin_init.txt", "recipes.txt")
    val fileContents = configFiles.map { path ->
        val stream = getInputStream(addonFile, path)
        if (stream == null) {
            if (path == "plugin_init.txt") {
                gameAPI.logError("Missing resource '${path}' for addon '${addonFile.name}'")
                return null
            }
            path to emptyList()
        } else path to readLines(stream)
    }.toMap()

    val addonIds = readAddonIds(fileContents["plugin_init.txt"]!!)

    val drops = listOfNotNull(
        addonIds.block?.let { it to "drops.txt" },
        addonIds.sword?.let { it to "sword_drops.txt" },
        addonIds.bow?.let { it to "bow_drops.txt" },
        addonIds.potion?.let { it to "potion_drops.txt" },
    ).toMap().mapValues { (_, v) ->
        parseDrops(fileContents[v]!!)
    }

    val addonId = listOfNotNull(addonIds.block, addonIds.sword, addonIds.bow, addonIds.potion).first()
    return AddonResources(
        addon = Addon(ids = addonIds, file = addonFile, addonId = addonId),
        settings = parseLocalSettings(fileContents["properties.txt"]!!),
        drops = drops,
        structures = readStructures(addonFile, fileContents["structures.txt"] ?: emptyList()),
        worldGenDrops = fileContents["natural_gen.txt"]?.let { readWorldGenDrops(it) } ?: emptyMap(),
        craftingLuckModifiers = fileContents["luck_crafting.txt"]?.let { readCraftingLuckModifiers(it) } ?: emptyMap(),
        blockCraftingRecipes = if (addonIds.block != null && "recipes.txt" in fileContents)
            readAddonCraftingRecipes(fileContents["recipes.txt"]!!, addonIds.block)
            else emptyList()
    )
}

fun findAddonsOrMakeDir(gameDir: File): List<File> {
    val addonsDirV1 = gameDir.resolve("addons/luckyBlock")
    val addonsDirV2 = gameDir.resolve("addons/lucky")
    val addonsDir = when {
        addonsDirV2.exists() -> addonsDirV2
        addonsDirV1.exists() -> addonsDirV1
        else -> {
            addonsDirV2.mkdirs()
            return emptyList()
        }
    }
    return addonsDir.listFiles()?.toList()?.filter {
        it.isDirectory || it.extension in listOf("zip", "jar")
    } ?: emptyList()
}

fun loadResources(gameDir: File): Pair<MainResources, List<AddonResources>> {
    val configDir = getConfigDir(gameDir)
    extractDefaultConfig(configDir)

    val mainResources = loadMainResources(configDir)
    val allAddonResources = findAddonsOrMakeDir(gameDir).mapNotNull { loadAddonResources(it) }
    return Pair(mainResources, allAddonResources)
}