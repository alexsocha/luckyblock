package mod.lucky.fabric

import com.mojang.authlib.minecraft.client.MinecraftClient
import mod.lucky.common.*
import mod.lucky.common.Random
import mod.lucky.common.attribute.*
import mod.lucky.java.*
import mod.lucky.fabric.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.NbtIo
import net.minecraft.network.chat.Component
import net.minecraft.util.datafix.fixes.ItemIdFix
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.io.File
import java.io.InputStream
import java.util.*

@Environment(EnvType.CLIENT)
annotation class OnlyInClient

@Environment(EnvType.SERVER)
annotation class OnlyInServer

fun isClientWorld(world: MCIWorld): Boolean = world.isClientSide

fun toMCItemStack(stack: ItemStack): MCItemStack {
    val mcStack = MCItemStack(BuiltInRegistries.ITEM.getOptional(MCIdentifier(stack.itemId)).orElse(null) ?: Items.AIR, stack.count)
    if (stack.nbt != null) mcStack.tag = stack.nbt as CompoundTag
    return mcStack
}

fun toItemStack(stack: MCItemStack): ItemStack {
    return ItemStack(JAVA_GAME_API.getItemId(stack.item) ?: "minecraft:air", stack.count, stack.tag)
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
        val gameDir = FabricLoader.getInstance().gameDir.toFile()
        if (gameDir.name == "datagen") return gameDir.resolve(File("../../run"))
        return gameDir
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
            is LongArrayTag -> ValueAttr(AttrType.INT_ARRAY, tag.asLongArray)
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
        val arrowEntity = Arrow(world as MCServerWorld, player as MCPlayerEntity)
        arrowEntity.shootFromRotation(
            player,
            (GAME_API.getPlayerHeadPitchDeg(player) + yawOffsetDeg).toFloat(),
            (GAME_API.getPlayerHeadYawDeg(player) + pitchOffsetDeg).toFloat(),
            0.0f,
            (bowPower * 3.0).toFloat(),
            1.0f
        )
        return Pair(
            GAME_API.getEntityPos(arrowEntity),
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
        val mcText = Component.Serializer.fromJson(textJsonStr)
        if (mcText === null) {
            GAME_API.logError("Invalid JSON text: $textJsonStr")
            return
        }
        player?.sendSystemMessage(mcText)
    }

    override fun getBlockId(block: Block): String? {
        return BuiltInRegistries.BLOCK.getResourceKey(block as MCBlock).orElse(null)?.location()?.toString()
    }

    override fun getItemId(item: Item): String? {
        return BuiltInRegistries.ITEM.getResourceKey(item as MCItem).orElse(null)?.location()?.toString()
    }

    override fun isValidItemId(id: String): Boolean {
        return BuiltInRegistries.ITEM.getOptional(MCIdentifier(id)).isPresent
    }

    override fun getEntityTypeId(entity: Entity): String {
        val key = BuiltInRegistries.ENTITY_TYPE.getResourceKey((entity as MCEntity).type).orElse(null)?.location()
        return key?.toString() ?: ""
    }

    override fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String, random: Random): ListAttr {
        val chestEntity = ChestBlockEntity(toMCBlockPos(pos), Blocks.CHEST.defaultBlockState())

        // world is needed to prevent a NullPointerException
        chestEntity.setLevel(toServerWorld(world))
        chestEntity.setLootTable(MCIdentifier(lootTableId), random.randInt(0..Int.MAX_VALUE).toLong())
        chestEntity.unpackLootTable(null)

        val tag = chestEntity.saveWithFullMetadata()
        return JAVA_GAME_API.nbtToAttr(JAVA_GAME_API.readNBTKey(tag, "Items")!!) as ListAttr
    }

    override fun isCreativeMode(player: PlayerEntity): Boolean {
        return (player as MCPlayerEntity).isCreative
    }

    override fun hasSilkTouch(player: PlayerEntity): Boolean {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player as MCPlayerEntity) > 0
    }

    override fun convertLegacyItemId(id: Int, data: Int): String? {
        val legacyName: String = ItemIdFix.getItem(id)
        if (legacyName == "minecraft:air" && id > 0) return null
        return ItemStackTheFlatteningFix.updateItem(legacyName, data) ?: legacyName
    }

    override fun readNbtStructure(stream: InputStream): Pair<MinecraftNbtStructure, Vec3i> {
        val structure = StructureTemplate()
        structure.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(stream))
        return Pair(structure, toVec3i(structure.size))
    }
}
