package mod.lucky.forge

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.util.registry.Registry
import mod.lucky.common.*
import mod.lucky.common.Entity
import mod.lucky.common.World
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.action.withBlockMode
import mod.lucky.forge.game.DelayedDrop
import mod.lucky.java.*
import mod.lucky.java.game.DelayedDropData
import mod.lucky.java.game.spawnEggSuffix
import mod.lucky.java.game.uselessPostionNames
import net.minecraft.command.CommandSource
import net.minecraft.command.ICommandSource
import net.minecraft.command.arguments.EntitySelectorParser
import net.minecraft.entity.*
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTUtil
import net.minecraft.particles.IParticleData
import net.minecraft.particles.ParticleType
import net.minecraft.potion.Effect
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.PotionUtils
import net.minecraft.util.Rotation
import net.minecraft.util.SoundCategory
import net.minecraft.world.*
import net.minecraft.world.gen.feature.template.IStructureProcessorType
import net.minecraft.world.gen.feature.template.PlacementSettings
import net.minecraft.world.gen.feature.template.StructureProcessor
import net.minecraft.world.gen.feature.template.Template
import net.minecraftforge.registries.ForgeRegistries
import java.util.*
import kotlin.random.asJavaRandom

typealias MCIdentifier = net.minecraft.util.ResourceLocation
typealias MCBlock = net.minecraft.block.Block
typealias MCItem = net.minecraft.item.Item
typealias MCIWorld = net.minecraft.world.IWorld
typealias MCIServerWorld = net.minecraft.world.IServerWorld
typealias MCWorld = net.minecraft.world.World
typealias MCServerWorld = net.minecraft.world.server.ServerWorld
typealias MCEntity = net.minecraft.entity.Entity
typealias MCPlayerEntity = net.minecraft.entity.player.PlayerEntity
typealias MCVec3d = net.minecraft.util.math.vector.Vector3d
typealias MCVec3i = net.minecraft.util.math.vector.Vector3i
typealias MCVec2f = net.minecraft.util.math.vector.Vector2f
typealias MCBlockPos = net.minecraft.util.math.BlockPos
typealias MCBox = net.minecraft.util.math.AxisAlignedBB

typealias MCText = net.minecraft.util.text.ITextComponent
typealias MCLiteralText = net.minecraft.util.text.StringTextComponent
typealias MCTextFormatting = net.minecraft.util.text.TextFormatting
typealias MCTranslatableText = net.minecraft.util.text.TranslationTextComponent

typealias Tag = net.minecraft.nbt.INBT
typealias ByteTag = net.minecraft.nbt.ByteNBT
typealias ShortTag = net.minecraft.nbt.ShortNBT
typealias IntTag = net.minecraft.nbt.IntNBT
typealias FloatTag = net.minecraft.nbt.FloatNBT
typealias DoubleTag = net.minecraft.nbt.DoubleNBT
typealias LongTag = net.minecraft.nbt.LongNBT
typealias StringTag = net.minecraft.nbt.StringNBT
typealias ByteArrayTag = net.minecraft.nbt.ByteArrayNBT
typealias IntArrayTag = net.minecraft.nbt.IntArrayNBT
typealias ListTag = net.minecraft.nbt.ListNBT
typealias CompoundTag = net.minecraft.nbt.CompoundNBT

fun toMCVec3d(vec: Vec3d): MCVec3d = MCVec3d(vec.x, vec.y, vec.z)
fun toMCBlockPos(vec: Vec3i): MCBlockPos = MCBlockPos(vec.x, vec.y, vec.z)

fun toVec3i(vec: MCVec3i): Vec3i = Vec3i(vec.x, vec.y, vec.z)
fun toVec3d(vec: MCVec3d): Vec3d = Vec3d(vec.x, vec.y, vec.z)

fun toServerWorld(world: World): MCServerWorld {
    return (world as MCServerWorld).worldServer
}

