package mod.lucky.neoforge

import mod.lucky.common.*
import mod.lucky.common.Random
import mod.lucky.common.attribute.*
import mod.lucky.java.*
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.PatchedDataComponentMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ProblemReporter.ScopedCollector
import net.minecraft.util.datafix.fixes.ItemIdFix
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput
import net.minecraft.world.level.storage.ValueInput
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.fml.loading.FMLLoader
import java.io.File
import java.io.InputStream
import java.util.*

@OnlyIn(Dist.CLIENT)
annotation class OnlyInClient

@OnlyIn(Dist.DEDICATED_SERVER)
annotation class OnlyInServer

fun isClientWorld(world: MCIWorld): Boolean = world.isClientSide

fun nbtToComponents(tag: CompoundTag, access: HolderLookup.Provider): DataComponentMap {
    try {
        return DataComponentMap.CODEC
            .parse(NbtOps.INSTANCE, tag)
            .getOrThrow()
    } catch (e: Exception) {
        GAME_API.logError("Failed to parse NBT: ${e}")
        return DataComponentMap.EMPTY
    }
}

fun componentsToNbt(components: DataComponentMap, access: HolderLookup.Provider): CompoundTag {
    try {
        return (DataComponentMap.CODEC
            .encodeStart(NbtOps.INSTANCE, components)
            .result() as Optional<CompoundTag>)
            .orElseThrow()
    } catch (e: Exception) {
        GAME_API.logError("Failed to parse components: ${e}")
        return CompoundTag()
    }
}

fun toMCItemStack(stack: ItemStack, access: HolderLookup.Provider): MCItemStack {
    val item = BuiltInRegistries.ITEM.get(MCIdentifier.parse(stack.itemId)).get()
    val mcStack = MCItemStack(item, stack.count)
    if (stack.nbt != null) {
        val components = nbtToComponents(stack.nbt as CompoundTag, access)
        mcStack.applyComponents(components)
    }
    return mcStack
}

fun toItemStack(stack: MCItemStack, access: HolderLookup.Provider, skipComponents: Boolean = false): ItemStack {
    return ItemStack(
        JAVA_GAME_API.getItemId(stack.item) ?: "minecraft:air",
        stack.count,
        if (skipComponents) null else componentsToNbt(stack.components, access)
    )
}

object ForgeJavaGameAPI : JavaGameAPI {
    override fun getLoaderName(): String {
        return "neoforge"
    }

    override fun getModVersion(): String {
        return ForgeLuckyRegistry.modVersion
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
            is StringTag -> stringAttrOf(tag.asString().get())
            // note that booleans are stored as bytes
            is ByteTag -> ValueAttr(AttrType.BYTE, tag.asByte().get())
            is ShortTag -> ValueAttr(AttrType.SHORT, tag.asShort().get())
            is IntTag -> ValueAttr(AttrType.INT, tag.asInt().get())
            is LongTag -> ValueAttr(AttrType.LONG, tag.asLong().get())
            is FloatTag -> ValueAttr(AttrType.FLOAT, tag.asFloat().get())
            is DoubleTag -> ValueAttr(AttrType.DOUBLE, tag.asDouble().get())
            is ByteArrayTag -> ValueAttr(AttrType.BYTE_ARRAY, tag.asByteArray)
            is IntArrayTag -> ValueAttr(AttrType.INT_ARRAY, tag.asIntArray)
            is LongArrayTag -> ValueAttr(AttrType.INT_ARRAY, tag.asLongArray)
            is ListTag -> ListAttr(tag.map { nbtToAttr(it) })
            is CompoundTag -> {
                dictAttrOf(*tag.keySet().map {
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
        val nbt = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap())
        return nbtToAttr(nbt)
    }

    override fun getArrowPosAndVelocity(
        world: World,
        player: PlayerEntity,
        bowPower: Double,
        yawOffsetDeg: Double,
        pitchOffsetDeg: Double,
    ): Pair<Vec3d, Vec3d> {
        val arrowEntity = Arrow(world as MCServerWorld, player as MCPlayerEntity, MCItemStack.EMPTY, null)
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

    override fun getBlockId(block: Block): String? {
        return BuiltInRegistries.BLOCK.getKeyOrNull(block as MCBlock)?.toString()
    }

    override fun getItemId(item: Item): String? {
        return BuiltInRegistries.ITEM.getKeyOrNull(item as MCItem)?.toString()
    }

    override fun isValidItemId(id: String): Boolean {
        return BuiltInRegistries.ITEM.containsKey(MCIdentifier.parse(id))
    }

    override fun getEntityTypeId(entity: Entity): String {
        val key = BuiltInRegistries.ENTITY_TYPE.getKeyOrNull((entity as MCEntity).type)
        return key?.toString() ?: "<invalid entity ${key}>"
    }

    override fun generateChestLoot(world: World, pos: Vec3i, lootTableId: String, random: Random): ListAttr {
        val chestEntity = ChestBlockEntity(toMCBlockPos(pos), Blocks.CHEST.defaultBlockState())

        // world is needed to prevent a NullPointerException
        chestEntity.setLevel(toServerWorld(world))
        chestEntity.setLootTable(
            ResourceKey.create(Registries.LOOT_TABLE, MCIdentifier.parse(lootTableId)),
            random.randInt(0..Int.MAX_VALUE).toLong()
        )
        chestEntity.unpackLootTable(null)

        val tag = chestEntity.saveWithFullMetadata((world as MCWorld).registryAccess())
        return JAVA_GAME_API.nbtToAttr(JAVA_GAME_API.readNBTKey(tag, "Items")!!) as ListAttr
    }

    override fun isCreativeMode(player: PlayerEntity): Boolean {
        return (player as MCPlayerEntity).isCreative
    }

    override fun hasSilkTouch(player: PlayerEntity): Boolean {
        return try {
            val enchantment = (player as MCPlayerEntity).level().holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH)
            EnchantmentHelper.getEnchantmentLevel(enchantment, player) > 0
        } catch (e: IllegalStateException) { false }
    }

    override fun convertLegacyItemId(id: Int, data: Int): String? {
        val legacyName: String = ItemIdFix.getItem(id)
        if (legacyName == "minecraft:air" && id > 0) return null
        return ItemStackTheFlatteningFix.updateItem(legacyName, data) ?: legacyName
    }

    override fun readNbtStructure(stream: InputStream): Pair<MinecraftNbtStructure, Vec3i> {
        val structure = StructureTemplate()
        structure.load(BuiltInRegistries.BLOCK, NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap()))
        return Pair(structure, toVec3i(structure.size))
    }
}
