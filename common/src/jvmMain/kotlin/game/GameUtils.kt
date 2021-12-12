package mod.lucky.java.game

import mod.lucky.common.attribute.*
import mod.lucky.java.NBTTag
import mod.lucky.java.javaGameAPI

val uselessPostionNames = listOf("empty", "water", "mundane", "thick", "awkward")
const val spawnEggSuffix = "_spawn_egg"
val usefulStatusEffectIds = listOf(
    "speed",
    "slowness",
    "haste",
    "strength",
    "instant_health",
    "instant_damage",
    "jump_boost",
    "regeneration",
    "resistance",
    "fire_resistance",
    "water_breathing",
    "invisibility",
    "blindness",
    "night_vision",
    "hunger",
    "weakness",
    "poison",
    "wither",
    "absorption",
    "saturation",
    "glowing",
    "glowing",
    "unluck",
)

fun writeNBTKeys(tag: NBTTag, attr: DictAttr) {
    attr.children.forEach { (k, v) -> javaGameAPI.writeNBTKey(tag, k, javaGameAPI.attrToNBT(v)) }
}

fun readNBTKeys(tag: NBTTag, keys: List<String>): DictAttr {
    return dictAttrOf(*keys.map { k ->
        k to javaGameAPI.readNBTKey(tag, k)?.let { javaGameAPI.nbtToAttr(it) }
    }.toTypedArray())
}
