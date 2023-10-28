package mod.lucky.common

typealias Block = Any
typealias Item = Any
typealias PlayerEntity = Any
typealias Entity = Any
typealias World = Any

data class Enchantment(
    val id: String,
    val type: EnchantmentType,
    val maxLevel: Int,
    val isCurse: Boolean,
    val intId: Int = -1, // Bedrock Edition only
)

data class StatusEffect(
    val id: String,
    val isNegative: Boolean,
    val isInstant: Boolean,
)

enum class EnchantmentType {
    ARMOR,
    ARMOR_FEET,
    ARMOR_LEGS,
    ARMOR_CHEST,
    ARMOR_HEAD,
    WEAPON,
    DIGGER,
    FISHING_ROD,
    TRIDENT,
    BREAKABLE,
    BOW,
    WEARABLE,
    CROSSBOW,
    VANISHABLE,
}
