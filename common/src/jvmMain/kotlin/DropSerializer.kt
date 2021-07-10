package mod.lucky.java

import mod.lucky.common.*
import mod.lucky.common.drop.*
import mod.lucky.common.attribute.*
import mod.lucky.java.game.readNBTKeys
import mod.lucky.java.game.writeNBTKeys

/*
fun SingleDrop.Companion.fromAttrV1(dict: DictAttr): SingleDrop {
    val propsDictV1 = dict["properties"] as DictAttr
    val type = (propsDictV1["type"]!! as DictAttr).getValue<String>("value")
    val propString = propsDictV1.children.map { (k, v) ->
        k + "=" + ((v as DictAttr).getOptionalValue<String>("rawValue") ?: v.getValue("value"))
    }.joinToString(",")

    return SingleDrop(
        type = type,
        propString = propString,
        props = parseAttr(propString, LuckyRegistry.dropSpecs[type], LuckyRegistry.parserContext) as DictAttr,
    )
}
 */

fun SingleDrop.toAttr(): DictAttr {
    return dictAttrOf(
        "type" to stringAttrOf(type),
        "drop" to propsString?.let { stringAttrOf(propsString) },
        "cachedDrop" to if (!props.needsEval) props else null
    )
}

fun SingleDrop.Companion.fromAttr(dict: DictAttr): SingleDrop {
    val type = dict.getValue<String>("type")
    val propsString = dict.getOptionalValue<String>("drop")
    return SingleDrop(
        type = type,
        props = dict["cachedDrop"] as DictAttr? ?: parseAttr(
            propsString!!,
            LuckyRegistry.dropSpecs[type],
            LuckyRegistry.parserContext
        ) as DictAttr,
        propsString,
    )
}

fun WeightedDrop.toAttr(): ValueAttr {
    return stringAttrOf(dropString)
}

fun dropsFromAttrList(attr: ListAttr): List<WeightedDrop> {
    return dropsFromStrList(attr.toValueList())
}

fun dropsToAttrList(drops: List<WeightedDrop>): ListAttr {
    return ListAttr(drops.map { it.toAttr() })
}

fun DropContext.Companion.fromAttr(attr: DictAttr, world: World): DropContext {
    val playerUUID = attr.getOptionalValue<String>("playerUUID")
    val hitEntityUUID = attr.getOptionalValue<String>("hitEntityUUID")

    return DropContext(
        pos = attr.getVec3("dropPos"),
        world = world,
        bowPower = attr.getValue("bowPower"),
        player = playerUUID?.let { javaGameAPI.findEntityByUUID(world, it) as PlayerEntity },
        hitEntity = hitEntityUUID?.let { javaGameAPI.findEntityByUUID(world, it) },
        sourceId = attr.getValue("sourceId"),
    )
}

fun DropContext.toAttr(): DictAttr {
    return dictAttrOf(
        "dropPos" to vec3AttrOf(AttrType.DOUBLE, pos),
        "bowPower" to doubleAttrOf(bowPower),
        "playerUUID" to player?.let { stringAttrOf(javaGameAPI.getEntityUUID(it)) },
        "hitEntityUUID" to hitEntity?.let { stringAttrOf(javaGameAPI.getEntityUUID(it)) },
        "sourceId" to stringAttrOf(sourceId),
    )
}

data class DropContainer(
    val customDrops: List<WeightedDrop>? = null,
    val luck: Int? = null,
) {
    companion object {
        val attrKeys = listOf("Drops", "Luck")
    }
}

fun DropContainer.toAttr(): DictAttr {
    return dictAttrOf(
        "Luck" to luck?.let { intAttrOf(it) },
        "Drops" to customDrops?.let { ListAttr(it.map { v ->  v.toAttr() }) }
    )
}

fun DropContainer.Companion.fromAttr(attr: DictAttr): DropContainer {
    return DropContainer(
        (attr["Drops"] as? ListAttr)?.let { dropsFromAttrList(it) },
        attr.getOptionalValue<Int>("Luck"),
    )
}

fun DropContainer.writeToTag(tag: NBTTag) {
    return writeNBTKeys(tag, toAttr())
}

fun DropContainer.Companion.readFromTag(tag: NBTTag): DropContainer {
    try {
        return DropContainer.fromAttr(readNBTKeys(tag, attrKeys))
    } catch (e: Exception) {
        gameAPI.logError("Error reading drops", e)
    }
    return DropContainer()
}
