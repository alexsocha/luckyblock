package mod.lucky.neoforge

//import mod.lucky.neoforge.game.DelayedDrop
import com.mojang.brigadier.StringReader
import mod.lucky.common.*
import mod.lucky.common.Entity
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.action.withBlockMode
import mod.lucky.java.*
import mod.lucky.java.game.*
import mod.lucky.neoforge.game.DelayedDrop
import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.ParticleArgument
import net.minecraft.commands.arguments.selector.EntitySelectorParser
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.NbtUtils
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.util.ProblemReporter.ScopedCollector
import net.minecraft.util.RandomSource
import net.minecraft.world.*
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.*
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.level.Level.ExplosionInteraction
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.storage.TagValueInput
import java.awt.Color
import kotlin.jvm.optionals.getOrNull

typealias MCIdentifier = net.minecraft.resources.ResourceLocation
typealias MCEnchantment = net.minecraft.world.item.enchantment.Enchantment
typealias MCBlock = net.minecraft.world.level.block.Block
typealias MCItem = net.minecraft.world.item.Item
typealias MCIWorld = net.minecraft.world.level.LevelAccessor
typealias MCIServerWorld = net.minecraft.world.level.ServerLevelAccessor
typealias MCWorld = net.minecraft.world.level.Level
typealias MCServerWorld = net.minecraft.server.level.ServerLevel
typealias MCEntity = net.minecraft.world.entity.Entity
typealias MCPlayerEntity = net.minecraft.world.entity.player.Player
typealias MCVec3d = net.minecraft.world.phys.Vec3
typealias MCVec3i = net.minecraft.core.Vec3i
typealias MCVec2f = net.minecraft.world.phys.Vec2
typealias MCBlockPos = net.minecraft.core.BlockPos
typealias MCBox = net.minecraft.world.phys.AABB
typealias MCItemStack = net.minecraft.world.item.ItemStack

typealias MCEnchantmentType = net.minecraft.world.item.enchantment.Enchantments
typealias MCStatusEffect = net.minecraft.world.effect.MobEffect

typealias MCChatComponent = net.minecraft.network.chat.Component
typealias MCChatFormatting = net.minecraft.ChatFormatting

typealias Tag = net.minecraft.nbt.Tag
typealias ByteTag = net.minecraft.nbt.ByteTag
typealias ShortTag = net.minecraft.nbt.ShortTag
typealias IntTag = net.minecraft.nbt.IntTag
typealias FloatTag = net.minecraft.nbt.FloatTag
typealias DoubleTag = net.minecraft.nbt.DoubleTag
typealias LongTag = net.minecraft.nbt.LongTag
typealias StringTag = net.minecraft.nbt.StringTag
typealias ByteArrayTag = net.minecraft.nbt.ByteArrayTag
typealias IntArrayTag = net.minecraft.nbt.IntArrayTag
typealias ListTag = net.minecraft.nbt.ListTag
typealias CompoundTag = net.minecraft.nbt.CompoundTag

fun toMCVec3d(vec: Vec3d): MCVec3d = MCVec3d(vec.x, vec.y, vec.z)
fun toMCBlockPos(vec: Vec3i): MCBlockPos = MCBlockPos(vec.x, vec.y, vec.z)

fun toVec3i(vec: MCVec3i): Vec3i = Vec3i(vec.x, vec.y, vec.z)
fun toVec3d(vec: MCVec3d): Vec3d = Vec3d(vec.x, vec.y, vec.z)

fun toServerWorld(world: World): MCServerWorld {
    return (world as MCServerWorld).level
}

private fun createCommandSource(
    world: MCServerWorld,
    pos: Vec3d,
    senderName: String = "Lucky Block",
    showOutput: Boolean,
): CommandSourceStack {
    val commandOutput = object : CommandSource {
        override fun sendSystemMessage(message: MCChatComponent) {}
        override fun acceptsSuccess(): Boolean = showOutput
        override fun acceptsFailure(): Boolean = showOutput
        override fun shouldInformAdmins(): Boolean = showOutput
    }

    return CommandSourceStack(
        commandOutput,
        toMCVec3d(pos),
        MCVec2f.ZERO, // (pitch, yaw)
        world,
        2,  // permission level
        senderName, MCChatComponent.literal(senderName),
        world.server,
        null, // entity
    )
}

object ForgeGameAPI : GameAPI {
    private var usefulPotionIds: List<String> = emptyList()
    private var spawnEggIds: List<String> = emptyList()
    private var enchantments: List<Enchantment> = ENCHANTMENTS
    private var usefulStatusEffects: List<StatusEffect> = emptyList()

