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

class BlockIdConversion {
    val conversion: Map<GameType, String?>
    constructor(conversion: Map<GameType, String?>) {
        this.conversion = conversion.mapValues { (_, id) -> id?.let { getIdWithNamespace(it) } }
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

data class BlockStateArray(
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

    @Serializer(forClass = BlockStateArray::class) 
    companion object : KSerializer<BlockStateArray> {
        override val descriptor: SerialDescriptor = buildSerialDescriptor("BlockStateArray", SerialKind.CONTEXTUAL)

        override fun serialize(encoder: Encoder, value: BlockStateArray) {
            if (value.blockStates.size == 1) {
                encoder.encodeSerializableValue(BlockState.Companion, value.blockStates.first())
            } else {
                encoder.encodeSerializableValue(ArraySerializer(BlockState.Companion), value.blockStates)
            }
        }

        override fun deserialize(decoder: Decoder): BlockStateArray {
            val blockStateOrArray = decoder.decodeSerializableValue(BlockStateOrArraySerializer)
            return when (blockStateOrArray) {
                is BlockState -> BlockStateArray(arrayOf(blockStateOrArray))
                is Array<*> -> BlockStateArray(blockStateOrArray.map { it as BlockState }.toTypedArray())
                else -> throw Exception()
            }
        }
    }
}

@Serializable
data class PartialBlockStateConversion(
    @SerialName("java") val _java: Map<String, BlockStateArray>,
    @SerialName("bedrock") val _bedrock: Map<String, BlockStateArray>,
    @Transient val conversion: Map<GameType, Map<String, BlockStateArray>> = mapOf(GameType.JAVA to _java, GameType.BEDROCK to _bedrock)
) {
    init {
        this.validate()
    }

    fun validate() {
        val conversionWithMultiArraysOnly = conversion.mapValues { (_, stateMap) ->
            stateMap.filterValues { it.blockStates.size > 1 }
        }

        for ((gameType, stateMap) in conversionWithMultiArraysOnly.entries) {
            if (stateMap.values.size > 1)
                throw Exception("Only one property can be an array. Found multiple for '${gameType}': ${stateMap.keys}")
        }

        val expectedArraySize = conversionWithMultiArraysOnly.values.first().values.firstOrNull()?.blockStates?.size
        val isEveryArraySizeSame = conversionWithMultiArraysOnly.values.all { stateMap ->
            stateMap.values.all { it.blockStates.size == 1 || it.blockStates.size == expectedArraySize }
        }
        if (!isEveryArraySizeSame) {
            throw Exception("Array properties have different sizes: \n" + conversionWithMultiArraysOnly.entries.joinToString("\n") { (gameType, stateMap) ->
                "- ${gameType}: Property '${stateMap.keys.firstOrNull()}', size ${stateMap.values.firstOrNull()?.blockStates?.size ?: 0}"
            })
        }
    }

    fun convert(fromGameType: GameType, toGameType: GameType, blockStates: BlockStates): BlockStates {
        val blockStateArrayIndex = conversion[fromGameType]!!.entries.firstNotNullOfOrNull { (k, blockStateArray) ->
            if (blockStateArray.blockStates.size > 1) {
                if (k !in blockStates) throw BlockConversionException("Missing block state '${k}'")
                val index = blockStateArray.blockStates.indexOf(blockStates[k])
                if (index < 0) throw BlockConversionException("Block state '${k}=${blockStates[k]}' not found in block state array '${blockStateArray}'")
                index
            } else null
        } ?: 0

        // check if the block states match the conversion
        conversion[fromGameType]!!.forEach { (k, blockStateArray) ->
            if (k !in blockStates) throw BlockConversionException("Missing block state '${k}'")
            if (blockStateArray.blockStates[blockStateArrayIndex] != blockStates[k])
                throw BlockConversionException(
                    "Expected block state '${k}=${blockStateArray.blockStates.first()}' at index ${blockStateArrayIndex}, got '${blockStates[k]}'"
                )
        }

        return conversion[toGameType]!!.mapValues { (_, blockStateArray) ->
            if (blockStateArray.blockStates.size == 1) blockStateArray.blockStates.first()
            else blockStateArray.blockStates[blockStateArrayIndex]
        }
    }
}

@Serializable
data class BlockConversion(
    @SerialName("ids") val blockIdConversions: List<BlockIdConversion>,
    @SerialName("states") val partialBlockStateConversions: List<PartialBlockStateConversion>,
) {

    fun convert(fromGameType: GameType, toGameType: GameType, blockId: String, blockStates: BlockStates): Pair<String, BlockStates> {
        val convertedBlockId = blockIdConversions.firstOrNull { it.conversion[fromGameType] == blockId }?.convert(toGameType)
        if (convertedBlockId == null) throw BlockConversionException("Block ID '${blockId}' is not listed in this conversion")

        val conversionKeys = partialBlockStateConversions.flatMap {
            it.conversion[fromGameType]!!.keys
        }
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
    @SerialName("blocks") val blockConversions: List<BlockConversion>,
) {
    fun convert(fromGameType: GameType, toGameType: GameType, blockId: String, blockStates: BlockStates): Pair<String, BlockStates> {
        return blockConversions.firstNotNullOfOrNull {
            try { it.convert(fromGameType, toGameType, blockId, blockStates) }
            catch (e: BlockConversionException) { null }
        } ?: throw BlockConversionException("No block conversions available for ID '${blockId}', states '${blockStates}'")
    }

    fun parseRegex(allBlockIds: List<BlockIdConversion>): BlockConversions {
        return BlockConversions(
            blockIdsFile=blockIdsFile,
            blockConversions=blockConversions.map { blockConversion ->
                BlockConversion(
                    blockIdConversions=blockConversion.blockIdConversions.flatMap { it.parseRegex(allBlockIds) },
                    partialBlockStateConversions=blockConversion.partialBlockStateConversions,
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

