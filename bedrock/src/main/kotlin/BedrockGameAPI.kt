package mod.lucky.bedrock

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.attribute.attrToJsonStr
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.dropsFromStrList

data class MockEntity(val pos: Vec3d)

fun toBlockPos(mcBlockPos: MCBlockPos): BlockPos {
    return BlockPos(mcBlockPos.x, mcBlockPos.y, mcBlockPos.z)
}

data class DropContainer(
    val luck: Int,
    val drops: List<WeightedDrop>,
)

class UnparsedDropContainer(
    val luck: Int,
    val drops: Array<String>,
)

fun parseDropContainer(unparsedContainer: UnparsedDropContainer): DropContainer {
    return DropContainer(
        drops = dropsFromStrList(unparsedContainer.drops.toList()),
        luck = unparsedContainer.luck
    )
}

fun posToString(pos: BlockPos): String {
    return "${pos.x},${pos.y},${pos.z}"
}

@Suppress("UNCHECKED_CAST")
fun <T> deepMerge(dst: T, src: T): T {
    fun <K, V> deepMergeMaps(dstMap: MutableMap<K, V>, srcMap: Map<*, *>): Map<K, V> {
        for (k in srcMap.keys) {
            dstMap[k as K] = deepMerge(dstMap[k], srcMap[k]) as V
        }
        return dstMap
    }

    if (dst is MutableMap<*, *> && src is Map<*, *>) {
        return deepMergeMaps(dst, src) as T
    }
    return dst
}

fun attrToJson(attr: Attr): Any {
    return when (attr) {
        is DictAttr -> {
            val jsDict: dynamic = object{}
            attr.children.forEach { (k, v) -> jsDict[k] = attrToJson(v) }
            jsDict as Any
        }
        is ListAttr -> attr.children.map { attrToJson(it) }
        is ValueAttr -> attr.value
        else -> throw Exception()
    }
}

object BedrockGameAPI : GameAPI {
    lateinit var server: MCServer
    lateinit var serverSystem: MCServerSystem
    private lateinit var spacialQuery: MCQuery
    private lateinit var luckyBlockEntityQuery: MCQuery

    fun initServer(server: MCServer, serverSystem: MCServerSystem) {
        BedrockGameAPI.serverSystem = serverSystem
        BedrockGameAPI.server = server

        spacialQuery = serverSystem.registerQuery("minecraft:position", "x", "y", "z")

        luckyBlockEntityQuery = serverSystem.registerQuery("minecraft:position", "x", "y", "z")
        serverSystem.addFilterToQuery(luckyBlockEntityQuery, "lucky:lucky_block_entity_data")
    }

    fun readAndDestroyLuckyBlockEntity(world: MCWorld, pos: BlockPos): DropContainer? {
        serverSystem.log(server)
        serverSystem.log(server.level)
        if (serverSystem.hasComponent(server.level, "lucky:all_lucky_block_entity_data")) {
            val allBlockData = serverSystem.getComponent<MutableMap<String, DropContainer>>(server.level, "lucky:all_lucky_block_entity_data")!!.data
            serverSystem.log(allBlockData)

            val posString = posToString(pos)
            val blockData = allBlockData[posString]
            if (blockData != null) {
                allBlockData.remove(posString)
            }
            return blockData

        } else {
            return null
        }

        /*
        BedrockGameAPI.logInfo("Running query 5...")
        val blockEntities = systemServer.getEntitiesFromQuery(luckyBlockEntityQuery, pos.x, pos.x + 1, pos.y, pos.y + 1, pos.z, pos.z + 1)
        BedrockGameAPI.logInfo("Finished query")

        val dropContainer = blockEntities.getOrNull(0)?.let {
            val unparsedDropContainer = systemServer.getComponent<UnparsedDropContainer?>(it, "lucky:lucky_block_entity_data").data
            unparsedDropContainer?.let { c -> parseDropContainer(c) }
        }

        blockEntities.forEach { systemServer.destroyEntity(it) }
         */
    }

    override fun logError(msg: String?, error: Exception?) {
        if (msg != null) serverSystem.log("Lucky Block Error: $msg")
        if (error != null) {
            serverSystem.log("Lucky Block Error: ${error.message}: ${error.stackTraceToString()}")
        }
    }

    override fun logInfo(msg: String) {
        serverSystem.log(msg)
    }

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

    override fun setBlock(world: World, pos: Vec3i, blockId: String, state: DictAttr?, rotation: Int, notify: Boolean) {
        serverSystem.log("setting block!")
    }

    override fun spawnEntity(
        world: World,
        id: String,
        pos: Vec3d,
        nbt: DictAttr,
        components: DictAttr,
        rotation: Double,
        randomizeMob: Boolean,
        player: PlayerEntity?,
        sourceId: String,
    ) {
        val entity = serverSystem.createEntity("entity", id)
        val posComponent = serverSystem.getComponent<MCVecPos>(entity, "minecraft:position")!!
        posComponent.data.x = pos.x
        posComponent.data.y = pos.y
        posComponent.data.z = pos.z
        serverSystem.applyComponentChanges(entity, posComponent)

        components.children.forEach {
            val component = serverSystem.getComponent<Any>(entity, it.key)
            if (component != null) {
                component.data = attrToJson(it.value)
                serverSystem.log(component.data)
                serverSystem.applyComponentChanges(entity, component)
            } else {
                logError("Invalid entity component: ${it.key}")
            }
        }
    }

    override fun setBlockEntity(world: World, pos: Vec3i, nbt: DictAttr) {}
    override fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean) {}
    override fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean) {}
    override fun sendMessage(player: PlayerEntity, message: String) {}
    override fun setDifficulty(world: World, difficulty: String) {}

    override fun setTime(world: World, time: Long) {}
    override fun dropItem(world: World, pos: Vec3d, itemId: String, nbt: DictAttr?) {
        serverSystem.log("dropping item: ", itemId)
    }
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