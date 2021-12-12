@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class, kotlinx.serialization.InternalSerializationApi::class)

package mod.lucky.tools

import mod.lucky.common.attribute.*
import br.com.gamemods.nbtmanipulator.*
import java.io.File
import java.io.DataOutputStream
import kotlin.comparisons.compareValuesBy
import kotlin.reflect.typeOf
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import com.charleskorn.kaml.*

fun <T>tripleComparator(fn: (v: T) -> Triple<Int, Int, Int>) = Comparator<T> { a, b ->
    val t1 = fn(a)
    val t2 = fn(b)
    compareValuesBy(t1, t2, { it.first }, { it.second }, { it.third })
}

fun nbtToAttr(nbt: NbtTag): Attr {
    return when (nbt) {
        is NbtByte -> ValueAttr(AttrType.BYTE, nbt.signed)
        is NbtShort -> ValueAttr(AttrType.SHORT, nbt.value)
        is NbtInt -> ValueAttr(AttrType.INT, nbt.value)
        is NbtFloat -> ValueAttr(AttrType.FLOAT, nbt.value)
        is NbtLong -> ValueAttr(AttrType.LONG, nbt.value)
        is NbtDouble -> ValueAttr(AttrType.DOUBLE, nbt.value)
        is NbtString -> ValueAttr(AttrType.STRING, nbt.value)
        is NbtByteArray -> ValueAttr(AttrType.BYTE_ARRAY, nbt.value)
        is NbtIntArray -> ValueAttr(AttrType.INT_ARRAY, nbt.value)
        is NbtLongArray -> ValueAttr(AttrType.LONG_ARRAY, nbt.value)
        is NbtCompound -> DictAttr(nbt.mapValues { (_, v) -> nbtToAttr(v) })
        is NbtList<*> -> ListAttr(nbt.map { nbtToAttr(it) })
        else -> throw Exception()
    }
}

fun readNbtFile(file: File, compressed: Boolean, isLittleEndian: Boolean): Attr {
    return nbtToAttr(NbtIO.readNbtFile(file, compressed=compressed, littleEndian=isLittleEndian).tag)
}

fun attrToNbt(attr: Attr): NbtTag {
    return when (attr) {
        is ValueAttr -> when (attr.type) {
            AttrType.BYTE -> NbtByte(signed=attr.value as Byte)
            AttrType.BOOLEAN -> NbtByte((if (attr.value as Boolean == false) 0 else 1).toByte())
            AttrType.SHORT -> NbtShort(attr.value as Short)
            AttrType.INT -> NbtInt(attr.value as Int)
            AttrType.FLOAT -> NbtFloat(attr.value as Float)
            AttrType.LONG -> NbtLong(attr.value as Long)
            AttrType.DOUBLE -> NbtDouble(attr.value as Double)
            AttrType.STRING -> NbtString(attr.value as String)
            AttrType.BYTE_ARRAY -> NbtByteArray(attr.value as ByteArray)
            AttrType.INT_ARRAY -> NbtIntArray(attr.value as IntArray)
            AttrType.LONG_ARRAY -> NbtLongArray(attr.value as LongArray)
            AttrType.LIST -> throw Exception()
            AttrType.DICT -> throw Exception()
        }
        is ListAttr -> NbtList(attr.children.map { attrToNbt(it) })
        is DictAttr -> NbtCompound(attr.children.mapValues { (_, v) -> attrToNbt(v) })
        else -> throw Exception()
    }
}

fun writeNbtFile(file: File, attr: Attr, compressed: Boolean, isLittleEndian: Boolean) {
    file.parentFile.mkdirs()
    NbtIO.writeNbtFile(DataOutputStream(file.outputStream()), NbtFile("", attrToNbt(attr)), compressed=compressed, littleEndian=isLittleEndian)
}

abstract class AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("AnySerializer", SerialKind.CONTEXTUAL)
    abstract val serializers: Map<*, KSerializer<*>>

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: Any) {
        val serializer = serializers.entries.first { (k) -> value::class == k }.value
        return encoder.encodeSerializableValue(serializer as KSerializer<Any>, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): Any {
        return serializers.values.firstNotNullOf {
            try {
                val valueDecoder = decoder.beginStructure(it.descriptor)
                valueDecoder.decodeSerializableElement(it.descriptor, 0, it)
            } catch (e: Exception) { null }
        }
    }
}

fun getIdWithNamespace(id: String): String {
    return if (":" in id) id else "minecraft:$id"
}
