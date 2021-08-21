package mod.lucky.common

import mod.lucky.common.attribute.*
import mod.lucky.common.attribute.EvalContext
import mod.lucky.common.drop.*

data class DropTemplateContext(
    val drop: SingleDrop?,
    val dropContext: DropContext?,
    val random: Random,
)

data class LuckyBlockSettings(
    val doDropsOnCreativeMode: Boolean = false
)

object LuckyRegistry {
    private val templateVarSpecs = HashMap<String, TemplateVarSpec>() // template name -> spec
    val templateVarFns = HashMap<String, TemplateVarFn>() // template name -> function

    val dropSpecs = HashMap<String, DictSpec>() // type -> spec
    val dropDefaults = HashMap<String, Map<String, Any>>() // type -> key -> default value
    val dropPropRenames = HashMap<String, Map<String, String>>() // type -> old -> new
    val dropTripleProps = HashMap<String, Triple<String, String, String>>() // key -> (key1, key2, key3)
    val dropActions = HashMap<String, (drop: SingleDrop, context: DropContext) -> Unit>() // type -> action

    val blockSettings = HashMap<String, LuckyBlockSettings>() // blockId -> settings
    val drops = HashMap<String, List<WeightedDrop>>() // sourceId -> drops
    val structureProps = HashMap<String, DictAttr>() // addonId:path -> props
    val structureDrops = HashMap<String, List<BaseDrop>>() // addonId:path -> drops

    val sourceToAddonId = HashMap<String, String>() // sourceId -> addonId

    val parserContext = ParserContext(
        templateVarSpecs = templateVarSpecs,
        escapeTemplatesInKeys = listOf("Drops", "drops", "impact").associateWith { true },
        escapeTemplatesInType = AttrType.LIST,
    )
    val simpleEvalContext = EvalContext(templateVarFns, null)

    init {
        registerDefaultDrops()
        registerDefaultTemplateVars()
    }

    fun registerDropPropRenames(type: String, renames: Map<String, String>) {
        // add the old keys to the spec
        val newSpecEntries = HashMap<String, AttrSpec>()
        val prevSpec = dropSpecs[type]!!
        newSpecEntries.putAll(prevSpec.children)
        for ((oldK, newK) in renames) {
            newSpecEntries[oldK] = prevSpec.children[newK]!!
        }

        dropSpecs[type] = prevSpec.copy(children=newSpecEntries)
        dropPropRenames[type] = renames
    }

    fun registerTemplateVar(name: String, spec: TemplateVarSpec = TemplateVarSpec(), fn: (TemplateVar, DropTemplateContext) -> Attr) {
        templateVarSpecs[name] = spec
        templateVarFns[name] = { args, context ->
            if (context is DropTemplateContext) fn(args, context)
            else throw EvalError("Can't evaluate template variable '$name' without context")
        }
    }

    fun registerAction(type: String, action: (drop: SingleDrop, context: DropContext) -> Unit) {
        dropActions[type] = action
    }
}