    fun init() {
        usefulPotionIds = BuiltInRegistries.POTION.keySet().stream().filter {
            it.namespace == "minecraft" && it.path !in uselessPostionNames
        }.map { it.toString() }.toList()

        spawnEggIds = BuiltInRegistries.ITEM.keySet().stream().filter {
            it.namespace == "minecraft"
                && it.path.endsWith(spawnEggSuffix)
        }.map { it.toString() }.toList()

        usefulStatusEffects = usefulStatusEffectIds.map {
            val mcId = MCIdentifier.parse(it)
            val mcStatusEffect = BuiltInRegistries.MOB_EFFECT.getOptional(mcId).get()
            StatusEffect(
                id = mcId.toString(),
                isNegative = mcStatusEffect.category == MobEffectCategory.HARMFUL,
                isInstant = mcStatusEffect.isInstantenous,
            )
        }
    }

    override fun logError(msg: String?, error: Exception?) {
        if (msg != null && error != null) ForgeLuckyRegistry.LOGGER.error(msg, error)
        else if (msg != null) ForgeLuckyRegistry.LOGGER.error(msg)
        else ForgeLuckyRegistry.LOGGER.error(error.toString())
    }

    override fun logInfo(msg: String) {
        ForgeLuckyRegistry.LOGGER.info(msg)
    }

    override fun getUsefulPotionIds(): List<String> = usefulPotionIds
    override fun getSpawnEggIds(): List<String> = spawnEggIds
    override fun getEnchantments(): List<Enchantment> = enchantments
    override fun getUsefulStatusEffects(): List<StatusEffect> = usefulStatusEffects

    override fun getRGBPalette(): List<Int> {
        return DyeColor.values().map {
            Color(it.textureDiffuseColor).rgb
        }
    }

    override fun getEntityPos(entity: Entity): Vec3d {
        return Vec3d((entity as MCEntity).x, entity.y, entity.z)
    }

    override fun getPlayerName(player: PlayerEntity): String {
        return (player as MCPlayerEntity).name.string
    }

    override fun applyStatusEffect(target: String?, targetEntity: Entity?, effectId: String, durationSeconds: Double, amplifier: Int) {
        val statusEffectHolder = BuiltInRegistries.MOB_EFFECT.get(MCIdentifier.parse(effectId)).getOrNull()
        if (statusEffectHolder === null) {
            GAME_API.logError("Unknown status effect: $effectId")
            return
        }
        val statusEffect = statusEffectHolder.value()
        val duration = if (statusEffect.isInstantenous) 1 else (durationSeconds * 20.0).toInt()
        if (targetEntity is LivingEntity) targetEntity.addEffect(MobEffectInstance(statusEffectHolder, duration, amplifier))
    }

    override fun getLivingEntitiesInBox(world: World, boxMin: Vec3d, boxMax: Vec3d): List<Entity> {
        val box = MCBox(toMCVec3d(boxMin), toMCVec3d(boxMax))
        return toServerWorld(world).getEntitiesOfClass(LivingEntity::class.java, box)
    }

