package mod.lucky.common.drop

import mod.lucky.common.*
import mod.lucky.common.attribute.*

interface BaseDrop

data class SingleDrop(
    val type: String,
    val props: DictAttr,
    val propsString: String? = null,
) : BaseDrop {
    companion object {
        val nothingDrop = SingleDrop("nothing", DictAttr())
    }

    operator fun contains(k: String) = k in props

    @Suppress("UNCHECKED_CAST")
    operator fun <T>get(k: String, default: T? = null): T {
        if (k in this.props) {
            val v = this.props[k]!!
            return if (v is ValueAttr) v.value as T else v as T
        }
        return default ?: LuckyRegistry.dropDefaults[this.type]!![k]!! as T
    }

    fun <T>getOrNull(k: String): T? {
        return if (k in this) this[k] else null
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Number>getVec3(k: String, default: Vec3<T>? = null): Vec3<T> {
        val baseVec = if (k in this.props) this.props.getVec3(k)
            else default ?: LuckyRegistry.dropDefaults[this.type]!![k]!! as Vec3<T>

        val tripleProps = LuckyRegistry.dropTripleProps[k]
        return if (tripleProps != null && (tripleProps.first in this || tripleProps.second in this || tripleProps.third in this)) {
            Vec3(
                this.props.getWithDefault(tripleProps.first, baseVec.x),
                this.props.getWithDefault(tripleProps.second, baseVec.y),
                this.props.getWithDefault(tripleProps.third, baseVec.z),
            )
        } else baseVec
    }

    fun getPos(default: Vec3d? = null, posKey: String = "pos", offsetKey: String? = "posOffset"): Vec3d {
        val basePos = getVec3(posKey, default)
        val centerOffset = getVec3<Double>("centerOffset")
        val posOffset = getVec3<Double>(offsetKey ?: "posOffset")

        if (centerOffset != zeroVec3d) return getWorldPos(posOffset, centerOffset, basePos, this["rotation"])
        if (posOffset != zeroVec3d) return basePos + posOffset
        return basePos
    }
}

fun SingleDrop.Companion.processProps(type: String, props: DictAttr): DictAttr {
    // rename props
    val renamedEntries = HashMap(props.children)
    if (type in LuckyRegistry.dropPropRenames) {
        for ((oldName, newName) in LuckyRegistry.dropPropRenames[type]!!) {
            if (oldName in renamedEntries) {
                renamedEntries[newName] = renamedEntries[oldName]!!
                renamedEntries.remove(oldName)
            }
        }
    }
    return props.copy(children = renamedEntries)
}

fun SingleDrop.Companion.fromString(propsString: String): SingleDrop {
    val typeAttr = parseSingleKey(propsString, "type", ValueSpec(AttrType.STRING), LuckyRegistry.parserContext) as? ValueAttr?
    val type = typeAttr?.value as? String? ?: "item"
    val spec = LuckyRegistry.dropSpecs[type] ?: throw ParserError("Unknown drop type '$type'")
    val parsedProps = parseAttr(propsString, spec, LuckyRegistry.parserContext) as DictAttr

    return SingleDrop(type, processProps(type, parsedProps), propsString = propsString)
}

fun SingleDrop.evalKeys(keys: List<String>, context: DropContext): SingleDrop {
    val filteredProps = dictAttrOf(*keys.map { it to props[it] }.toTypedArray())
    val newProps = evalAttr(filteredProps, createDropEvalContext(this, context)) as DictAttr
    return copy(props=props.with(newProps.children))
}

fun SingleDrop.eval(context: DropContext): SingleDrop {
    val newProps = evalAttr(props, createDropEvalContext(this, context)) as DictAttr
    return copy(props=newProps)
}