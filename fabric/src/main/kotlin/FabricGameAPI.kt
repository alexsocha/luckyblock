package mod.lucky.fabric

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.command.EntitySelectorReader
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.registry.Registry
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.WorldAccess
import mod.lucky.common.*
import mod.lucky.common.Entity
import mod.lucky.common.World
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.action.withBlockMode
import mod.lucky.fabric.game.DelayedDrop
import mod.lucky.java.*
import mod.lucky.java.game.DelayedDropData
import mod.lucky.java.game.spawnEggSuffix
import mod.lucky.java.game.uselessPostionNames
import mod.lucky.java.game.usefulStatusEffectIds
import net.minecraft.entity.*
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.effect.StatusEffectType
import net.minecraft.nbt.NbtHelper
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.potion.PotionUtil
import net.minecraft.sound.SoundCategory
import net.minecraft.structure.Structure
import net.minecraft.structure.StructurePlacementData
import net.minecraft.structure.processor.StructureProcessor
import net.minecraft.structure.processor.StructureProcessorType
import net.minecraft.text.LiteralText
import net.minecraft.util.BlockRotation
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Vec2f
import net.minecraft.world.Difficulty
import net.minecraft.world.WorldView
import net.minecraft.world.explosion.Explosion
import java.awt.Color
import java.util.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

typealias MCBlock = net.minecraft.block.Block
typealias MCItem = net.minecraft.item.Item
typealias MCWorld = net.minecraft.world.World
typealias MCIWorld = net.minecraft.world.WorldAccess
typealias MCEntity = net.minecraft.entity.Entity
typealias MCPlayerEntity = net.minecraft.entity.player.PlayerEntity
typealias MCVec3d = net.minecraft.util.math.Vec3d
typealias MCVec3i = net.minecraft.util.math.Vec3i
typealias MCBlockPos = net.minecraft.util.math.BlockPos
typealias MCItemStack = net.minecraft.item.ItemStack
typealias MCIdentifier = net.minecraft.util.Identifier
typealias MCStatusEffect = net.minecraft.entity.effect.StatusEffect
typealias MCEnchantmentType = net.minecraft.enchantment.EnchantmentTarget

typealias Tag = net.minecraft.nbt.NbtElement
typealias ByteTag = net.minecraft.nbt.NbtByte
typealias ShortTag = net.minecraft.nbt.NbtShort
typealias IntTag = net.minecraft.nbt.NbtInt
typealias FloatTag = net.minecraft.nbt.NbtFloat
typealias DoubleTag = net.minecraft.nbt.NbtDouble
typealias LongTag = net.minecraft.nbt.NbtLong
typealias StringTag = net.minecraft.nbt.NbtString
typealias ByteArrayTag = net.minecraft.nbt.NbtByteArray
typealias IntArrayTag = net.minecraft.nbt.NbtIntArray
typealias ListTag = net.minecraft.nbt.NbtList
typealias CompoundTag = net.minecraft.nbt.NbtCompound

fun toMCVec3d(vec: Vec3d): MCVec3d = MCVec3d(vec.x, vec.y, vec.z)
fun toMCBlockPos(vec: Vec3i): MCBlockPos = MCBlockPos(vec.x, vec.y, vec.z)

fun toVec3i(vec: MCVec3i): Vec3i = Vec3i(vec.x, vec.y, vec.z)
fun toVec3d(vec: MCVec3d): Vec3d = Vec3d(vec.x, vec.y, vec.z)

fun toServerWorld(world: World): ServerWorld {
    return (world as ServerWorldAccess).toServerWorld()
}

