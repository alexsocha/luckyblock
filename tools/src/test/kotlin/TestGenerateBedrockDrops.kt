import mod.lucky.common.attribute.AttrType
import mod.lucky.common.attribute.ValueAttr
import mod.lucky.common.attribute.dictAttrOf
import mod.lucky.common.attribute.listAttrOf
import mod.lucky.common.drop.dropsFromStrList
import kotlin.test.Test
import kotlin.test.assertEquals
import mod.lucky.tools.*
import java.nio.ByteOrder

fun singleLine(s: String): String = s.split("\n").joinToString("") { it.trim() }

internal class BedrockDropsTests {
    @Test
    fun testGenerateBedrockDropsSampling() {
        val drops = listOf(
            "type=entity,nbttag=(x=#rand(0,3)),sample=1@chance=1",
            "type=entity,nbttag=#randList((Short=23s),(Short=25s)),amount=#rand(1,2),sample=3@luck=3",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, createEmptyGeneratedDrops("lucky_block"))

        assertEquals(listOf(
            """type="structure",id="mystructure:lucky_block_drop_1"@chance=1.0""",
            singleLine("""type="structure",amount=#rand(1,2),id=#randList(
                "mystructure:lucky_block_drop_2.1",
                "mystructure:lucky_block_drop_2.2",
                "mystructure:lucky_block_drop_2.3"
            )@luck=3""")
        ), newDrops)

        assertEquals(generatedDrops.dropStructures.size, 4)
    }

    @Test
    fun testGenerateBedrockDropsCaching() {
        val drops = listOf(
            "type=entity,nbttag=(x=#rand(0,3)),sample=2",
            "type=entity,pos=#pPos,nbttag=(x=#rand(0,3)),sample=2",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, createEmptyGeneratedDrops("lucky_block"))
        assertEquals(listOf(
            """type="structure",id=#randList("mystructure:lucky_block_drop_1.1","mystructure:lucky_block_drop_1.2")""",
            """type="structure",id=#randList("mystructure:lucky_block_drop_1.1","mystructure:lucky_block_drop_1.2"),pos=#pPos""",
        ), newDrops)

        assertEquals(generatedDrops.dropStructures.size, 2)
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
