@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools.uploadToCurseForge

import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import java.io.File
import io.github.cdimascio.dotenv.dotenv
import io.github.g00fy2.versioncompare.Version as ComparableVersion

fun findCompatibleGameVersions(
    gameVersions: List<CurseForgeGameVersion>,
    luckyBlockDist: LuckyBlockDist,
): List<CurseForgeGameVersion> {
    val javaVersions = gameVersions.filter {
        if (it.gameVersionTypeID == CurseForgeGameVersionType.JAVA.id) {
            val javaVersion = it.name.split(" ")[1] // e.g. Java 17 -> 17
            luckyBlockDist.meta.dependencies["java"]!!.contains(javaVersion)
        } else false
    }

    // TODO: Filter relevant Minecraft versions using gameVersionTypeID. Currently we look at
    // everything that has a similar version format, and special-case a few non-Minecraft
    // versions.
    val invalidGameVersionIds = listOf(7430)
    val invalidGameVersionTypeIds = listOf(1, 615)
    val minecraftVersions = gameVersions.filter {
        if (it.id in invalidGameVersionIds || it.gameVersionTypeID in invalidGameVersionTypeIds) false
        else {
            val minecraftVersion = it.name.replace("-Snapshot", "")
            luckyBlockDist.meta.dependencies["minecraft"]!!.contains(minecraftVersion)
        }
    }

    val loaders = gameVersions.filter {
        it.gameVersionTypeID == CurseForgeGameVersionType.LOADER_TYPE.id &&
        it.name == getCurseForgeLoaderType(luckyBlockDist.loader)
    }

    // TODO: CurseForge currently doesn't allow you to specify Forge/Fabric as a dependency
    return javaVersions + minecraftVersions + loaders
}

fun uploadToCurseForge(
    curseForgeClient: CurseForgeClient,
    inputDistFolder: File
) = runBlocking {
    val luckyBlockDists = readLuckyBlockDists(inputDistFolder).sortedBy { ComparableVersion(it.meta.version) }
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
        val dotenv = dotenv {
            ignoreIfMissing = true
            directory = ".."
        }
        val apiToken = dotenv["CURSEFORGE_API_TOKEN"] ?: throw Exception("Missing CURSEFORGE_API_TOKEN")

        val curseForgeClient = CurseForgeClient(apiToken)
        uploadToCurseForge(curseForgeClient, File(inputDistFolder))
    }
}
