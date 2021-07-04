package mod.lucky.forge

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.java.*
import mod.lucky.forge.*
import mod.lucky.forge.MCIdentifier
import net.minecraft.client.Minecraft
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.item.DyeColor
import net.minecraft.item.Items
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.tileentity.ChestTileEntity
import net.minecraft.util.datafix.fixes.ItemIntIDToString
import net.minecraft.util.datafix.fixes.ItemStackDataFlattening
import net.minecraft.util.registry.Registry
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.gen.feature.template.Template
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.versions.mcp.MCPVersion
import java.awt.Color
import java.io.File
import java.io.InputStream
import java.util.*

@OnlyIn(Dist.CLIENT)
annotation class OnlyInClient

@OnlyIn(Dist.DEDICATED_SERVER)
annotation class OnlyInServer

typealias MCItemStack = net.minecraft.item.ItemStack
typealias MCEnchantmentType = net.minecraft.enchantment.EnchantmentType
typealias MCStatusEffect = net.minecraft.potion.Effect

fun isClientWorld(world: MCIWorld): Boolean = world.isClientSide

private fun toMCEnchantmentType(type: EnchantmentType): MCEnchantmentType {
    return when (type) {
        EnchantmentType.ARMOR -> MCEnchantmentType.ARMOR
        EnchantmentType.ARMOR_FEET -> MCEnchantmentType.ARMOR_FEET
        EnchantmentType.ARMOR_LEGS -> MCEnchantmentType.ARMOR_LEGS
        EnchantmentType.ARMOR_CHEST -> MCEnchantmentType.ARMOR_CHEST
        EnchantmentType.ARMOR_HEAD -> MCEnchantmentType.ARMOR_HEAD
        EnchantmentType.WEAPON -> MCEnchantmentType.WEAPON
        EnchantmentType.DIGGER -> MCEnchantmentType.DIGGER
        EnchantmentType.FISHING_ROD -> MCEnchantmentType.FISHING_ROD
        EnchantmentType.TRIDENT -> MCEnchantmentType.TRIDENT
        EnchantmentType.BREAKABLE -> MCEnchantmentType.BOW
        EnchantmentType.BOW -> MCEnchantmentType.BOW
        EnchantmentType.WEARABLE -> MCEnchantmentType.WEARABLE
        EnchantmentType.CROSSBOW -> MCEnchantmentType.CROSSBOW
        EnchantmentType.VANISHABLE -> MCEnchantmentType.VANISHABLE
    }
}

fun toMCItemStack(stack: ItemStack): MCItemStack {
    val mcStack = MCItemStack(ForgeRegistries.ITEMS.getValue(MCIdentifier(stack.itemId)) ?: Items.AIR, stack.count)
    if (stack.nbt != null) mcStack.tag = stack.nbt as CompoundTag
    return mcStack
}

fun toItemStack(stack: MCItemStack): ItemStack {
    return ItemStack(javaGameAPI.getItemId(stack.item) ?: "minecraft:air", stack.count, stack.tag)
}

object ForgeJavaGameAPI : JavaGameAPI {
    override fun getLoaderName(): String {
        return "forge"
    }

    override fun getModVersion(): String {
        return ForgeLuckyRegistry.modVersion
    }

    override fun getMinecraftVersion(): String {
        return MCPVersion.getMCVersion()
    }

    override fun getGameDir(): File {
        return FMLLoader.getGamePath().toFile()
    }

    override fun attrToNBT(attr: Attr): Tag {
        return when (attr) {
            is ValueAttr -> when (attr.type) {
                AttrType.STRING -> StringTag.valueOf(attr.value as String)
                AttrType.BYTE -> ByteTag.valueOf(attr.value as Byte)
                AttrType.BOOLEAN -> ByteTag.valueOf(attr.value == true)
                AttrType.SHORT -> ShortTag.valueOf(attr.value as Short)
                AttrType.INT -> IntTag.valueOf(attr.value as Int)
                AttrType.LONG -> LongTag.valueOf(attr.value as Long)
                AttrType.FLOAT -> FloatTag.valueOf(attr.value as Float)
                AttrType.DOUBLE -> DoubleTag.valueOf(attr.value as Double)
                AttrType.INT_ARRAY -> IntArrayTag(attr.value as IntArray)
                AttrType.BYTE_ARRAY -> ByteArrayTag(attr.value as ByteArray)
                AttrType.LIST, AttrType.DICT -> throw Exception()
            }
            is ListAttr -> {
                val listTag = ListTag()
                attr.children.forEach { listTag.add(attrToNBT(it)) }
                listTag
            }
            is DictAttr -> {
                val dictTag = CompoundTag()
                attr.children.forEach { (k, v) -> dictTag.put(k, attrToNBT(v)) }
                dictTag
            }
            else -> throw Exception()
        }
    }

    override fun nbtToAttr(tag: NBTTag): Attr {
        return when (tag) {
            is StringTag -> stringAttrOf(tag.asString)
            // note that booleans are stored as bytes
            is ByteTag -> ValueAttr(AttrType.BYTE, tag.asByte)
            is ShortTag -> ValueAttr(AttrType.SHORT, tag.asShort)
            is IntTag -> ValueAttr(AttrType.INT, tag.asInt)
            is LongTag -> ValueAttr(AttrType.LONG, tag.asLong)
            is FloatTag -> ValueAttr(AttrType.FLOAT, tag.asFloat)
            is DoubleTag -> ValueAttr(AttrType.DOUBLE, tag.asDouble)
            is ByteArrayTag -> ValueAttr(AttrType.BYTE_ARRAY, tag.asByteArray)
            is IntArrayTag -> ValueAttr(AttrType.INT_ARRAY, tag.asIntArray)
            is ListTag -> ListAttr(tag.map { nbtToAttr(it) })
            is CompoundTag -> {
                dictAttrOf(*tag.allKeys.map {
                    it to tag.get(it)?.let { v -> nbtToAttr(v) }
                }.toTypedArray())
            }
            else -> throw Exception()
        }
    }

