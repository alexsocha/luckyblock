package mod.lucky.common.drop.action

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.*

fun adjustY(world: World, pos: Vec3d, adjustRange: IntRange): Vec3d {
    for (y in adjustRange) {
        val newPos = Vec3d(pos.x, pos.y + y, pos.z)
        if (GAME_API.isAirBlock(world, newPos.floor())) return newPos
    }
    return pos
}

fun calculatePos(
    drop: SingleDrop,
    default: Vec3d? = null,
    world: World? = null,
    posKey: String = "pos",
    offsetKey: String? = "posOffset"
): Vec3d {
    val basePos = drop.getVec3(posKey, default)
    val centerOffset = drop.getVec3<Double>("centerOffset")
    val posOffset = drop.getVec3<Double>(offsetKey ?: "posOffset")

    val posWithOffset =
        if (centerOffset != zeroVec3d) getWorldPos(posOffset, centerOffset, basePos, drop["rotation"])
        else if (posOffset != zeroVec3d) basePos + posOffset
        else basePos

    if (world == null) return posWithOffset

    val adjustList: ListAttr = drop["adjustY"]
    val adjustRange = adjustList.getValue<Int>(0)..adjustList.getValue(1)
    return adjustY(world, posWithOffset, adjustRange)
}

fun withBlockMode(mode: String, blockId: String): String? {
    return when(mode) {
        "air" -> if (blockId != "minecraft:air") "minecraft:air" else null
        "overlay" -> if (blockId != "minecraft:air") blockId else null
        "replace" -> blockId
        else -> {
            GAME_API.logError("Invalid block mode '$mode'")
            null
        }
    }
}

fun setBlock(world: World, pos: Vec3i, drop: SingleDrop) {
    if ("id" !in drop) throw DropError("Missing block ID")
    val blockId = withBlockMode(drop["blockMode"], drop["id"]) ?: return

    GAME_API.setBlock(
        world = world,
        pos = pos,
        id = blockId,
        state = drop.getOrNull<DictAttr>("state"),
        components = drop.getOrNull<DictAttr>("components"),
        rotation = positiveMod(drop.get<Int>("facing") + drop.get<Int>("rotation"), 4),
        notify = drop["blockUpdate"],
    )
    if ("nbttag" in drop) {
        GAME_API.setBlockEntity(world, pos, drop["nbttag"])
    }
}

fun doBlockDrop(drop: SingleDrop, context: DropContext) {
    val pos = calculatePos(drop, context.pos, context.world).floor()
    setBlock(context.world, pos, drop)
}

fun doFillDrop(drop: SingleDrop, context: DropContext) {
    val pos = calculatePos(drop, context.pos, context.world).floor()
    val pos2 = if ("pos2" in drop) {
        calculatePos(drop, world = context.world, posKey = "pos2", offsetKey = "posOffset2").floor()
    } else pos + drop.getVec3("size") - Vec3i(1, 1, 1)

    val xRange = minOf(pos.x, pos2.x)..maxOf(pos.x, pos2.x)
    val yRange = minOf(pos.y, pos2.y)..maxOf(pos.y, pos2.y)
    val zRange = minOf(pos.z, pos2.z)..maxOf(pos.z, pos2.z)
    for (x in xRange) {
        for (y in yRange) {
            for (z in zRange) {
                setBlock(context.world, Vec3i(x, y, z), drop)
            }
        }
    }
}

fun doItemDrop(drop: SingleDrop, context: DropContext) {
    GAME_API.dropItem(
        world = context.world,
        pos = calculatePos(drop, context.pos, context.world),
        id = drop["id"],
        nbt = drop.getOrNull("nbttag"),
        components = drop.getOrNull("components"),
    )
}

fun doMessageDrop(drop: SingleDrop, context: DropContext) {
    val player = context.player ?: GAME_API.getNearestPlayer(
        context.world,
        calculatePos(drop, context.pos, context.world),
    )
    if (player != null) GAME_API.sendMessage(player, drop["id"])
}

fun doCommandDrop(drop: SingleDrop, context: DropContext) {
    GAME_API.runCommand(
        world = context.world,
        pos = calculatePos(drop, context.pos, context.world),
        command = drop["id"],
        senderName = drop["commandSender"],
        showOutput = drop["displayOutput"],
    )
}

fun doDifficultyDrop(drop: SingleDrop, context: DropContext) {
    val value: String = drop["id"]
    val difficulty = when (value.lowercase()) {
        in listOf("peacful", "p", "0") -> "peacful"
        in listOf("easy", "3", "1") -> "easy"
        in listOf("normal", "n", "2") -> "normal"
        in listOf("hard", "h", "3") -> "hard"
        else -> {
            GAME_API.logError("Invalid diffictulty: $value")
            return
        }
    }
    GAME_API.setDifficulty(context.world, difficulty)
}

fun doTimeDrop(drop: SingleDrop, context: DropContext) {
    val timeAttr = drop.props["id"] as ValueAttr
    val time = if (isNumType(timeAttr.type)) castNum(AttrType.LONG, timeAttr.value as Number) as Long
        else when (timeAttr.value as String) {
            "day" -> 1000L
            "night" -> 13000L
            else -> {
                GAME_API.logError("Invalid time: ${timeAttr.value}")
                return
            }
        }

    GAME_API.setTime(context.world, time)
}

fun doSoundDrop(drop: SingleDrop, context: DropContext) {
    val pos = calculatePos(drop, context.pos, context.world)
    GAME_API.playSound(context.world, pos, drop["id"], drop["volume"], drop["pitch"])
}

fun doExplosionDrop(drop: SingleDrop, context: DropContext) {
    val pos = calculatePos(drop, context.pos, context.world) + Vec3d(0.0, 0.5, 0.0)
    GAME_API.createExplosion(context.world, pos, drop["damage"], drop["fire"])
}

fun doParticleDrop(drop: SingleDrop, context: DropContext) {
    val effectIdAttr = drop.props["id"] as ValueAttr
    val pos = calculatePos(drop, context.pos, context.world)
    val particleData: Int = drop["damage"]

    if (isNumType(effectIdAttr.type)) {
        val particleIdInt = castNum(AttrType.INT, effectIdAttr.value as Number) as Int
        GAME_API.playParticleEvent(context.world, pos, particleIdInt, particleData)
    } else {
        val particleId = effectIdAttr.value as String
        if (particleId.equals("splashpotion", ignoreCase = true)) {
            val potionName = drop.getOrNull<String>("potion")
            GAME_API.playSplashPotionEvent(context.world, pos, potionName, if (potionName == null) particleData else null)
        } else {
            val splitId = particleId.split('.')
            val particleArgs = if (splitId.size >= 2) splitId.subList(1, splitId.size) else emptyList()
            GAME_API.spawnParticle(context.world, pos, splitId[0], particleArgs, drop.getVec3("size"), drop["particleAmount"])
        }
    }
}

fun doEntityDrop(drop: SingleDrop, context: DropContext) {
    val pos = calculatePos(drop, context.pos, context.world)

    if ("id" !in drop) throw DropError("Missing entity ID")
    val id = when(val initId: String = drop["id"]) {
        "LightningBolt" -> "lightning_bolt"
        else -> initId
    }

    GAME_API.spawnEntity(
        world=context.world,
        id=id,
        pos=pos,
        nbt = drop["nbttag"],
        components = drop.getOrNull("components"),
        player = context.player,
        sourceId = context.sourceId,
        randomizeMob = drop["randomizeMob"],
        rotation=positiveMod(drop.get<Double>("facing") + drop.get<Int>("rotation"), 4.0),
    )
}
