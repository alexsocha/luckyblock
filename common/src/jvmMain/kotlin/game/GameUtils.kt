package mod.lucky.java.game

import mod.lucky.common.attribute.*
import mod.lucky.java.NBTTag
import mod.lucky.java.javaGameAPI

val uselessPostionNames = listOf("empty", "water", "mundane", "thick", "awkward")
const val spawnEggSuffix = "_spawn_egg"

fun writeNBTKeys(tag: NBTTag, attr: DictAttr) {
    attr.children.forEach { (k, v) -> javaGameAPI.writeNBTKey(tag, k, javaGameAPI.attrToNBT(v)) }
}

fun readNBTKeys(tag: NBTTag, keys: List<String>): DictAttr {
    return dictAttrOf(*keys.map { k ->
        k to javaGameAPI.readNBTKey(tag, k)?.let { javaGameAPI.nbtToAttr(it) }
    }.toTypedArray())
}
