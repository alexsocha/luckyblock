package mod.lucky.common.attribute

import mod.lucky.common.Vec3
import mod.lucky.common.chooseRandomFrom

enum class AttrType {
    STRING,
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BYTE_ARRAY,
    INT_ARRAY,
    LONG_ARRAY,
    DICT,
    LIST,
}

interface Attr { val type: AttrType }
interface AttrSpec

data class ValueSpec(
    val type: AttrType? = null, // auto determine if this is null
) : AttrSpec

data class DictSpec(val children: Map<String, AttrSpec>) : AttrSpec
data class ListSpec(val elements: List<AttrSpec>) : AttrSpec

fun dictSpecOf(vararg entries: Pair<String, AttrSpec>) = DictSpec(hashMapOf(*entries))


data class TemplateVarSpec(
    val args: List<Pair<String, AttrSpec>> = emptyList(),
    val argRange: IntRange = args.size..args.size,
)

data class TemplateVar(
    val name: String,
    val args: ListAttr,
)

typealias TemplateVarFn = (templateVar: TemplateVar, context: Any?) -> Attr

data class ValueAttr(
    override val type: AttrType,
    val value: Any,
    val needsEval: Boolean = false,
) : Attr {
    override fun toString(): String = this.value.toString()
}

data class TemplateAttr(
    val spec: AttrSpec?, // the spec that the result of the template var should conform to
    val templateVars: List<Pair<String?, TemplateVar?>>,
    val needsEval: Boolean = true,
    override val type: AttrType = AttrType.BOOLEAN // ignored
) : Attr

data class ListAttr(
    val children: List<Attr> = emptyList(),
    override val type: AttrType = AttrType.LIST,
    val needsEval: Boolean = children.any { needsEval(it) },
) : Attr {
    fun toList() = children
    operator fun contains(i: Int): Boolean = i in children.indices
    operator fun get(i: Int): Attr? = if (i in children.indices) children[i] else null

    @Suppress("UNCHECKED_CAST")
    fun <T>getOptionalValue(i: Int): T? = (this[i] as? ValueAttr)?.value as T? ?: this[i] as T?
    @Suppress("UNCHECKED_CAST")
    fun <T>getValue(i: Int): T = getOptionalValue(i)!!
    @Suppress("UNCHECKED_CAST")
    fun <T>toValueList(): List<T> {
        return this.children.map { (it as ValueAttr).value as T }
    }

    fun <T : Number>toVec3(): Vec3<T> {
        return Vec3(getValue(0), getValue(1), getValue(2))
    }

    override fun toString(): String = this.children.toString()
}

data class DictAttr(
    val children: Map<String, Attr> = emptyMap(),
    override val type: AttrType = AttrType.DICT,
    val needsEval: Boolean = children.any { (_, v) -> needsEval(v) },
) : Attr {
    operator fun contains(k: String) = k in children
    operator fun get(k: String): Attr? = children[k]

    @Suppress("UNCHECKED_CAST")
    fun <T>getOptionalValue(k: String): T? = (this[k] as? ValueAttr)?.value as T? ?: this[k] as T?
    fun <T>getValue(k: String): T = getOptionalValue(k)!!

    fun getDict(k: String) = this[k] as DictAttr
    fun getList(k: String) = this[k] as ListAttr
    fun with(v: Map<String, Attr>): DictAttr = this.copy(children=children.plus(v))
    fun <T>getWithDefault(k: String, default: T): T = this.getOptionalValue<T>(k) ?: default

    fun <T : Number>getVec3(k: String): Vec3<T> {
        return (this[k] as ListAttr).toVec3()
    }

    override fun toString(): String = this.children.toString()
}

fun DictAttr.withDefaults(defaults: Map<String, Attr>): DictAttr {
    val newEntries = HashMap(children)
    for ((k, v) in defaults) if (k !in this) newEntries[k] = v
    return this.copy(children = newEntries)
}

fun castNum(toType: AttrType, n: Number): Number {
    return when (toType) {
        AttrType.BYTE -> n.toByte()
        AttrType.SHORT -> n.toShort()
        AttrType.INT -> n.toInt()
        AttrType.LONG -> n.toLong()
        AttrType.FLOAT -> n.toFloat()
        AttrType.DOUBLE -> n.toDouble()
        else -> 0
    }
}

