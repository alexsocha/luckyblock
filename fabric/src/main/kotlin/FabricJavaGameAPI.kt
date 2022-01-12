package mod.lucky.fabric

import mod.lucky.common.*
import mod.lucky.common.Random
import mod.lucky.common.attribute.*
import mod.lucky.java.*
import mod.lucky.fabric.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Blocks
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.datafixer.fix.ItemIdFix
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.nbt.NbtIo
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.Structure
import net.minecraft.text.Text
import net.minecraft.util.registry.Registry
import java.io.File
import java.io.InputStream
import java.util.*

@Environment(EnvType.CLIENT)
annotation class OnlyInClient

@Environment(EnvType.SERVER)
annotation class OnlyInServer

fun isClientWorld(world: MCIWorld): Boolean = world.isClient

fun toMCItemStack(stack: ItemStack): MCItemStack {
    val mcStack = MCItemStack(Registry.ITEM.get(MCIdentifier(stack.itemId)), stack.count)
    if (stack.nbt != null) mcStack.nbt = stack.nbt as CompoundTag
    return mcStack
}

fun toItemStack(stack: MCItemStack): ItemStack {
    return ItemStack(JAVA_GAME_API.getItemId(stack.item) ?: "minecraft:air", stack.count, stack.nbt)
}

object FabricJavaGameAPI : JavaGameAPI {
    override fun getLoaderName(): String {
        return "fabric"
    }

    override fun getModVersion(): String {
        return (FabricLoader.getInstance() as net.fabricmc.loader.FabricLoader).getModContainer("lucky")
            .get().metadata.version.friendlyString

    }
    override fun getMinecraftVersion(): String {
        return (FabricLoader.getInstance() as net.fabricmc.loader.FabricLoader).gameProvider.normalizedGameVersion
    }

    override fun getGameDir(): File {
        return (FabricLoader.getInstance() as net.fabricmc.loader.FabricLoader).gameProvider.launchDirectory.toFile()
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
                AttrType.BYTE_ARRAY -> ByteArrayTag(attr.value as ByteArray)
                AttrType.INT_ARRAY -> IntArrayTag(attr.value as IntArray)
                AttrType.LONG_ARRAY -> LongArrayTag(attr.value as LongArray)
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
            is ByteTag -> ValueAttr(AttrType.BYTE, tag.byteValue())
            is ShortTag -> ValueAttr(AttrType.SHORT, tag.shortValue())
            is IntTag -> ValueAttr(AttrType.INT, tag.intValue())
            is LongTag -> ValueAttr(AttrType.LONG, tag.longValue())
            is FloatTag -> ValueAttr(AttrType.FLOAT, tag.floatValue())
            is DoubleTag -> ValueAttr(AttrType.DOUBLE, tag.doubleValue())
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
        arrowEntity.setProperties(
            player,
            (GAME_API.getPlayerHeadPitchDeg(player) + yawOffsetDeg).toFloat(),
            (GAME_API.getPlayerHeadYawDeg(player) + pitchOffsetDeg).toFloat(),
            0.0f,
            (bowPower * 3.0).toFloat(),
            1.0f
        )
        return Pair(
            GAME_API.getEntityPos(arrowEntity),
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
        val mcText = Text.Serializer.fromJson(textJsonStr)
        if (mcText == null) {
            GAME_API.logError("Invalid JSON text: $textJsonStr")
            return
        }
        player?.sendSystemMessage(mcText, UUID.fromString(getEntityUUID(player)))
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

    override fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String, random: Random): ListAttr {
        val chestEntity = ChestBlockEntity(toMCBlockPos(pos), Blocks.CHEST.defaultState)
        // world is needed to prevent a NullPointerException
        chestEntity.world = toServerWorld(world)
        chestEntity.setLootTable(MCIdentifier(lootTableId), random.randInt(0..Int.MAX_VALUE).toLong())
        chestEntity.checkLootInteraction(null)

        val tag = CompoundTag()
        chestEntity.writeNbt(tag)

        return JAVA_GAME_API.nbtToAttr(JAVA_GAME_API.readNBTKey(tag, "Items")!!) as ListAttr
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

    override fun readNbtStructure(stream: InputStream): Pair<MinecraftNbtStructure, Vec3i> {
        val structure = Structure()
        structure.readNbt(NbtIo.readCompressed(stream))
        return Pair(structure, toVec3i(structure.size))
    }
}
