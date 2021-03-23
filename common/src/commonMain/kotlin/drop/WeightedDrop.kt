package mod.lucky.common.drop

import mod.lucky.common.*
import mod.lucky.common.attribute.*

data class WeightedDrop(
    val drop: BaseDrop,
    val dropString: String,
    val luck: Int = 0,
    val chance: Double?
) : BaseDrop {
    companion object
}

private val luckChanceSpec = dictSpecOf(
    "luck" to ValueSpec(AttrType.INT),
    "chance" to ValueSpec(AttrType.DOUBLE),
)

private fun readLuckChance(dropStr: String): Pair<String, DictAttr> {
    val extraPropIndex = indexOfUnnested(dropStr, 0, '@')
    return if (extraPropIndex == null) {
        Pair(dropStr, DictAttr())
    } else {
        val newDropStr = dropStr.substring(0 until extraPropIndex)

        val attrStr = dropStr.substring((extraPropIndex + 1) until dropStr.length).split('@').joinToString(",")
        val attr = try {
            parseEvalAttr(attrStr, luckChanceSpec, LuckyRegistry.parserContext, LuckyRegistry.simpleEvalContext) as DictAttr
        } catch (e: ParserError) {
            gameAPI.logError("Error reading extra drop properties '$attrStr' for drop: $dropStr", e)
            DictAttr()
        }
        Pair(newDropStr, attr)
    }
}

fun WeightedDrop.Companion.fromString(dropStr: String): WeightedDrop {
    val (strippedDropStr, extraProps) = readLuckChance(dropStr)

    val innerDrop: BaseDrop =
        if (strippedDropStr.toLowerCase().startsWith("group")) GroupDrop.fromString(strippedDropStr)
        else SingleDrop.fromString(strippedDropStr)

    return WeightedDrop(
        drop = innerDrop,
        dropString = dropStr,
        luck = extraProps.getOptionalValue<Int>("luck") ?: 0,
        chance = extraProps.getOptionalValue<Double>("chance"),
    )
}

fun dropsFromStrList(drops: List<String>): List<WeightedDrop> {
    return drops.mapNotNull {
        try {
            WeightedDrop.fromString(it)
        } catch (e: Exception) {
            gameAPI.logError("Error parsing drop: $it", e)
            null
        }
    }
}