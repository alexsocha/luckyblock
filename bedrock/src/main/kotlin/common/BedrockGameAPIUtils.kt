package mod.lucky.bedrock.common

import mod.lucky.common.*

fun getRGBPalette(): List<Int> {
    // from https://www.schemecolor.com/bright-rainbow-colors.php
    val colors = listOf("A800FF", "0079FF", "00F11D", "FFEF00", "FF7F00", "FF0900")
    return colors.map { it.toInt(16) }
}

fun getEnchantments(types: List<EnchantmentType>): List<Enchantment> {
    return emptyList() // TODO
}
fun getStatusEffect(id: String): StatusEffect? {
    return null // TODO
}
