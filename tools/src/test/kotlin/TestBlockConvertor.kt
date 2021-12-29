import mod.lucky.tools.*
import mod.lucky.common.drop.dropsFromStrList
import mod.lucky.common.GameType
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertContains
import java.nio.ByteOrder

fun testBlockConvertor(
    blockConversions: BlockConversions,
    blockIds: Map<GameType, String>,
    blockStates: Map<GameType, BlockStates>,
    fromGameTypes: List<GameType> = GameType.values().toList(),
    toGameTypes: List<GameType> = GameType.values().toList(),
) {
    for (fromGameType in fromGameTypes) {
        for (toGameType in toGameTypes) {
            if (fromGameType != toGameType) {
                val (convertedBlockId, convertedBlockStates) = blockConversions.convert(fromGameType, toGameType, blockIds[fromGameType]!!, blockStates[fromGameType]!!)
                assertEquals(blockIds[toGameType], convertedBlockId)
                assertEquals(blockStates[toGameType], convertedBlockStates)
            }
        }
    }
}

internal class TestBlockConversion {
    @Test
    fun testBlockIdRegex() {
        val blockIdsYaml = """
            blockIds:
            - minecraft:andesite_stairs
            - java: null
              bedrock: minecraft:deepslate_tile_stairs
            - java: minecraft:prismarine_brick_stairs
              bedrock: minecraft:prismarine_bricks_stairs
        """.trimIndent()

        val blockConversionsYaml = """
            blockIdsFile: block-ids.yaml
            blocks:
              - ids:
                - blockIdRegex:.*_stairs
                states: []
        """.trimIndent()

        val blockConversions = BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml) { yamlFileName ->
            if (yamlFileName == "block-ids.yaml") blockIdsYaml 
            else throw Exception()
        }

        val expectedResult = """
            blockIdsFile: "block-ids.yaml"
            blocks:
            - ids:
              - "minecraft:andesite_stairs"
              - "java": null
                "bedrock": "minecraft:deepslate_tile_stairs"
              - "java": "minecraft:prismarine_brick_stairs"
                "bedrock": "minecraft:prismarine_bricks_stairs"
              states: []
        """.trimIndent()

