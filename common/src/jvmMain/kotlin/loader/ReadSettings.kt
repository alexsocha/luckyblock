package mod.lucky.java.loader

import mod.lucky.common.LuckyBlockSettings
import mod.lucky.common.LuckyRegistry
import mod.lucky.common.attribute.*
import mod.lucky.common.gameAPI
import mod.lucky.common.logger
import mod.lucky.java.AddonIds

fun parseGlobalSettings(lines: List<String>): GlobalSettings {
    val spec = dictSpecOf(
        "showUpdateMessage" to ValueSpec(AttrType.BOOLEAN),
    )

    val props = try {
        val propsStr = splitLines(lines).joinToString(",")
        parseEvalAttr(propsStr, spec, LuckyRegistry.parserContext, LuckyRegistry.simpleEvalContext) as DictAttr
    } catch (e: Exception) {
        logger.logError("Error reading global settings", e)
        DictAttr()
    }

    return GlobalSettings(checkForUpdates=props.getWithDefault("showUpdateMessage", true))
}

fun parseLocalSettings(lines: List<String>): LocalSettings {
    val spec = dictSpecOf(
        "doDropsOnCreativeMode" to ValueSpec(AttrType.BOOLEAN),
    )

    val props = try {
        val propsStr = splitLines(lines).joinToString(",")
        parseEvalAttr(propsStr, spec, LuckyRegistry.parserContext, LuckyRegistry.simpleEvalContext) as DictAttr
    } catch (e: Exception) {
        logger.logError("Error reading global settings", e)
        DictAttr()
    }

return LocalSettings(
        block = LuckyBlockSettings(
            doDropsOnCreativeMode=props.getWithDefault("doDropsOnCreativeMode", false)
        )
    )
}

fun readAddonIds(lines: List<String>): AddonIds {
    val spec = dictSpecOf(
        "block_id" to ValueSpec(AttrType.STRING),
        "id" to ValueSpec(AttrType.STRING), // compatibility
        "sword_id" to ValueSpec(AttrType.STRING),
        "bow_id" to ValueSpec(AttrType.STRING),
        "potion_id" to ValueSpec(AttrType.STRING),
    )
    val props = try {
        val propsStr = splitLines(lines).joinToString(",")
        parseEvalAttr(propsStr, spec, LuckyRegistry.parserContext, LuckyRegistry.simpleEvalContext) as DictAttr
    } catch (e: Exception) {
        logger.logError("Error reading addon info", e)
        DictAttr()
    }

    return AddonIds(
        block = (props.getOptionalValue<String>("block_id") ?: props.getOptionalValue<String>("id"))?.let { "lucky:$it" },
        sword = props.getOptionalValue<String>("sword_id")?.let { "lucky:$it" },
        bow = props.getOptionalValue<String>("bow_id")?.let { "lucky:$it" },
        potion = props.getOptionalValue<String>("potion_id")?.let { "lucky:$it" },
    )
}
