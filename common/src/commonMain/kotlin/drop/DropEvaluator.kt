package mod.lucky.common.drop

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.attribute.evalAttr

import kotlin.math.pow

const val DEBUG = false
private var debugDropFilters = listOf<String>()
private var debugDropIndexRange = 0..1000
private var debugDropIndex = debugDropIndexRange.first

class DropError(msg: String) : Exception("Error performing Lucky Block function: $msg")

data class DropContext(
    val world: World,
    val pos: Vec3d,
    val player: PlayerEntity? = null,
    val hitEntity: Entity? = null,
    val bowPower: Double = 1.0,
    val sourceId: String,
) { companion object }


fun createDropEvalContext(drop: SingleDrop, dropContext: DropContext): EvalContext {
    return EvalContext(LuckyRegistry.templateVarFns, DropTemplateContext(drop, dropContext))
}

fun createVecSpec(baseName: String, partNames: Triple<String, String, String>, type: AttrType? = null): Array<Pair<String, AttrSpec>> {
    return arrayOf(
        partNames.first to ValueSpec(type),
        partNames.second to ValueSpec(type),
        partNames.third to ValueSpec(type),
        baseName to ListSpec(listOf(ValueSpec(type), ValueSpec(type), ValueSpec(type)))
    )
}

fun evalDrop(template: BaseDrop, context: DropContext): List<SingleDrop> {
    if (template is WeightedDrop) {
        return evalDrop(template.drop, context)
    }
    if (template is GroupDrop) {
        val evalContext = createDropEvalContext(SingleDrop.nothingDrop, context)
        val shuffledDrops = if (template.shuffle) template.drops.shuffled() else template.drops
        val groupAmount = (evalAttr(template.amount, evalContext) as ValueAttr).value as Int

        return (0 until groupAmount).map {
            evalDrop(shuffledDrops[it], context)
        }.flatten().toList()
    }
    if (template is SingleDrop) {
        val drop = template.eval(context)
        val repeatAmount: Int = drop["amount"]
        val evalOnRepeat: Boolean = drop["reinitialize"]
        val evalAfterDelay: Boolean = drop["postDelayInit"]

        if ("delay" in template) {
            // Evaluation conditions for delayed drops:
            // evalAfterDelay & evalOnRepeat: add N templates, evaluate each later
            // evalAfterDelay & not evalOnRepeat: add one template, evaluate later
            // not evalAfterDelay & evalOnRepeat: add N differently evaluated drops
            // not evalAfterDelay & not evalOnRepeat: add one evaluated drop

            if (evalOnRepeat) {
                return (0 until repeatAmount).map {
                    if (evalAfterDelay) template.evalKeys(listOf("delay"), context) else if (it == 0) drop else template.eval(context)
                }.toList()
            }
            return listOf(if (evalAfterDelay) template.copy(props = template.props.with(mapOf("delay" to drop["delay"]))) else drop)
        }

        return (0 until repeatAmount).map {
            if (it == 0) drop else if (evalOnRepeat) template.eval(context) else drop
        }.toList()
    }
    return emptyList()
}

fun evalDropAfterDelay(dropOrTemplate: SingleDrop, context: DropContext): List<SingleDrop> {
    val evalAfterDelay: Boolean = dropOrTemplate["postDelayInit"]
    val drop = if (evalAfterDelay) dropOrTemplate.eval(context) else dropOrTemplate

    // if buildOnRepeat is true, this must be one of N delayed drops, each with amount=1
    val evalOnRepeat: Boolean = drop["reinitialize"]
    if (evalOnRepeat) return listOf(drop)

    return (0 until drop["amount"]).map { drop }.toList()
}

private fun getDropIndexByWeight(weightPoints: ArrayList<Double>, randomIndex: Double): Int {
    for (i in weightPoints.indices) {
        if (randomIndex >= weightPoints[i] && randomIndex < weightPoints[i + 1]) return i
    }
    return 0
}

private fun chooseRandomDrop(drops: List<WeightedDrop>, luck: Int): WeightedDrop {
    if (drops.isEmpty()) throw DropError("No drops found")

    var lowestLuck = 0
    var heighestLuck = 0
    for (i in drops.indices) {
        if (drops[i].luck < lowestLuck) lowestLuck = drops[i].luck
        if (drops[i].luck > heighestLuck) heighestLuck = drops[i].luck
    }
    heighestLuck += lowestLuck * -1 + 1
    val levelIncrease = 1.0 / (1.0 - (if (luck < 0) luck * -1 else luck) * 0.77 / 100.0)
    var weightTotal = 0.0
    val weightPoints = ArrayList<Double>()
    weightPoints.add(0.0)
    for (drop in drops) {
        val dropLuck = drop.luck + lowestLuck * -1 + 1
        val newLuck =
            if (luck >= 0) levelIncrease.pow(dropLuck.toDouble()).toFloat() else levelIncrease
                .pow(heighestLuck + 1 - dropLuck.toDouble()).toFloat()
        val newChance = (drop.chance ?: 1.0) * newLuck * 100
        weightTotal += newChance
        weightPoints.add(weightTotal)
    }
    val randomIndex = randDouble(0.0, 1.0) * weightTotal
    return drops[getDropIndexByWeight(weightPoints, randomIndex)]
}

fun runEvaluatedDrop(drop: SingleDrop, context: DropContext) {
    if ("delay" in drop && drop.get<Double>("delay") > 0.0) {
        gameAPI.scheduleDrop(drop, context, drop["delay"])
    } else {
        val fn = LuckyRegistry.dropActions[drop.type]!!
        fn(drop, context)
    }
}

fun runDrop(drop: WeightedDrop, context: DropContext, showOutput: Boolean) {
    if (showOutput) gameAPI.logInfo("Chosen Lucky Drop: ${drop.dropString}")
    val singleDrops = evalDrop(drop, context)
    singleDrops.forEach { runEvaluatedDrop(it, context) }
}

fun runDropAfterDelay(delayedDrop: SingleDrop, context: DropContext) {
    val singleDrops = evalDropAfterDelay(delayedDrop, context)
    for (drop in singleDrops) {
        val fn = LuckyRegistry.dropActions[drop.type]!!
        fn(drop, context)
    }
}

fun nextDebugDrop(drops: List<WeightedDrop>): WeightedDrop {
    // during hot reloading, edit the values here
    val newFilters: List<String> = listOf()
    val newIndexRange = 0..1000

    if (newIndexRange != debugDropIndexRange || newFilters != debugDropFilters) {
        debugDropIndexRange = newIndexRange
        debugDropFilters = newFilters
        debugDropIndex = debugDropIndexRange.first
    }

    val filteredDrops = if (debugDropFilters.isNotEmpty()) drops.filter { d -> debugDropFilters.any { it in d.dropString } } else drops

    if (debugDropIndex > debugDropIndexRange.last || debugDropIndex >= filteredDrops.size) debugDropIndex = debugDropIndexRange.first
    if (debugDropIndex >= filteredDrops.size) debugDropIndex = 0
    val index = debugDropIndex
    debugDropIndex += 1

    return filteredDrops[index]
}

fun runRandomDrop(customDrops: List<WeightedDrop>? = null, luck: Int, context: DropContext, showOutput: Boolean) {
    val drops = customDrops ?: LuckyRegistry.drops[context.sourceId] ?: emptyList()
    val drop = if (DEBUG && customDrops == null) nextDebugDrop(drops) else chooseRandomDrop(drops, luck)
    runDrop(drop, context, showOutput)
}