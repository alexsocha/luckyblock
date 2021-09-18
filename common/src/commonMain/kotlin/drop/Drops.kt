package mod.lucky.common.drop

import mod.lucky.common.LuckyRegistry
import mod.lucky.common.Vec3d
import mod.lucky.common.Vec3i
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.action.*

fun registerDefaultDrops() {
    LuckyRegistry.dropTripleProps.putAll(mapOf(
        "pos" to Triple("posX", "posY", "posZ"),
        "pos2" to Triple("pos2X", "pos2Y", "pos2Z"),
        "posOffset" to Triple("posOffsetX", "posOffsetY", "posOffsetZ"),
        "posOffset2" to Triple("posOffset2X", "posOffset2Y", "posOffset2Z"),
        "centerOffset" to Triple("centerOffsetX", "centerOffsetY", "centerOffsetZ"),
        "size" to Triple("length", "height", "width"),
    ))

    val commonSpecs: Array<Pair<String, AttrSpec>> = arrayOf(
        "type" to ValueSpec(AttrType.STRING),
        "amount" to ValueSpec(AttrType.INT),
        "reinitialize" to ValueSpec(AttrType.BOOLEAN),
        "postDelayInit" to ValueSpec(AttrType.BOOLEAN),
        "delay" to ValueSpec(AttrType.DOUBLE),
        "rotation" to ValueSpec(AttrType.INT),
        *createVecSpec("pos", LuckyRegistry.dropTripleProps["pos"]!!, AttrType.DOUBLE),
        *createVecSpec("posOffset", LuckyRegistry.dropTripleProps["posOffset"]!!, AttrType.DOUBLE),
        *createVecSpec("centerOffset", LuckyRegistry.dropTripleProps["centerOffset"]!!, AttrType.DOUBLE),
    )

    val dropSpecs = mapOf(
        "block" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            "nbttag" to DictSpec(emptyMap()),
            "state" to DictSpec(emptyMap()),
            "components" to DictSpec(emptyMap()), // Bedrock Edition only
            "blockUpdate" to ValueSpec(AttrType.BOOLEAN),
            "blockMode" to ValueSpec(AttrType.STRING),
            "facing" to ValueSpec(AttrType.INT),
        ),
        "item" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            "nbttag" to DictSpec(emptyMap()),
            "components" to DictSpec(emptyMap()), // Bedrock Edition only
            "data" to ValueSpec(AttrType.INT), // Bedrock Edition only
        ),
        "entity" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            "nbttag" to DictSpec(emptyMap()),
            "components" to DictSpec(emptyMap()), // Bedrock Edition only
            "adjustY" to ListSpec(listOf(ValueSpec(AttrType.INT), ValueSpec(AttrType.INT))),
            "randomizeMob" to ValueSpec(AttrType.BOOLEAN),
            "facing" to ValueSpec(AttrType.DOUBLE),
        ),
        "command" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            "commandSender" to ValueSpec(AttrType.STRING),
            "displayOutput" to ValueSpec(AttrType.BOOLEAN),
        ),
        "effect" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            "duration" to ValueSpec(AttrType.INT),
            "amplifier" to ValueSpec(AttrType.INT),
            "excludePlayer" to ValueSpec(AttrType.BOOLEAN),
            "target" to ValueSpec(AttrType.STRING), // either target or range should be provided
            "range" to ValueSpec(AttrType.DOUBLE),
            "power" to ValueSpec(AttrType.DOUBLE),
            "directionYaw" to ValueSpec(AttrType.DOUBLE),
            "directionPitch" to ValueSpec(AttrType.DOUBLE),
        ),
        "explosion" to dictSpecOf(
            *commonSpecs,
            "damage" to ValueSpec(AttrType.INT),
            "fire" to ValueSpec(AttrType.BOOLEAN),
        ),
        "fill" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            *createVecSpec("size", LuckyRegistry.dropTripleProps["size"]!!, AttrType.INT),
            *createVecSpec("pos2", LuckyRegistry.dropTripleProps["posOffset"]!!, AttrType.INT), // optional, can be used instead of size
            *createVecSpec("posOffset2", LuckyRegistry.dropTripleProps["posOffset2"]!!, AttrType.INT),
            "nbttag" to DictSpec(emptyMap()),
            "state" to DictSpec(emptyMap()),
            "components" to DictSpec(emptyMap()), // Bedrock Edition only
            "blockUpdate" to ValueSpec(AttrType.BOOLEAN),
            "blockMode" to ValueSpec(AttrType.STRING),
            "facing" to ValueSpec(AttrType.INT),
        ),
        "particle" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(),
            *createVecSpec("size", LuckyRegistry.dropTripleProps["size"]!!, AttrType.DOUBLE),
            *createVecSpec("pos2", LuckyRegistry.dropTripleProps["pos2"]!!, AttrType.DOUBLE), // optional, can be used instead of size
            *createVecSpec("posOffset2", LuckyRegistry.dropTripleProps["posOffset2"]!!, AttrType.DOUBLE),
            "particleAmount" to ValueSpec(AttrType.INT),
            "damage" to ValueSpec(AttrType.INT),
            "potion" to ValueSpec(AttrType.STRING),
        ),
        "sound" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            "volume" to ValueSpec(AttrType.DOUBLE),
            "pitch" to ValueSpec(AttrType.DOUBLE),
        ),
        "structure" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
            *createVecSpec("size", LuckyRegistry.dropTripleProps["size"]!!, AttrType.DOUBLE),
            "blockUpdate" to ValueSpec(AttrType.BOOLEAN),
            "blockMode" to ValueSpec(AttrType.STRING),
        ),
        "difficulty" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
        ),
        "time" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(), // int or string
        ),
        "message" to dictSpecOf(
            *commonSpecs,
            "id" to ValueSpec(AttrType.STRING),
        ),
        "nothing" to dictSpecOf(*commonSpecs),
    )

    val commonDefaults: Array<Pair<String, Any>> = arrayOf(
        "type" to "item",
        "amount" to 1,
        "reinitialize" to true,
        "postDelayInit" to true,
        "delay" to 0.0,
        "rotation" to 0,
        "pos" to Vec3d(0.0, 0.0, 0.0), // defaults to dropPos
        "posOffset" to Vec3d(0.0, 0.0, 0.0),
        "centerOffset" to Vec3d(0.0, 0.0, 0.0),
    )

    val dropDefaults: Map<String, Map<String, Any>> = mapOf(
        "block" to mapOf(
            *commonDefaults,
            "nbttag" to DictAttr(),
            "state" to DictAttr(),
            "blockUpdate" to true,
            "blockMode" to "replace",
            "facing" to 0,
        ),
        "item" to mapOf(
            *commonDefaults,
            "nbttag" to DictAttr(),
        ),
        "entity" to mapOf(
            *commonDefaults,
            "nbttag" to DictAttr(),
            "adjustY" to listAttrOf(intAttrOf(0), intAttrOf(10)),
            "randomizeMob" to true,
            "facing" to 2.0,
        ),
        "command" to mapOf(
            *commonDefaults,
            "commandSender" to "Lucky Block",
            "displayOutput" to false,
        ),
        "effect" to mapOf(
            *commonDefaults,
            "duration" to 30,
            "amplifier" to 0,
            "excludePlayer" to false,
            "range" to 4.0,
            "power" to 1.0,
            "directionYaw" to 0.0,
            "directionPitch" to -30.0,
        ),
        "explosion" to mapOf(
            *commonDefaults,
            "damage" to 3,
            "fire" to false,
        ),
        "fill" to mapOf(
            *commonDefaults,
            "size" to Vec3i(0, 0, 0),
            "pos2" to Vec3i(0, 0, 0),
            "posOffset2" to Vec3i(0, 0, 0),
            "nbttag" to DictAttr(),
            "state" to DictAttr(),
            "blockUpdate" to true,
            "blockMode" to "replace",
            "facing" to 0,
        ),
        "particle" to mapOf(
            *commonDefaults,
            "size" to Vec3d(0.0, 0.0, 0.0),
            "pos2" to Vec3d(0.0, 0.0, 0.0),
            "posOffset2" to Vec3d(0.0, 0.0, 0.0),
            "particleAmount" to 1,
            "damage" to 0,
            "potion" to "poison",
        ),
        "sound" to mapOf(
            *commonDefaults,
            "volume" to 1.0,
            "pitch" to 1.0,
        ),
        "structure" to mapOf(
            *commonDefaults,
            "size" to Vec3d(0.0, 0.0, 0.0),
            "blockUpdate" to true,
            "blockMode" to "replace",
        ),
        "difficulty" to mapOf(*commonDefaults),
        "time" to mapOf(*commonDefaults),
        "message" to mapOf(*commonDefaults),
        "nothing" to mapOf(*commonDefaults),
    )

    LuckyRegistry.dropDefaults.putAll(dropDefaults)
    LuckyRegistry.dropSpecs.putAll(dropSpecs)

    LuckyRegistry.registerAction("block", ::doBlockDrop)
    LuckyRegistry.registerAction("item", ::doItemDrop)
    LuckyRegistry.registerAction("entity", ::doEntityDrop)
    LuckyRegistry.registerAction("command", ::doCommandDrop)
    LuckyRegistry.registerAction("effect", ::doEffectDrop)
    LuckyRegistry.registerAction("explosion", ::doExplosionDrop)
    LuckyRegistry.registerAction("fill", ::doFillDrop)
    LuckyRegistry.registerAction("particle", ::doParticleDrop)
    LuckyRegistry.registerAction("sound", ::doSoundDrop)
    LuckyRegistry.registerAction("structure", ::doStructureDrop)
    LuckyRegistry.registerAction("difficulty", ::doDifficultyDrop)
    LuckyRegistry.registerAction("time", ::doTimeDrop)
    LuckyRegistry.registerAction("message", ::doMessageDrop)
    LuckyRegistry.registerAction("nothing") { _, _ -> }

    // compatibility
    LuckyRegistry.registerDropPropRenames("block", mapOf("tileEntity" to "nbttag"))
    LuckyRegistry.registerDropPropRenames("fill", mapOf("tileEntity" to "nbttag"))
    LuckyRegistry.registerDropPropRenames("explosion", mapOf("radius" to "damage"))
    LuckyRegistry.registerDropPropRenames("command", mapOf("displayCommandOutput" to "displayOutput"))
    LuckyRegistry.registerDropPropRenames("structure", mapOf(
        "applyBlockMode" to "blockMode",
        "center" to "centerOffset",
        "centerX" to "centerOffsetX",
        "centerY" to "centerOffsetY",
        "centerZ" to "centerOffsetZ",
    ))
}