private fun toEnchantmentType(mcType: MCEnchantmentType): EnchantmentType {
    return when (mcType) {
        MCEnchantmentType.ARMOR -> EnchantmentType.ARMOR
        MCEnchantmentType.ARMOR_FEET -> EnchantmentType.ARMOR_FEET
        MCEnchantmentType.ARMOR_LEGS -> EnchantmentType.ARMOR_LEGS
        MCEnchantmentType.ARMOR_CHEST -> EnchantmentType.ARMOR_CHEST
        MCEnchantmentType.ARMOR_HEAD -> EnchantmentType.ARMOR_HEAD
        MCEnchantmentType.WEAPON -> EnchantmentType.WEAPON
        MCEnchantmentType.DIGGER -> EnchantmentType.DIGGER
        MCEnchantmentType.FISHING_ROD -> EnchantmentType.FISHING_ROD
        MCEnchantmentType.TRIDENT -> EnchantmentType.TRIDENT
        MCEnchantmentType.BREAKABLE -> EnchantmentType.BOW
        MCEnchantmentType.BOW -> EnchantmentType.BOW
        MCEnchantmentType.WEARABLE -> EnchantmentType.WEARABLE
        MCEnchantmentType.CROSSBOW -> EnchantmentType.CROSSBOW
        MCEnchantmentType.VANISHABLE -> EnchantmentType.VANISHABLE
    }
}

private fun createCommandSource(
    world: ServerWorld,
    pos: Vec3d,
    senderName: String? = "Lucky Block",
    showOutput: Boolean,
): ServerCommandSource {
    val commandOutput = object : CommandOutput {
        override fun sendSystemMessage(message: Text?, senderUUID: UUID?) {}
        override fun shouldReceiveFeedback(): Boolean = showOutput
        override fun shouldTrackOutput(): Boolean = showOutput
        override fun shouldBroadcastConsoleToOps(): Boolean = showOutput
    }

    return ServerCommandSource(
        commandOutput,
        toMCVec3d(pos),
        Vec2f.ZERO, // (pitch, yaw)
        world,
        2,  // permission level
        senderName, LiteralText(senderName),
        world.server,
        null, // entity
    )
}

object FabricGameAPI : GameAPI {
    private var usefulPotionIds: List<String> = emptyList()
    private var spawnEggIds: List<String> = emptyList()
    private var enchantments: List<Enchantment> = emptyList()
    private var usefulStatusEffects: List<StatusEffect> = emptyList()

    fun init() {
        usefulPotionIds = Registry.POTION.ids.filter {
            it.namespace == "minecraft" && it.path !in uselessPostionNames
        }.map { it.toString() }.toList()

        spawnEggIds = Registry.ITEM.ids.filter {
            it.namespace == "minecraft"
                && it.path.endsWith(spawnEggSuffix)
        }.map { it.toString() }.toList()

        enchantments = Registry.ENCHANTMENT.entries.map {
            Enchantment(
                it.key.toString(),
                type = toEnchantmentType(it.value.type),
                maxLevel = it.value.maxLevel,
                isCurse = it.value.isCursed,
            )
        }

        usefulStatusEffects = usefulStatusEffectIds.map {
            val mcId = MCIdentifier(it)
            val mcStatusEffect = Registry.STATUS_EFFECT.get(mcId)!!
            StatusEffect(
                id = mcId.toString(),
                intId = MCStatusEffect.getRawId(mcStatusEffect),
                isNegative = mcStatusEffect.type == StatusEffectType.HARMFUL,
                isInstant = mcStatusEffect.isInstant,
            )
        }
    }

    override fun logError(msg: String?, error: Exception?) {
        if (msg != null && error != null) FabricLuckyRegistry.LOGGER.error(msg, error)
        else if (msg != null) FabricLuckyRegistry.LOGGER.error(msg)
        else FabricLuckyRegistry.LOGGER.error(error)
    }

    override fun logInfo(msg: String) {
        FabricLuckyRegistry.LOGGER.info(msg)
    }

    override fun getUsefulPotionIds(): List<String> = usefulPotionIds
    override fun getSpawnEggIds(): List<String> = spawnEggIds
    override fun getEnchantments(): List<Enchantment> = enchantments
    override fun getUsefulStatusEffects(): List<StatusEffect> = usefulStatusEffects

    override fun getRGBPalette(): List<Int> {
        return DyeColor.values().toList().map {
            val c = it.colorComponents
            Color(c[0], c[1], c[2]).rgb
        }
    }

    override fun getEntityPos(entity: Entity): Vec3d {
        val mcPos = (entity as MCEntity).pos
        return Vec3d(mcPos.x, mcPos.y, mcPos.z)
    }