    override fun readNBTKey(tag: NBTTag, k: String): NBTTag? {
        return (tag as CompoundTag).get(k)
    }

    override fun writeNBTKey(tag: NBTTag, k: String, v: NBTTag) {
        (tag as CompoundTag).put(k, v as Tag)
    }

    override fun readCompressedNBT(stream: InputStream): Attr {
        val nbt = CompressedStreamTools.readCompressed(stream)
        return nbtToAttr(nbt)
    }

    override fun getArrowPosAndVelocity(
        world: World,
        player: PlayerEntity,
        bowPower: Double,
        yawOffsetDeg: Double,
        pitchOffsetDeg: Double,
    ): Pair<Vec3d, Vec3d> {
        val arrowEntity = ArrowEntity(world as MCServerWorld, player as MCPlayerEntity)
        arrowEntity.shootFromRotation(
            player,
            (gameAPI.getPlayerHeadPitchDeg(player) + yawOffsetDeg).toFloat(),
            (gameAPI.getPlayerHeadYawDeg(player) + pitchOffsetDeg).toFloat(),
            0.0f,
            (bowPower * 3.0).toFloat(),
            1.0f
        )
        return Pair(
            gameAPI.getEntityPos(arrowEntity),
            Vec3d(arrowEntity.deltaMovement.x, arrowEntity.deltaMovement.y, arrowEntity.deltaMovement.z)
        )
    }

    override fun getEntityVelocity(entity: Entity): Vec3d {
        return toVec3d((entity as MCEntity).deltaMovement)
    }

    override fun getEntityUUID(entity: Entity): String {
        return (entity as MCEntity).uuid.toString()
    }

    override fun findEntityByUUID(world: World, uuid: String): Entity? {
        return (world as MCServerWorld).getEntity(UUID.fromString(uuid))
    }

    @OnlyInClient
    override fun showClientMessage(textJsonStr: String) {
        val player = Minecraft.getInstance().player
        val mcText = ITextComponent.Serializer.fromJson(textJsonStr)
        if (mcText == null) {
            gameAPI.logError("Invalid JSON text: $textJsonStr")
            return
        }
        player?.sendMessage(mcText, UUID.fromString(getEntityUUID(player)))
    }

    override fun getBlockId(block: Block): String? {
        return ForgeRegistries.BLOCKS.getKey(block as MCBlock)?.toString()
    }

    override fun getItemId(item: Item): String? {
        return ForgeRegistries.ITEMS.getKey(item as MCItem)?.toString()
    }

    override fun isValidItemId(id: String): Boolean {
        return ForgeRegistries.ITEMS.containsKey(MCIdentifier(id))
    }

    override fun getEntityTypeId(entity: Entity): String {
        val key = ForgeRegistries.ENTITIES.getKey((entity as MCEntity).type)
        return key?.toString() ?: ""
    }

    override fun getRBGPalette(): List<Int> {
        return DyeColor.values().toList().map {
            val c = it.textureDiffuseColors
            Color(c[0], c[1], c[2]).rgb
        }
    }

    override fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String): ListAttr {
        val chestEntity = ChestTileEntity()
        chestEntity.setLootTable(MCIdentifier(lootTableId), RANDOM.nextLong())
        chestEntity.setLevelAndPosition(world as MCWorld, toMCBlockPos(pos))
        chestEntity.unpackLootTable(null)

        val tag = CompoundTag()
        chestEntity.save(tag)
        return javaGameAPI.nbtToAttr(javaGameAPI.readNBTKey(tag, "Items")!!) as ListAttr
    }


    override fun getEnchantments(types: List<EnchantmentType>): List<Enchantment> {
        val mcTypes = types.map { toMCEnchantmentType(it)  }
        return ForgeRegistries.ENCHANTMENTS.entries.filter { it.value.category in mcTypes }.map {
            Enchantment(it.key.toString(), it.value.maxLevel, it.value.isCurse)
        }
    }

    override fun getStatusEffect(id: String): StatusEffect? {
        val mcId = MCIdentifier(id)
        val effect = ForgeRegistries.POTIONS.getValue(mcId) ?: return null
        return StatusEffect(id = mcId.toString(), intId = MCStatusEffect.getId(effect), isInstant = effect.isInstantenous)
    }

    override fun isCreativeMode(player: PlayerEntity): Boolean {
        return (player as MCPlayerEntity).isCreative
    }

    override fun hasSilkTouch(player: PlayerEntity): Boolean {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player as MCPlayerEntity) > 0
    }

    override fun convertLegacyItemId(id: Int, data: Int): String? {
        val legacyName: String = ItemIntIDToString.getItem(id)
        if (legacyName == "minecraft:air" && id > 0) return null
        return ItemStackDataFlattening.updateItem(legacyName, data) ?: legacyName
    }

    override fun readNBTStructure(stream: InputStream): Pair<NBTStructure, Vec3i> {
        val structure = Template()
        structure.load(CompressedStreamTools.readCompressed(stream))
        return Pair(structure, toVec3i(structure.size))
    }
}