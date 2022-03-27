package mod.lucky.tools.uploadToCurseForge

import java.io.File

import kotlinx.serialization.*
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import io.github.g00fy2.versioncompare.Version as ComparableVersion

@Serializable
data class VersionRange(
    val minInclusive: String? = null,
    val minExclusive: String? = null,
    val maxInclusive: String? = null,
    val maxExclusive: String? = null,
) {
    fun contains(version: String): Boolean {
        val comparableVersion = ComparableVersion(version)
        val isWithinMin = when {
            minInclusive != null -> comparableVersion >= ComparableVersion(minInclusive)
            minExclusive != null -> comparableVersion > ComparableVersion(minExclusive)
            else -> true
        }
        val isWithinMax = when {
            maxInclusive != null -> comparableVersion <= ComparableVersion(maxInclusive)
            maxExclusive != null -> comparableVersion < ComparableVersion(maxExclusive)
            else -> true
        }
        return isWithinMin && isWithinMax
    }
}

@Serializable
data class ProjectDistMeta(
    val metafileVersion: Int = 2,
    val projectName: String,
    val version: String,
    val dependencies: Map<String, VersionRange>,
    val revision: String,
    val datetime: String,
)

enum class LuckyBlockLoader {
    FORGE,
    FABRIC,
}

data class LuckyBlockDist(
    val loader: LuckyBlockLoader,
    val meta: ProjectDistMeta,
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
            val meta = Yaml.default.decodeFromString<ProjectDistMeta>(metaText)
            val jarFile = distVersionFolder.resolve("${distVersionFolder.name}.jar")
            LuckyBlockDist(loader=loader, meta=meta, jarFile=jarFile)
        } else null
    }
}
