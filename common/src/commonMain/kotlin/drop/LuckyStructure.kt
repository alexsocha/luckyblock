package mod.lucky.common.drop

import mod.lucky.common.LuckyRegistry
import mod.lucky.common.attribute.*
import mod.lucky.common.gameAPI

private fun parseDictOrList(spec: DictSpec, orderedSpecKeys: List<String>, value: String): DictAttr {
    val splitProps = splitBracketString(value, ',')
    val childProps = splitProps.mapIndexed { i, childStr ->
        if ("=" in childStr && childStr.split("=")[0].matches("[a-zA-Z]+".toRegex())) {
            val dictAttr = parseAttr(childStr, spec, LuckyRegistry.parserContext) as DictAttr
            dictAttr.children.entries.first().key to dictAttr.children.entries.first().value
        } else {
            val key = orderedSpecKeys[i]
            val attr = parseAttr(childStr, spec.children[key], LuckyRegistry.parserContext)
            key to attr
        }
    }
    return dictAttrOf(*childProps.toTypedArray())
}

@Throws(ParserError::class)
fun readLuckyStructure(lines: List<String>): Pair<DictAttr, List<SingleDrop>> {
    val defaultBlockSpec = LuckyRegistry.dropSpecs["block"]!!
    // for legacy reasons block states might be provided as numbers, so we remove them from the spec
    val blockSpec = defaultBlockSpec.copy(children = defaultBlockSpec.children.minus("state"))
    val orderedBlockSpecKeys = listOf("posOffsetX", "posOffsetY", "posOffsetZ", "id", "state", "nbttag")

    val entitySpec = LuckyRegistry.dropSpecs["entity"]!!
    val orderedEntitySpecKeys = listOf("posOffsetX", "posOffsetY", "posOffsetZ", "id", "nbttag")

    val sections = parseSectionedLines(lines)

    val defaultPropsStr = sections.getOrElse("properties", { sections.getOrElse("default") { emptyList() } }).joinToString(",")
    val defaultProps = try {
        val attr = parseAttr(defaultPropsStr, LuckyRegistry.dropSpecs["structure"]!!, LuckyRegistry.parserContext) as DictAttr
        SingleDrop.processProps("structure", attr)
    } catch (e: ParserError) {
        gameAPI.logError("Error reading structure props '$defaultPropsStr'", e)
        throw e
    }

    val blockDrops = sections.getOrElse("blocks", { emptyList() }).mapNotNull {
        try {
            val props = parseDictOrList(blockSpec, orderedBlockSpecKeys, it)
            val propsWithValidState = if (props["state"] is DictAttr) props else props.copy(children = props.children.minus("state"))
            SingleDrop("block", SingleDrop.processProps("block", propsWithValidState))
        } catch (e: ParserError) {
            gameAPI.logError("Error reading structure drop '$it'", e)
            null
        }
    }
    val entityDrops = sections.getOrElse("entities", { emptyList() }).mapNotNull {
        try {
            val props = parseDictOrList(entitySpec, orderedEntitySpecKeys, it)
            SingleDrop("entity", SingleDrop.processProps("entity", props))
        } catch (e: ParserError) {
            gameAPI.logError("Error reading structure entity '$it'", e)
            null
        }
    }

    return Pair(defaultProps, blockDrops + entityDrops)
}
