package mod.lucky.common.drop

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.LuckyRegistry.registerTemplateVar
import mod.lucky.common.attribute.EvalError
import mod.lucky.common.colorNames
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

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

fun registerDefaultTemplateVars() {
    registerTemplateVar("randPotion") { _, context -> stringAttrOf(chooseRandomFrom(context.random, gameAPI.getUsefulPotionIds())) }
    registerTemplateVar("randSpawnEgg") { _, context -> stringAttrOf(chooseRandomFrom(context.random, gameAPI.getSpawnEggIds())) }
    registerTemplateVar("randColor") { _, context -> stringAttrOf(chooseRandomFrom(context.random, colorNames)) }
    registerTemplateVar("time") { _, context ->
        stringAttrOf(
            gameAPI.getWorldTime(context.dropContext!!.world).toString()
        )
    }

    registerVec3TemplateVar("bPos", AttrType.INT) { _, context ->
        context.dropContext!!.pos.floor()
    }
    registerVec3TemplateVar("bExactPos", AttrType.DOUBLE) { _, context ->
        context.dropContext!!.pos
    }

    registerVec3TemplateVar("ePos", AttrType.INT) { _, context ->
        context.dropContext!!.hitEntity?.let {
            gameAPI.getEntityPos(it).floor()
        }
    }
    registerVec3TemplateVar("eExactPos", AttrType.DOUBLE) { _, context ->
        context.dropContext!!.hitEntity?.let { gameAPI.getEntityPos(it) }
    }
    registerVec3TemplateVar("pPos", AttrType.INT) { _, context ->
        context.dropContext!!.player?.let { gameAPI.getEntityPos(it).floor() }
    }
    registerVec3TemplateVar("pExactPos", AttrType.DOUBLE) { _, context ->
        context.dropContext!!.player?.let { gameAPI.getEntityPos(it) }
    }

    registerTemplateVar("pName") { _, context ->
        stringAttrOf(context.dropContext!!.player?.let { gameAPI.getPlayerName(it) } ?: "")
    }

    registerTemplateVar("pDirect") { _, context ->
        context.dropContext!!.player?.let {
            val rotInt = round((gameAPI.getPlayerHeadYawDeg(it) + 180.0) / 90.0).toInt() % 4
            val rotIntClamped = if (rotInt < 0) (rotInt + 4) else rotInt
            intAttrOf(rotIntClamped)
        } ?: intAttrOf(0)
    }
    registerTemplateVar("pYaw") { _, context ->
        doubleAttrOf(context.dropContext!!.player?.let { gameAPI.getPlayerHeadYawDeg(it) } ?: 0.0)
    }
    registerTemplateVar("pPitch") { _, context ->
        doubleAttrOf(context.dropContext!!.player?.let { gameAPI.getPlayerHeadPitchDeg(it) } ?: 0.0)
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


    val motionFromDirectionSpec = TemplateVarSpec(
        listOf(
            "yawDeg" to ValueSpec(AttrType.DOUBLE),
            "pitchDeg" to ValueSpec(AttrType.DOUBLE),
            "power" to ValueSpec(AttrType.DOUBLE)
        ),
        argRange = 2..3
    )
    registerVec3TemplateVar("motionFromDirection", AttrType.DOUBLE, motionFromDirectionSpec) { templateVar, _ ->
        val yawDeg = templateVar.args.getValue<Double>(0)
        val pitchDeg = templateVar.args.getValue<Double>(1)
        val power = templateVar.args.getOptionalValue(2) ?: 1.0
        directionToVelocity(degToRad(yawDeg), degToRad(pitchDeg), power)
    }

    val launchMotionSpec = TemplateVarSpec(
        listOf(
            "power" to ValueSpec(AttrType.DOUBLE),
            "angleDeg" to ValueSpec(AttrType.DOUBLE)
        ),
        argRange = 0..2
    )
    registerVec3TemplateVar("randLaunchMotion", AttrType.DOUBLE, launchMotionSpec) { templateVar, context ->
        val power = templateVar.args.getOptionalValue(0) ?: 0.9
        val pitchOffsetDeg = templateVar.args.getOptionalValue(1) ?: 15.0
        val yawDeg = context.random.randDouble(-180.0, 180.0)
        val pitchDeg = -90.0 + context.random.randDouble(-pitchOffsetDeg, pitchOffsetDeg)
        directionToVelocity(degToRad(yawDeg), degToRad(pitchDeg), power)
    }

    val evalSpec = TemplateVarSpec(listOf("jsScript" to ValueSpec(AttrType.INT)))
    registerTemplateVar("eval", evalSpec) { v, _ ->
        val result = platformAPI.evalJS(v.args.getValue(0))
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
        val type = when {
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
        return vec3AttrOf(type, worldPos)
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