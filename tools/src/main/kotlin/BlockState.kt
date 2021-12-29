@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class, kotlinx.serialization.InternalSerializationApi::class)

package mod.lucky.tools

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import mod.lucky.common.GameType
import mod.lucky.common.attribute.*
import com.charleskorn.kaml.*
import java.io.File

enum class BlockStateType {
    BOOLEAN,
    NUMBER,
    STRING,
}

class BlockState(
    val blockStateType: BlockStateType,
    val value: Any,
) {
    fun toAttr(gameType: GameType): Attr {
        return when (gameType) {
            GameType.JAVA -> when (blockStateType) {
                BlockStateType.BOOLEAN -> stringAttrOf(if (value as Boolean) "true" else "false")
                BlockStateType.NUMBER -> intAttrOf(value as Int)
                BlockStateType.STRING -> stringAttrOf(value as String)
            }
            GameType.BEDROCK -> when (blockStateType) {
                BlockStateType.BOOLEAN -> booleanAttrOf(value as Boolean)
                BlockStateType.NUMBER -> intAttrOf(value as Int)
                BlockStateType.STRING -> stringAttrOf(value as String)
            }
        }
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other is BlockState && when {
            // in Java edition, booleans are encoded as strings
            blockStateType == BlockStateType.BOOLEAN || other.blockStateType == BlockStateType.BOOLEAN ->
                value.toString() == other.value.toString()
            else -> value == other.value
        }
    }

    private object ValueSerializer : AnySerializer() {
        override val serializers = mapOf(
            Boolean::class to Boolean.serializer(),
            Int::class to Int.serializer(),
            String::class to String.serializer(),
        )
    }

    @Serializer(forClass = BlockState::class) 
    companion object : KSerializer<BlockState> {
        override val descriptor: SerialDescriptor = buildSerialDescriptor("BlockState", SerialKind.CONTEXTUAL)

        override fun serialize(encoder: Encoder, value: BlockState) {
            encoder.encodeSerializableValue(ValueSerializer, value.value)
        }

        override fun deserialize(decoder: Decoder): BlockState {
            val valueDecoder = decoder.beginStructure(descriptor)
            val value = valueDecoder.decodeSerializableElement(descriptor, 0, ValueSerializer)
            return when(value) {
                is Boolean -> BlockState(BlockStateType.BOOLEAN, value)
                is Number -> BlockState(BlockStateType.NUMBER, value)
                is String -> BlockState(BlockStateType.STRING, value)
                else -> throw Exception("Invalid block state value '${value}'")
            }
        }

        fun fromAttr(attr: Attr): BlockState {
            return when(attr.type) {
                AttrType.BOOLEAN -> BlockState(BlockStateType.BOOLEAN, (attr as ValueAttr).value)
                AttrType.INT -> BlockState(BlockStateType.NUMBER, ((attr as ValueAttr).value as Byte).toInt())
                AttrType.STRING -> BlockState(BlockStateType.STRING, (attr as ValueAttr).value)
                else -> throw Exception()
            }
        }
    }
}

typealias BlockStates = Map<String, BlockState>
