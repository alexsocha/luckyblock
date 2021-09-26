import io.mockk.mockkObject
import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.*
import mod.lucky.common.drop.action.calculatePos
import kotlin.test.*

val testDropContext = DropContext(
    world = Any(),
    pos = Vec3d(1.0, 2.0, 3.0),
    player = MockEntity(Vec3d(4.5, 5.0, 6.5)),
    sourceId = "lucky:lucky_block",
)
val testEvalContext = createDropEvalContext(testDropContext)

fun createSingleDrop(propsStr: String): SingleDrop =
    SingleDrop.fromString(propsStr).eval(testEvalContext)

class Tests {
    @BeforeTest
    fun beforeTest() {
        mockkObject(MockGameAPI)
        gameAPI = MockGameAPI
        logger = MockGameAPI
    }

    @Test
    fun testPos() {
        val drop1 = createSingleDrop("type = item, id = minecraft:stick, pos = #pPos")
        assertEquals(Vec3d(4.0, 5.0, 6.0), calculatePos(drop1))

        val drop2 = createSingleDrop("posY=#pPosY + 3, POSZ=50")
        assertEquals(Vec3d(1.0, 8.0, 50.0), calculatePos(drop2, testDropContext.pos))

        val drop3 = createSingleDrop("pos=(#bPosY/4.0, #pExactPosZ-#bPosX, #bPosY*2.5)")
        assertEquals(Vec3d(0.5, 5.5, 5.0), calculatePos(drop3))
    }

    @Test
    fun testPosOffset() {
        val drop1 = createSingleDrop("pos=(1, 2, 3),posOffset=(10,10,10)")
        assertEquals(Vec3d(11.0, 12.0, 13.0), calculatePos(drop1))

        val drop2 = createSingleDrop("pos=(1, 2, 3),posOffsetY=10")
        assertEquals(Vec3d(1.0, 12.0, 3.0), calculatePos(drop2))

        val drop3 = createSingleDrop("pos=(4,4,4),posOffsetX=2,centerOffset=(1,-1,1),rotation=2")
        assertEquals(Vec3d(3.0, 5.0, 5.0), calculatePos(drop3))
    }

    @Test
    fun testTypes() {
        val drop = createSingleDrop("type=entity,amount=2.5,posY=26f,nbttag=("
            + "i=5,b=5b,s=5 s,f=5.5F,d1=5.5,d2=5D,"
            + "str=\"5\",list=[1, 2, 3], dict=(k1 = 1, k2 = 2),"
            + "ba=1:2:3b:4, ia=1:2:3:5"
            + ")"
        )
        val nbt: DictAttr = drop["nbttag"]
        assertEquals(Vec3d(1.0, 26.0, 3.0), calculatePos(drop, testDropContext.pos))
        assertEquals(2, drop["amount"])
        assertEquals(AttrType.INT, nbt["i"]!!.type)
        assertEquals(AttrType.BYTE, nbt["b"]!!.type)
        assertEquals(AttrType.SHORT, nbt["s"]!!.type)
        assertEquals(AttrType.FLOAT, nbt["f"]!!.type)
        assertEquals(AttrType.DOUBLE, nbt["d1"]!!.type)
        assertEquals(AttrType.DOUBLE, nbt["d2"]!!.type)
        assertEquals(AttrType.STRING, nbt["str"]!!.type)
        assertEquals(AttrType.LIST, nbt["list"]!!.type)
        assertEquals(AttrType.DICT, nbt["dict"]!!.type)
        assertEquals(AttrType.BYTE_ARRAY, nbt["ba"]!!.type)
        assertEquals(AttrType.INT_ARRAY, nbt["ia"]!!.type)
    }

    @Test
    fun testNestedPos() {
        val props = createSingleDrop("type=entity,nbttag=(posList=#bPos,posDict=(x=#bPosX,y=#bPosY,z=#bPosZ))")
        val nbt: DictAttr = props["nbttag"]
        assertEquals(Vec3i(1, 2, 3), nbt.getVec3("posList"))
        assertEquals(1, nbt.getDict("posDict").getValue("x"))
        assertEquals(2, nbt.getDict("posDict").getValue("y"))
        assertEquals(3, nbt.getDict("posDict").getValue("z"))
    }

    @Test
    fun testNestedJsonStr() {
        val drop = createSingleDrop("type=entity,nbttag=(name=#jsonStr(text=#randList(A)))")
        val nbt: DictAttr = drop["nbttag"]
        assertEquals("{\"text\": \"A\"}", nbt.getValue("name"))
    }

