import mod.lucky.common.LuckyRegistry
import mod.lucky.common.drop.*
import mod.lucky.common.gameAPI
import mod.lucky.java.JavaLuckyRegistry
import mod.lucky.java.javaGameAPI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JavaTests {
    @BeforeTest
    fun beforeTest() {
        gameAPI = MockGameAPI
        javaGameAPI = MockJavaGameAPI
    }

    @Test
    fun integrationTest() {
        JavaLuckyRegistry.init()

        assertEquals(true, JavaLuckyRegistry.globalSettings.checkForUpdates)
        assertEquals(false, LuckyRegistry.blockSettings["lucky:lucky_block"]?.doDropsOnCreativeMode)
        assertEquals(false, LuckyRegistry.blockSettings["lucky:custom_lucky_block"]?.doDropsOnCreativeMode)

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
