package mod.lucky.fabric

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.java.*
import mod.lucky.java.loader.ShapedCraftingRecipe
import mod.lucky.java.loader.ShapelessCraftingRecipe
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.minecraft.block.BlockState
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.datafixer.fix.ItemIdFix
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.nbt.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.Structure
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.WorldView
import java.awt.Color
import java.io.File
import java.io.InputStream
import java.util.*

@Environment(EnvType.CLIENT)
annotation class OnlyInClient

@Environment(EnvType.SERVER)
annotation class OnlyInServer

typealias MCItemStack = net.minecraft.item.ItemStack
typealias MCIdentifier = net.minecraft.util.Identifier
typealias MCStatusEffect = net.minecraft.entity.effect.StatusEffect

fun isClientWorld(world: MCWorld): Boolean = world.isClient


private fun toMCEnchantmentType(type: EnchantmentType): EnchantmentTarget {
    return when (type) {
        EnchantmentType.ARMOR -> EnchantmentTarget.ARMOR
        EnchantmentType.ARMOR_FEET -> EnchantmentTarget.ARMOR_FEET
        EnchantmentType.ARMOR_LEGS -> EnchantmentTarget.ARMOR_LEGS
        EnchantmentType.ARMOR_CHEST -> EnchantmentTarget.ARMOR_CHEST
        EnchantmentType.ARMOR_HEAD -> EnchantmentTarget.ARMOR_HEAD
        EnchantmentType.WEAPON -> EnchantmentTarget.WEAPON
        EnchantmentType.DIGGER -> EnchantmentTarget.DIGGER
        EnchantmentType.FISHING_ROD -> EnchantmentTarget.FISHING_ROD
        EnchantmentType.TRIDENT -> EnchantmentTarget.TRIDENT
        EnchantmentType.BREAKABLE -> EnchantmentTarget.BOW
        EnchantmentType.BOW -> EnchantmentTarget.BOW
        EnchantmentType.WEARABLE -> EnchantmentTarget.WEARABLE
        EnchantmentType.CROSSBOW -> EnchantmentTarget.CROSSBOW
        EnchantmentType.VANISHABLE -> EnchantmentTarget.VANISHABLE
    }
}

fun toMCItemStack(stack: ItemStack): MCItemStack {
    val mcStack = MCItemStack(Registry.ITEM.get(MCIdentifier(stack.itemId)), stack.count)
    if (stack.nbt != null) mcStack.tag = stack.nbt as CompoundTag
    return mcStack
}

fun toItemStack(stack: MCItemStack): ItemStack {
    return ItemStack(javaGameAPI.getItemId(stack.item) ?: "minecraft:air", stack.count, stack.tag)
}

object FabricJavaGameAPI : JavaGameAPI {
    override fun getLoaderName(): String {
        return "fabric"
    }

    override fun getModVersion(): String {
        return FabricLoader.getInstance().getModContainer("lucky")
            .get().metadata.version.friendlyString

    }
    override fun getMinecraftVersion(): String {
        return (FabricLoader.getInstance() as FabricLoaderImpl).gameProvider.normalizedGameVersion
    }

    override fun getGameDir(): File {
        return FabricLoader.getInstance().gameDir.toFile()
    }

