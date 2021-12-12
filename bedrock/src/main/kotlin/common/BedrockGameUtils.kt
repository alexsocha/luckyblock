package mod.lucky.bedrock.common

import mod.lucky.common.*
import mod.lucky.common.LuckyRegistry.registerTemplateVar
import mod.lucky.common.attribute.*

data class BedrockPotion(
    val name: String,
    val id: Int,
    val extendedId: Int? = null,
    val level2Id: Int? = null,
    val isNegative: Boolean,
)

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

val USEFUL_POTIONS = listOf(
    BedrockPotion("night_vision", 5, extendedId=6, isNegative=false),
    BedrockPotion("invisibility", 7, extendedId=8, isNegative=false),
    BedrockPotion("leaping", 9, extendedId=10, level2Id=11, isNegative=false),
    BedrockPotion("fire_resistance", 12, extendedId=13, isNegative=false),
    BedrockPotion("swiftness", 15, extendedId=16, level2Id=17, isNegative=false),
    BedrockPotion("slowness", 17, extendedId=18, level2Id=42, isNegative=true),
    BedrockPotion("water_breathing", 19, extendedId=20, isNegative=false),
    BedrockPotion("healing", 21, level2Id=22, isNegative=false),
    BedrockPotion("harming", 23, level2Id=24, isNegative=true),
    BedrockPotion("poison", 25, extendedId=26, level2Id=27, isNegative=true),
    BedrockPotion("regeneration", 28, extendedId=29, level2Id=30, isNegative=false),
    BedrockPotion("strength", 31, extendedId=32, level2Id=33, isNegative=false),
    BedrockPotion("weakness", 34, extendedId=35, isNegative=true),
    BedrockPotion("decay", 35, isNegative=true),
    BedrockPotion("turtle_master", 37, extendedId=38, level2Id=39, isNegative=false),
    BedrockPotion("slow_falling", 40, extendedId=41, isNegative=false),
)

val ENCHANTMENTS = listOf(
    Enchantment("aqua_affinity", EnchantmentType.ARMOR_HEAD, intId=8, maxLevel=1, isCurse=false),
    Enchantment("bane_of_arthropods", EnchantmentType.WEAPON, intId=11, maxLevel=5, isCurse=false),
    Enchantment("blast_protection", EnchantmentType.ARMOR, intId=3, maxLevel=4, isCurse=false),
    Enchantment("channeling", EnchantmentType.TRIDENT, intId=32, maxLevel=1, isCurse=false),
    Enchantment("binding", EnchantmentType.WEARABLE, intId=27, maxLevel=1, isCurse=true),
    Enchantment("vanishing", EnchantmentType.VANISHABLE, intId=28, maxLevel=1, isCurse=true),
    Enchantment("depth_strider", EnchantmentType.ARMOR_FEET, intId=7, maxLevel=3, isCurse=false),
    Enchantment("efficiency", EnchantmentType.DIGGER, intId=15, maxLevel=5, isCurse=false),
    Enchantment("feather_falling", EnchantmentType.ARMOR_FEET, intId=2, maxLevel=4, isCurse=false),
    Enchantment("fire_aspect", EnchantmentType.WEAPON, intId=13, maxLevel=2, isCurse=false),
    Enchantment("fire_protection", EnchantmentType.ARMOR, intId=1, maxLevel=4, isCurse=false),
    Enchantment("flame", EnchantmentType.BOW, intId=21, maxLevel=1, isCurse=false),
    Enchantment("fortune", EnchantmentType.DIGGER, intId=18, maxLevel=3, isCurse=false),
    Enchantment("frost_walker", EnchantmentType.ARMOR_FEET, intId=25, maxLevel=2, isCurse=false),
    Enchantment("impaling", EnchantmentType.TRIDENT, intId=29, maxLevel=5, isCurse=false),
    Enchantment("infinity", EnchantmentType.BOW, intId=22, maxLevel=1, isCurse=false),
    Enchantment("knockback", EnchantmentType.WEAPON, intId=12, maxLevel=2, isCurse=false),
    Enchantment("looting", EnchantmentType.WEAPON, intId=14, maxLevel=3, isCurse=false),
    Enchantment("loyalty", EnchantmentType.TRIDENT, intId=31, maxLevel=3, isCurse=false),
    Enchantment("luck_of_the_sea", EnchantmentType.FISHING_ROD, intId=23, maxLevel=3, isCurse=false),
    Enchantment("lure", EnchantmentType.FISHING_ROD, intId=24, maxLevel=3, isCurse=false),
    Enchantment("mending", EnchantmentType.BREAKABLE, intId=26, maxLevel=1, isCurse=false),
    Enchantment("multishot", EnchantmentType.CROSSBOW, intId=33, maxLevel=1, isCurse=false),
    Enchantment("piercing", EnchantmentType.CROSSBOW, intId=34, maxLevel=4, isCurse=false),
    Enchantment("power", EnchantmentType.BOW, intId=19, maxLevel=5, isCurse=false),
    Enchantment("projectile_protection", EnchantmentType.ARMOR, intId=4, maxLevel=4, isCurse=false),
    Enchantment("protection", EnchantmentType.ARMOR, intId=0, maxLevel=4, isCurse=false),
    Enchantment("punch", EnchantmentType.BOW, intId=20, maxLevel=2, isCurse=false),
    Enchantment("quick_charge", EnchantmentType.CROSSBOW, intId=35, maxLevel=3, isCurse=false),
    Enchantment("respiration", EnchantmentType.ARMOR_HEAD, intId=6, maxLevel=3, isCurse=false),
    Enchantment("riptide", EnchantmentType.TRIDENT, intId=30, maxLevel=3, isCurse=false),
    Enchantment("sharpness", EnchantmentType.WEAPON, intId=9, maxLevel=5, isCurse=false),
    Enchantment("silk_touch", EnchantmentType.DIGGER, intId=16, maxLevel=1, isCurse=false),
    Enchantment("smite", EnchantmentType.WEAPON, intId=10, maxLevel=5, isCurse=false),
    Enchantment("soul_speed", EnchantmentType.ARMOR_FEET, intId=36, maxLevel=3, isCurse=false),
    Enchantment("thorns", EnchantmentType.ARMOR, intId=5, maxLevel=3, isCurse=false),
    Enchantment("unbreaking", EnchantmentType.BREAKABLE, intId=17, maxLevel=3, isCurse=false),
)

