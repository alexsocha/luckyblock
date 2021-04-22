package mod.lucky.java

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.registerVec3TemplateVar
import java.util.*

fun registerMultiList(
    templateName: String,
    getValues: () -> List<Attr>,
    defaultAmount: IntRange = 4..6,
) {
    val spec = TemplateVarSpec(
        listOf(
            Pair("minAmount", ValueSpec(AttrType.INT)),
            Pair("maxAmount", ValueSpec(AttrType.INT))
        ),
        argRange = 0..2
    )
    LuckyRegistry.registerTemplateVar(templateName, spec) { templateVar, _ ->
        val minAmount = templateVar.args.getOptionalValue(0) ?: defaultAmount.first
        val maxAmount = templateVar.args.getOptionalValue(1) ?: templateVar.args.getOptionalValue(0) ?: defaultAmount.last
        ListAttr(chooseMultiRandomFrom(getValues(), minAmount..maxAmount))
    }
}

private fun randEnchInstance(enchantment: Enchantment): DictAttr {
    return dictAttrOf(
        "id" to stringAttrOf(enchantment.id),
        "lvl" to ValueAttr(AttrType.SHORT, randInt(1..enchantment.maxLevel).toShort()),
    )
}

private fun randEffectInstance(effect: StatusEffect): DictAttr {
    val maxDurationTicks = if (effect.isInstant) 0 else 9600
    val minDurationTicks = maxDurationTicks / 3
    return dictAttrOf(
        "Id" to ValueAttr(AttrType.BYTE, effect.intId.toByte()),
        "Amplifier" to ValueAttr(AttrType.BYTE, randInt(0..3).toByte()),
        "Duration" to intAttrOf(randInt(minDurationTicks..maxDurationTicks)),
        "Ambient" to booleanAttrOf(false),
        "ShowParticles" to booleanAttrOf(true),
        "ShowIcon" to booleanAttrOf(true),
    )
}

fun registerEnchantments(
    templateName: String,
    types: List<EnchantmentType>,
    includeCurses: Boolean = false,
    defaultAmount: IntRange = 4..6,
) {
    registerMultiList(
        templateName,
        getValues = {
            val enchantments = javaGameAPI.getEnchantments(types).filter { if (!includeCurses) !it.isCurse else true }
            enchantments.map { randEnchInstance(it) }
        },
        defaultAmount = defaultAmount,
    )
}

fun registerStatusEffects(templateName: String, effectIds: List<String>, defaultAmount: IntRange) {
    registerMultiList(
        templateName,
        getValues = {
            val effects = effectIds.map { javaGameAPI.getStatusEffect(it)!! }
            effects.map { randEffectInstance(it) }
        },
        defaultAmount = defaultAmount,
    )
}