private fun createCommandSource(
    world: MCServerWorld,
    pos: Vec3d,
    senderName: String = "Lucky Block",
    showOutput: Boolean,
): CommandSource {
    val commandOutput = object : ICommandSource {
        override fun sendMessage(message: MCText?, senderUUID: UUID?) {}
        override fun acceptsSuccess(): Boolean = showOutput
        override fun acceptsFailure(): Boolean = showOutput
        override fun shouldInformAdmins(): Boolean = showOutput
    }

    return CommandSource(
        commandOutput,
        toMCVec3d(pos),
        MCVec2f.ZERO, // (pitch, yaw)
        world,
        2,  // permission level
        senderName, MCLiteralText(senderName),
        world.server,
        null, // entity
    )
}

object ForgeGameAPI : GameAPI {
    private var usefulPotionIds: List<String> = emptyList()
    private var spawnEggIds: List<String> = emptyList()

    fun init() {
        usefulPotionIds = ForgeRegistries.POTION_TYPES.keys.filter {
            it.namespace == "minecraft" && it.path !in uselessPostionNames
        }.map { it.toString() }.toList()

        spawnEggIds = ForgeRegistries.ITEMS.keys.filter {
            it.namespace == "minecraft"
                && it.path.endsWith(spawnEggSuffix)
        }.map { it.toString() }.toList()
    }

    override fun getUsefulPotionIds(): List<String> = usefulPotionIds
    override fun getSpawnEggIds(): List<String> = spawnEggIds

    override fun logError(msg: String?, error: Exception?) {
        if (msg != null && error != null) ForgeLuckyRegistry.LOGGER.error(msg, error)
        else if (msg != null) ForgeLuckyRegistry.LOGGER.error(msg)
        else ForgeLuckyRegistry.LOGGER.error(error)
    }

    override fun logInfo(msg: String) {
        ForgeLuckyRegistry.LOGGER.info(msg)
    }

    override fun getEntityPos(entity: Entity): Vec3d {
        return Vec3d((entity as MCEntity).x, entity.y, entity.z)
    }

    override fun getPlayerName(player: PlayerEntity): String {
        return (player as MCPlayerEntity).name.string
    }

    override fun applyStatusEffect(entity: Entity, effectId: String, durationSeconds: Double, amplifier: Int) {
        val statusEffect = ForgeRegistries.POTIONS.getValue(MCIdentifier(effectId))
        if (statusEffect == null) {
            gameAPI.logError("Unknown status effect: $effectId")
            return
        }
        val duration = if (statusEffect.isInstantenous) 1 else (durationSeconds * 20.0).toInt()
        if (entity is LivingEntity) entity.addEffect(EffectInstance(statusEffect, duration, amplifier))
    }

    // compatibility only
    override fun convertStatusEffectId(effectId: Int): String? {
        val effect = Effect.byId(effectId)
        return effect?.let { ForgeRegistries.POTIONS.getKey(effect).toString() }
    }

    override fun getLivingEntitiesInBox(world: World, boxMin: Vec3d, boxMax: Vec3d): List<Entity> {
        val box = MCBox(toMCVec3d(boxMin), toMCVec3d(boxMax))
        return toServerWorld(world).getEntitiesOfClass(LivingEntity::class.java, box)
    }

    override fun setEntityOnFire(entity: Entity, durationSeconds: Int) {
        (entity as MCEntity).setSecondsOnFire(durationSeconds)
    }

    override fun setEntityMotion(entity: Entity, motion: Vec3d) {
        (entity as MCEntity).deltaMovement = toMCVec3d(motion)
        entity.hurtMarked = true
        entity.hasImpulse = true
    }

    override fun getWorldTime(world: World): Long {
        return toServerWorld(world).dayTime
    }

    override fun getPlayerHeadYawDeg(player: PlayerEntity): Double {
        return (player as MCPlayerEntity).yHeadRot.toDouble()
    }

    override fun getPlayerHeadPitchDeg(player: PlayerEntity): Double {
        return (player as MCPlayerEntity).xRot.toDouble()
    }

    override fun isAirBlock(world: World, pos: Vec3i): Boolean {
        return (world as MCIWorld).isEmptyBlock(toMCBlockPos(pos))
    }

