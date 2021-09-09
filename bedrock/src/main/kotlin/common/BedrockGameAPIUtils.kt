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
    Enchantment("xxx", EnchantmentType.ARMOR, maxLevel=2, isCurse=false),
)

fun getRGBPalette(): List<Int> {
    // from https://www.schemecolor.com/bright-rainbow-colors.php
    val colors = listOf("A800FF", "0079FF", "00F11D", "FFEF00", "FF7F00", "FF0900")
    return colors.map { it.toInt(16) }
}

fun getEnchantments(): List<Enchantment> {
    return ENCHANTMENTS
}

fun getUsefulStatusEffects(): List<StatusEffect> {
    return USEFUL_STATUS_EFFECTS
}
