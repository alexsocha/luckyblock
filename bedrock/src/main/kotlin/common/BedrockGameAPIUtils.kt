package mod.lucky.bedrock.common

import mod.lucky.common.*

val USEFUL_STATUS_EFFECTS = listOf(
    StatusEffect("speed", 1, isNegative=false, isInstant=false),
    StatusEffect("slowness", 2, isNegative=true, isInstant=false),
    StatusEffect("haste", 3, isNegative=false, isInstant=false),
    StatusEffect("strength", 5, isNegative=false, isInstant=false),
    StatusEffect("instant_health", 6, isNegative=false, isInstant=true),
    StatusEffect("instant_damage", 7, isNegative=true, isInstant=true),
    StatusEffect("jump_boost", 8, isNegative=false, isInstant=false),
    StatusEffect("regeneration", 10, isNegative=false, isInstant=false),
    StatusEffect("resistance", 11, isNegative=false, isInstant=false),
    StatusEffect("fire_resistance", 12, isNegative=false, isInstant=false),
    StatusEffect("water_breathing", 13, isNegative=false, isInstant=false),
    StatusEffect("invisibility", 14, isNegative=false, isInstant=false),
    StatusEffect("blindness", 15, isNegative=true, isInstant=false),
    StatusEffect("night_vision", 16, isNegative=false, isInstant=false),
    StatusEffect("hunger", 17, isNegative=true, isInstant=false),
    StatusEffect("weakness", 18, isNegative=true, isInstant=false),
    StatusEffect("poison", 19, isNegative=true, isInstant=false),
    StatusEffect("wither", 20, isNegative=true, isInstant=false),
    StatusEffect("absorption", 22, isNegative=false, isInstant=false),
    StatusEffect("saturation", 23, isNegative=false, isInstant=false),
    StatusEffect("fatal_poison", 25, isNegative=true, isInstant=false),
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

fun getRGBPalette(): List<Int> {
    // from https://www.schemecolor.com/bright-rainbow-colors.php
    val colors = listOf("A800FF", "0079FF", "00F11D", "FFEF00", "FF7F00", "FF0900")
    return colors.map { it.toInt(16) }
}

fun getUsefulPotionIds(): List<String> {
    return listOf("")
}

fun getSpawnEggIds(): List<String> {
    return listOf("")
}

fun getEnchantments(): List<Enchantment> {
    return ENCHANTMENTS
}

fun getUsefulStatusEffects(): List<StatusEffect> {
    return USEFUL_STATUS_EFFECTS
}