    override fun spawnEntity(world: World, id: String, pos: Vec3d, nbt: DictAttr, rotation: Double, randomizeMob: Boolean, player: PlayerEntity?, sourceId: String) {
        val entityNBT = if (id == JavaLuckyRegistry.projectileId && "sourceId" !in nbt)
            nbt.with(mapOf("sourceId" to stringAttrOf(sourceId))) else nbt

        val mcEntityNBT = javaGameAPI.attrToNBT(nbt.with(mapOf("id" to stringAttrOf(id)))) as CompoundTag

        val serverWorld = toServerWorld(world)
        val entity = EntityType.loadEntityRecursive(mcEntityNBT, serverWorld) { entity ->
            val entityRotation = positiveMod(rotation + 2.0, 4.0) // entities face south by default
            val rotationDeg = (entityRotation * 90.0)
            val yaw = positiveMod(entity.yRot + entityRotation, 360.0)
            val velocity = if (entityRotation == 0.0) entity.deltaMovement
            else toMCVec3d(rotateVec3d(toVec3d(entity.deltaMovement), degToRad(rotationDeg)))

            entity.absMoveTo(pos.x, pos.y, pos.z, yaw.toFloat(), entity.xRot)
            entity.yHeadRot = yaw.toFloat()
            entity.deltaMovement = velocity
            if (serverWorld.addFreshEntity(entity)) entity else null
        } ?: return

        if (entity is FallingBlockEntity && "Time" !in entityNBT) entity.time = 1
        if (player != null && entity is ArrowEntity) entity.owner = player as MCEntity

        if (entity is MobEntity && randomizeMob && "Passengers" !in entityNBT) {
            entity.finalizeSpawn(
                serverWorld,
                serverWorld.getCurrentDifficultyAt(toMCBlockPos(pos.floor())),
                SpawnReason.EVENT,
                null, null
            )
            entity.readAdditionalSaveData(mcEntityNBT)
        }
    }

    override fun getNearestPlayer(world: World, pos: Vec3d): PlayerEntity? {
        val commandSource = createCommandSource(world as MCServerWorld, pos, showOutput = false)
        return EntitySelectorParser(StringReader("@p")).parse().findSinglePlayer(commandSource)
    }

    override fun scheduleDrop(drop: SingleDrop, context: DropContext, seconds: Double) {
        val world = toServerWorld(context.world)
        val delayedDrop = DelayedDrop(world = world, data = DelayedDropData(drop, context, (seconds * 20).toInt()))
        delayedDrop.setPos(context.pos.x, context.pos.y, context.pos.z)
        world.addFreshEntity(delayedDrop)
    }

    override fun setBlock(world: World, pos: Vec3i, blockId: String, state: DictAttr?, rotation: Int, notify: Boolean) {
        val blockStateNBT = javaGameAPI.attrToNBT(dictAttrOf(
            "Name" to stringAttrOf(blockId),
            "Properties" to state,
        )) as CompoundTag
        val mcBlockState = NBTUtil.readBlockState(blockStateNBT).rotate(world as MCIWorld, toMCBlockPos(pos), Rotation.values()[rotation])

        world.setBlock(toMCBlockPos(pos), mcBlockState, if (notify) 3 else 2)
    }

    override fun setBlockEntity(world: World, pos: Vec3i, nbt: DictAttr) {
        val mcPos = toMCBlockPos(pos)
        val blockEntity = (world as MCIServerWorld).getBlockEntity(mcPos)
        if (blockEntity != null) {
            val fullNBT = nbt.with(mapOf(
                "x" to intAttrOf(pos.x),
                "y" to intAttrOf(pos.y),
                "z" to intAttrOf(pos.z),
            ))
            blockEntity.load(world.getBlockState(mcPos), javaGameAPI.attrToNBT(fullNBT) as CompoundTag)
            blockEntity.setChanged()
            //if (world is MCWorld) world.setBlockEntity(mcPos, blockEntity)
        }
    }

    override fun dropItem(world: World, pos: Vec3d, itemId: String, nbt: DictAttr?) {
        val item = ForgeRegistries.ITEMS.getValue(MCIdentifier(itemId))
        if (item == null) {
            gameAPI.logError("Invalid item ID: '$itemId'")
            return
        }

        val itemStack = ItemStack(item, 1)
        if (nbt != null) itemStack.tag = javaGameAPI.attrToNBT(nbt) as CompoundTag
        MCBlock.popResource(toServerWorld(world), toMCBlockPos(pos.floor()), itemStack)
    }

