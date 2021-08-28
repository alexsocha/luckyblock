package mod.lucky.tools

import mod.lucky.common.attribute.*
import mod.lucky.common.drop.BaseDrop
import mod.lucky.common.drop.GroupDrop
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.WeightedDrop

fun attrToSerializedString(attr: Attr): String {
    return when(attr) {
        is ListAttr -> "[${attr.children.joinToString(",") { attrToSerializedString(it) }}]"
        is DictAttr -> "(${attr.children.toSortedMap().entries.joinToString(",") { (k, v) -> "$k=${attrToSerializedString(v)}" }})"
        is ValueAttr -> when(attr.type) {
            AttrType.STRING -> "\"${attr.value as String}\""
            AttrType.BOOLEAN -> (attr.value as Boolean).toString()
            AttrType.BYTE -> "${attr.value}b"
            AttrType.SHORT -> "${attr.value}s"
            AttrType.INT -> attr.value.toString()
            AttrType.LONG -> "${attr.value}l"
            AttrType.FLOAT -> "${attr.value}f"
            AttrType.DOUBLE -> "${attr.value}d"
            AttrType.BYTE_ARRAY -> (attr.value as ByteArray).joinToString(":") { "${it}b" }
            AttrType.INT_ARRAY -> (attr.value as IntArray).joinToString(":")
            AttrType.LONG_ARRAY -> (attr.value as ByteArray).joinToString(":") { "${it}l" }
            AttrType.DICT -> throw Exception()
            AttrType.LIST -> throw Exception()
        }
        is TemplateAttr -> attr.templateVars.joinToString("") { (stringValue, templateVar) ->
            stringValue ?: if (templateVar != null) {
                if (templateVar.args.children.isNotEmpty()) {
                    val argsStr = attrToSerializedString(templateVar.args)
                    "#${templateVar.name}(${argsStr.substring(1 until argsStr.length - 1)})"
                } else "#${templateVar.name}"
            } else ""
        }
        else -> throw Exception()
    }
}

fun dropToString(drop: BaseDrop): String {
    return when(drop) {
        is WeightedDrop -> dropToString(drop.drop) +
            (if (drop.luck != 0) "@luck=${drop.luck}" else "") +
            (if (drop.chance != null) "@chance=${drop.chance}" else "")
        is GroupDrop -> {
            val amountAttr = drop.amount
            val dropStrs = drop.drops.map { dropToString(it) }

            if (amountAttr is ValueAttr && amountAttr.value is Int && amountAttr.value == drop.drops.size) {
                "group(${dropStrs.joinToString(";")})"
            } else {
                "group:${attrToSerializedString(drop.amount)}:(${dropStrs.joinToString(";")})"
            }
        }
        is SingleDrop -> {
            val sortedProps = drop.props.children.toSortedMap { k1, k2 ->
                if (k1 == "type") -1 else if (k2 == "type") 1 else k1.compareTo(k2)
            }
            sortedProps.entries.joinToString(",") { (k, v) -> "${k}=${attrToSerializedString(v)}" }
        }
        else -> throw Exception()
    }
}


fun luckyStructToString(defaultProps: DictAttr, drops: List<BaseDrop>): List<String> {
    val lines = if (defaultProps.children.size == 0) drops.map { dropToString(it) }
        else listOf(
            ">properties",
            attrToSerializedString(defaultProps),
            ">drops",
            *(drops.map { dropToString(it) }).toTypedArray()
        )

    return lines
}
