package mod.lucky.java.game

import mod.lucky.common.Enchantment
import mod.lucky.common.EnchantmentType
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

val ENCHANTMENTS = listOf(
    Enchantment("aqua_affinity", EnchantmentType.ARMOR_HEAD, maxLevel=1, isCurse=false),
    Enchantment("bane_of_arthropods", EnchantmentType.WEAPON, maxLevel=5, isCurse=false),
    Enchantment("blast_protection", EnchantmentType.ARMOR, maxLevel=4, isCurse=false),
    Enchantment("channeling", EnchantmentType.TRIDENT, maxLevel=1, isCurse=false),
    Enchantment("binding", EnchantmentType.WEARABLE, maxLevel=1, isCurse=true),
    Enchantment("vanishing", EnchantmentType.VANISHABLE, maxLevel=1, isCurse=true),
    Enchantment("depth_strider", EnchantmentType.ARMOR_FEET, maxLevel=3, isCurse=false),
    Enchantment("efficiency", EnchantmentType.DIGGER, maxLevel=5, isCurse=false),
    Enchantment("feather_falling", EnchantmentType.ARMOR_FEET, maxLevel=4, isCurse=false),
    Enchantment("fire_aspect", EnchantmentType.WEAPON, maxLevel=2, isCurse=false),
    Enchantment("fire_protection", EnchantmentType.ARMOR, maxLevel=4, isCurse=false),
    Enchantment("flame", EnchantmentType.BOW, maxLevel=1, isCurse=false),
    Enchantment("fortune", EnchantmentType.DIGGER, maxLevel=3, isCurse=false),
    Enchantment("frost_walker", EnchantmentType.ARMOR_FEET, maxLevel=2, isCurse=false),
    Enchantment("impaling", EnchantmentType.TRIDENT, maxLevel=5, isCurse=false),
    Enchantment("infinity", EnchantmentType.BOW, maxLevel=1, isCurse=false),
    Enchantment("knockback", EnchantmentType.WEAPON, maxLevel=2, isCurse=false),
    Enchantment("looting", EnchantmentType.WEAPON, maxLevel=3, isCurse=false),
    Enchantment("loyalty", EnchantmentType.TRIDENT, maxLevel=3, isCurse=false),
    Enchantment("luck_of_the_sea", EnchantmentType.FISHING_ROD, maxLevel=3, isCurse=false),
    Enchantment("lure", EnchantmentType.FISHING_ROD, maxLevel=3, isCurse=false),
    Enchantment("mending", EnchantmentType.BREAKABLE, maxLevel=1, isCurse=false),
    Enchantment("multishot", EnchantmentType.CROSSBOW, maxLevel=1, isCurse=false),
    Enchantment("piercing", EnchantmentType.CROSSBOW, maxLevel=4, isCurse=false),
    Enchantment("power", EnchantmentType.BOW, maxLevel=5, isCurse=false),
    Enchantment("projectile_protection", EnchantmentType.ARMOR, maxLevel=4, isCurse=false),
    Enchantment("protection", EnchantmentType.ARMOR, maxLevel=4, isCurse=false),
    Enchantment("punch", EnchantmentType.BOW, maxLevel=2, isCurse=false),
    Enchantment("quick_charge", EnchantmentType.CROSSBOW, maxLevel=3, isCurse=false),
    Enchantment("respiration", EnchantmentType.ARMOR_HEAD, maxLevel=3, isCurse=false),
    Enchantment("riptide", EnchantmentType.TRIDENT, maxLevel=3, isCurse=false),
    Enchantment("sharpness", EnchantmentType.WEAPON, maxLevel=5, isCurse=false),
    Enchantment("silk_touch", EnchantmentType.DIGGER, maxLevel=1, isCurse=false),
    Enchantment("smite", EnchantmentType.WEAPON, maxLevel=5, isCurse=false),
    Enchantment("soul_speed", EnchantmentType.ARMOR_FEET, maxLevel=3, isCurse=false),
    Enchantment("thorns", EnchantmentType.ARMOR, maxLevel=3, isCurse=false),
    Enchantment("unbreaking", EnchantmentType.BREAKABLE, maxLevel=3, isCurse=false),
)

fun writeNBTKeys(tag: NBTTag, attr: DictAttr) {
    attr.children.forEach { (k, v) -> JAVA_GAME_API.writeNBTKey(tag, k, JAVA_GAME_API.attrToNBT(v)) }
}

fun readNBTKeys(tag: NBTTag, keys: List<String>): DictAttr {
    return dictAttrOf(*keys.map { k ->
        k to JAVA_GAME_API.readNBTKey(tag, k)?.let { JAVA_GAME_API.nbtToAttr(it) }
    }.toTypedArray())
}