    override fun setEntityOnFire(entity: Entity, durationSeconds: Int) {
        (entity as MCEntity).igniteForSeconds(durationSeconds.toFloat())
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

    override fun spawnEntity(world: World, id: String, pos: Vec3d, nbt: DictAttr, components: DictAttr?, rotation: Double, randomizeMob: Boolean, player: PlayerEntity?, sourceId: String) {
        val entityNBT = if (id == JavaLuckyRegistry.projectileId && "sourceId" !in nbt)
            nbt.with(mapOf("sourceId" to stringAttrOf(sourceId))) else nbt

        val mcEntityNBT = JAVA_GAME_API.attrToNBT(nbt.with(mapOf("id" to stringAttrOf(id)))) as CompoundTag
        val sourceItem = BuiltInRegistries.ITEM.getOptional(MCIdentifier.parse(sourceId))

        val serverWorld = toServerWorld(world)
        val entity = EntityType.loadEntityRecursive(mcEntityNBT, serverWorld, EntitySpawnReason.EVENT) { entity ->
            val entityRotation = positiveMod(rotation + 2.0, 4.0) // entities face south by default
            val rotationDeg = (entityRotation * 90.0)
            val yaw = positiveMod(entity.yRot + entityRotation, 360.0)
            val velocity = if (entityRotation == 0.0) entity.deltaMovement
            else toMCVec3d(rotateVec3d(toVec3d(entity.deltaMovement), degToRad(rotationDeg)))

            entity.absSnapTo(pos.x, pos.y, pos.z, yaw.toFloat(), entity.xRot)
            entity.yHeadRot = yaw.toFloat()
            entity.deltaMovement = velocity

            if (sourceItem.isPresent && sourceItem.get() is BowItem) {
                val d0 = velocity.horizontalDistance()
                entity.yRot = ((Mth.atan2(velocity.x, velocity.z) * 180.0 / Math.PI.toFloat().toDouble()).toFloat())
                entity.xRot = ((Mth.atan2(velocity.y, d0) * 180.0 / Math.PI.toFloat().toDouble()).toFloat())
                entity.yRotO = entity.yRot
                entity.xRotO = entity.xRot
            }

            if (serverWorld.addFreshEntity(entity)) entity else null
        } ?: return

        if (entity is FallingBlockEntity && "Time" !in entityNBT) entity.time = 1
        if (player != null && entity is Arrow) entity.owner = player as MCEntity

        if (entity is Mob && randomizeMob && "Passengers" !in entityNBT) {
            entity.finalizeSpawn(
                serverWorld,
                serverWorld.getCurrentDifficultyAt(toMCBlockPos(pos.floor())),
                EntitySpawnReason.EVENT,
                null
            )
        }
    }

    override fun getNearestPlayer(world: World, pos: Vec3d): PlayerEntity? {
        val commandSource = createCommandSource(world as MCServerWorld, pos, showOutput = false)
        return EntitySelectorParser(StringReader("@p"), true).parse().findSinglePlayer(commandSource)
    }

    override fun scheduleDrop(drop: SingleDrop, context: DropContext, seconds: Double) {
        val world = toServerWorld(context.world)
        val delayedDrop = DelayedDrop(world = world, data = DelayedDropData(
            singleDrop = drop,
            singleDropString = drop.propsString,
            context=context,
            ticksRemaining = (seconds * 20).toInt())
        )
        delayedDrop.setPos(context.pos.x, context.pos.y, context.pos.z)
        world.addFreshEntity(delayedDrop)
    }

    override fun setBlock(world: World, pos: Vec3i, id: String, state: DictAttr?, components: DictAttr?, rotation: Int, notify: Boolean) {
        val blockStateNBT = JAVA_GAME_API.attrToNBT(dictAttrOf(
            "Name" to stringAttrOf(id),
            "Properties" to state,
        )) as CompoundTag

        val mcBlockState = NbtUtils
            .readBlockState((world as MCIWorld).holderLookup(Registries.BLOCK), blockStateNBT)
            .rotate(world, toMCBlockPos(pos), Rotation.values()[rotation])

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
            val tag = JAVA_GAME_API.attrToNBT(fullNBT) as CompoundTag
            ScopedCollector(ForgeLuckyRegistry.LOGGER).use {
                val input = TagValueInput.create(it, world.registryAccess(), tag)
                blockEntity.loadWithComponents(input)
            }
            blockEntity.setChanged()
        }
    }

    override fun dropItem(world: World, pos: Vec3d, id: String, nbt: DictAttr?, components: DictAttr?) {
        val itemKey = MCIdentifier.parse(id)
        if (!BuiltInRegistries.ITEM.containsKey(itemKey)) {
            GAME_API.logError("Invalid item ID: '$id'")
            return
        }

        val item = BuiltInRegistries.ITEM.getOptional(itemKey).get()
        var itemStack = MCItemStack(item, 1)
        val componentsAttr = nbt ?: components;
        if (componentsAttr != null) {
            val tag = JAVA_GAME_API.attrToNBT(componentsAttr) as CompoundTag
            val parsedComponents = nbtToComponents(tag, (world as MCWorld).registryAccess())
            itemStack.applyComponents(parsedComponents)
        }

        MCBlock.popResource(toServerWorld(world), toMCBlockPos(pos.floor()), itemStack)
    }

