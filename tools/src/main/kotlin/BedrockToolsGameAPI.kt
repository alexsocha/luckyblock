package mod.lucky.tools

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.*
import mod.lucky.java.*
import java.io.File
import java.io.InputStream

class NoGameContextException : Exception("This API feature cannot be used without any game context")

object ToolsLogger : Logger {
    override fun logError(msg: String?, error: Exception?) {
        if (msg != null) println(error)
        if (error != null) println(error)
    }

    override fun logInfo(msg: String) {
        println(msg)
    }
}

object BedrockToolsGameAPI : GameAPI {
    override fun logError(msg: String?, error: Exception?) = ToolsLogger.logError(msg, error)
    override fun logInfo(msg: String) = ToolsLogger.logInfo(msg)

    override fun getRGBPalette(): List<Int> {
        return mod.lucky.bedrock.common.getRGBPalette()
    }

    override fun getEnchantments(types: List<EnchantmentType>): List<Enchantment> {
        return mod.lucky.bedrock.common.getEnchantments(types)
    }

    override fun getStatusEffect(id: String): StatusEffect? { 
        return mod.lucky.bedrock.common.getStatusEffect(id)
    }

    override fun getUsefulPotionIds(): List<String> = throw NoGameContextException()
    override fun getSpawnEggIds(): List<String> = throw NoGameContextException()
    override fun getEntityPos(entity: Entity): Vec3d = throw NoGameContextException()
    override fun getPlayerName(player: PlayerEntity): String = throw NoGameContextException()
    override fun applyStatusEffect(entity: Entity, effectId: String, durationSeconds: Double, amplifier: Int) = throw NoGameContextException()
    override fun convertStatusEffectId(effectId: Int): String? = throw NoGameContextException()
    override fun getLivingEntitiesInBox(world: World, boxMin: Vec3d, boxMax: Vec3d): List<Entity> = throw NoGameContextException()
    override fun setEntityOnFire(entity: Entity, durationSeconds: Int) = throw NoGameContextException()
    override fun setEntityMotion(entity: Entity, motion: Vec3d) = throw NoGameContextException()
    override fun getWorldTime(world: World): Long = throw NoGameContextException()
    override fun getPlayerHeadYawDeg(player: PlayerEntity): Double = throw NoGameContextException()
    override fun getPlayerHeadPitchDeg(player: PlayerEntity): Double = throw NoGameContextException()
    override fun getNearestPlayer(world: World, pos: Vec3d): PlayerEntity? = throw NoGameContextException()
    override fun scheduleDrop(drop: SingleDrop, context: DropContext, seconds: Double) = throw NoGameContextException()
    override fun isAirBlock(world: World, pos: Vec3i): Boolean = throw NoGameContextException()
    override fun spawnEntity(world: World, id: String, pos: Vec3d, nbt: DictAttr, components: DictAttr?, rotation: Double, randomizeMob: Boolean, player: PlayerEntity?, sourceId: String) = throw NoGameContextException()
    override fun setBlock(world: World, pos: Vec3i, id: String, state: DictAttr?, components: DictAttr?, rotation: Int, notify: Boolean) = throw NoGameContextException()
    override fun setBlockEntity(world: World, pos: Vec3i, nbt: DictAttr) = throw NoGameContextException()
    override fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean) = throw NoGameContextException()
    override fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean) = throw NoGameContextException()
    override fun sendMessage(player: PlayerEntity, message: String) = throw NoGameContextException()
    override fun setDifficulty(world: World, difficulty: String) = throw NoGameContextException()
    override fun setTime(world: World, time: Long) = throw NoGameContextException()
    override fun dropItem(world: World, pos: Vec3d, id: String, nbt: DictAttr?, components: DictAttr?) = throw NoGameContextException()
    override fun playSound(world: World, pos: Vec3d, id: String, volume: Double, pitch: Double) = throw NoGameContextException()
    override fun spawnParticle(world: World, pos: Vec3d, id: String, args: List<String>, boxSize: Vec3d, amount: Int) = throw NoGameContextException()
    override fun playParticleEvent(world: World, pos: Vec3d, eventId: Int, data: Int) = throw NoGameContextException()
    override fun playSplashPotionEvent(world: World, pos: Vec3d, potionName: String?, potionColor: Int?) = throw NoGameContextException()
    override fun createStructure(world: World, structureId: String, pos: Vec3i, centerOffset: Vec3i, rotation: Int, mode: String, notify: Boolean) = throw NoGameContextException()
}

object ToolsJavaGameAPI : JavaGameAPI {
    override fun getLoaderName(): String = throw NoGameContextException()
    override fun getModVersion() = throw NoGameContextException()
    override fun getMinecraftVersion() = throw NoGameContextException()
    override fun getGameDir() = throw NoGameContextException()

    override fun attrToNBT(attr: Attr): NBTTag = throw NoGameContextException()
    override fun nbtToAttr(tag: NBTTag): Attr = throw NoGameContextException()
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

    override fun getEntityTypeId(entity: Entity): String? = throw NoGameContextException()
    override fun isCreativeMode(player: PlayerEntity): Boolean = throw NoGameContextException()
    override fun hasSilkTouch(player: PlayerEntity): Boolean = throw NoGameContextException()
    override fun convertLegacyItemId(id: Int, data: Int): String? = throw NoGameContextException()
    override fun readNBTStructure(stream: InputStream): Pair<NBTStructure, Vec3i> = throw NoGameContextException()
}
