package mod.lucky.java

import mod.lucky.common.*
import mod.lucky.common.attribute.Attr
import mod.lucky.common.attribute.ListAttr
import mod.lucky.java.loader.ShapedCraftingRecipe
import mod.lucky.java.loader.ShapelessCraftingRecipe
import java.io.File
import java.io.InputStream
import java.util.*

typealias NBTTag = Any
typealias NBTStructure = Any

data class ItemStack(
    val itemId: String,
    val count: Int = 1,
    val nbt: NBTTag? = null,
)

enum class EnchantmentType {
    ARMOR,
    ARMOR_FEET,
    ARMOR_LEGS,
    ARMOR_CHEST,
    ARMOR_HEAD,
    WEAPON,
    DIGGER,
    FISHING_ROD,
    TRIDENT,
    BREAKABLE,
    BOW,
    WEARABLE,
    CROSSBOW,
    VANISHABLE,
}

data class Enchantment(
    val id: String,
    val maxLevel: Int,
    val isCurse: Boolean,
)

data class StatusEffect(
    val id: String,
    val intId: Int,
    val isInstant: Boolean,
)

interface JavaGameAPI {
    fun getLoaderName(): String
    fun getModVersion(): String
    fun getMinecraftVersion(): String
    fun getGameDir(): File
    fun attrToNBT(attr: Attr): NBTTag
    fun nbtToAttr(tag: NBTTag): Attr
    fun readNBTKey(tag: NBTTag, k: String): NBTTag?
    fun writeNBTKey(tag: NBTTag, k: String, v: NBTTag)
    fun readCompressedNBT(stream: InputStream): Attr

    fun getArrowPosAndVelocity(world: World, player: PlayerEntity, bowPower: Double, yawOffsetDeg: Double = 0.0, pitchOffsetDeg: Double = 0.0): Pair<Vec3d, Vec3d>
    fun getEntityVelocity(entity: Entity): Vec3d
    fun getEntityUUID(entity: Entity): String
    fun findEntityByUUID(world: World, uuid: String): Entity?
    fun showClientMessage(textJsonStr: String)
    fun getBlockId(block: Block): String?
    fun getItemId(item: Item): String?
    fun isValidItemId(id: String): Boolean
    fun getRBGPalette(): List<Int>
    fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String): ListAttr
    fun getEnchantments(types: List<EnchantmentType>): List<Enchantment>
    fun getStatusEffect(id: String): StatusEffect?
    fun getEntityTypeId(entity: Entity): String?
    fun isCreativeMode(player: PlayerEntity): Boolean
    fun hasSilkTouch(player: PlayerEntity): Boolean
    fun convertLegacyItemId(id: Int, data: Int): String?
    fun readNBTStructure(stream: InputStream): Pair<NBTStructure, Vec3i>
}

lateinit var javaGameAPI: JavaGameAPI