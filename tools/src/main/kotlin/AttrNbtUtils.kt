package mod.lucky.tools

import mod.lucky.common.attribute.*
import br.com.gamemods.nbtmanipulator.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.zip.GZIPInputStream

class DynamicByteBuffer(
    private val byteOrder: ByteOrder,
    var byteBuffer: ByteBuffer = ByteBuffer.allocate(8).order(byteOrder),
) {
    private fun ensureSpace() {
        if (byteBuffer.remaining() < 8) {
            val newByteBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2).order(byteOrder)
            byteBuffer.flip()
            newByteBuffer.put(byteBuffer)
            byteBuffer = newByteBuffer
        }
    }

    fun putByte(v: Byte) { ensureSpace(); byteBuffer.put(v) }
    fun putShort(v: Short) { ensureSpace(); byteBuffer.putShort(v) }
    fun putInt(v: Int) { ensureSpace(); byteBuffer.putInt(v) }
    fun putFloat(v: Float) { ensureSpace(); byteBuffer.putFloat(v) }
    fun putDouble(v: Double) { ensureSpace(); byteBuffer.putDouble(v) }
    fun putLong(v: Long) { ensureSpace(); byteBuffer.putLong(v) }
}

private fun putString(byteBuffer: DynamicByteBuffer, value: String) {
    val byteArray = value.encodeToByteArray()
    byteBuffer.putShort(byteArray.size.toShort())
    byteArray.forEach { byteBuffer.putByte(it) }
}

private fun readString(byteBuffer: ByteBuffer): String {
    val size = byteBuffer.get().toInt()
    val byteArray = ByteArray(size)
    byteBuffer.get(byteArray, 0, size)
    return byteArray.decodeToString()
}

val NBT_TYPE_TO_ID = mapOf(
    AttrType.BYTE to 1,
    AttrType.BOOLEAN to 1,
    AttrType.SHORT to 2,
    AttrType.INT to 3,
    AttrType.LONG to 4,
    AttrType.FLOAT to 5,
    AttrType.DOUBLE to 6,
    AttrType.BYTE_ARRAY to 7,
    AttrType.STRING to 8,
    AttrType.LIST to 9,
    AttrType.DICT to 10,
    AttrType.INT_ARRAY to 11,
    AttrType.LONG_ARRAY to 12,
)
val NBT_ID_TO_TYPE = NBT_TYPE_TO_ID.entries.associate { (k, v) -> v to k }

fun readNbt(byteBuffer: ByteBuffer, attrType: AttrType): Attr {
    /*
    return when (attrType) {
        AttrType.BYTE -> ValueAttr(AttrType.BYTE, byteBuffer.get())
        AttrType.BOOLEAN -> ValueAttr(AttrType.BOOLEAN, false if byteBuffer.get().toInt() == 0 else true)
        AttrType.SHORT -> ValueAttr(AttrType.SHORT, byteBuffer.getShort())
        AttrType.INT -> ValueAttr(AttrType.INT, byteBuffer.getInt())
        AttrType.FLOAT -> ValueAttr(AttrType.FLOAT, byteBuffer.getFloat())
        AttrType.DOUBLE -> ValueAttr(AttrType.DOUBLE, byteBuffer.getDouble())
        AttrType.LONG -> ValueAttr(AttrType.LONG, byteBuffer.getLong())
        AttrType.STRING -> putString(buffer, attr.value as String)
        AttrType.BYTE_ARRAY -> {
            val array = attr.value as ByteArray
            buffer.putInt(array.size)
            array.forEach { buffer.putByte(it) }
        }
        AttrType.INT_ARRAY -> {
            val array = attr.value as IntArray
            buffer.putInt(array.size)
            array.forEach { buffer.putInt(it) }
        }
        AttrType.LONG_ARRAY -> {
            val array = attr.value as LongArray
            buffer.putInt(array.size)
            array.forEach { buffer.putLong(it) }
        }
        AttrType.DICT -> {
            val entries = generateSequence {
                val childTypeId = byteBuffer.get().toInt()
                println(childTypeId)
                if (childTypeId != 0) {
                    val childType = NBT_ID_TO_TYPE[childTypeId]!!
                    val childKey = readString(byteBuffer)
                    println(childKey)
                    val childAttr = readNbt(byteBuffer, childType)
                    Pair(childKey, childAttr)
                } else null
            }
            return DictAttr(entries.toMap())
        }
        AttrType.LIST -> {
            val childTypeId = byteBuffer.get().toInt()
            val count = byteBuffer.get().toInt()

            if (count == 0) ListAttr()
            else {
                val childType = NBT_ID_TO_TYPE[childTypeId]!!
                val elements = (0 until count).map { readNbt(byteBuffer, childType) }
                ListAttr(elements)
            }
        }
        else -> DictAttr()
    }
    */
    return DictAttr()
}