    override fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean) {
        try {
            val commandSource = createCommandSource(toServerWorld(world), pos, senderName, showOutput)
            commandSource.server.commands.performCommand(commandSource, command)
        } catch (e: Exception) {
            gameAPI.logError("Invalid command: $command", e)
        }
    }

    override fun sendMessage(player: PlayerEntity, message: String) {
        (player as MCPlayerEntity).displayClientMessage(MCLiteralText(message), false)
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
        toServerWorld(world).dayTime = time
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
        val particleType = ForgeRegistries.PARTICLE_TYPES.getValue(MCIdentifier(id)) as ParticleType<IParticleData>?
        if (particleType == null) {
            gameAPI.logError("Invalid partical: $id")
            return
        }

        try {
            val particleData = try {
                particleType.deserializer.fromCommand(particleType, StringReader(" " + args.joinToString(" ")))
            } catch (e: CommandSyntaxException) {
                gameAPI.logError("Error processing partice '$id' with arguments '$args'", e)
                return
            }
            toServerWorld(world).sendParticles(
                particleData,
                pos.x, pos.y, pos.z,
                amount,
                boxSize.x, boxSize.y, boxSize.z,
                0.0 // spead
            )
        } catch (e: Exception) {
            gameAPI.logError("Invalid partical arguments: $args", e)
            return
        }
    }

    override fun playParticleEvent(world: World, pos: Vec3d, eventId: Int, data: Int) {
        toServerWorld(world).levelEvent(eventId, toMCBlockPos(pos.floor()), data)
    }

    override fun playSplashPotionEvent(world: World, pos: Vec3d, potionName: String?, potionColor: Int?) {
        if (potionName != null) {
            val potion = ForgeRegistries.POTION_TYPES.getValue(MCIdentifier(potionName))
            if (potion == null) {
                gameAPI.logError("Invalid splash potion name: $potionName")
                return
            }

            val color = PotionUtils.getColor(potion.effects)
            playParticleEvent(world, pos, if (potion.hasInstantEffects()) 2007 else 2002, color)
        } else if (potionColor != null) {
            playParticleEvent(world, pos, 2002, potionColor)
        }
    }

    override fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean) {
        toServerWorld(world).explode(null, pos.x, pos.y, pos.z, damage.toFloat(), fire, Explosion.Mode.DESTROY)
    }

    override fun createStructure(world: World, structureId: String, pos: Vec3i, centerOffset: Vec3i, rotation: Int, mode: String, notify: Boolean) {
        val nbtStructure = JavaLuckyRegistry.nbtStructures[structureId]
        val processor = object : StructureProcessor() {
            override fun process(
                world: IWorldReader,
                oldPos: MCBlockPos,
                newPos: MCBlockPos,
                oldBlockInfo: Template.BlockInfo,
                newBlockInfo: Template.BlockInfo,
                settings: PlacementSettings,
                template: Template?
            ): Template.BlockInfo {
                val blockId = javaGameAPI.getBlockId(newBlockInfo.state.block) ?: return newBlockInfo
                val blockIdWithMode = withBlockMode(mode, blockId)

                if (blockIdWithMode == blockId) return newBlockInfo

                val newState = if (blockIdWithMode == null) world.getBlockState(newBlockInfo.pos)
                    else ForgeRegistries.BLOCKS.getValue(MCIdentifier(blockIdWithMode))?.defaultBlockState()

                return if (newState == newBlockInfo.state) newBlockInfo
                    else Template.BlockInfo(newBlockInfo.pos, newState, newBlockInfo.nbt)
            }

            override fun getType(): IStructureProcessorType<*> {
                return IStructureProcessorType.BLOCK_IGNORE
            }
        }

        val mcRotation = Rotation.values()[rotation]
        val placementSettings: PlacementSettings = PlacementSettings()
            .setRotation(mcRotation)
            .setRotationPivot(toMCBlockPos(centerOffset))
            .setIgnoreEntities(false)
            .addProcessor(processor)

        val mcCornerPos = toMCBlockPos(pos - centerOffset)
        (nbtStructure as Template).placeInWorld(
            world as MCIServerWorld,
            mcCornerPos,
            mcCornerPos,
            placementSettings,
            RANDOM.asJavaRandom(),
            if (notify) 3 else 2
        )
    }
}