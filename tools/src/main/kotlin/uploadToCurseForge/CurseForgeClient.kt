package mod.lucky.tools.uploadToCurseForge

import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.InlineDataPart
import kotlinx.cli.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpUpload
import com.github.kittinunf.fuel.core.Method as HttpMethod
import com.github.kittinunf.result.Result
import mod.lucky.common.GameType
import java.io.File
import java.lang.Exception
import java.util.logging.Logger

@Serializable
data class CurseForgeGameVersion(
    val id: Int,
    val gameVersionTypeID: Int,
    val name: String,
    val slug: String,
)

enum class CurseForgeGameVersionType(val id: Int) {
    MINECRAFT(1),
    JAVA(2),
    FORGE(3),
    FABRIC_LOADER(73247),
    LOADER_TYPE(68441), // Forge, Fabric, Rift
}

fun getCurseForgeLoaderType(loader: LuckyBlockLoader): String {
    return when(loader) {
        LuckyBlockLoader.FORGE -> "Forge"
        LuckyBlockLoader.FABRIC -> "Fabric"
    }
}

@Serializable
data class CurseForgeUploadMetadata(
    val changelog: String, // Can be HTML or markdown if changelogType is set.
    val changelogType: String = "text", // One of "text", "html", "markdown"
    val displayName: String? = null, // Optional: A friendly display name used on the site if provided.
    val parentFileID: Int? = null, // Optional: The parent file of this file.
    val gameVersions: List<Int>, // A list of supported game versions, see the Game Versions API for details. Not supported if parentFileID is provided.
    val releaseType: String, // One of "alpha", "beta", "release".
)

class CurseForgeClient(private val token: String) {
    private val logger = Logger.getLogger(this.javaClass.name)
    private val LUCKY_BLOCK_PROJECT_ID = 576825

    suspend fun httpGet(endpoint: String, httpMethod: HttpMethod): String {
        val (_, _, result) = "https://minecraft.curseforge.com${endpoint}"
            .httpGet()
            .header(mapOf("X-Api-Token" to token))
            .awaitStringResponseResult()

        when (result) {
            is Result.Failure -> {
                throw result.getException()
            }
            is Result.Success -> {
                return result.get()
            }
        }
    }

    suspend fun httpUploadFile(jarFile: File, meta: String): String {
        val (_, _, result) = "https://minecraft.curseforge.com/"
            .httpGet()
            .header(mapOf("X-Api-Token" to token))
            .awaitStringResponseResult()

        when (result) {
            is Result.Failure -> throw result.getException()
            is Result.Success -> return result.get()
        }
    }

    suspend fun getGameVersions(): List<CurseForgeGameVersion> {
        logger.info("Downloading game versions from CurseForge")
        val rawResult = httpGet("/api/game/versions", HttpMethod.GET)
        return Json.decodeFromString(rawResult)
    }

    suspend fun uploadDist(jarFile: File, metadata: CurseForgeUploadMetadata) {
        logger.info("Uploading file to CurseForge, file=${jarFile}, metadata=${metadata}")

        val (_, response, result) = "https://minecraft.curseforge.com/api/projects/${LUCKY_BLOCK_PROJECT_ID}/upload-file"
            .httpUpload()
            .add(FileDataPart.from(jarFile.path, name="file"))
            .add(InlineDataPart(Json.encodeToString(metadata), contentType = "application/json", name = "metadata"))
            .header(mapOf("X-Api-Token" to token))
            .awaitStringResponseResult()

        when (result) {
            is Result.Failure -> {
                logger.severe(String(response.data))
                throw result.getException()
            }
            is Result.Success -> return
        }
    }
}