    override fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean) {
        try {
            val commandSource = createCommandSource(toServerWorld(world), pos, senderName, showOutput)
            val commandWithoutPrefix = command.substring(1) // remove the slash (/)
            val parsedCommand = commandSource.server.commands.dispatcher.parse(commandWithoutPrefix, commandSource)
            commandSource.server.commands.performCommand(parsedCommand, commandWithoutPrefix)
        } catch (e: Exception) {
            GAME_API.logError("Invalid command: $command", e)
        }
    }

    override fun sendMessage(player: PlayerEntity, message: String) {
        (player as MCPlayerEntity).displayClientMessage(MCChatComponent.literal(message), false)
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
        val soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(MCIdentifier.parse(id)).getOrNull()
        if (soundEvent == null) {
            GAME_API.logError("Invalid sound event: $id")
            return
        }
        toServerWorld(world).playSound(
            null, // player to exclude
            pos.x, pos.y, pos.z,
            soundEvent,
            SoundSource.BLOCKS,
            volume.toFloat(), pitch.toFloat(),
        )
    }

    override fun spawnParticle(world: World, pos: Vec3d, id: String, args: List<String>, boxSize: Vec3d, amount: Int) {
        try {
            val particleData = ParticleArgument.readParticle(
                StringReader(id + " " + args.joinToString(" ")),
                (world as MCWorld).registryAccess()
            )
            toServerWorld(world).sendParticles(
                particleData,
                pos.x, pos.y, pos.z,
                amount,
                boxSize.x, boxSize.y, boxSize.z,
                0.0 // spread
            )
        } catch (e: Exception) {
            GAME_API.logError("Error processing particle '$id' with arguments '$args'", e)
            return
        }
    }

    override fun playParticleEvent(world: World, pos: Vec3d, eventId: Int, data: Int) {
        toServerWorld(world).levelEvent(eventId, toMCBlockPos(pos.floor()), data)
    }

    override fun playSplashPotionEvent(world: World, pos: Vec3d, potionName: String?, potionColor: Int?) {
        if (potionName != null) {
            val potion = BuiltInRegistries.POTION.getOptional(MCIdentifier.parse(potionName)).getOrNull()
            if (potion == null) {
                GAME_API.logError("Invalid splash potion name: $potionName")
                return
            }

            val color = PotionContents.getColorOptional(potion.effects).asInt
            playParticleEvent(world, pos, if (potion.hasInstantEffects()) 2007 else 2002, color)
        } else if (potionColor != null) {
            playParticleEvent(world, pos, 2002, potionColor)
        }
    }

    override fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean) {
        toServerWorld(world).explode(null, pos.x, pos.y, pos.z, damage.toFloat(), fire, ExplosionInteraction.BLOCK)
    }

    override fun createStructure(world: World, structureId: String, pos: Vec3i, centerOffset: Vec3i, rotation: Int, mode: String, notify: Boolean) {
        val nbtStructure = JavaLuckyRegistry.nbtStructures[structureId]
        if (nbtStructure == null) {
            GAME_API.logError("Missing structure '$structureId'")
            return
        }

        val processor = object : StructureProcessor() {
            override fun process(
                world: LevelReader,
                oldPos: MCBlockPos,
                newPos: MCBlockPos,
                oldBlockInfo: StructureTemplate.StructureBlockInfo,
                newBlockInfo: StructureTemplate.StructureBlockInfo,
                settings: StructurePlaceSettings,
                template: StructureTemplate?
            ): StructureTemplate.StructureBlockInfo {
                val blockId = JAVA_GAME_API.getBlockId(newBlockInfo.state.block) ?: return newBlockInfo
                val blockIdWithMode = withBlockMode(mode, blockId)

                if (blockIdWithMode == blockId) return newBlockInfo

                val newState = if (blockIdWithMode == null) world.getBlockState(newBlockInfo.pos)
                    else BuiltInRegistries.BLOCK.getOptional(MCIdentifier.parse(blockIdWithMode)).getOrNull()?.defaultBlockState()!!

                return if (newState.equals(newBlockInfo.state)) newBlockInfo
                    else StructureTemplate.StructureBlockInfo(newBlockInfo.pos, newState, newBlockInfo.nbt)
            }

            override fun getType(): StructureProcessorType<*> {
                return StructureProcessorType.BLOCK_IGNORE
            }
        }

        val mcRotation = Rotation.values()[rotation]
        val placementSettings: StructurePlaceSettings = StructurePlaceSettings()
            .setRotation(mcRotation)
            .setRotationPivot(toMCBlockPos(centerOffset))
            .setIgnoreEntities(false)
            .addProcessor(processor)

        val mcCornerPos = toMCBlockPos(pos - centerOffset)
        (nbtStructure as StructureTemplate).placeInWorld(
            world as MCIServerWorld,
            mcCornerPos,
            mcCornerPos,
            placementSettings,
            RandomSource.create(),
            if (notify) 3 else 2
        )
    }
}