    @Test
    fun testTemplateVarReturnsDict() {
        val drop = createSingleDrop("type=entity,nbttag=#randList((x=4))")
        val nbt: DictAttr = drop["nbttag"]
        assertEquals(4, nbt.getValue("x"))
    }

    @Test
    fun testDropTemplateVar() {
        val drop = createSingleDrop("type=block,posZ=#pExactPosZ,tileEntity=(y=#drop(posZ))")
        assertEquals(6.5, drop.get<DictAttr>("nbttag").getValue("y"))

        assertFailsWith<EvalError> { createSingleDrop("pos=#drop(pos)") }
    }

    @Test
    fun testDirectionToVelocity() {
        val drop = createSingleDrop("type=entity,id=arrow,nbttag=(Motion=#motionFromDirection(225, 45, 10))")
        assertEquals(Vec3i(5, -8, -6), drop.get<DictAttr>("nbttag").getVec3<Double>("Motion").floor())
    }

    @Test
    fun testTemplateCancelling() {
        val drop = createSingleDrop("type=entity,nbttag=("
            + "Drops=[\"group(a1=#bPosY,b1=[#]bPosY,c1='#'bPosY)\"],"
            + "a2=#bPosY,b2='#'bPosY,"
            + "otherList=[\"group(a3=#bPosY,b3='#'bPosY)\"])",
        )

        val nbt: DictAttr = drop["nbttag"]
        assertEquals("group(a1=#bPosY,b1=2,c1=#bPosY)", (nbt.getList("Drops")[0] as ValueAttr).value)
        assertEquals(2, nbt.getValue("a2"))
        assertEquals("#bPosY", nbt.getValue("b2"))
        assertEquals("group(a3=2,b3=#bPosY)", (nbt.getList("otherList")[0] as ValueAttr).value)
    }

    @Test
    fun testGroup() {
        val groupDrop = WeightedDrop.fromString("group:2:(id=cow;id=pig;id=sheep),type=entity,nbttag=(value=5)")
        val singleDrops = evalDrop(groupDrop, testEvalContext)

        assertEquals(2, ((groupDrop.drop as GroupDrop).amount as ValueAttr).value)
        assertEquals(2, singleDrops.size)
        for (i in 0..1) {
            assertTrue { singleDrops[i]["id"] in listOf("cow", "pig", "sheep") }
            assertEquals("entity", singleDrops[i].type)
            assertEquals(5, singleDrops[i].get<DictAttr>("nbttag").getValue("value"))
        }
    }

    @Test
    fun testLuckChance() {
        val drop = WeightedDrop.fromString("type=message,message=\"@luck=0\"@luck=-2@chance=0.1")
        assertEquals(-2, drop.luck)
        assertEquals(0.1, drop.chance)
    }

    @Test
    fun testReadLuckyStructure() {
        val (props, drops) = readLuckyStructure(listOf(
            ">properties",
            "width=9,length=10",
            ">blocks",
            "1,2,3,lucky:lucky_block,nbttag=(Luck=3)",
            ">entities",
            "4,5,6,bat",
            ">drops",
            "group(type=nothing,type=nothing)"
        ))
        assertEquals(9.0, props.getValue("width"))
        assertEquals(10.0, props.getValue("length"))

        assertEquals("block", (drops[0] as SingleDrop).type)
        assertEquals("lucky:lucky_block", (drops[0] as SingleDrop).get("id"))
        assertEquals(
            Vec3d(1.0, 2.0, 3.0),
            (drops[0] as SingleDrop).getVec3<Double>("posOffset"),
        )
        assertEquals(3, (drops[0] as SingleDrop).get<DictAttr>("nbttag").getValue("Luck"))

        assertEquals("entity", (drops[1] as SingleDrop).type)
        assertEquals("bat", (drops[1] as SingleDrop).get("id"))
        assertEquals(
            Vec3d(4.0, 5.0, 6.0),
            (drops[1] as SingleDrop).getVec3<Double>("posOffset"),
        )

        assert(drops[2] is GroupDrop)
    }

    @Test
    fun testReadLuckyStructureWithDropsOnly() {
        val (props, drops) = readLuckyStructure(listOf(
            "posOffset=(1,2,3),type=entity,ID=pig",
        ))

        assertEquals(0, props.children.size)
        assertEquals("entity", (drops[0] as SingleDrop).type)
        assertEquals("pig", (drops[0] as SingleDrop).get("id"))
    }

    @Test
    fun testNotDebug() {
        assertEquals(false, DEBUG)
    }
}