    override fun attrToNBT(attr: Attr): Tag {
        return when (attr) {
            is ValueAttr -> when (attr.type) {
                AttrType.STRING -> StringTag.of(attr.value as String)
                AttrType.BYTE -> ByteTag.of(attr.value as Byte)
                AttrType.BOOLEAN -> ByteTag.of(if (attr.value == true) 1 else 0)
                AttrType.SHORT -> ShortTag.of(attr.value as Short)
                AttrType.INT -> IntTag.of(attr.value as Int)
                AttrType.LONG -> LongTag.of(attr.value as Long)
                AttrType.FLOAT -> FloatTag.of(attr.value as Float)
                AttrType.DOUBLE -> DoubleTag.of(attr.value as Double)
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
            is StringTag -> stringAttrOf(tag.asString())
            // note that booleans are stored as bytes
            is ByteTag -> ValueAttr(AttrType.BYTE, tag.byte)
            is ShortTag -> ValueAttr(AttrType.SHORT, tag.short)
            is IntTag -> ValueAttr(AttrType.INT, tag.int)
            is LongTag -> ValueAttr(AttrType.LONG, tag.long)
            is FloatTag -> ValueAttr(AttrType.FLOAT, tag.float)
            is DoubleTag -> ValueAttr(AttrType.DOUBLE, tag.double)
            is ByteArrayTag -> ValueAttr(AttrType.BYTE_ARRAY, tag.byteArray)
            is IntArrayTag -> ValueAttr(AttrType.INT_ARRAY, tag.intArray)
            is ListTag -> ListAttr(tag.map { nbtToAttr(it) })
            is CompoundTag -> {
                dictAttrOf(*tag.keys.map {
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
        val nbt = NbtIo.readCompressed(stream)
        return nbtToAttr(nbt)
    }

    override fun getArrowPosAndVelocity(
        world: World,
        player: PlayerEntity,
        bowPower: Double,
        yawOffsetDeg: Double,
        pitchOffsetDeg: Double,
    ): Pair<Vec3d, Vec3d> {
        val arrowEntity = ArrowEntity(world as ServerWorld, player as MCPlayerEntity)
        arrowEntity.setProperties( // setArrowMotion
            player,
            (gameAPI.getPlayerHeadPitchDeg(player) + yawOffsetDeg).toFloat(),
            (gameAPI.getPlayerHeadYawDeg(player) + pitchOffsetDeg).toFloat(),
            0.0f,
            (bowPower * 3.0).toFloat(),
            1.0f
        )
        return Pair(
            gameAPI.getEntityPos(arrowEntity),
            Vec3d(arrowEntity.velocity.x, arrowEntity.velocity.y, arrowEntity.velocity.z)
        )
    }

    override fun getEntityVelocity(entity: Entity): Vec3d {
        return toVec3d((entity as MCEntity).velocity)
    }

    override fun getEntityUUID(entity: Entity): String {
        return (entity as MCEntity).uuid.toString()
    }

    override fun findEntityByUUID(world: World, uuid: String): Entity? {
        return (world as ServerWorld).getEntity(UUID.fromString(uuid))
    }

    @OnlyInClient
    override fun showClientMessage(textJsonStr: String) {
        val player = MinecraftClient.getInstance().player
        player?.sendSystemMessage(Text.Serializer.fromJson(textJsonStr), UUID.fromString(getEntityUUID(player)))
    }

    override fun getBlockId(block: Block): String? {
        return Registry.BLOCK.getKey(block as MCBlock).orElse(null)?.value?.toString()
    }

    override fun getItemId(item: Item): String? {
        return Registry.ITEM.getKey(item as MCItem).orElse(null)?.value?.toString()
    }

    override fun isValidItemId(id: String): Boolean {
        return Registry.ITEM.getOrEmpty(MCIdentifier(id)).isPresent
    }

    override fun getEntityTypeId(entity: Entity): String {
        val key = Registry.ENTITY_TYPE.getKey((entity as MCEntity).type).orElse(null)
        return key?.value?.toString() ?: ""
    }

    override fun getRBGPalette(): List<Int> {
        return DyeColor.values().toList().map {
            val c = it.colorComponents
            Color(c[0], c[1], c[2]).rgb
        }
    }

    override fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String): ListAttr {
        val chestEntity = ChestBlockEntity()
        // world is needed to prevent a NullPointerException
        chestEntity.setLocation(toServerWorld(world), toMCBlockPos(pos))
        chestEntity.setLootTable(MCIdentifier(lootTableId), RANDOM.nextLong())
        chestEntity.checkLootInteraction(null)

        val tag = CompoundTag()
        chestEntity.toTag(tag)
        return javaGameAPI.nbtToAttr(javaGameAPI.readNBTKey(tag, "Items")!!) as ListAttr
    }


    override fun getEnchantments(types: List<EnchantmentType>): List<Enchantment> {
        val mcTypes = types.map { toMCEnchantmentType(it)  }
        return Registry.ENCHANTMENT.entries.filter { it.value.type in mcTypes }.map {
            Enchantment(it.key.value.toString(), it.value.maxLevel, it.value.isCursed)
        }
    }

    override fun getStatusEffect(id: String): StatusEffect? {
        val mcId = MCIdentifier(id)
        val effect = Registry.STATUS_EFFECT.get(mcId) ?: return null
        return StatusEffect(id = mcId.toString(), intId = MCStatusEffect.getRawId(effect), isInstant = effect.isInstant)
    }

    override fun isCreativeMode(player: PlayerEntity): Boolean {
        return (player as MCPlayerEntity).isCreative
    }

    override fun hasSilkTouch(player: PlayerEntity): Boolean {
        return EnchantmentHelper.getEquipmentLevel(Enchantments.SILK_TOUCH, player as MCPlayerEntity) > 0
    }

    override fun convertLegacyItemId(id: Int, data: Int): String? {
        val legacyName: String = ItemIdFix.fromId(id)
        if (legacyName == "minecraft:air" && id > 0) return null
        return ItemInstanceTheFlatteningFix.getItem(legacyName, data) ?: legacyName
    }

    override fun readNBTStructure(stream: InputStream): Pair<NBTStructure, Vec3i> {
        val structure = Structure()
        structure.fromTag(NbtIo.readCompressed(stream))
        return Pair(structure, toVec3i(structure.size))
    }
}
