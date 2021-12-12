import mod.lucky.tools.*
import mod.lucky.common.drop.dropsFromStrList
import mod.lucky.common.GameType
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertContains
import java.nio.ByteOrder

fun testBlockConversion(
    blockConversions: BlockConversions,
    fromGameType: GameType, 
    fromBlockId: String,
    fromBlockStates: BlockStates,
    toGameType: GameType,
    toBlockId: String,
    toBlockStates: BlockStates,
) {
    val (convertedBlockId, convertedBlockStates) = blockConversions.convert(fromGameType, toGameType, fromBlockId, fromBlockStates)
    assertEquals(convertedBlockId, toBlockId)
    assertEquals(toBlockStates, convertedBlockStates)
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
            blockIdsFile: block_ids.yaml
            blocks:
              - ids:
                - blockIdRegex:.*_stairs
                states: []
        """.trimIndent()

        val blockConversions = BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml) { yamlFileName ->
            if (yamlFileName == "block_ids.yaml") blockIdsYaml 
            else throw Exception()
        }

        val expectedResult = """
            blockIdsFile: "block_ids.yaml"
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

        testBlockConversion(
            blockConversions=BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml),
            fromGameType=GameType.JAVA,
            fromBlockId="minecraft:beetroots",
            fromBlockStates=emptyMap(),
            toGameType=GameType.BEDROCK,
            toBlockId="minecraft:beetroot",
            toBlockStates=emptyMap(),
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
        """.trimIndent()
        // TODO: include these states:
        //      - java: 
        //          waterlogged: [False, True]
        //      - bedrock:
        //          waterlogged: null

        testBlockConversion(
            blockConversions=BlockConversions.readAndParseRegexFromYaml(blockConversionsYaml),
            fromGameType=GameType.JAVA,
            fromBlockId="minecraft:oak_stairs",
            fromBlockStates=mapOf(
                "facing" to BlockState(BlockStateType.STRING, "south"),
                "half" to BlockState(BlockStateType.STRING, "top"),
                // "waterlogged" to BlockState(BlockStateType.BOOLEAN, "false"), // TODO
            ),
            toGameType=GameType.BEDROCK,
            toBlockId="minecraft:oak_stairs",
            toBlockStates=mapOf(
                "weirdo_direction" to BlockState(BlockStateType.NUMBER, 2),
                "upside_down_bit" to BlockState(BlockStateType.BOOLEAN, true),
            ),
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

        testBlockConversion(
            blockConversions=blockConversions,
            fromGameType=GameType.JAVA,
            fromBlockId="minecraft:quartz_pillar",
            fromBlockStates=mapOf(
                "axis" to BlockState(BlockStateType.STRING, "z"),
            ),
            toGameType=GameType.BEDROCK,
            toBlockId="minecraft:quartz_block",
            toBlockStates=mapOf(
                "chisel_type" to BlockState(BlockStateType.STRING, "lines"),
                "pillar_axis" to BlockState(BlockStateType.STRING, "z"),
            ),
        )
    }
}
