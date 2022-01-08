package mod.lucky.java
import mod.lucky.common.LuckyRegistry
import mod.lucky.common.GameType
import mod.lucky.common.drop.*
import mod.lucky.common.attribute.*
import mod.lucky.java.loader.*
import java.io.File

data class AddonIds(
    val block: String? = null,
    val sword: String? = null,
    val bow: String? = null,
    val potion: String? = null,
) {
    fun getItemIds() = listOfNotNull(block, sword, bow, potion)
}

data class Addon(
    val ids: AddonIds,
    val file: File,
    val addonId: String,
)

object JavaLuckyRegistry {
    lateinit var notificationState: ModNotificationState
    lateinit var globalSettings: GlobalSettings
    lateinit var allAddonResources: List<AddonResources>
    var addons = ArrayList<Addon>()
    val nbtStructures = HashMap<String, MinecraftNbtStructure>() // addonId:path -> structure
    val craftingLuckModifiers = HashMap<String, Map<String, Int>>() // luckyItemId -> itemId -> luck modifier
    val worldGenDrops = HashMap<String, Map<String, List<WeightedDrop>>>() // blockId -> dimensionId -> drops

    const val blockId = "lucky:lucky_block"
    const val swordId = "lucky:lucky_sword"
    const val bowId = "lucky:lucky_bow"
    const val potionId = "lucky:lucky_potion"
    const val projectileId = "lucky:lucky_projectile"
    const val delayedDropId = "lucky:delayed_drop"
    private val itemIds = listOf(blockId, swordId, bowId, potionId)

    lateinit var allLuckyItemIds: List<String>
    lateinit var allLuckyItemIdsByType: Map<String, List<String>>

    private fun registerMainResources(mainResources: MainResources) {
        globalSettings = mainResources.globalSettings
        LuckyRegistry.blockSettings[blockId] = mainResources.settings.block
        LuckyRegistry.drops.putAll(mainResources.drops)
        worldGenDrops[blockId] = mainResources.worldGenDrops

        craftingLuckModifiers.putAll(itemIds.associateWith {
            mainResources.craftingLuckModifiers
        })

        for ((path, structure) in mainResources.dropStructures) {
            LuckyRegistry.registerDropStructure("$blockId:$path", structure)
        }
        for ((path, structure) in mainResources.nbtStructures) {
            nbtStructures["$blockId:$path"] = structure.structure
            LuckyRegistry.defaultStructureProps["$blockId:$path"] = structure.defaultProps
        }
    }

    private fun registerAddon(addonResources: AddonResources) {
        val addon = addonResources.addon

        if (addon.ids.block != null) {
            LuckyRegistry.blockSettings[addon.ids.block] = addonResources.settings.block
            worldGenDrops[addon.ids.block] = addonResources.worldGenDrops
        }
        LuckyRegistry.drops.putAll(addonResources.drops)

        craftingLuckModifiers.putAll(addon.ids.getItemIds().associateWith {
            addonResources.craftingLuckModifiers
        })

        for ((path, structure) in addonResources.dropStructures) {
            LuckyRegistry.registerDropStructure("$blockId:$path", structure)
        }
        for ((path, structure) in addonResources.nbtStructures) {
            nbtStructures["$blockId:$path"] = structure.structure
            LuckyRegistry.defaultStructureProps["$blockId:$path"] = structure.defaultProps
        }
    }

    fun init() {
        registerCommonTemplateVars(GameType.JAVA)
        registerJavaTemplateVars()

        val (mainResources, allAddonResources) = loadResources(JAVA_GAME_API.getGameDir())
        JavaLuckyRegistry.allAddonResources = allAddonResources

        registerMainResources(mainResources)

        addons.addAll(allAddonResources.map { it.addon })
        allAddonResources.forEach { registerAddon(it) }

        allLuckyItemIdsByType = mapOf(
            blockId to listOf(blockId) + addons.mapNotNull { it.ids.block },
            swordId to listOf(swordId) + addons.mapNotNull { it.ids.sword },
            bowId to listOf(bowId) + addons.mapNotNull { it.ids.bow },
            potionId to listOf(potionId) + addons.mapNotNull { it.ids.potion },
        )
        allLuckyItemIds = itemIds + addons.flatMap { it.ids.getItemIds() }

        LuckyRegistry.sourceToAddonId.putAll(itemIds.associateBy { blockId } + addons.flatMap { addon ->
            addon.ids.getItemIds().map { it to addon.addonId }
        }.toMap())

        notificationState = ModNotificationState.fromCache()
    }
}
