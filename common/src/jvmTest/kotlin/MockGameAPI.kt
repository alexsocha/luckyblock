import mod.lucky.common.*
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop

data class MockEntity(val pos: Vec3d)

object MockGameAPI : GameAPI {
    override fun getUsefulPotionIds(): List<String> = listOf("minecraft:healing")
    override fun getSpawnEggIds(): List<String> = listOf("minecraft:pig_spawn_egg")
    override fun getEntityPos(entity: Entity): Vec3d = (entity as MockEntity).pos
    override fun getPlayerName(player: PlayerEntity): String = "Test Player"
    override fun applyStatusEffect(entity: Entity, effectId: String, durationSeconds: Double, amplifier: Int) {}
    override fun convertStatusEffectId(effectId: Int): String? = null
    override fun getLivingEntitiesInBox(world: World, boxMin: Vec3d, boxMax: Vec3d): List<Entity> = emptyList()
    override fun setEntityOnFire(entity: Entity, durationSeconds: Int) {}
    override fun setEntityMotion(entity: Entity, motion: Vec3d) {}
    override fun getWorldTime(world: World): Long = 0
    override fun getPlayerHeadYawDeg(player: PlayerEntity): Double = 0.0
    override fun getPlayerHeadPitchDeg(player: PlayerEntity): Double = 0.0
    override fun getNearestPlayer(world: World, pos: Vec3d): PlayerEntity? = null
    override fun scheduleDrop(drop: SingleDrop, context: DropContext, seconds: Double) {}
    override fun isAirBlock(world: World, pos: Vec3i): Boolean = true

    override fun setBlock(world: World, pos: Vec3i, blockId: String, state: DictAttr?, rotation: Int, notify: Boolean) {}
    override fun spawnEntity(
        world: World,
        id: String,
        pos: Vec3d,
        nbt: DictAttr,
        rotation: Double,
        randomizeMob: Boolean,
        player: PlayerEntity?,
        sourceId: String
    ) {}
    override fun setBlockEntity(world: World, pos: Vec3i, nbt: DictAttr) {}
    override fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean) {}
    override fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean) {}
    override fun sendMessage(player: PlayerEntity, message: String) {}
    override fun setDifficulty(world: World, difficulty: String) {}

    override fun setTime(world: World, time: Long) {}
    override fun dropItem(world: World, pos: Vec3d, itemId: String, nbt: DictAttr?) {}
    override fun playSound(world: World, pos: Vec3d, id: String, volume: Double, pitch: Double) {}
    override fun spawnParticle(world: World, pos: Vec3d, id: String, args: List<String>, boxSize: Vec3d, amount: Int) {}
    override fun playParticleEvent(world: World, pos: Vec3d, eventId: Int, data: Int) {}
    override fun playSplashPotionEvent(world: World, pos: Vec3d, potionName: String?, potionColor: Int?) {}
    override fun createStructure(
        world: World,
        structureId: String,
        pos: Vec3i,
        centerOffset: Vec3i,
        rotation: Int,
        mode: String,
        notify: Boolean,
    ) {}
}