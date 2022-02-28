package mod.lucky.tools.uploadToCurseForge

import java.io.File

import kotlinx.serialization.*
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

sealed interface LuckyBlockMeta {
    val version: String
    val version_number: Int
    val min_minecraft_version: String
    val min_java_version: String
    val revision: String?
    val datetime: String
}

@Serializable
data class LuckyBlockForgeMeta(
    override val version: String,
    override val version_number: Int,
    override val min_minecraft_version: String,
    override val min_java_version: String,
    override val revision: String? = null,
    override val datetime: String,
    val min_forge_version: String,
) : LuckyBlockMeta

@Serializable
data class LuckyBlockFabricMeta(
    override val version: String,
    override val version_number: Int,
    override val min_minecraft_version: String,
    override val min_java_version: String,
    override val revision: String? = null,
    override val datetime: String,
    val min_fabric_loader_version: String,
) : LuckyBlockMeta

enum class LuckyBlockLoader {
    FORGE,
    FABRIC,
}

data class LuckyBlockDist(
    val loader: LuckyBlockLoader,
    val meta: LuckyBlockMeta,
    val jarFile: File,
)

fun readLuckyBlockDists(distFolder: File): List<LuckyBlockDist> {
    return distFolder.listFiles()!!.mapNotNull { distVersionFolder ->
        val loader = when {
            distVersionFolder.isDirectory && distVersionFolder.name.startsWith("lucky-block-forge") ->
                LuckyBlockLoader.FORGE
            distVersionFolder.isDirectory && distVersionFolder.name.startsWith("lucky-block-fabric") ->
                LuckyBlockLoader.FABRIC
            else -> null
        }
        if (loader != null) {
            val metaText = distVersionFolder.resolve("meta.yaml").readText()
            val meta = when (loader) {
                LuckyBlockLoader.FORGE -> Yaml.default.decodeFromString<LuckyBlockForgeMeta>(metaText)
                LuckyBlockLoader.FABRIC -> Yaml.default.decodeFromString<LuckyBlockFabricMeta>(metaText)
            }
            val jarFile = distVersionFolder.resolve("${distVersionFolder.name}.jar")
            LuckyBlockDist(loader=loader, meta=meta, jarFile=jarFile)
        } else null
    }
}