val SPAWN_EGG_IDS = listOf(
    "agent_spawn_egg",
    "axolotl_spawn_egg",
    "bat_spawn_egg",
    "bee_spawn_egg",
    "blaze_spawn_egg",
    "cat_spawn_egg",
    "cave_spider_spawn_egg",
    "chicken_spawn_egg",
    "cod_spawn_egg",
    "cow_spawn_egg",
    "creeper_spawn_egg",
    "dolphin_spawn_egg",
    "donkey_spawn_egg",
    "drowned_spawn_egg",
    "elder_guardian_spawn_egg",
    "enderman_spawn_egg",
    "endermite_spawn_egg",
    "evoker_spawn_egg",
    "fox_spawn_egg",
    "ghast_spawn_egg",
    "glow_squid_spawn_egg",
    "goat_spawn_egg",
    "guardian_spawn_egg",
    "hoglin_spawn_egg",
    "horse_spawn_egg",
    "husk_spawn_egg",
    "llama_spawn_egg",
    "magma_cube_spawn_egg",
    "mooshroom_spawn_egg",
    "mule_spawn_egg",
    "npc_spawn_egg",
    "ocelot_spawn_egg",
    "panda_spawn_egg",
    "parrot_spawn_egg",
    "phantom_spawn_egg",
    "pig_spawn_egg",
    "piglin_brute_spawn_egg",
    "piglin_spawn_egg",
    "pillager_spawn_egg",
    "polar_bear_spawn_egg",
    "pufferfish_spawn_egg",
    "rabbit_spawn_egg",
    "ravager_spawn_egg",
    "salmon_spawn_egg",
    "sheep_spawn_egg",
    "shulker_spawn_egg",
    "silverfish_spawn_egg",
    "skeleton_horse_spawn_egg",
    "skeleton_spawn_egg",
    "slime_spawn_egg",
    "spider_spawn_egg",
    "squid_spawn_egg",
    "stray_spawn_egg",
    "strider_spawn_egg",
    "tropical_fish_spawn_egg",
    "turtle_spawn_egg",
    "vex_spawn_egg",
    "villager_spawn_egg",
    "vindicator_spawn_egg",
    "wandering_trader_spawn_egg",
    "witch_spawn_egg",
    "wither_skeleton_spawn_egg",
    "wolf_spawn_egg",
    "zoglin_spawn_egg",
    "zombie_horse_spawn_egg",
    "zombie_pigman_spawn_egg",
    "zombie_spawn_egg",
    "zombie_villager_spawn_egg",
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

fun registerBedrockTemplateVars() {
    fun getRandomPotionData(potion: BedrockPotion, random: Random): Int {
        val dataValues = listOf(potion.id, potion.extendedId, potion.level2Id).filterNotNull()
        return chooseRandomFrom(random, dataValues)
    }

    registerTemplateVar("randPotionData") { _, context ->
        val potion = chooseRandomFrom(context.random, USEFUL_POTIONS)
        intAttrOf(getRandomPotionData(potion, context.random))
    }

    registerTemplateVar("randLuckyPotionData") { _, context ->
        val potion = chooseRandomFrom(context.random, USEFUL_POTIONS.filter { !it.isNegative })
        intAttrOf(getRandomPotionData(potion, context.random))
    }

    registerTemplateVar("randUnluckyPotionData") { _, context ->
        val potion = chooseRandomFrom(context.random, USEFUL_POTIONS.filter { it.isNegative })
        intAttrOf(getRandomPotionData(potion, context.random))
    }

    registerTemplateVar("randSpawnEgg") { _, context ->
        stringAttrOf(chooseRandomFrom(context.random, SPAWN_EGG_IDS))
    }
}
