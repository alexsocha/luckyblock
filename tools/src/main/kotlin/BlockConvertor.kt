@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class, kotlinx.serialization.InternalSerializationApi::class)

package mod.lucky.tools

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import mod.lucky.common.GameType
import com.charleskorn.kaml.*
import java.io.File

class BlockConversionException(message: String = "") : Exception(message)

class BlockIdConversion(
    private val _conversion: Map<GameType, String?>
) {
    val conversion: Map<GameType, String?> = _conversion.mapValues { (_, id) ->
        id?.let { getIdWithNamespace(it) }
    }

    fun convert(toGameType: GameType): String {
        return conversion[toGameType] ?: throw BlockConversionException()
    }

    fun parseRegex(allBlockIds: List<BlockIdConversion>): List<BlockIdConversion> {
        val firstId = conversion.values.firstOrNull()
        if (firstId != null && firstId.startsWith("blockIdRegex:")) {
            val blockIdRegex = firstId.substring("blockIdRegex:".length).toRegex()
            return allBlockIds.filter { knownBlockId ->
                knownBlockId.conversion.values.any {
                    it != null && it.matches(blockIdRegex)
                }
            }
        } else return listOf(this)
    }

    @Serializer(forClass = BlockIdConversion::class)
    companion object : KSerializer<BlockIdConversion> {
        override val descriptor: SerialDescriptor = buildSerialDescriptor("BlockIdConversion", SerialKind.CONTEXTUAL)

        override fun serialize(encoder: Encoder, value: BlockIdConversion) {
            val firstId = value.conversion.values.firstOrNull()
            if (!value.conversion.values.all { it == firstId }) {
                encoder.encodeSerializableValue(
                    MapSerializer(String.serializer(), String.serializer().nullable),
                    mapOf("java" to value.conversion[GameType.JAVA], "bedrock" to value.conversion[GameType.BEDROCK]),
                )
            } else {
                encoder.encodeSerializableValue(String.serializer().nullable, firstId)
            }
        }

        override fun deserialize(decoder: Decoder): BlockIdConversion {
            val yamlDecoder = decoder.beginStructure(descriptor) as YamlInput

            val result = if (yamlDecoder.node is YamlMap) {
                val serializer = MapSerializer(String.serializer(), String.serializer().nullable)
                val struct = (yamlDecoder.beginStructure(serializer.descriptor) as YamlInput).decodeSerializableValue(serializer)
                BlockIdConversion(mapOf(GameType.JAVA to struct["java"], GameType.BEDROCK to struct["bedrock"]))
            } else {
                val id = (yamlDecoder.beginStructure(String.serializer().descriptor) as YamlInput).decodeString()
                BlockIdConversion(mapOf(GameType.JAVA to id, GameType.BEDROCK to id))
            }

            return result
        }
    }
}

@Serializable
data class BlockIdConversions(
    @SerialName("blockIds") val blockIdConversions: List<BlockIdConversion>
) {
    fun toYaml(): String {
        return Yaml.default.encodeToString(BlockIdConversions.serializer(), this)
    }

    companion object {
        fun fromYaml(yaml: String): BlockIdConversions {
            return Yaml.default.decodeFromString(BlockIdConversions.serializer(), yaml)
        }
    }
}

data class BlockStateConversionValue(
    val blockStates: Array<BlockState>
) {
    private object BlockStateOrArraySerializer : AnySerializer() {
        @Suppress("UNCHECKED_CAST")
        @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
        override val serializers = mapOf<Any, KSerializer<Any>>(
            BlockState::class to BlockState.Companion as KSerializer<Any>,
            Array<BlockState>::class to ArraySerializer(BlockState.Companion) as KSerializer<Any>,
        )
    }

    @Serializer(forClass = BlockStateConversionValue::class) 
    companion object : KSerializer<BlockStateConversionValue> {
        override val descriptor: SerialDescriptor = buildSerialDescriptor("BlockStateConversionValue", SerialKind.CONTEXTUAL)

        override fun serialize(encoder: Encoder, value: BlockStateConversionValue) {
            when (value.blockStates.size) {
                1 -> encoder.encodeSerializableValue(BlockState.Companion, value.blockStates.first())
                else -> encoder.encodeSerializableValue(ArraySerializer(BlockState.Companion), value.blockStates)
            }
        }

        override fun deserialize(decoder: Decoder): BlockStateConversionValue {
            return when (
                val blockStateOrArray = decoder.decodeSerializableValue(BlockStateOrArraySerializer)
            ) {
                is BlockState -> BlockStateConversionValue(arrayOf(blockStateOrArray))
                is Array<*> -> BlockStateConversionValue(blockStateOrArray.map { it as BlockState }.toTypedArray())
                else -> throw Exception()
            }
        }
    }
}

