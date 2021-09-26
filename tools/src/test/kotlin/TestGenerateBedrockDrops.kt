import mod.lucky.common.attribute.AttrType
import mod.lucky.common.attribute.ValueAttr
import mod.lucky.common.attribute.dictAttrOf
import mod.lucky.common.attribute.listAttrOf
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.drop.dropsFromStrList
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import mod.lucky.tools.*
import java.nio.ByteOrder

fun singleLine(s: String): String = s.split("\n").joinToString("") { it.trim() }

internal class TestGenerateBedrockDrops {
    @BeforeTest
    fun init() {
        prepareToGenerateDrops()
    }

    @Test
    fun testWithoutNBT() {
        val drops = listOf(
            "type=entity,id=cow,pos=#pPos",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, "lucky_block", createEmptyGeneratedDrops())
        assertEquals(listOf(
            """type="entity",id="cow",pos=#pPos""",
        ), newDrops.map { dropToString(it) })

        assertEquals(generatedDrops.dropStructures.size, 0)
    }

    @Test
    fun testSampling() {
        val drops = listOf(
            "type=item,id=sword,nbttag=(x=#rand(0,3)),samples=1@chance=1",
            "type=item,id=axe,nbttag=#randList((Short=23s),(Short=25s)),samples=3@luck=3",
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
    fun testPreservesDefaults() {
        val drops = listOf(
            "type=entity,id=cow,nbttag=(x=1)",
        )

        val (newDrops, _) = generateDrops(dropsFromStrList(drops), 0, "lucky_block", createEmptyGeneratedDrops())
        assertEquals(listOf(
            """type="structure",adjustY=[0,10],id="lucky:lucky_block_drop_1"""",
        ), newDrops.map { dropToString(it) })
    }

    @Test
    fun testCaching() {
        val drops = listOf(
            "type=item,id=sword,nbttag=(x=#rand(0,3)),samples=2",
            "type=item,id=sword,pos=#pPos,nbttag=(x=#rand(0,3)),samples=2",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, "lucky_block", createEmptyGeneratedDrops())
        assertEquals(listOf(
            """type="structure",id=#randList("lucky:lucky_block_drop_1.1","lucky:lucky_block_drop_1.2")""",
            """type="structure",id=#randList("lucky:lucky_block_drop_1.1","lucky:lucky_block_drop_1.2"),pos=#pPos""",
        ), newDrops.map { dropToString(it) })

        assertEquals(generatedDrops.dropStructures.size, 2)
    }

    @Test
    fun testOnePerSample() {
        val drops = listOf(
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=1,posOffset=(1,1,1),amount=30",
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=1,amount=30,onePerSample=true",
            "type=entity,id=pig,nbttag=(x=#rand(0,3)),samples=1,amount=#rand(1,2),onePerSample=true",
        )

        val (newDrops, generatedDrops) = generateDrops(dropsFromStrList(drops), 0, "lucky_block", createEmptyGeneratedDrops())
        assertEquals(listOf(
            """type="structure",adjustY=[0,10],id="lucky:lucky_block_drop_1"""",
            """type="structure",adjustY=[0,10],amount=30,id="lucky:lucky_block_drop_2"""",
            """type="structure",adjustY=[0,10],amount=#rand(1,2),id="lucky:lucky_block_drop_2"""",
        ), newDrops.map { dropToString(it) })

        assertEquals(generatedDrops.dropStructures.size, 2)

        val struct1 = generatedDrops.dropStructures["lucky_block_drop_1"]!!
        val entities1 = struct1.getDict("").getDict("structure").getList("entities")
        assertEquals(entities1.children.size, 30)
        assertEquals(
            (entities1[0] as DictAttr).getList("Pos").toList().map { (it as ValueAttr).value as Float },
            listOf(1.5f, 1f, 1.5f),
        )

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