        assertEquals(expectedResult, blockConversions.toYaml())
    }

    @Test
    fun testValidation() {
        // invalid because facing.size != weirdo_direction.size
        val arrayLengthMismatchYaml = """
            blocks:
              - ids:
                - oak_stairs
                states:
                - java:
                    facing: [east, north, south, west]
                  bedrock:
                    weirdo_direction: [0, 1, 2]
        """.trimIndent()
        assertContains(
            assertFailsWith<Exception> {
                BlockConversions.readAndParseRegexFromYaml(arrayLengthMismatchYaml)
            }.message!!,
            "Array properties have different sizes",
        )

        // invalid because states.bedrock has >1 array property
        val multiArrayYaml = """
            blocks:
              - ids:
                - oak_stairs
                states:
                - java:
                    facing: [east, north, south, west]
                  bedrock:
                    weirdo_direction: [0, 1, 2, 4]
                    weirdo_direction2: [0, 1, 2, 4]
        """.trimIndent()
        assertContains(
            assertFailsWith<Exception> {
                BlockConversions.readAndParseRegexFromYaml(multiArrayYaml)
            }.message!!,
            "Only one property can be an array",
        )
    }

    @Test
    fun testStatesDoNotMatch() {
        val blockConversionsYaml = """
            blocks:
              - ids:
                - oak_stairs
                states:
                - java:
                    facing: [east, north, south, west]
                  bedrock:
                    weirdo_direction: [0, 1, 2, 3]
        """.trimIndent()

        val blockConversions = BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml)
        val message = assertFailsWith<Exception> {
            blockConversions.conversions.first().convert(
                fromGameType=GameType.JAVA,
                toGameType=GameType.BEDROCK,
                blockId="minecraft:oak_stairs",
                blockStates=mapOf(
                    "facing" to BlockState(BlockStateType.STRING, "south"),
                    "half" to BlockState(BlockStateType.STRING, "top"),
                ),
            )
        }.message!!
        assertEquals("Block state keys '[facing, half]' do not match conversion keys '[facing]'", message)
    }

    @Test
    fun testSimpleIdConversion() {
        val blockConversionsYaml = """
            blocks:
              - ids:
                - oak_stairs
                states:
                - java:
                    facing: [east, north, south, west]
                  bedrock:
                    weirdo_direction: [0, 1, 2, 3]
              - ids:
                - java: minecraft:beetroots
                  bedrock: minecraft:beetroot
                states: []
        """.trimIndent()

        testBlockConvertor(
            blockConversions=BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml),
            blockStates=mapOf(
                GameType.JAVA to emptyMap(),
                GameType.BEDROCK to emptyMap(),
            ),
            blockIds=mapOf(
                GameType.JAVA to "minecraft:beetroots",
                GameType.BEDROCK to "minecraft:beetroot",
            ),
        )
    }

    @Test
    fun testOneSidedState() {
        val blockConversionsYaml = """
            blocks:
              - ids:
                - oak_stairs
                states:
                - java: 
                    waterlogged: [false, true]
                  bedrock: {}
        """.trimIndent()

        testBlockConvertor(
            blockConversions=BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml),
            blockIds=mapOf(
                GameType.JAVA to "minecraft:oak_stairs",
                GameType.BEDROCK to "minecraft:oak_stairs",
            ),
            blockStates=mapOf(
                GameType.JAVA to mapOf(
                    "waterlogged" to BlockState(BlockStateType.BOOLEAN, true),
                ),
                GameType.BEDROCK to emptyMap(),
            ),
            fromGameTypes=listOf(GameType.JAVA),
            toGameTypes=listOf(GameType.BEDROCK),
        )

        testBlockConvertor(
            blockConversions=BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml),
            blockIds=mapOf(
                GameType.JAVA to "minecraft:oak_stairs",
                GameType.BEDROCK to "minecraft:oak_stairs",
            ),
            blockStates=mapOf(
                GameType.JAVA to mapOf(
                    "waterlogged" to BlockState(BlockStateType.BOOLEAN, false),
                ),
                GameType.BEDROCK to emptyMap(),
            ),
            fromGameTypes=listOf(GameType.BEDROCK),
            toGameTypes=listOf(GameType.JAVA),
        )
    }

    @Test
    fun testStairs() {
        val blockConversionsYaml = """
            blocks:
              - ids:
                - oak_stairs
                states:
                - java:
                    facing: [east, north, south, west]
                  bedrock:
                    weirdo_direction: [0, 1, 2, 3]
                - java:
                    half: [bottom, top]
                  bedrock:
                    upside_down_bit: [false, true]
                - java:
                    shape: [straight, inner_left, inner_right, outer_left, outer_right]
                  bedrock: {}
                - java: 
                    waterlogged: [false, true]
                  bedrock: {}
        """.trimIndent()

        testBlockConvertor(
            blockConversions=BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml),
            blockIds=mapOf(
                GameType.JAVA to "minecraft:oak_stairs",
                GameType.BEDROCK to "minecraft:oak_stairs",
            ),
            blockStates=mapOf(
                GameType.JAVA to mapOf(
                    "facing" to BlockState(BlockStateType.STRING, "south"),
                    "half" to BlockState(BlockStateType.STRING, "top"),
                    "shape" to BlockState(BlockStateType.STRING, "straight"),
                    "waterlogged" to BlockState(BlockStateType.BOOLEAN, false),
                ),
                GameType.BEDROCK to mapOf(
                    "weirdo_direction" to BlockState(BlockStateType.NUMBER, 2),
                    "upside_down_bit" to BlockState(BlockStateType.BOOLEAN, true),
                ),
            )
        )
    }

    @Test
    fun testQuartzPillar() {
        val blockConversionsYaml = """
            blocks:
              - ids:
                - java: quartz_pillar
                  bedrock: quartz_block
                states:
                - java:
                    axis: [x, y, z]
                  bedrock:
                    chisel_type: lines
                    pillar_axis: [x, y, z]
        """.trimIndent()

        val blockConversions = BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml)

        testBlockConvertor(
            blockConversions=blockConversions,
            blockIds=mapOf(
                GameType.JAVA to "minecraft:quartz_pillar",
                GameType.BEDROCK to "minecraft:quartz_block",
            ),
            blockStates=mapOf(
                GameType.JAVA to mapOf(
                    "axis" to BlockState(BlockStateType.STRING, "z"),
                ),
                GameType.BEDROCK to mapOf(
                    "chisel_type" to BlockState(BlockStateType.STRING, "lines"),
                    "pillar_axis" to BlockState(BlockStateType.STRING, "z"),
                ),
            )
        )
    }
}
