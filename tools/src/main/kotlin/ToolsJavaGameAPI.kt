package mod.lucky.tools

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.java.*
import java.io.File
import java.io.InputStream

class NoGameContextException : Exception("This API feature cannot be used without any game context")

object ToolsJavaGameAPI : JavaGameAPI {
    override fun getRBGPalette(): List<Int> {
        // from https://www.schemecolor.com/bright-rainbow-colors.php
        val colors = listOf("A800FF", "0079FF", "00F11D", "FFEF00", "FF7F00", "FF0900")
        return colors.map { it.toInt(16) }
    }

    override fun getLoaderName(): String = throw NoGameContextException()
    override fun getModVersion() = throw NoGameContextException()
    override fun getMinecraftVersion() = throw NoGameContextException()
    override fun getGameDir() = throw NoGameContextException()

    override fun attrToNBT(attr: Attr): NBTTag = throw NoGameContextException()
    override fun nbtToAttr(tag: NBTTag): Attr = DictAttr()
    override fun readNBTKey(tag: NBTTag, k: String): NBTTag? = throw NoGameContextException()
    override fun writeNBTKey(tag: NBTTag, k: String, v: NBTTag) = throw NoGameContextException()
    override fun readCompressedNBT(stream: InputStream): Attr = throw NoGameContextException()

    override fun getArrowPosAndVelocity(
        world: World,
        player: PlayerEntity,
        bowPower: Double,
        yawOffsetDeg: Double,
        pitchOffsetDeg: Double
    ): Pair<Vec3d, Vec3d> = throw NoGameContextException()

    override fun getEntityVelocity(entity: Entity): Vec3d = throw NoGameContextException()
    override fun getEntityUUID(entity: Entity): String = throw NoGameContextException()
    override fun findEntityByUUID(world: World, uuid: String): Entity? = throw NoGameContextException()
    override fun showClientMessage(textJsonStr: String) = throw NoGameContextException()
    override fun getBlockId(block: Block): String? = throw NoGameContextException()
    override fun getItemId(item: Item): String? = throw NoGameContextException()
    override fun isValidItemId(id: String): Boolean = throw NoGameContextException()
    override fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String): ListAttr = throw NoGameContextException()
    override fun getEnchantments(types: List<EnchantmentType>): List<Enchantment> =
        throw NoGameContextException()
    override fun getStatusEffect(id: String): StatusEffect =
        throw NoGameContextException()

    override fun getEntityTypeId(entity: Entity): String? = throw NoGameContextException()
    override fun isCreativeMode(player: PlayerEntity): Boolean = throw NoGameContextException()
    override fun hasSilkTouch(player: PlayerEntity): Boolean = throw NoGameContextException()
    override fun convertLegacyItemId(id: Int, data: Int): String? = throw NoGameContextException()
    override fun readNBTStructure(stream: InputStream): Pair<NBTStructure, Vec3i> = throw NoGameContextException()
}