    override fun getPlayerName(player: PlayerEntity): String {
        return (player as MCPlayerEntity).name.asString()
    }

    override fun applyStatusEffect(entity: Entity, effectId: String, durationSeconds: Double, amplifier: Int) {
        val statusEffect = Registry.STATUS_EFFECT.get(MCIdentifier(effectId))
        if (statusEffect == null) {
            gameAPI.logError("Unknown status effect: $effectId")
            return
        }
        val duration = if (statusEffect.isInstant) 1 else (durationSeconds * 20.0).toInt()
        if (entity is LivingEntity) entity.addStatusEffect(StatusEffectInstance(statusEffect, duration, amplifier))
    }

    // compatibility only
    override fun convertStatusEffectId(effectId: Int): String? {
        val effect = Registry.STATUS_EFFECT.get(effectId)
        return effect?.let { Registry.STATUS_EFFECT.getId(effect).toString() }
    }

    override fun getLivingEntitiesInBox(world: World, boxMin: Vec3d, boxMax: Vec3d): List<Entity> {
        val box = Box(toMCVec3d(boxMin), toMCVec3d(boxMax))
        return toServerWorld(world).getNonSpectatingEntities(LivingEntity::class.java, box)
    }

    override fun setEntityOnFire(entity: Entity, durationSeconds: Int) {
        (entity as MCEntity).setOnFireFor(durationSeconds)
    }

    override fun setEntityMotion(entity: Entity, motion: Vec3d) {
        (entity as MCEntity).velocity = toMCVec3d(motion)
        entity.velocityDirty = true
        entity.velocityModified = true
    }

    override fun getWorldTime(world: World): Long {
        return toServerWorld(world).timeOfDay
    }

    override fun getPlayerHeadYawDeg(player: PlayerEntity): Double {
        return (player as MCPlayerEntity).headYaw.toDouble()
    }

    override fun getPlayerHeadPitchDeg(player: PlayerEntity): Double {
        return (player as MCPlayerEntity).pitch.toDouble()
    }

    override fun isAirBlock(world: World, pos: Vec3i): Boolean {
        return (world as WorldAccess).isAir(toMCBlockPos(pos))
    }

    override fun spawnEntity(world: World, id: String, pos: Vec3d, nbt: DictAttr, components: DictAttr?, rotation: Double, randomizeMob: Boolean, player: PlayerEntity?, sourceId: String) {
        val entityNBT = if (id == JavaLuckyRegistry.projectileId && "sourceId" !in nbt)
            nbt.with(mapOf("sourceId" to stringAttrOf(sourceId))) else nbt

        val mcEntityNBT = javaGameAPI.attrToNBT(nbt.with(mapOf("id" to stringAttrOf(id)))) as CompoundTag

        val serverWorld = toServerWorld(world)
        val entity = EntityType.loadEntityWithPassengers(mcEntityNBT, serverWorld) { entity ->
            val entityRotation = positiveMod(rotation + 2.0, 4.0) // entities face south by default
            val rotationDeg = (entityRotation * 90.0)
            val yaw = positiveMod(entity.yaw + entityRotation, 360.0)
            val velocity = if (entityRotation == 0.0) entity.velocity
            else toMCVec3d(rotateVec3d(toVec3d(entity.velocity), degToRad(rotationDeg)))

            entity.updatePositionAndAngles(pos.x, pos.y, pos.z, yaw.toFloat(), entity.pitch)
            entity.headYaw = yaw.toFloat()
            entity.velocity = velocity
            if (serverWorld.spawnEntity(entity)) entity else null
        } ?: return

        if (entity is FallingBlockEntity && "Time" !in entityNBT) entity.timeFalling = 1
        if (player != null && entity is ArrowEntity) entity.owner = player as MCEntity

        if (entity is MobEntity && randomizeMob && "Passengers" !in entityNBT) {
            entity.initialize(
                serverWorld,
                serverWorld.getLocalDifficulty(toMCBlockPos(pos.floor())),
                SpawnReason.EVENT,
                null, null
            )
            entity.readCustomDataFromNbt(mcEntityNBT)
        }
    }

