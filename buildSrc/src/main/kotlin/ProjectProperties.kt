package mod.lucky.build

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlinx.serialization.*
import org.ajoberstar.grgit.Grgit
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration

val yaml = Yaml(configuration = YamlConfiguration(encodeDefaults=false))

@Serializable
data class VersionRange(
    val minInclusive: String? = null,
    val minExclusive: String? = null,
    val maxInclusive: String? = null,
    val maxExclusive: String? = null,
) {
    fun toGradleRange(): String {
        if (minInclusive != null && maxInclusive != null && minInclusive == maxInclusive) return minInclusive
        val minPart = when {
            minInclusive != null -> "[$minInclusive"
            minExclusive != null -> "($minExclusive"
            else -> "("
        }
        val maxPart = when {
            maxInclusive != null -> "$maxInclusive]"
            maxExclusive != null -> "$maxExclusive)"
            else -> ")"
        }
        return "$minPart,$maxPart"
    }
}

@Serializable
data class ProjectProperties(
    val version: String,
    val dependencies: Map<String, VersionRange>,
    val devDependencies: Map<String, VersionRange> = emptyMap(),
    val lockedDependencies: Map<String, String> = emptyMap(),
)

@Serializable
data class ProjectDistMeta(
    val metafileVersion: Int,
    val projectName: ProjectName,
    val version: String,
    val dependencies: Map<String, VersionRange>,
    val revision: String,
    val datetime: String,
) {
    fun toYaml(): String {
        return yaml.encodeToString(serializer(), this)
    }
}

fun getModVersionAsInt(modVersion: String): Int {
    val splitVersion = modVersion.split('-')
    val mcVersion = splitVersion[0].split('.')
    val luckyBlockVersion = splitVersion[1].split('.')
    return (mcVersion[0].toInt()) * 100000000 +
            (mcVersion[1].toInt()) * 1000000 +
            (mcVersion.getOrElse(2) { "0" }.toInt()) * 10000 +
            luckyBlockVersion[0].toInt() * 100 +
            luckyBlockVersion[1].toInt()
}

@Serializable
enum class ProjectName(val fullName: String, val shortName: String? = null) {
    @SerialName("lucky-block-forge") LUCKY_BLOCK_FORGE("lucky-block-forge", "forge"),
    @SerialName("lucky-block-fabric") LUCKY_BLOCK_FABRIC("lucky-block-fabric", "fabric"),
    @SerialName("lucky-block-bedrock") LUCKY_BLOCK_BEDROCK("lucky-block-bedrock", "bedrock"),
    @SerialName("custom-lucky-block-java") CUSTOM_LUCKY_BLOCK_JAVA("custom-lucky-block-java"),
}

@Serializable
data class RootProjectProperties(
    val projects: Map<ProjectName, ProjectProperties>,
) {
    fun getDistMeta(rootDir: File, projectName: ProjectName): ProjectDistMeta {
        val gitClient = Grgit.open(mapOf("currentDir" to rootDir))
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val subprojectProps = projects[projectName]!!
        return ProjectDistMeta(
            metafileVersion=2,
            projectName=projectName,
            version=subprojectProps.version,
            dependencies=subprojectProps.dependencies,
            revision=gitClient.head().abbreviatedId,
            datetime=dateFormat.format(Date()),
        )
    }

    companion object {
        fun fromProjectYaml(rootDir: File): RootProjectProperties {
            val yamlStr = rootDir.resolve("project.yaml").readText()
            return yaml.decodeFromString(serializer(), yamlStr)
        }
    }
}
