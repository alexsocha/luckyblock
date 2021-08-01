package mod.lucky.common

import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop

typealias Block = Any
typealias Item = Any
typealias PlayerEntity = Any
typealias Entity = Any
typealias World = Any

interface Logger {
    fun logError(msg: String? = null, error: Exception? = null)
    fun logInfo(msg: String)
}

interface GameAPI : Logger {
    fun getUsefulPotionIds(): List<String>
    fun getSpawnEggIds(): List<String>
    fun getEntityPos(entity: Entity): Vec3d
    fun getPlayerName(player: PlayerEntity): String
    fun applyStatusEffect(entity: Entity, effectId: String, durationSeconds: Double, amplifier: Int)
    fun convertStatusEffectId(effectId: Int): String? // compatibility
    fun getLivingEntitiesInBox(world: World, boxMin: Vec3d, boxMax: Vec3d): List<Entity>
    fun setEntityOnFire(entity: Entity, durationSeconds: Int)
    fun setEntityMotion(entity: Entity, motion: Vec3d)
    fun getWorldTime(world: World): Long
    fun getPlayerHeadYawDeg(player: PlayerEntity): Double
    fun getPlayerHeadPitchDeg(player: PlayerEntity): Double
    fun getNearestPlayer(world: World, pos: Vec3d): PlayerEntity?
    fun scheduleDrop(drop: SingleDrop, context: DropContext, seconds: Double)
    fun isAirBlock(world: World, pos: Vec3i): Boolean
    fun spawnEntity(world: World, id: String, pos: Vec3d, nbt: DictAttr, components: DictAttr, rotation: Double, randomizeMob: Boolean, player: PlayerEntity?, sourceId: String)
    fun setBlock(world: World, pos: Vec3i, blockId: String, state: DictAttr?, rotation: Int, notify: Boolean)
    fun setBlockEntity(world: World, pos: Vec3i, nbt: DictAttr)
    fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean)
    fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean)
    fun sendMessage(player: PlayerEntity, message: String)
    fun setDifficulty(world: World, difficulty: String)
    fun setTime(world: World, time: Long)
    fun dropItem(world: World, pos: Vec3d, itemId: String, nbt: DictAttr?)
    fun playSound(world: World, pos: Vec3d, id: String, volume: Double, pitch: Double)
    fun spawnParticle(world: World, pos: Vec3d, id: String, args: List<String>, boxSize: Vec3d, amount: Int)
    fun playParticleEvent(world: World, pos: Vec3d, eventId: Int, data: Int)
    fun playSplashPotionEvent(world: World, pos: Vec3d, potionName: String?, potionColor: Int?)
    fun createStructure(world: World, structureId: String, pos: Vec3i, centerOffset: Vec3i, rotation: Int, mode: String, notify: Boolean)
}

interface PlatformAPI {
    fun evalJS(script: String): Any
}

lateinit var logger: Logger
lateinit var gameAPI: GameAPI
lateinit var platformAPI: PlatformAPI