fun registerJavaTemplateVars() {
    LuckyRegistry.registerDropTemplateVar("pUUID") { _, context ->
        stringAttrOf(context.dropContext.player?.let { javaGameAPI.getEntityUUID(it) } ?: "")
    }

    LuckyRegistry.registerDropTemplateVar("pUUIDArray") { _, context ->
        ValueAttr(AttrType.INT_ARRAY, context.dropContext.player?.let {
            val uuid = UUID.fromString(javaGameAPI.getEntityUUID(it))
            intArrayOf(
                (uuid.mostSignificantBits shr 32).toInt(),
                uuid.mostSignificantBits.toInt(),
                (uuid.leastSignificantBits shr 32).toInt(),
                uuid.leastSignificantBits.toInt(),
            )
        } ?: intArrayOf())
    }

    registerVec3TemplateVar("bowPos", AttrType.DOUBLE, needsContext = true) { _, context ->
        val dropContext = context!!.dropContext
        dropContext.player?.let {
            javaGameAPI.getArrowPosAndVelocity(dropContext.world, it, dropContext.bowPower).first
        }
    }

    val bowMotionSpec = TemplateVarSpec(
        listOf(
            Pair("power", ValueSpec(AttrType.DOUBLE)),
            Pair("inaccuracyDeg", ValueSpec(AttrType.DOUBLE))
        ),
        argRange = 0..2
    )
    registerVec3TemplateVar("bowMotion", AttrType.DOUBLE, bowMotionSpec, needsContext = true) { templateVar, context ->
        val power = templateVar.args.getOptionalValue(0) ?: 1.0
        val inaccuracyDeg = templateVar.args.getOptionalValue(1) ?: 0.0
        val dropContext = context!!.dropContext
        dropContext.player?.let {
            javaGameAPI.getArrowPosAndVelocity(
                world = dropContext.world,
                player = it,
                bowPower = dropContext.bowPower * power,
                yawOffsetDeg = randDouble(-inaccuracyDeg, inaccuracyDeg),
                pitchOffsetDeg = randDouble(-inaccuracyDeg, inaccuracyDeg)
            ).second
        }
    }

    LuckyRegistry.registerTemplateVar("randFireworksRocket") { _, _ ->
        val colorAmount = randInt(1..4)
        val colors = (0 until colorAmount).map { chooseRandomFrom(javaGameAPI.getRBGPalette()) }.toIntArray()

        val explosion = dictAttrOf(
            "Type" to ValueAttr(AttrType.BYTE, randInt(0..4).toByte()),
            "Flicker" to booleanAttrOf(randInt(0..1) == 1),
            "Trail" to booleanAttrOf(randInt(0..1) == 1),
            "Colors" to ValueAttr(AttrType.INT_ARRAY, colors),
        )

        val firework = dictAttrOf(
            "Explosions" to listAttrOf(explosion),
            "Flight" to ValueAttr(AttrType.BYTE, randInt(1..2).toByte())
        )

        dictAttrOf("Fireworks" to firework)
    }

    registerEnchantments(
        "luckySwordEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEAPON),
    )
    registerEnchantments(
        "luckyAxeEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.DIGGER, EnchantmentType.WEAPON),
    )
    registerEnchantments(
        "luckyToolEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.DIGGER),
        defaultAmount = 2..3,
    )

    registerEnchantments(
        "luckyBowEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.BOW),
    )
    registerEnchantments(
        "luckyFishingRodEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.FISHING_ROD),
        defaultAmount = 2..3,
    )
    registerEnchantments(
        "luckyCrossbowEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.CROSSBOW),
        defaultAmount = 2..4,
    )
    registerEnchantments(
        "luckyTridentEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.TRIDENT),
        defaultAmount = 3..5,
    )

    registerEnchantments(
        "luckyHelmetEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_HEAD),
    )

    registerEnchantments(
        "luckyChestplateEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_CHEST),
    )

    registerEnchantments(
        "luckyLeggingsEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_LEGS),
    )

    registerEnchantments(
        "luckyBootsEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_FEET),
    )

    registerEnchantments("randEnchantment", EnchantmentType.values().toList(), defaultAmount = 1..1)

    val goodPotionEffects = listOf(
        "speed",
        "haste",
        "strength",
        "instant_health",
        "jump_boost",
        "regeneration",
        "resistance",
        "fire_resistance",
        "water_breathing",
        "invisibility",
        "night_vision",
        "health_boost",
        "absorption",
        "saturation",
        "glowing",
        "luck",
    )
    registerStatusEffects("luckyPotionEffects", goodPotionEffects, defaultAmount = 7..10)

    val badPotionEffects = listOf(
        "slowness",
        "mining_fatigue",
        "instant_damage",
        "nausea",
        "blindness",
        "hunger",
        "weakness",
        "poison",
        "wither",
        "levitation",
        "unluck",
        "slow_falling",
    )
    registerStatusEffects("unluckyPotionEffects", badPotionEffects, defaultAmount = 5..7)

    val chestLootSpec = TemplateVarSpec(listOf("lootTableId" to ValueSpec(AttrType.STRING)))
    LuckyRegistry.registerDropTemplateVar("chestLootTable", chestLootSpec) { templateVar, context ->
        javaGameAPI.generateChestLoot(
            context.dropContext.world,
            context.dropContext.pos.floor(),
            (templateVar.args[0] as ValueAttr).value as String
        )
    }

    // compatibility
    LuckyRegistry.registerDropTemplateVar("chestVillageArmorer") { _, context ->
        javaGameAPI.generateChestLoot(context.dropContext.world, context.dropContext.pos.floor(), "chests/village/village_armorer")
    }
    LuckyRegistry.registerDropTemplateVar("chestBonusChest") { _, context ->
        javaGameAPI.generateChestLoot(context.dropContext.world, context.dropContext.pos.floor(), "chests/spawn_bonus_chest")
    }
    LuckyRegistry.registerDropTemplateVar("chestDungeonChest") { _, context ->
        javaGameAPI.generateChestLoot(context.dropContext.world, context.dropContext.pos.floor(), "chests/simple_dungeon")
    }
}