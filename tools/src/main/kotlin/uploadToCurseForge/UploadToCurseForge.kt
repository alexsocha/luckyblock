@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools.uploadToCurseForge

import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import io.github.g00fy2.versioncompare.Version as ComparableVersion
import java.io.File

fun findCompatibleGameVersions(
    gameVersions: List<CurseForgeGameVersion>,
    luckyBlockDist: LuckyBlockDist,
): List<CurseForgeGameVersion> {
    fun findWithMin(
        type: CurseForgeGameVersionType,
        minVersionString: String,
        isCompatibleWithNextMinor: Boolean = true,
    ): List<CurseForgeGameVersion> {
        return gameVersions.filter {
            if (it.gameVersionTypeID == type.id) {
                val version = ComparableVersion(it.name)
                val minVersion = ComparableVersion(minVersionString)
                val nextIncompatibleVersion = ComparableVersion(listOf(minVersion.major + 1).joinToString("."))
                version >= minVersion && version < nextIncompatibleVersion
            } else false
        }
    }

    val javaVersions = gameVersions.filter {
        if (it.gameVersionTypeID == CurseForgeGameVersionType.JAVA.id) {
            val javaVersion = ComparableVersion(it.name.split(" ")[1])
            val minJavaVersion = ComparableVersion(luckyBlockDist.meta.min_java_version)
            javaVersion >= minJavaVersion
        } else false
    }

    val minecraftVersions = gameVersions.filter {
        // TODO: Filter relevant Minecraft versions using gameVersionTypeID. Currently we look at
        // everything that has a similar version format, and special-case a few non-Minecraft
        // versions.

        val version = ComparableVersion(it.name.replace("-Snapshot", ""))
        val minVersion = ComparableVersion(luckyBlockDist.meta.min_minecraft_version)
        val nextIncompatibleVersion = ComparableVersion(listOf(minVersion.major, minVersion.minor + 1).joinToString("."))
        version >= minVersion && version < nextIncompatibleVersion && it.gameVersionTypeID != 1 && it.id != 7430 && it.gameVersionTypeID != 615
    }

    val loaders = gameVersions.filter {
        it.gameVersionTypeID == CurseForgeGameVersionType.LOADER_TYPE.id &&
        it.name == getCurseForgeLoaderType(luckyBlockDist.loader)
    }

    return javaVersions + minecraftVersions + loaders

    // TODO: CurseForge currently doesn't allow you to specify Forge/Fabric as a dependency
    /*
    when(luckyBlockDist.loader) {
         LuckyBlockLoader.FORGE -> {
             val forgeVersions = findWithMin(
                 CurseForgeGameVersionType.FORGE,
                 (luckyBlockDist.meta as LuckyBlockForgeMeta).min_forge_version
             )
             return javaVersions + minecraftVersions + loaders + forgeVersions
         }
         LuckyBlockLoader.FABRIC -> {
             val fabricLoaderVersions = findWithMin(
                 CurseForgeGameVersionType.FABRIC_LOADER,
                 (luckyBlockDist.meta as LuckyBlockFabricMeta).min_fabric_loader_version
             )
             return javaVersions + minecraftVersions + loaders + fabricLoaderVersions
         }
    }
    */
}

fun uploadToCurseForge(
    curseForgeClient: CurseForgeClient,
    inputDistFolder: File
) = runBlocking {
    val luckyBlockDists = readLuckyBlockDists(inputDistFolder).sortedBy { it.meta.version_number }
    val gameVersions = curseForgeClient.getGameVersions()

    luckyBlockDists.forEach { luckyBlockDist ->
        val compatibleGameVersions = findCompatibleGameVersions(gameVersions, luckyBlockDist)
        val uploadMeta = CurseForgeUploadMetadata(
            changelog = "",
            displayName = luckyBlockDist.jarFile.nameWithoutExtension,
            gameVersions = compatibleGameVersions.map { it.id },
            releaseType = "release",
        )
        curseForgeClient.uploadDist(luckyBlockDist.jarFile, uploadMeta)
    }
}

class UploadToCurseForge: Subcommand("upload-to-curseforge", "Upload mod files to CurseForge") {
    val inputDistFolder by option(ArgType.String, description = "Folder containing mod files").default("./dist")

    override fun execute() = runBlocking {
        val apiToken = System.getenv("CURSEFORGE_API_TOKEN") ?: throw Exception("Missing CURSEFORGE_API_TOKEN")

        val curseForgeClient = CurseForgeClient(apiToken)
        uploadToCurseForge(curseForgeClient, File(inputDistFolder))
    }
}
