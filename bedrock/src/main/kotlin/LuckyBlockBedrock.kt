package mod.lucky.bedrock

import mod.lucky.common.BlockPos
import mod.lucky.common.Vec3d
import mod.lucky.common.drop.*
import mod.lucky.common.attribute.*
import mod.lucky.common.gameAPI
import kotlin.js.JSON.stringify

class UnparsedLuckyBlockConfig(
    val luck: Int,
    val drops: String,
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
            drops = dropsFromStrList(splitLines(unparsedConfig.drops.split('\n'))),
            luck = unparsedConfig.luck
        ),
        doDropsOnCreativeMode = unparsedConfig.doDropsOnCreativeMode,
    )
}

fun onPlayerDestroyedLuckyBlock(world: MCWorld, player: MCPlayer, pos: BlockPos, blockId: String, blockConfig: LuckyBlockConfig) {
    try {
        BedrockGameAPI.logInfo("Starting drop")
        val vecPos = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)

        val blockEntityDropContainer = BedrockGameAPI.readAndDestroyLuckyBlockEntity(world, pos)

        // run a randrom drop
        val context = DropContext(world = world, pos = vecPos, player = player, sourceId = blockId)

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

fun initServer(server: MCServer, serverSystem: MCServerSystem) {
    gameAPI = BedrockGameAPI
    BedrockGameAPI.initServer(server, serverSystem)

    serverSystem.initialize = {
        // turn on logging of information, warnings, and errors
        val scriptLoggerConfig = serverSystem.createEventData<MCLoggerConfigEvent>("minecraft:script_logger_config");
        scriptLoggerConfig.data.log_errors = true
        scriptLoggerConfig.data.log_information = true
        scriptLoggerConfig.data.log_warnings = true
        serverSystem.broadcastEvent("minecraft:script_logger_config", scriptLoggerConfig);
    }

    serverSystem.log = { msg ->
        val chatEvent = serverSystem.createEventData<MCChatEvent>("minecraft:display_chat_event");
        chatEvent.data.message = stringify(msg)
        serverSystem.broadcastEvent("minecraft:display_chat_event", chatEvent);
    }

    serverSystem.registerComponent("lucky:lucky_block_entity_data", UnparsedDropContainer(
        drops = emptyArray(),
        luck = 0,
    ))

    // (optimization) parse all drops for the default block
    BedrockLuckyRegistry.blocks["lucky:lucky_block"] = parseLuckyBlockConfig(
        serverSystem.createEventData<UnparsedLuckyBlockConfig>("lucky:lucky_block_config").data
    )

    serverSystem.listenForEvent<MCPlayerDestroyedBlockEvent>("minecraft:player_placed_block") { eventWrapper ->
        /*
        BedrockGameAPI.logInfo("Block interacted")
        val event = eventWrapper.data
        val pos = toBlockPos(event.block_position)
        val world = serverSystem.getComponent<MCTickWorldComponent>(event.player, "minecraft:tick_world")!!.data

        val block = serverSystem.getBlock(world.ticking_area, toMCBlockPos(BlockPos(pos.x, pos.y + 1, pos.z)))
        serverSystem.log(block)

        val container = serverSystem.getComponent<Any>(block, "minecraft:container")
        serverSystem.log(container)
        */
    }


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
                serverSystem.log("finished event!")
            }
        }
    }
}
