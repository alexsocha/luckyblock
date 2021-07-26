import mod.lucky.common.attribute.AttrType
import mod.lucky.common.attribute.ValueAttr
import mod.lucky.common.attribute.dictAttrOf
import mod.lucky.common.attribute.listAttrOf
import mod.lucky.common.drop.dropsFromStrList
import kotlin.test.Test
import kotlin.test.assertEquals
import mod.lucky.tools.*
import java.nio.ByteOrder

internal class BedrockDropsTests {
    @Test
    fun testGenerateBedrockDropsSampling() {
        val drops = listOf(
            "type=entity,nbttag=(x=#rand(0,3)),sample=1@chance=1",
            "type=entity,nbttag=#randList((Short=23s),(Short=25s)),amount=#rand(1,2),sample=3@luck=3",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, createEmptyGeneratedDrops("lucky_block"))

        assertEquals(listOf(
            """type="structure",id="mystructure:lucky_generated_drop_1"@chance=1""",
            """type="structure",amount=#rand(1,2),id=#randList("mystructure:lucky_generated_drop_2.1","mystructure:lucky_generated_drop_2.2")@luck=3"""
        ), newDrops)
    }

    @Test
    fun testAttrToNBT() {
        val attr = dictAttrOf(
            "a" to listAttrOf(ValueAttr(AttrType.DOUBLE, 4.0), ValueAttr(AttrType.INT, 4)),
            "b" to ValueAttr(AttrType.STRING, "a"),
        )
        assertEquals(15, attrToNBT(attr, ByteOrder.LITTLE_ENDIAN).position())
    }
}