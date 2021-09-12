package mod.lucky.bedrock

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.attribute.attrToJsonStr
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.dropsFromStrList

data class MockEntity(val pos: Vec3d)

fun toBlockPos(mcPos: MCBlockPos): BlockPos {
    return BlockPos(mcPos.x, mcPos.y, mcPos.z)
}

fun toMCBlockPos(pos: BlockPos): MCBlockPos {
    val mcPos: dynamic = object{}
    mcPos["x"] = pos.x
    mcPos["y"] = pos.y
    mcPos["z"] = pos.z
    return mcPos
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

fun getIDWithNamespace(id: String): String {
    return if (":" in id) id else "minecraft:$id"
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

fun runCommand(serverSystem: MCServerSystem, command: String) {
    serverSystem.executeCommand(command) { result ->
        if (result.data.statusCode < 0) {
            BedrockGameAPI.logError(result.data.statusMessage)
        }
    }
}

object BedrockGameAPI : GameAPI {
    lateinit var server: MCServer
    lateinit var serverSystem: MCServerSystem
    private lateinit var spacialQuery: MCQuery
    private lateinit var luckyBlockEntityQuery: MCQuery

    fun initServer(server: MCServer, serverSystem: MCServerSystem) {
        BedrockGameAPI.server = server
        BedrockGameAPI.serverSystem = serverSystem

        spacialQuery = serverSystem.registerQuery("minecraft:position", "x", "y", "z")

        luckyBlockEntityQuery = serverSystem.registerQuery("minecraft:position", "x", "y", "z")
        serverSystem.addFilterToQuery(luckyBlockEntityQuery, "lucky:lucky_block_entity_data")
    }

    fun readAndDestroyLuckyBlockEntity(world: MCWorld, pos: BlockPos): DropContainer? {
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

    override fun getRGBPalette(): List<Int> {
        return mod.lucky.bedrock.common.getRGBPalette()
    }

    override fun getEnchantments(): List<Enchantment> {
        return mod.lucky.bedrock.common.getEnchantments()
    }
    override fun getUsefulStatusEffects(): List<StatusEffect> {
        return mod.lucky.bedrock.common.getUsefulStatusEffects()
    }

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

    override fun setBlock(world: World, pos: Vec3i, id: String, state: DictAttr?, components: DictAttr?, rotation: Int, notify: Boolean) {
        val blockStatesStr = state?.let {
            "[${it.children.entries.joinToString(",") {
                (k, v) -> "${k}:${attrToJson(v)}"
            }}]"
        }

        runCommand(serverSystem, "/setblock " +
            "${pos.x} ${pos.y} ${pos.z} " +
            "${getIDWithNamespace(id)} " +
            if (blockStatesStr != null) blockStatesStr else ""
        )

        if (components != null) {
            val block = serverSystem.getBlock((world as MCWorld).ticking_area, toMCBlockPos(pos))
            components.children.forEach {
                val component = serverSystem.getComponent<Any>(block, it.key)
                if (component != null) {
                    component.data = attrToJson(it.value)
                    serverSystem.applyComponentChanges(block, component)
                } else {
                    logError("Invalid block component: ${it.key}")
                }
            }
        }
    }

    override fun spawnEntity(
        world: World,
        id: String,
        pos: Vec3d,
        nbt: DictAttr,
        components: DictAttr?,
        rotation: Double,
        randomizeMob: Boolean,
        player: PlayerEntity?,
        sourceId: String,
    ) {
        logInfo("spawning entity!!!!")
        val entity = serverSystem.createEntity("entity", getIDWithNamespace(id))
        val posComponent = serverSystem.getComponent<MCVecPos>(entity, "minecraft:position")!!
        posComponent.data.x = pos.x
        posComponent.data.y = pos.y
        posComponent.data.z = pos.z
        serverSystem.applyComponentChanges(entity, posComponent)

        components?.children?.forEach {
            val component = serverSystem.getComponent<Any>(entity, it.key)
            if (component != null) {
                component.data = attrToJson(it.value)
                serverSystem.applyComponentChanges(entity, component)
            } else {
                logError("Invalid entity component: ${it.key}")
            }
        }
    }

    override fun setBlockEntity(world: World, pos: Vec3i, nbt: DictAttr) {}

    override fun runCommand(world: World, pos: Vec3d, command: String, senderName: String, showOutput: Boolean) {
        serverSystem.executeCommand(command) { result ->
            if (result.data.statusCode < 0) {
                BedrockGameAPI.logError(result.data.statusMessage)
            } else if (showOutput) {
                BedrockGameAPI.logInfo(result.data.statusMessage)
            }
        }
    }
    override fun createExplosion(world: World, pos: Vec3d, damage: Double, fire: Boolean) {}
    override fun sendMessage(player: PlayerEntity, message: String) {}
    override fun setDifficulty(world: World, difficulty: String) {}

    override fun setTime(world: World, time: Long) {}
    override fun dropItem(world: World, pos: Vec3d, id: String, nbt: DictAttr?, components: DictAttr?) {
        //serverSystem.log("dropping item...")

        val itemEntity = serverSystem.createEntity("item_entity", getIDWithNamespace(id))
        val posComponent = serverSystem.getComponent<MCVecPos>(itemEntity, "minecraft:position")!!
        posComponent.data.x = pos.x + (defaultRandom.randDouble(0.0, 1.0) - 0.5)
        posComponent.data.y = pos.y + 0.5
        posComponent.data.z = pos.z + (defaultRandom.randDouble(0.0, 1.0) - 0.5)
        serverSystem.applyComponentChanges(itemEntity, posComponent)
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
    ) {
        val cornerPos = pos - rotatePos(centerOffset.toDouble(), Vec3d(0.0, 0.0, 0.0), rotation).floor()
        
        runCommand(serverSystem, "/structure load lucky:${structureId.split(":").last()} " +
            "${cornerPos.x} ${cornerPos.y} ${cornerPos.z}"
        )
    }
}