@Serializable
data class PartialBlockStateConversion(
    @SerialName("java") val _java: Map<String, BlockStateConversionValue>,
    @SerialName("bedrock") val _bedrock: Map<String, BlockStateConversionValue>,
    @Transient val conversion: Map<GameType, Map<String, BlockStateConversionValue>> = mapOf(GameType.JAVA to _java, GameType.BEDROCK to _bedrock)
) {
    init {
        this.validate()
    }

    fun validate() {
        val conversionValueArrays = conversion.mapValues { (_, stateMap) ->
            stateMap.filterValues { it.blockStates.size > 1 }
        }

        for ((gameType, stateMap) in conversionValueArrays.entries) {
            if (stateMap.values.size > 1)
                throw Exception("Only one property can be an array. Found multiple for '${gameType}': ${stateMap.keys}")
        }

        val expectedArraySize = conversionValueArrays.values.first().values.firstOrNull()?.blockStates?.size
        val isEveryArraySizeSame = conversionValueArrays.values.all { stateMap ->
            stateMap.values.all { it.blockStates.size == 1 || it.blockStates.size == expectedArraySize }
        }
        if (!isEveryArraySizeSame) {
            throw Exception("Array properties have different sizes: \n" + conversionValueArrays.entries.joinToString("\n") { (gameType, stateMap) ->
                "- ${gameType}: Property '${stateMap.keys.firstOrNull()}', size ${stateMap.values.firstOrNull()?.blockStates?.size ?: 0}"
            })
        }
    }

    fun convert(fromGameType: GameType, toGameType: GameType, blockStates: BlockStates): BlockStates {
        val conversionArrayIndex = conversion[fromGameType]!!.entries.firstNotNullOfOrNull { (k, fromConversionValue) ->
            if (k !in blockStates) throw BlockConversionException("Missing block state '${k}'")
            if (fromConversionValue.blockStates.size <= 1) null
            else {
                val index = fromConversionValue.blockStates.indexOf(blockStates[k])
                if (index < 0) throw BlockConversionException("Block state '${k}=${blockStates[k]}' not found in '${fromConversionValue}'")
                index
            }
        } ?: 0

        // check if the block states match the conversion
        conversion[fromGameType]!!.forEach { (k, fromConversionValue) ->
            val fromBlockState =
                if (fromConversionValue.blockStates.size == 1) fromConversionValue.blockStates.first()
                else fromConversionValue.blockStates[conversionArrayIndex]

            if (fromBlockState != blockStates[k])
                throw BlockConversionException(
                    "Expected block state '${k}=${fromConversionValue.blockStates[conversionArrayIndex]}' at index ${conversionArrayIndex}, got '${blockStates[k]}'"
                )
        }

        return conversion[toGameType]!!.mapNotNull { (k, toConversionValue) ->
            when(toConversionValue.blockStates.size) {
                1 -> k to toConversionValue.blockStates.first()
                else -> k to toConversionValue.blockStates[conversionArrayIndex]

            }
        }.toMap()
    }
}

@Serializable
data class BlockConversion(
    @SerialName("ids") val blockIdConversions: List<BlockIdConversion>,
    @SerialName("states") val partialBlockStateConversions: List<PartialBlockStateConversion>,
) {

    fun convert(fromGameType: GameType, toGameType: GameType, blockId: String, blockStates: BlockStates): Pair<String, BlockStates> {
        val convertedBlockId = blockIdConversions.firstOrNull {
            it.conversion[fromGameType] == blockId
        }?.convert(toGameType) ?: throw BlockConversionException("Block ID '${blockId}' is not listed in this conversion")

        val conversionKeys = partialBlockStateConversions.flatMap { it.conversion[fromGameType]!!.keys  }
        if (blockStates.keys != conversionKeys.toSet()) throw BlockConversionException("Block state keys '${blockStates.keys}' do not match conversion keys '${conversionKeys}'")

        val convertedBlockStates = partialBlockStateConversions.fold(blockStates) { acc, conversion ->
            val oldKeys = conversion.conversion[fromGameType]?.keys ?: emptyList()
            val partiallyConvertedBlockStates = conversion.convert(fromGameType, toGameType, blockStates)
            acc.filterKeys { it !in oldKeys } + partiallyConvertedBlockStates
        }

        return Pair(convertedBlockId, convertedBlockStates)
    }
}

@Serializable
data class BlockConversions(
    val blockIdsFile: String? = null,
    @SerialName("blocks") val conversions: List<BlockConversion>,
) {
    fun convert(fromGameType: GameType, toGameType: GameType, blockId: String, blockStates: BlockStates): Pair<String, BlockStates> {
        return conversions.firstNotNullOfOrNull {
            try { it.convert(fromGameType, toGameType, blockId, blockStates) }
            catch (e: BlockConversionException) { null }
        } ?: throw BlockConversionException("No block conversions available for 'id=${blockId}', 'states=${blockStates}'")
    }

    fun parseRegex(allBlockIds: List<BlockIdConversion>): BlockConversions {
        return BlockConversions(
            blockIdsFile=blockIdsFile,
            conversions=conversions.map { conversion ->
                BlockConversion(
                    blockIdConversions=conversion.blockIdConversions.flatMap { it.parseRegex(allBlockIds) },
                    partialBlockStateConversions=conversion.partialBlockStateConversions,
                )
            }
        )
    }

    fun toYaml(): String {
        return Yaml.default.encodeToString(BlockConversions.serializer(), this)
    }

    companion object {
        fun readAndParseRegexFromYaml(yaml: String, getExtraYaml: (fileName: String) -> String? = { null }): BlockConversions {
            val rawBlockConversions = Yaml.default.decodeFromString<BlockConversions>(BlockConversions.serializer(), yaml)

            val allBlockIds = if (rawBlockConversions.blockIdsFile != null) {
                val blockIdConversionsYaml = getExtraYaml(rawBlockConversions.blockIdsFile)
                    ?: throw Error("YAML file '${rawBlockConversions.blockIdsFile}' is required")
                BlockIdConversions.fromYaml(blockIdConversionsYaml).blockIdConversions
            } else emptyList()

            return rawBlockConversions.parseRegex(allBlockIds)
        }

        fun readAndParseRegexFromYamlFile(file: File): BlockConversions {
            return readAndParseRegexFromYaml(file.readText(), { file.parentFile.resolve(it).readText() })
        }
    }
}

