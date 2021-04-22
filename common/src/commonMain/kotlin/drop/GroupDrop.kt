package mod.lucky.common.drop

import mod.lucky.common.LuckyRegistry
import mod.lucky.common.attribute.*

data class GroupDrop(
    val drops: List<BaseDrop> = ArrayList(),
    val amount: Attr,
    val shuffle: Boolean = false,
) : BaseDrop {
    companion object
}

fun GroupDrop.Companion.fromString(groupStr: String): GroupDrop {
    val splitByColon = splitBracketString(groupStr, ':')

    val splitByComma = splitBracketString(splitByColon.last(), ',')

    val innerGroupStr = if (splitByComma[0].startsWith("group", ignoreCase = true))
        splitByComma[0].substring("group(".length until splitByComma[0].length - 1)
        else splitByComma[0].substring(1 until splitByComma[0].length - 1)

    val commonAttrStr = if (splitByComma.size > 1) splitByComma.subList(1, splitByComma.size).joinToString(",") else null
    val groupAttrStrs = splitBracketString(innerGroupStr, ';')

    val hasAmount = splitByColon.size >= 2
    val amountAttr = if (hasAmount) parseAttr(splitByColon[1], ValueSpec(AttrType.INT), LuckyRegistry.parserContext)
        else ValueAttr(AttrType.INT, groupAttrStrs.size)

    val drops = groupAttrStrs.map {
        val dropStr = if (commonAttrStr != null) "$it,$commonAttrStr" else it
        if (dropStr.startsWith("group", ignoreCase = true)) {
            GroupDrop.fromString(dropStr)
        } else {
            SingleDrop.fromString(dropStr)
        }
    }
    return GroupDrop(drops = drops, amount = amountAttr, shuffle = hasAmount)
}