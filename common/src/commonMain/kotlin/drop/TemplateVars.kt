package mod.lucky.common.drop

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.LuckyRegistry.registerTemplateVar
import mod.lucky.common.attribute.EvalError
import mod.lucky.common.colorNames
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class MissingDropContextException : Exception() {}

fun <T : Number> registerVec3TemplateVar(
    baseName: String,
    type: AttrType,
    spec: TemplateVarSpec = TemplateVarSpec(),
    posFn: (TemplateVar, DropTemplateContext) -> Vec3<T>?
) {
    val zero = castNum(type, 0)
    registerTemplateVar("${baseName}X", spec) { t, c -> ValueAttr(type, posFn(t, c)?.x ?: zero) }
    registerTemplateVar("${baseName}Y", spec) { t, c -> ValueAttr(type, posFn(t, c)?.y ?: zero) }
    registerTemplateVar("${baseName}Z", spec) { t, c -> ValueAttr(type, posFn(t, c)?.z ?: zero) }
    registerTemplateVar(baseName, spec) { t, c ->
        vec3AttrOf(type, posFn(t, c) ?: Vec3(zero, zero, zero))
    }
}

fun registerMultiListTemplateVar(
    templateName: String,
    getValues: (context: DropTemplateContext) -> List<Attr>,
    defaultAmount: IntRange = 4..6,
) {
    val spec = TemplateVarSpec(
        listOf(
            Pair("minAmount", ValueSpec(AttrType.INT)),
            Pair("maxAmount", ValueSpec(AttrType.INT))
        ),
        argRange = 0..2
    )
    LuckyRegistry.registerTemplateVar(templateName, spec) { templateVar, context ->
        val minAmount = templateVar.args.getOptionalValue(0) ?: defaultAmount.first
        val maxAmount = templateVar.args.getOptionalValue(1) ?: templateVar.args.getOptionalValue(0) ?: defaultAmount.last
        ListAttr(chooseMultiRandomFrom(context.random, getValues(context), minAmount..maxAmount))
    }
}

private fun randEnchInstance(gameType: GameType, random: Random, enchantment: Enchantment): DictAttr {
    return dictAttrOf(
        "id" to when(gameType) {
            GameType.JAVA -> stringAttrOf(enchantment.id)
            GameType.BEDROCK -> ValueAttr(AttrType.SHORT, enchantment.intId.toShort())
        },
        "lvl" to ValueAttr(AttrType.SHORT, random.randInt(1..enchantment.maxLevel).toShort()),
    )
}

private fun randEffectInstance(random: Random, effect: StatusEffect): DictAttr {
    val maxDurationTicks = if (effect.isInstant) 0 else 9600
    val minDurationTicks = maxDurationTicks / 3
    return dictAttrOf(
        "Id" to ValueAttr(AttrType.BYTE, effect.intId.toByte()),
        "Amplifier" to ValueAttr(AttrType.BYTE, random.randInt(0..3).toByte()),
        "Duration" to intAttrOf(random.randInt(minDurationTicks..maxDurationTicks)),
        "Ambient" to booleanAttrOf(false),
        "ShowParticles" to booleanAttrOf(true),
        "ShowIcon" to booleanAttrOf(true),
    )
}

fun registerEnchantments(
    templateName: String,
    types: List<EnchantmentType>,
    gameType: GameType,
    includeCurses: Boolean = false,
    defaultAmount: IntRange = 4..6,
) {
    registerMultiListTemplateVar(
        templateName,
        getValues = { context ->
            val enchantments = GAME_API.getEnchantments().filter {
                if (it.type !in types) false 
                else if (!includeCurses && it.isCurse) false
                else true
            }
            enchantments.map { randEnchInstance(gameType, context.random, it) }
        },
        defaultAmount = defaultAmount,
    )
}

fun registerStatusEffects(templateName: String, statusEffects: List<StatusEffect>, defaultAmount: IntRange) {
    registerMultiListTemplateVar(
        templateName,
        getValues = { context ->
            statusEffects.map { randEffectInstance(context.random, it) }
        },
        defaultAmount = defaultAmount,
    )
}