    override fun getNearestPlayer(world: World, pos: Vec3d): PlayerEntity? {
        val commandSource = createCommandSource(world as ServerWorld, pos, showOutput = false)
        return EntitySelectorReader(StringReader("@p")).read().getPlayer(commandSource)
    }

    override fun scheduleDrop(drop: SingleDrop, context: DropContext, seconds: Double) {
        val world = toServerWorld(context.world)
        val delayedDrop = DelayedDrop(world = world, data = DelayedDropData(drop, context, (seconds * 20).toInt()))
        delayedDrop.setPos(context.pos.x, context.pos.y, context.pos.z)
        world.spawnEntity(delayedDrop)
    }

    override fun setBlock(world: World, pos: Vec3i, id: String, state: DictAttr?, components: DictAttr?, rotation: Int, notify: Boolean) {
        val blockStateNBT = javaGameAPI.attrToNBT(dictAttrOf(
            "Name" to stringAttrOf(id),
            "Properties" to state,
        )) as CompoundTag
        val mcBlockState = NbtHelper.toBlockState(blockStateNBT).rotate(BlockRotation.values()[rotation])

        (world as WorldAccess).setBlockState(toMCBlockPos(pos), mcBlockState, if (notify) 3 else 2)
    }

    override fun setBlockEntity(world: World, pos: Vec3i, nbt: DictAttr) {
        val mcPos = toMCBlockPos(pos)
        val blockEntity = (world as ServerWorldAccess).getBlockEntity(mcPos)
        if (blockEntity != null) {
            val fullNBT = nbt.with(mapOf(
                "x" to intAttrOf(pos.x),
                "y" to intAttrOf(pos.y),
                "z" to intAttrOf(pos.z),
            ))
            blockEntity.readNbt(javaGameAPI.attrToNBT(fullNBT) as CompoundTag)
            blockEntity.markDirty()
            //if (world is MCWorld) world.setBlockEntity(mcPos, blockEntity)
        }
    }

    override fun dropItem(world: World, pos: Vec3d, id: String, nbt: DictAttr?, components: DictAttr?) {
        val item = Registry.ITEM.getOrEmpty(MCIdentifier(id)).orElse(null)
        if (item == null) {
            gameAPI.logError("Invalid item ID: '$id'")
            return
        }

        val itemStack = MCItemStack(item, 1)
        if (nbt != null) itemStack.nbt = javaGameAPI.attrToNBT(nbt) as CompoundTag
        MCBlock.dropStack(toServerWorld(world), toMCBlockPos(pos.floor()), itemStack)
    }