fun castAttr(toSpec: AttrSpec, attr: Attr): Attr? {
    if (toSpec is ValueSpec && attr is ValueAttr) {
        return when {
            toSpec.type == null -> attr
            toSpec.type == attr.type -> attr
            toSpec.type == AttrType.STRING -> ValueAttr(AttrType.STRING, attr.value.toString())
            isNumType(toSpec.type) && isNumType(attr.type) ->
                ValueAttr(toSpec.type, castNum(toSpec.type, attr.value as Number))
            else -> null
        }
    }
    if (toSpec is ListSpec && attr is ListAttr && toSpec.elements.size == attr.children.size) {
        val newElements = attr.children.mapIndexed { i, v ->
            if (i in toSpec.elements.indices) castAttr(toSpec.elements[i], v) ?: return null
            else v
        }
        return attr.copy(newElements)
    }
    if (toSpec is DictSpec && attr is DictAttr) {
        val newChildren = attr.children.mapValues { (k, v) ->
            if (k in toSpec.children) castAttr(toSpec.children[k] ?: return null, v) ?: return null
            else v
        }
        return attr.copy(newChildren)
    }
    return null
}

fun attrToJsonStr(attr: Attr): String {
    return when (attr) {
        is DictAttr -> {
            val children = attr.children.map { (k, v) -> "\"$k\": ${attrToJsonStr(v)}" }
            "{${children.joinToString(",")}}"
        }
        is ListAttr -> {
            val children = attr.children.map { attrToJsonStr(it) }
            "[${children.joinToString(",")}]"
        }
        is ValueAttr -> when(attr.type) {
            AttrType.BYTE -> "${attr.value}b"
            AttrType.SHORT -> "${attr.value}s"
            AttrType.INT -> "${attr.value}"
            AttrType.LONG -> "${attr.value}L"
            AttrType.FLOAT -> "${attr.value}f"
            AttrType.DOUBLE -> "${attr.value}"
            AttrType.INT_ARRAY -> "[${(attr.value as IntArray).joinToString(";")}]"
            AttrType.BYTE_ARRAY -> "[${(attr.value as ByteArray).joinToString(";") { "${it}b" }}]"
            AttrType.LONG_ARRAY -> "[${(attr.value as LongArray).joinToString(";") { "${it}l" }}]"
            AttrType.STRING -> "\"${attr.value}\""
            AttrType.BOOLEAN -> "${attr.value}"
            AttrType.LIST -> throw Exception()
            AttrType.DICT -> throw Exception()
        }
        else -> ""
    }
}

fun needsEval(attr: Attr): Boolean {
    return attr is TemplateAttr || (attr is ValueAttr && attr.needsEval) || (attr is ListAttr && attr.needsEval) || (attr is DictAttr && attr.needsEval)
}

fun isDecimalType(type: AttrType) = type == AttrType.FLOAT || type == AttrType.DOUBLE
fun isNumType(type: AttrType) = type in listOf(AttrType.BYTE, AttrType.SHORT, AttrType.INT, AttrType.LONG, AttrType.FLOAT, AttrType.DOUBLE)

fun intAttrOf(v: Int) = ValueAttr(AttrType.INT, v)
fun booleanAttrOf(v: Boolean) = ValueAttr(AttrType.BOOLEAN, v)
fun doubleAttrOf(v: Double) = ValueAttr(AttrType.DOUBLE, v)
fun stringAttrOf(v: String) = ValueAttr(AttrType.STRING, v)
fun listAttrOf(vararg attrs: Attr) = ListAttr(listOf(*attrs))

@Suppress("UNCHECKED_CAST")
fun dictAttrOf(vararg attrs: Pair<String, Attr?>) = DictAttr(
    mapOf(*attrs).filterValues { v -> v != null } as Map<String, Attr>
)

fun <T : Number>vec3AttrOf(numType: AttrType, vec: Vec3<T>) = ListAttr(listOf(
    ValueAttr(numType, vec.x),
    ValueAttr(numType, vec.y),
    ValueAttr(numType, vec.z),
))

fun attrToString(attr: Attr): String {
    return when(attr) {
        is ValueAttr -> attr.value.toString()
        is ListAttr -> "[${attr.children.joinToString(",") { attrToString(it) }}]"
        is DictAttr -> "(${attr.children.entries.joinToString(",") { (k, v) -> "$k=${attrToString(v)}" }})"
        else -> throw Exception()
    }
}