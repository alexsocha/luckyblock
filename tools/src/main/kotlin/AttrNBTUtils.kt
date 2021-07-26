package mod.lucky.tools

import mod.lucky.common.attribute.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class DynamicByteBuffer(
    private val byteOrder: ByteOrder,
    var byteBuffer: ByteBuffer = ByteBuffer.allocate(8).order(byteOrder),
) {
    private fun ensureSpace() {
        if (byteBuffer.remaining() < 8) {
            val newByteBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2).order(byteOrder)
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

val NBT_IDS = mapOf(
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

private fun putString(buffer: DynamicByteBuffer, value: String) {
    buffer.putShort(value.length.toShort())
    value.encodeToByteArray().forEach { buffer.putByte(it) }
}

fun writeAttrToNBT(buffer: DynamicByteBuffer, attr: Attr) {
    buffer.putByte(NBT_IDS[AttrType.LIST]!!.toByte())
    when(attr) {
        is ValueAttr -> {
            if (attr.type == AttrType.BYTE_ARRAY || attr.type == AttrType.INT_ARRAY || attr.type == AttrType.LONG_ARRAY) {
                buffer.putInt((attr.value as Array<*>).size)
            }
            when(attr.type) {
                AttrType.BYTE -> buffer.putByte(attr.value as Byte)
                AttrType.BOOLEAN -> buffer.putByte(if (attr.value as Boolean) 1 else 0)
                AttrType.SHORT -> buffer.putShort(attr.value as Short)
                AttrType.INT -> buffer.putInt(attr.value as Int)
                AttrType.FLOAT -> buffer.putFloat(attr.value as Float)
                AttrType.DOUBLE -> buffer.putDouble(attr.value as Double)
                AttrType.LONG -> buffer.putLong(attr.value as Long)
                AttrType.STRING -> putString(buffer, attr.value as String)
                AttrType.BYTE_ARRAY -> (attr.value as ByteArray).forEach { buffer.putByte(it) }
                AttrType.INT_ARRAY -> (attr.value as IntArray).forEach { buffer.putInt(it) }
                AttrType.LONG_ARRAY -> (attr.value as IntArray).forEach { buffer.putInt(it) }
                AttrType.LIST -> throw Exception()
                AttrType.DICT -> throw Exception()
            }
        }
        is ListAttr -> {
            buffer.putInt(attr.children.size)
            attr.children.forEach {
                writeAttrToNBT(buffer, it)
            }
        }
        is DictAttr -> {
            attr.children.forEach { (k, v) ->
                buffer.putByte(NBT_IDS[v.type]!!.toByte())
                putString(buffer, k)
                writeAttrToNBT(buffer, v)
            }
            buffer.putByte(0)
        }
    }
}

fun attrToNBT(attr: Attr, byteOrder: ByteOrder): ByteBuffer {
    val byteBuffer = DynamicByteBuffer(byteOrder)
    writeAttrToNBT(byteBuffer, attr)
    return byteBuffer.byteBuffer
}

fun writeBufferToFile(buffer: ByteBuffer, file: File) {
    val stream = FileOutputStream(file).channel
    stream.write(buffer)
    stream.close()
}