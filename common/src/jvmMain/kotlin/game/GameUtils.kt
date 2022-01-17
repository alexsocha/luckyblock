package mod.lucky.java.game

import mod.lucky.common.attribute.*
import mod.lucky.java.NBTTag
import mod.lucky.java.JAVA_GAME_API

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
    attr.children.forEach { (k, v) -> JAVA_GAME_API.writeNBTKey(tag, k, JAVA_GAME_API.attrToNBT(v)) }
}

fun readNBTKeys(tag: NBTTag, keys: List<String>): DictAttr {
    return dictAttrOf(*keys.map { k ->
        k to JAVA_GAME_API.readNBTKey(tag, k)?.let { JAVA_GAME_API.nbtToAttr(it) }
    }.toTypedArray())
}