fun readNbtFromRoot(byteBuffer: ByteBuffer): Attr {
    //val attrType = NBT_ID_TO_TYPE[byteBuffer.get().toInt()]!!
    return readNbt(byteBuffer, AttrType.DICT)
}

fun readCompressedNbt(stream: GZIPInputStream, byteOrder: ByteOrder): Attr {
    val byteStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024);

    do {
        val numBytes = stream.read(buffer, 0, buffer.size)
        if (numBytes > 0) byteStream.write(buffer, 0, numBytes)
    } while (numBytes > 0)

    stream.close()
    byteStream.close()

    val byteBuffer = ByteBuffer.wrap(byteStream.toByteArray()).order(byteOrder)
    return readNbtFromRoot(byteBuffer)
}

fun writeAttrToNbt(buffer: DynamicByteBuffer, attr: Attr) {
    when(attr) {
        is ValueAttr -> {
            when(attr.type) {
                AttrType.BYTE -> buffer.putByte(attr.value as Byte)
                AttrType.BOOLEAN -> buffer.putByte(if (attr.value as Boolean) 1 else 0)
                AttrType.SHORT -> buffer.putShort(attr.value as Short)
                AttrType.INT -> buffer.putInt(attr.value as Int)
                AttrType.FLOAT -> buffer.putFloat(attr.value as Float)
                AttrType.DOUBLE -> buffer.putDouble(attr.value as Double)
                AttrType.LONG -> buffer.putLong(attr.value as Long)
                AttrType.STRING -> putString(buffer, attr.value as String)
                AttrType.BYTE_ARRAY -> {
                    val array = attr.value as ByteArray
                    buffer.putInt(array.size)
                    array.forEach { buffer.putByte(it) }
                }
                AttrType.INT_ARRAY -> {
                    val array = attr.value as IntArray
                    buffer.putInt(array.size)
                    array.forEach { buffer.putInt(it) }
                }
                AttrType.LONG_ARRAY -> {
                    val array = attr.value as LongArray
                    buffer.putInt(array.size)
                    array.forEach { buffer.putLong(it) }
                }
                AttrType.LIST -> throw Exception()
                AttrType.DICT -> throw Exception()
            }
        }
        is ListAttr -> {
            buffer.putByte(
                if (attr.children.size == 0) 0
                else NBT_TYPE_TO_ID[attr.children.first().type]!!.toByte()
            )
            buffer.putInt(attr.children.size)
            attr.children.forEach {
                writeAttrToNbt(buffer, it)
            }
        }
        is DictAttr -> {
            attr.children.forEach { (k, v) ->
                buffer.putByte(NBT_TYPE_TO_ID[v.type]!!.toByte())
                putString(buffer, k)
                writeAttrToNbt(buffer, v)
            }
            buffer.putByte(0)
        }
    }
}

fun attrToNbt(attr: Attr, byteOrder: ByteOrder): ByteBuffer {
    val byteBuffer = DynamicByteBuffer(byteOrder)
    writeAttrToNbt(byteBuffer, attr)
    return byteBuffer.byteBuffer
}

fun writeBufferToFile(buffer: ByteBuffer, file: File) {
    file.parentFile.mkdirs()
    val channel = FileOutputStream(file).channel
    buffer.flip()
    channel.write(buffer)
    channel.close()
}