fun registerCommonTemplateVars(gameType: GameType) {
    registerTemplateVar("randPotion") { _, context -> stringAttrOf(chooseRandomFrom(context.random, GAME_API.getUsefulPotionIds())) }
    registerTemplateVar("randSpawnEgg") { _, context -> stringAttrOf(chooseRandomFrom(context.random, GAME_API.getSpawnEggIds())) }
    registerTemplateVar("randColor") { _, context -> stringAttrOf(chooseRandomFrom(context.random, colorNames)) }

    fun <T> ensureDropContext(context: DropTemplateContext, fn: (dropContext: DropContext) -> T): T {
        if (context.dropContext == null) throw MissingDropContextException()
        return fn(context.dropContext)
    }

    registerTemplateVar("time") { _, context ->
        ensureDropContext(context) {
            stringAttrOf(GAME_API.getWorldTime(it.world).toString())
        }
    }

    registerVec3TemplateVar("bPos", AttrType.INT) { _, context ->
        ensureDropContext(context) { it.pos.floor() }
    }
    registerVec3TemplateVar("bExactPos", AttrType.DOUBLE) { _, context ->
        ensureDropContext(context) { it.pos }
    }

    registerVec3TemplateVar("ePos", AttrType.INT) { _, context ->
        ensureDropContext(context) {
            it.hitEntity?.let { GAME_API.getEntityPos(it).floor() }
        }
    }
    registerVec3TemplateVar("eExactPos", AttrType.DOUBLE) { _, context ->
        ensureDropContext(context) {
            it.hitEntity?.let { GAME_API.getEntityPos(it) }
        }
    }
    registerVec3TemplateVar("pPos", AttrType.INT) { _, context ->
        ensureDropContext(context) {
            it.player?.let { GAME_API.getEntityPos(it).floor() }
        }
    }
    registerVec3TemplateVar("pExactPos", AttrType.DOUBLE) { _, context ->
        ensureDropContext(context) {
            it.player?.let { GAME_API.getEntityPos(it) }
        }
    }

    registerTemplateVar("pName") { _, context ->
        ensureDropContext(context) {
            stringAttrOf(it.player?.let { GAME_API.getPlayerName(it) } ?: "")
        }
    }

    registerTemplateVar("pDirect") { _, context ->
        ensureDropContext(context) {
            it.player?.let {
                val rotInt = round((GAME_API.getPlayerHeadYawDeg(it) + 180.0) / 90.0).toInt() % 4
                val rotIntClamped = if (rotInt < 0) (rotInt + 4) else rotInt
                intAttrOf(rotIntClamped)
            } ?: intAttrOf(0)
        }
    }
    registerTemplateVar("pYaw") { _, context ->
        ensureDropContext(context) {
            doubleAttrOf(it.player?.let { GAME_API.getPlayerHeadYawDeg(it) } ?: 0.0)
        }
    }
    registerTemplateVar("pPitch") { _, context ->
        ensureDropContext(context) {
            doubleAttrOf(it.player?.let { GAME_API.getPlayerHeadPitchDeg(it) } ?: 0.0)
        }
    }

    fun randInRange(random: Random, minAttr: ValueAttr, maxAttr: ValueAttr): ValueAttr {
        val decimalType = when {
            isDecimalType(minAttr.type) -> minAttr.type
            isDecimalType(maxAttr.type) -> maxAttr.type
            else -> null
        }
        if (decimalType != null) {
            val result = random.randDouble((minAttr.value as Number).toDouble(), (maxAttr.value as Number).toDouble())
            return ValueAttr(decimalType, castNum(decimalType, result))
        }
        val result = random.randInt((minAttr.value as Number).toInt()..(maxAttr.value as Number).toInt())
        return ValueAttr(minAttr.type, castNum(minAttr.type, result))
    }

    val randSpec = TemplateVarSpec(listOf("min" to ValueSpec(), "max" to ValueSpec()))
    registerTemplateVar("rand", randSpec) { templateVar, context ->
        randInRange(context.random, templateVar.args[0] as ValueAttr, templateVar.args[1] as ValueAttr)
    }
    registerTemplateVar("randPosNeg", randSpec) { templateVar, context ->
        val numAttr = randInRange(context.random, templateVar.args[0] as ValueAttr, templateVar.args[1] as ValueAttr)
        val multiplier = if (context.random.randInt(0..1) == 0) -1 else 1
        ValueAttr(numAttr.type, castNum(numAttr.type, (numAttr.value as Number).toDouble() * multiplier.toDouble()))
    }

    val randListSpec = TemplateVarSpec(listOf("value" to ValueSpec()), argRange = 0..Int.MAX_VALUE)
    registerTemplateVar("randList", randListSpec) { v, context -> chooseRandomFrom(context.random, v.args.toList()) }

    val circleOffsetSpec = TemplateVarSpec(
        listOf("min" to ValueSpec(AttrType.INT), "max" to ValueSpec(AttrType.INT)),
        argRange = 1..2
    )
    registerTemplateVar("circleOffset", circleOffsetSpec) { templateAttr, context ->
        val min = templateAttr.args.getValue<Int>(0)
        val max = templateAttr.args.getOptionalValue<Int>(1) ?: min

        val radius = context.random.randInt(min..max)
        val angle = context.random.randInt(0..360)
        val length = round(radius * sin(degToRad(angle.toDouble()))).toInt()
        val width = round(radius * cos(degToRad(angle.toDouble()))).toInt()

        listAttrOf(intAttrOf(length), intAttrOf(0), intAttrOf(width))
    }

    val motionTagType = when(gameType) {
        GameType.JAVA -> AttrType.DOUBLE
        GameType.BEDROCK -> AttrType.FLOAT
    }

    val motionFromDirectionSpec = TemplateVarSpec(
        listOf(
            "yawDeg" to ValueSpec(AttrType.DOUBLE),
            "pitchDeg" to ValueSpec(AttrType.DOUBLE),
            "power" to ValueSpec(AttrType.DOUBLE)
        ),
        argRange = 2..3
    )
    registerVec3TemplateVar("motionFromDirection", motionTagType, motionFromDirectionSpec) { templateVar, _ ->
        val yawDeg = templateVar.args.getValue<Double>(0)
        val pitchDeg = templateVar.args.getValue<Double>(1)
        val power = templateVar.args.getOptionalValue(2) ?: 1.0

        val motion = directionToVelocity(degToRad(yawDeg), degToRad(pitchDeg), power)
        when(gameType) {
            GameType.JAVA -> motion
            GameType.BEDROCK -> motion.toFloat()
        }
    }

    val launchMotionSpec = TemplateVarSpec(
        listOf(
            "power" to ValueSpec(AttrType.DOUBLE),
            "angleDeg" to ValueSpec(AttrType.DOUBLE)
        ),
        argRange = 0..2
    )
    registerVec3TemplateVar("randLaunchMotion", motionTagType, launchMotionSpec) { templateVar, context ->
        val power = templateVar.args.getOptionalValue(0) ?: 0.9
        val pitchOffsetDeg = templateVar.args.getOptionalValue(1) ?: 15.0
        val yawDeg = context.random.randDouble(-180.0, 180.0)
        val pitchDeg = -90.0 + context.random.randDouble(-pitchOffsetDeg, pitchOffsetDeg)

        val motion = directionToVelocity(degToRad(yawDeg), degToRad(pitchDeg), power)
        when(gameType) {
            GameType.JAVA -> motion
            GameType.BEDROCK -> motion.toFloat()
        }
    }

    LuckyRegistry.registerTemplateVar("randFireworksRocket") { _, context ->
        val fireworkType = ValueAttr(AttrType.BYTE, context.random.randInt(0..4).toByte())
        val fireworkFlicker = booleanAttrOf(context.random.randInt(0..1) == 1)
        val fireworkTrail = booleanAttrOf(context.random.randInt(0..1) == 1)

        val colorAmount = context.random.randInt(1..4)
        val fireworkColors = when(gameType) {
            GameType.JAVA -> ValueAttr(AttrType.INT_ARRAY, (0 until colorAmount).map {
                chooseRandomFrom(context.random, GAME_API.getRGBPalette())
            }.toIntArray())
            GameType.BEDROCK -> ValueAttr(AttrType.BYTE_ARRAY, (0 until colorAmount).map {
                context.random.randInt(0..8).toByte()
            }.toByteArray())
        }

        val explosion = when(gameType) {
            GameType.JAVA -> dictAttrOf(
                "Type" to fireworkType,
                "Flicker" to fireworkFlicker,
                "Trail" to fireworkTrail,
                "Colors" to fireworkColors,
            )
            GameType.BEDROCK -> dictAttrOf(
                "FireworkType" to fireworkType,
                "FireworkFlicker" to fireworkFlicker,
                "FireworkTrail" to fireworkTrail,
                "FireworkColor" to fireworkColors,
            )
        }

        val firework = dictAttrOf(
            "Explosions" to listAttrOf(explosion),
            "Flight" to ValueAttr(AttrType.BYTE, context.random.randInt(1..2).toByte())
        )

        dictAttrOf("Fireworks" to firework)
    }

    registerEnchantments(
        "luckySwordEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEAPON),
        gameType,
    )
    registerEnchantments(
        "luckyAxeEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.DIGGER, EnchantmentType.WEAPON),
        gameType,
    )
    registerEnchantments(
        "luckyToolEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.DIGGER),
        gameType,
        defaultAmount = 2..3,
    )

    registerEnchantments(
        "luckyBowEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.BOW),
        gameType,
    )
    registerEnchantments(
        "luckyFishingRodEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.FISHING_ROD),
        gameType,
        defaultAmount = 2..3,
    )
    registerEnchantments(
        "luckyCrossbowEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.CROSSBOW),
        gameType,
        defaultAmount = 2..4,
    )
    registerEnchantments(
        "luckyTridentEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.TRIDENT),
        gameType,
        defaultAmount = 3..5,
    )

    registerEnchantments(
        "luckyHelmetEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_HEAD),
        gameType,
    )

    registerEnchantments(
        "luckyChestplateEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_CHEST),
        gameType,
    )

    registerEnchantments(
        "luckyLeggingsEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_LEGS),
        gameType,
    )

    registerEnchantments(
        "luckyBootsEnchantments",
        listOf(EnchantmentType.BREAKABLE, EnchantmentType.WEARABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_FEET),
        gameType,
    )

    registerEnchantments(
        "randEnchantment",
        EnchantmentType.values().toList(),
        gameType,
        defaultAmount = 1..1
    )

    registerStatusEffects(
        "luckyPotionEffects",
        GAME_API.getUsefulStatusEffects().filter { !it.isNegative },
        defaultAmount = 7..10
    )
    registerStatusEffects(
        "unluckyPotionEffects",
        GAME_API.getUsefulStatusEffects().filter { it.isNegative },
        defaultAmount = 5..7
    )

    val evalSpec = TemplateVarSpec(listOf("jsScript" to ValueSpec(AttrType.INT)))
    registerTemplateVar("eval", evalSpec) { v, _ ->
        val result = PLATFORM_API.evalJS(v.args.getValue(0))
        if (result is String) stringAttrOf(unescapeSpecialChars(result))
        else stringAttrOf(result.toString())
    }

    val jsonStrSpec = TemplateVarSpec(listOf("key=value" to DictSpec(emptyMap())), argRange = 0..Int.MAX_VALUE)
    fun jsonStr(templateVar: TemplateVar): ValueAttr {
        // each dict entry is provided as a separate argument
        val dictAttr = DictAttr(templateVar.args.children.map {
            val entry = (it as DictAttr).children.entries.first()
            entry.key to entry.value
        }.toMap())
        return stringAttrOf(attrToJsonStr(dictAttr))
    }

    registerTemplateVar("jsonStr", jsonStrSpec) { v, _ -> jsonStr(v) }
    registerTemplateVar("json", jsonStrSpec) { v, _ -> jsonStr(v) } // compatibility

    val sPosSpec = TemplateVarSpec(
        listOf("x" to ValueSpec(), "y" to ValueSpec(), "z" to ValueSpec())
    )

    val sPosUnrelatedVars = LuckyRegistry.templateVarFns.filterKeys { !it.startsWith("sPos") }
    val sPosRelatedKeys = (
        listOf("pos", "centerOffset", "rotation")
        + LuckyRegistry.dropTripleProps["pos"]!!.toList()
        + LuckyRegistry.dropTripleProps["centerOffset"]!!.toList()
    )

    fun getStructurePos(templateVar: TemplateVar, context: DropTemplateContext): ListAttr {
        val args = templateVar.args
        val posNumberType = when {
            isDecimalType(args[0]!!.type) -> args[0]!!.type
            isDecimalType(args[1]!!.type) -> args[1]!!.type
            isDecimalType(args[2]!!.type) -> args[2]!!.type
            else -> args[0]!!.type
        }

        val structProps = dictAttrOf(*sPosRelatedKeys.map { it to context.drop!!.props.children[it] }.toTypedArray())
        val structDrop = SingleDrop(context.drop!!.type, evalAttr(structProps, EvalContext(sPosUnrelatedVars, context)) as DictAttr)

        val pos = structDrop.getVec3<Double>("pos")
        val centerOffset = structDrop.getVec3<Double>("centerOffset")
        val rotation: Int = structDrop["rotation"]

        val worldPos = getWorldPos(args.toVec3<Number>().toDouble(), centerOffset, pos, rotation)

        if (isDecimalType(posNumberType)) return vec3AttrOf(AttrType.DOUBLE, worldPos)
        else return vec3AttrOf(AttrType.INT, worldPos.floor())
    }
    registerTemplateVar("sPos", sPosSpec) { templateVar, context -> getStructurePos(templateVar, context) }

    val dropPropSpec = TemplateVarSpec(listOf("property" to ValueSpec(AttrType.STRING)))
    registerTemplateVar("drop", dropPropSpec) { templateVar, context ->
        val propNameInit = templateVar.args.getValue<String>(0).lowercase()
        val propNameMap = LuckyRegistry.dropSpecs[context.drop!!.type]!!.children.map { (k) -> k.lowercase() to k }.toMap()
        val propName = propNameMap[propNameInit.lowercase()] ?: propNameInit

        // prevent potential infinite recursion by excluding the 'drop' template var
        val evalContext = EvalContext(LuckyRegistry.templateVarFns.filterKeys { it != "drop" }, context)

        if (propName in context.drop.props) evalAttr(context.drop.props[propName]!!, evalContext)
        else throw EvalError("Can't reference missing drop property '$propNameInit' in drop '${context.drop.propsString}'")
    }
}
