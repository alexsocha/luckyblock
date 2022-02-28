import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mod.lucky.tools.uploadToCurseForge.*
import kotlin.test.Test
import java.io.File
import io.mockk.*
import kotlin.test.BeforeTest

const val UPDATE_SNAPSHOTS = false
val curDir = File("./src/test/kotlin/uploadToCurseForge")

internal class TestUploadToCurseForge {
    private lateinit var mockCurseForgeClient: CurseForgeClient

    @BeforeTest
    fun setup() {
        mockCurseForgeClient = CurseForgeClient("")
        mockkObject(mockCurseForgeClient)

        val gameVersionsJson = curDir.resolve("game-versions.snapshot.json").readText()
        coEvery {
            mockCurseForgeClient.getGameVersions()
        } returns Json.decodeFromString(gameVersionsJson)
    }

    @Test
    fun testUploadToCurseForge() {
        if (UPDATE_SNAPSHOTS) {
            coEvery { mockCurseForgeClient.uploadDist(any(), any()) } answers {
                val jarFile = firstArg<File>()
                val meta = secondArg<CurseForgeUploadMetadata>()
                val distFolderName = jarFile.nameWithoutExtension
                curDir.resolve("${distFolderName}-upload-meta.snapshot.json").writeText(
                    Json.encodeToString(meta)
                )
            }
        } else {
            coEvery { mockCurseForgeClient.uploadDist(any(), any()) } answers {}
        }

        uploadToCurseForge(mockCurseForgeClient, curDir.resolve("dist-snapshot"))

        coVerify {
            val distFolderNames = listOf(
                "lucky-block-forge-1.10.2-2.0",
                "lucky-block-fabric-1.17.1-9.0",
            )
            for (distFolderName in distFolderNames) {
                mockCurseForgeClient.uploadDist(
                    curDir.resolve("dist-snapshot/$distFolderName/$distFolderName.jar"),
                    Json.decodeFromString(curDir.resolve("${distFolderName}-upload-meta.snapshot.json").readText())
                )
            }
        }
    }
}
