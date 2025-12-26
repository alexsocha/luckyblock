import mod.lucky.common.LuckyRegistry
import mod.lucky.common.drop.*
import mod.lucky.common.LOGGER
import mod.lucky.common.GAME_API
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.JAVA_GAME_API
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertTrue

class JavaTests {
    @BeforeTest
    fun beforeTest() {
        LOGGER = MockGameAPI
        GAME_API = MockGameAPI
        JAVA_GAME_API = MockJavaGameAPI
        JavaLuckyRegistry.init()
    }

    @Test
    fun testLuckyRegistry() {
        assertEquals(true, JavaLuckyRegistry.globalSettings.checkForUpdates)
        assertEquals(false, LuckyRegistry.blockSettings["lucky:lucky_block"]?.doDropsOnRightClick)
        assertEquals(false, LuckyRegistry.blockSettings["lucky:custom_lucky_block"]?.doDropsOnRightClick)

        listOf(
            "lucky:lucky_block:giant_lucky_block_inner_good",
            "lucky:lucky_block:giant_blocks/lucky_block_inner_good.luckystruct"
        ).forEach {
            assertContains(LuckyRegistry.dropStructures.keys, it)
        }

        listOf(
            "lucky:lucky_block:giant_lucky_block_good",
            "lucky:lucky_block:giant_lucky_block_bad",
            "lucky:lucky_block:giant_blocks/lucky_block.nbt",
        ).forEach {
            assertContains(JavaLuckyRegistry.nbtStructures.keys, it)
        }

        assertEquals(mapOf(
            "lucky:lucky_block" to "lucky:lucky_block",
            "lucky:lucky_sword" to "lucky:lucky_block",
            "lucky:lucky_bow" to "lucky:lucky_block",
            "lucky:lucky_potion" to "lucky:lucky_block",
            "lucky:custom_lucky_block" to "lucky:custom_lucky_block",
            "lucky:custom_lucky_sword" to "lucky:custom_lucky_block",
            "lucky:custom_lucky_bow" to "lucky:custom_lucky_block",
            "lucky:custom_lucky_potion" to "lucky:custom_lucky_block",
        ), LuckyRegistry.sourceToAddonId)
    }

    @Test
    fun testAllDrops() {
        val sourceIds = listOf(
            "lucky:lucky_block",
            "lucky:lucky_sword",
            "lucky:lucky_bow",
            "lucky:lucky_potion",
            "lucky:custom_lucky_block",
            "lucky:custom_lucky_sword",
            "lucky:custom_lucky_bow",
            "lucky:custom_lucky_potion",
        )

        val dropLists = sourceIds.map { LuckyRegistry.drops[it]!! } +
            JavaLuckyRegistry.worldGenDrops["lucky:lucky_block"]!!.values +
            JavaLuckyRegistry.worldGenDrops["lucky:custom_lucky_block"]!!.values

        for (dropList in dropLists) {
            assertTrue(dropList.isNotEmpty())
            for (drop in dropList) {
                runDrop(drop, testDropContext, showOutput = false)
            }
        }
    }
}
