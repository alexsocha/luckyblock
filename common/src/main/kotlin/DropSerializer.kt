package mod.lucky.java

import mod.lucky.common.*
import mod.lucky.common.drop.*
import mod.lucky.common.attribute.*
import mod.lucky.java.game.readNBTKeys
import mod.lucky.java.game.writeNBTKeys

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
        player = playerUUID?.let { JAVA_GAME_API.findEntityByUUID(world, it) },
        playerUUID = playerUUID,
        hitEntity = hitEntityUUID?.let { JAVA_GAME_API.findEntityByUUID(world, it) },
        hitEntityUUID = hitEntityUUID,
        sourceId = attr.getValue("sourceId"),
    )
}

fun DropContext.toAttr(): DictAttr {
    return dictAttrOf(
        "dropPos" to vec3AttrOf(AttrType.DOUBLE, pos),
        "bowPower" to doubleAttrOf(bowPower),
        "playerUUID" to (player?.let { stringAttrOf(JAVA_GAME_API.getEntityUUID(it)) } ?: playerUUID?.let {stringAttrOf(it)}),
        "hitEntityUUID" to (hitEntity?.let { stringAttrOf(JAVA_GAME_API.getEntityUUID(it)) } ?: hitEntityUUID?.let { stringAttrOf(it) }),
        "sourceId" to stringAttrOf(sourceId),
    )
}

data class DropContainer(
    val customDrops: List<WeightedDrop>? = null,
    val luck: Int? = null,
) {
    companion object {
        val attrKeys = listOf("lucky:drops", "lucky:luck")
    }
}

fun DropContainer.toAttr(): DictAttr {
    return dictAttrOf(
        "lucky:luck" to luck?.let { intAttrOf(it) },
        "lucky:drops" to customDrops?.let { ListAttr(it.map { v ->  v.toAttr() }) }
    )
}

fun DropContainer.Companion.fromAttr(attr: DictAttr): DropContainer {
    return DropContainer(
        (attr["lucky:drops"] as? ListAttr)?.let { dropsFromAttrList(it) },
        attr.getOptionalValue<Int>("lucky:luck"),
    )
}

fun DropContainer.writeToTag(tag: NBTTag) {
    return writeNBTKeys(tag, toAttr())
}

fun DropContainer.Companion.readFromTag(tag: NBTTag): DropContainer {
    try {
        return DropContainer.fromAttr(readNBTKeys(tag, attrKeys))
    } catch (e: Exception) {
        GAME_API.logError("Error reading drops", e)
    }
    return DropContainer()
}
