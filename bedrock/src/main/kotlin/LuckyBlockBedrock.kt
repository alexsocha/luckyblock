package mod.lucky.bedrock

import mod.lucky.common.BlockPos
import mod.lucky.common.Vec3d
import mod.lucky.common.drop.*
import mod.lucky.common.gameAPI

class UnparsedLuckyBlockConfig(
    val luck: Int,
    val drops: Array<String>,
    val doDropsOnCreativeMode: Boolean = false,
)

data class LuckyBlockConfig(
    val dropContainer: DropContainer,
    val doDropsOnCreativeMode: Boolean,
)

object BedrockLuckyRegistry {
    val blocks: MutableMap<String, LuckyBlockConfig> = HashMap()
}

fun parseLuckyBlockConfig(unparsedConfig: UnparsedLuckyBlockConfig): LuckyBlockConfig {
    return LuckyBlockConfig(
        dropContainer = DropContainer(
            drops = dropsFromStrList(unparsedConfig.drops.toList()),
            luck = unparsedConfig.luck
        ),
        doDropsOnCreativeMode = unparsedConfig.doDropsOnCreativeMode,
    )
}

fun onPlayerDestroyedLuckyBlock(world: MCWorld, player: MCPlayer, pos: BlockPos, blockId: String, blockConfig: LuckyBlockConfig) {
    try {
        val vecPos = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)

        val blockEntityDropContainer = BedrockGameAPI.readAndDestroyLuckyBlockEntity(world, pos)

        // run a randrom drop
        val context = DropContext(world = world, pos = vecPos, player = player, sourceId = blockId)

        BedrockGameAPI.logInfo("Starting drop")
        runRandomDrop(
            blockEntityDropContainer?.drops ?: blockConfig.dropContainer.drops,
            blockEntityDropContainer?.luck ?: blockConfig.dropContainer.luck,
            context,
            showOutput = true
        )
    } catch (e: Exception) {
        BedrockGameAPI.logError("Error performing Lucky Block function", e)
    }
}

@JsName("initServer")
fun initServer(server: MCServer, serverSystem: MCServerSystem) {
    gameAPI = BedrockGameAPI
    BedrockGameAPI.initServer(server, serverSystem)

    serverSystem.registerComponent("lucky:lucky_block_entity_data", UnparsedDropContainer(
        drops = emptyArray(),
        luck = 0,
    ))

    serverSystem.registerComponent("lucky:all_lucky_block_entity_data", mapOf("" to UnparsedDropContainer(
        drops = emptyArray(),
        luck = 0,
    )))

    // parse all drops for the default block
    BedrockLuckyRegistry.blocks["lucky:lucky_block"] = parseLuckyBlockConfig(
        serverSystem.createEventData<UnparsedLuckyBlockConfig>("lucky:lucky_block_config").data
    )

    serverSystem.listenForEvent<MCPlayerDestroyedBlockEvent>("minecraft:player_destroyed_block") { eventWrapper ->
        BedrockGameAPI.logInfo("Received event")
        val event = eventWrapper.data
        if (event.block_identifier.startsWith("lucky:")) {
            val blockConfig = BedrockLuckyRegistry.blocks.getOrElse(event.block_identifier) {
                val unparsedConfig = serverSystem.createEventData<UnparsedLuckyBlockConfig?>("${event.block_identifier}_config").data

                if (unparsedConfig == null) {
                    BedrockGameAPI.logError("Missing Lucky Block config. Make sure to call createEventData(\"${event.block_identifier}_config\", ...) at the start of your serverScript.js.")
                    null
                } else {
                    val parsedConfig = parseLuckyBlockConfig(unparsedConfig)
                    BedrockLuckyRegistry.blocks[event.block_identifier] = parsedConfig
                    parsedConfig
                }
            }

            if (blockConfig != null) {
                val world = serverSystem.getComponent<MCTickWorldComponent>(event.player, "minecraft:tick_world")!!.data

                onPlayerDestroyedLuckyBlock(
                    world = world,
                    player = event.player,
                    pos = toBlockPos(event.block_position),
                    blockConfig = blockConfig,
                    blockId = event.block_identifier,
                )
            }
        }
    }
}
