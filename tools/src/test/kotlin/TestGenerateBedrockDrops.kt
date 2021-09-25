import mod.lucky.common.attribute.AttrType
import mod.lucky.common.attribute.ValueAttr
import mod.lucky.common.attribute.dictAttrOf
import mod.lucky.common.attribute.listAttrOf
import mod.lucky.common.drop.dropsFromStrList
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import mod.lucky.tools.*
import java.nio.ByteOrder

fun singleLine(s: String): String = s.split("\n").joinToString("") { it.trim() }

internal class BedrockDropsTests {
    @BeforeTest
    fun init() {
        prepareToGenerateDrops()
    }

    @Test
    fun testGenerateBedrockDropsSampling() {
        val drops = listOf(
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=1@chance=1",
            "type=entity,id=cow,nbttag=#randList((Short=23s),(Short=25s)),samples=3@luck=3",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, "lucky_block", createEmptyGeneratedDrops())

        assertEquals(listOf(
            """type="structure",id="lucky:lucky_block_drop_1"@chance=1.0""",
            singleLine("""type="structure",id=#randList(
                "lucky:lucky_block_drop_2.1",
                "lucky:lucky_block_drop_2.2",
                "lucky:lucky_block_drop_2.3"
            )@luck=3""")
        ), newDrops.map { dropToString(it) })

        assertEquals(generatedDrops.dropStructures.size, 4)
    }

    @Test
    fun testGenerateBedrockDropsCaching() {
        val drops = listOf(
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=2",
            "type=entity,id=pig,pos=#pPos,nbttag=(x=#rand(0,3)),samples=2",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, "lucky_block", createEmptyGeneratedDrops())
        assertEquals(listOf(
            """type="structure",id=#randList("lucky:lucky_block_drop_1.1","lucky:lucky_block_drop_1.2")""",
            """type="structure",id=#randList("lucky:lucky_block_drop_1.1","lucky:lucky_block_drop_1.2"),pos=#pPos""",
        ), newDrops.map { dropToString(it) })

        assertEquals(generatedDrops.dropStructures.size, 2)
    }

    @Test
    fun testGenerateBedrockDropsWithAmount() {
        val drops = listOf(
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=1,amount=30",
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=1,amount=30,onePerSample=true",
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=1,amount=#rand(1,2),onePerSample=true",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, "lucky_block", createEmptyGeneratedDrops())
        assertEquals(listOf(
            """type="structure",id="lucky:lucky_block_drop_1"""",
            """type="structure",amount=30,id="lucky:lucky_block_drop_2"""",
            """type="structure",amount=#rand(1,2),id="lucky:lucky_block_drop_2"""",
        ), newDrops.map { dropToString(it) })

        assertEquals(generatedDrops.dropStructures.size, 2)

        val struct1 = generatedDrops.dropStructures["lucky_block_drop_1"]!!
        assertEquals(struct1.getDict("").getDict("structure").getList("entities").children.size, 30)

        val struct2 = generatedDrops.dropStructures["lucky_block_drop_2"]!!
        assertEquals(struct2.getDict("").getDict("structure").getList("entities").children.size, 1)
    }

    @Test
    fun testAttrToNBT() {
        val attr = dictAttrOf(
            "a" to listAttrOf(ValueAttr(AttrType.DOUBLE, 4.0), ValueAttr(AttrType.INT, 4)),
            "b" to ValueAttr(AttrType.STRING, "a"),
        )
        assertEquals(29, attrToNBT(attr, ByteOrder.LITTLE_ENDIAN).position())
    }
}