    override fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean) {
        try {
            val commandSource = createCommandSource(toServerWorld(world), pos, senderName, showOutput)
            commandSource.server.commandManager.execute(commandSource, command)
        } catch (e: Exception) {
            gameAPI.logError("Invalid command: $command", e)
        }
    }

    override fun sendMessage(player: PlayerEntity, message: String) {
        (player as MCPlayerEntity).sendMessage(LiteralText(message), false)
    }

    override fun setDifficulty(world: World, difficulty: String) {
        val difficultyEnum: Difficulty = when (difficulty) {
            "peaceful" -> Difficulty.PEACEFUL
            "easy" -> Difficulty.EASY
            "normal" -> Difficulty.NORMAL
            else -> Difficulty.HARD
        }
        toServerWorld(world).server.setDifficulty(difficultyEnum, false /* don't force */)
    }

    override fun setTime(world: World, time: Long) {
        toServerWorld(world).timeOfDay = time
    }

    override fun playSound(world: World, pos: Vec3d, id: String, volume: Double, pitch: Double) {
        val soundEvent = Registry.SOUND_EVENT.get(MCIdentifier(id))
        if (soundEvent == null) {
            gameAPI.logError("Invalid sound event: $id")
            return
        }
        toServerWorld(world).playSound(
            null, // player to exclude
            pos.x, pos.y, pos.z,
            soundEvent,
            SoundCategory.BLOCKS,
            volume.toFloat(), pitch.toFloat(),
        )
    }

    override fun spawnParticle(world: World, pos: Vec3d, id: String, args: List<String>, boxSize: Vec3d, amount: Int) {
        @Suppress("UNCHECKED_CAST")
        val particleType = Registry.PARTICLE_TYPE.get(MCIdentifier(id)) as ParticleType<ParticleEffect>?
        if (particleType == null) {
            gameAPI.logError("Invalid partical: $id")
            return
        }

        try {
            val particleData = try {
                particleType.parametersFactory.read(particleType, StringReader(" " + args.joinToString(" ")))
            } catch (e: CommandSyntaxException) {
                gameAPI.logError("Error processing partice '$id' with arguments '$args'", e)
                return
            }
            toServerWorld(world).spawnParticles(
                particleData,
                pos.x, pos.y, pos.z,
                amount,
                boxSize.x, boxSize.y, boxSize.z,
                0.0 // spread
            )
        } catch (e: Exception) {
            gameAPI.logError("Invalid partical arguments: $args", e)
            return
        }
    }

    override fun playParticleEvent(world: World, pos: Vec3d, eventId: Int, data: Int) {
        toServerWorld(world).syncWorldEvent(eventId, toMCBlockPos(pos.floor()), data)
    }

    override fun playSplashPotionEvent(world: World, pos: Vec3d, potionName: String?, potionColor: Int?) {
        if (potionName != null) {
            val potion = Registry.POTION.getOrEmpty(MCIdentifier(potionName)).orElse(null)
            if (potion == null) {
                gameAPI.logError("Invalid splash potion name: $potionName")
                return
            }

            val color = PotionUtil.getColor(potion.effects)
            playParticleEvent(world, pos, if (potion.hasInstantEffect()) 2007 else 2002, color)
        } else if (potionColor != null) {
            playParticleEvent(world, pos, 2002, potionColor)
        }
    }

    override fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean) {
        toServerWorld(world).createExplosion(null, pos.x, pos.y, pos.z, damage.toFloat(), fire, Explosion.DestructionType.DESTROY)
    }

    override fun createStructure(world: World, structureId: String, pos: Vec3i, centerOffset: Vec3i, rotation: Int, mode: String, notify: Boolean) {
        val nbtStructure = JavaLuckyRegistry.nbtStructures[structureId]
        if (nbtStructure == null) {
            gameAPI.logError("Missing structure '$structureId'")
            return
        }

        val processor = object : StructureProcessor() {
            override fun process(
                world: WorldView,
                oldPos: MCBlockPos,
                newPos: MCBlockPos,
                oldBlockInfo: Structure.StructureBlockInfo,
                newBlockInfo: Structure.StructureBlockInfo,
                settings: StructurePlacementData,
            ): Structure.StructureBlockInfo {
                val blockId = javaGameAPI.getBlockId(newBlockInfo.state.block) ?: return newBlockInfo
                val blockIdWithMode = withBlockMode(mode, blockId)

                if (blockIdWithMode == blockId) return newBlockInfo

                val newState = if (blockIdWithMode == null) world.getBlockState(newBlockInfo.pos)
                    else Registry.BLOCK.get(MCIdentifier(blockIdWithMode)).defaultState

                return if (newState == newBlockInfo.state) newBlockInfo
                    else Structure.StructureBlockInfo(newBlockInfo.pos, newState, newBlockInfo.nbt)
            }

            override fun getType(): StructureProcessorType<*> {
                return StructureProcessorType.BLOCK_IGNORE
            }
        }

        val mcRotation = BlockRotation.values()[rotation]
        val placementSettings: StructurePlacementData = StructurePlacementData()
            .setRotation(mcRotation)
            .setPosition(toMCBlockPos(centerOffset))
            .setIgnoreEntities(false)
            .addProcessor(processor)

        val mcCornerPos = toMCBlockPos(pos - centerOffset)
        (nbtStructure as Structure).place(
            world as ServerWorldAccess,
            mcCornerPos,
            mcCornerPos,
            placementSettings,
            Random.asJavaRandom(),
            if (notify) 3 else 2
        )
    }
}
