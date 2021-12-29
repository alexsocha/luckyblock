@file:OptIn(ExperimentalCli::class)

package mod.lucky.tools

import kotlinx.cli.*
import kotlinx.serialization.builtins.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import mod.lucky.common.GameType
import com.charleskorn.kaml.Yaml
import java.nio.ByteOrder
import java.io.File

fun getBedrockBlockNameToIdMapping(page: Document): Map<String, String> {
    return page.select("[data-description=\"Block IDs\"]")
        .filter {
            it.select("th").toList().last().text() == "Block"
        }
        .flatMap { it.select("tr") }
        .mapNotNull {
            val columns = it.select("td").toList()
            if (columns.size < 2) null
            else {
                val blockLink = columns.last().select("a")
                if (blockLink.isEmpty()) null
                else {
                    val blockId = columns[columns.size - 2].text()
                    val blockName = blockLink[0].text()
                    blockName to getIdWithNamespace(blockId)
                }
            }
        }.toMap()
}

fun getJavaBlockNameToIdMapping(page: Document): Map<String, String> {
    return page.select("table")
        .filter {
            it.select("th").getOrNull(1)?.text() == "Block"
        }.first()
        .select("tr")
        .mapNotNull {
            val columns = it.select("td").toList()
            if (columns.size < 3) null
            else {
                val blockName = columns[1].text()
                val blockId = columns[2].text()
                blockName to getIdWithNamespace(blockId)
            }
        }.toMap()
}

fun downloadUrlWithCache(url: String): Document {
    val fileName = url.replace("/", "-")
    val cachedFile = File(".cached-pages/${fileName}.html")
    if (cachedFile.exists()) {
        return Jsoup.parse(cachedFile.readText())
    } else {
        val page = Jsoup.connect(url).get()
        cachedFile.parentFile.mkdirs()
        cachedFile.writeText(page.toString())
        return page
    }
}

class DownloadBlockIds: Subcommand("download-block-ids", "Download a block ID mapping using Minecraft Wiki pages") {
    val outputFile by option(ArgType.String, description = "Output YAML file").default("block-ids.yaml")

    override fun execute() {
        val bedrockIdsPage = downloadUrlWithCache("https://minecraft.fandom.com/wiki/Bedrock_Edition_data_values")
        val bedrockBlockNameToId = getBedrockBlockNameToIdMapping(bedrockIdsPage)

        val javaIdsPage = downloadUrlWithCache("https://minecraft.fandom.com/wiki/Java_Edition_data_values/Blocks")
        val javaBlockNameToId = getJavaBlockNameToIdMapping(javaIdsPage)

        val blockNames = (javaBlockNameToId.keys + bedrockBlockNameToId.keys).distinct()

        val blockIdConversions = blockNames.map {
            BlockIdConversion(mapOf(
                GameType.JAVA to javaBlockNameToId[it],
                GameType.BEDROCK to bedrockBlockNameToId[it],
            ))
        }.sortedWith(compareBy({ it.conversion[GameType.JAVA] }, { it.conversion[GameType.BEDROCK] }))

        File(outputFile).writeText(BlockIdConversions(blockIdConversions).toYaml())
    }
}

