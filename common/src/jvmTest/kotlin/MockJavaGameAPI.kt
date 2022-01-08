import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.java.*
import java.io.File
import java.io.InputStream

object MockJavaGameAPI : JavaGameAPI {
    override fun getLoaderName(): String = "test"
    override fun getModVersion() = "0.0.0-0"
    override fun getMinecraftVersion() = "0.0.0"
    override fun getGameDir() = File("build/test-run")

    override fun attrToNBT(attr: Attr): NBTTag = ""
    override fun nbtToAttr(tag: NBTTag): Attr = DictAttr()
    override fun readNBTKey(tag: NBTTag, k: String): NBTTag? = null
    override fun writeNBTKey(tag: NBTTag, k: String, v: NBTTag) {}
    override fun readCompressedNBT(stream: InputStream): Attr = dictAttrOf(
        // .schematic data
        "Width" to ValueAttr(AttrType.SHORT, 1),
        "Height" to ValueAttr(AttrType.SHORT, 1),
        "Length" to ValueAttr(AttrType.SHORT, 1),
        "Blocks" to ValueAttr(AttrType.BYTE_ARRAY, byteArrayOf(1, 2)),
        "Data" to ValueAttr(AttrType.BYTE_ARRAY, byteArrayOf(0, 0)),
        "Entities" to listAttrOf(),
    )

    override fun getArrowPosAndVelocity(
        world: World,
        player: PlayerEntity,
        bowPower: Double,
        yawOffsetDeg: Double,
        pitchOffsetDeg: Double
    ): Pair<Vec3d, Vec3d> = Pair(Vec3d(0.0, 0.0, 0.0), Vec3d(0.0, 0.0, 0.0))

    override fun getEntityVelocity(entity: Entity): Vec3d = Vec3d(0.0, 0.0, 0.0)
    override fun getEntityUUID(entity: Entity): String = "123e4567-e89b-12d3-a456-426614174000"
    override fun findEntityByUUID(world: World, uuid: String): Entity? = null
    override fun showClientMessage(textJsonStr: String) {}
    override fun getBlockId(block: Block): String? = null
    override fun getItemId(item: Item): String? = null
    override fun isValidItemId(id: String): Boolean = true
    override fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String, random: Random): ListAttr = ListAttr()

    override fun getEntityTypeId(entity: Entity): String? = null
    override fun isCreativeMode(player: PlayerEntity): Boolean = false
    override fun hasSilkTouch(player: PlayerEntity): Boolean = false
    override fun convertLegacyItemId(id: Int, data: Int): String? = null
    override fun readNbtStructure(stream: InputStream): Pair<MinecraftNbtStructure, Vec3i> = Pair("", Vec3i(0, 0, 0))
}
