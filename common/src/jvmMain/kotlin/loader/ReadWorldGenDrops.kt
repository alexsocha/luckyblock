package mod.lucky.java.loader

import mod.lucky.common.attribute.parseSectionedLines
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.dropsFromStrList

fun resolveDimensionKey(key: String): String {
    val mappedKey = when (key) {
        "surface" -> "overworld"
        "nether" -> "the_nether"
        "end" -> "the_end"
        else -> key
    }
    return if (':' in mappedKey) mappedKey else "minecraft:$mappedKey"
}

fun readWorldGenDrops(lines: List<String>): Map<String, List<WeightedDrop>> {
    val sections = parseSectionedLines(lines)
    return sections.map { (k, dropLines) ->
        resolveDimensionKey(k) to dropsFromStrList(dropLines)
    }.toMap()
}