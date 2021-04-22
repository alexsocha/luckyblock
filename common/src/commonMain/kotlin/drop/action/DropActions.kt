package mod.lucky.common.drop.action

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.*

fun withBlockMode(mode: String, blockId: String): String? {
    return when(mode) {
        "air" -> if (blockId != "minecraft:air") "minecraft:air" else null
        "overlay" -> if (blockId != "minecraft:air") blockId else null
        "replace" -> blockId
        else -> {
            gameAPI.logError("Invalid block mode '$mode'")
            null
        }
    }
}

fun setBlock(world: World, pos: Vec3i, drop: SingleDrop) {
    if ("id" !in drop) throw DropError("Missing block ID")
    val blockId = withBlockMode(drop["blockMode"], drop["id"]) ?: return

    gameAPI.setBlock(
        world = world,
        pos = pos,
        blockId = blockId,
        state = drop.get<DictAttr>("state", null),
        rotation = positiveMod(drop.get<Int>("facing") + drop.get<Int>("rotation"), 4),
        notify = drop["blockUpdate"],
    )
    if ("nbttag" in drop) {
        gameAPI.setBlockEntity(world, pos, drop["nbttag"])
    }
}

fun doBlockDrop(drop: SingleDrop, context: DropContext) {
    setBlock(context.world, drop.getPos(context.pos).floor(), drop)
}

fun doFillDrop(drop: SingleDrop, context: DropContext) {
    val pos = drop.getPos(context.pos).floor()
    val pos2 = if ("pos2" in drop) drop.getPos(posKey = "pos2", offsetKey = "posOffset2").floor()
        else pos + drop.getVec3("size") - Vec3i(1, 1, 1)

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
    gameAPI.dropItem(context.world, drop.getPos(context.pos), drop["id"], drop.getOrNull("nbttag"))
}

fun doMessageDrop(drop: SingleDrop, context: DropContext) {
    val player = context.player ?: gameAPI.getNearestPlayer(context.world, drop.getPos(context.pos))
    if (player != null) gameAPI.sendMessage(player, drop["id"])
}

fun doCommandDrop(drop: SingleDrop, context: DropContext) {
    gameAPI.runCommand(
        world = context.world,
        pos = drop.getPos(context.pos),
        command = drop["id"],
        senderName = drop["commandSender"],
        showOutput = drop["displayOutput"],
    )
}

fun doDifficultyDrop(drop: SingleDrop, context: DropContext) {
    val value: String = drop["id"]
    val difficulty = when (value.toLowerCase()) {
        in listOf("peacful", "p", "0") -> "peacful"
        in listOf("easy", "3", "1") -> "easy"
        in listOf("normal", "n", "2") -> "normal"
        in listOf("hard", "h", "3") -> "hard"
        else -> {
            gameAPI.logError("Invalid diffictulty: $value")
            return
        }
    }
    gameAPI.setDifficulty(context.world, difficulty)
}

fun doTimeDrop(drop: SingleDrop, context: DropContext) {
    val timeAttr = drop.props["id"] as ValueAttr
    val time = if (isNumType(timeAttr.type)) castNum(AttrType.LONG, timeAttr.value as Number) as Long
        else when (timeAttr.value as String) {
            "day" -> 1000L
            "night" -> 13000L
            else -> {
                gameAPI.logError("Invalid time: ${timeAttr.value}")
                return
            }
        }

    gameAPI.setTime(context.world, time)
}

fun doSoundDrop(drop: SingleDrop, context: DropContext) {
    gameAPI.playSound(context.world, drop.getPos(context.pos), drop["id"], drop["volume"], drop["pitch"])
}

fun doExplosionDrop(drop: SingleDrop, context: DropContext) {
    val pos = drop.getPos(context.pos) + Vec3d(0.0, 0.5, 0.0)
    gameAPI.createExplosion(context.world, pos, drop["damage"], drop["fire"])
}

fun doParticleDrop(drop: SingleDrop, context: DropContext) {
    val effectIdAttr = drop.props["id"] as ValueAttr
    val pos = drop.getPos(context.pos)
    val particleData: Int = drop["damage"]

    if (isNumType(effectIdAttr.type)) {
        val particleIdInt = castNum(AttrType.INT, effectIdAttr.value as Number) as Int
        gameAPI.playParticleEvent(context.world, pos, particleIdInt, particleData)
    } else {
        val particleId = effectIdAttr.value as String
        if (particleId.equals("splashpotion", ignoreCase = true)) {
            val potionName = drop.getOrNull<String>("potion")
            gameAPI.playSplashPotionEvent(context.world, pos, potionName, if (potionName == null) particleData else null)
        } else {
            val splitId = particleId.split('.')
            val particleArgs = if (splitId.size >= 2) splitId.subList(1, splitId.size) else emptyList()
            gameAPI.spawnParticle(context.world, pos, splitId[0], particleArgs, drop.getVec3("size"), drop["particleAmount"])
        }
    }
}

fun doEntityDrop(drop: SingleDrop, context: DropContext) {
    fun adjustY(world: World, pos: Vec3d, adjustRange: IntRange): Vec3d {
        for (y in adjustRange) {
            val newPos = Vec3d(pos.x, pos.y + y, pos.z)
            if (gameAPI.isAirBlock(world, newPos.floor())) return newPos
        }
        return pos
    }

    val initPos = drop.getPos(context.pos)
    val adjustList: ListAttr = drop["adjustY"]
    val adjustRange = adjustList.getValue<Int>(0)..adjustList.getValue(1)
    val pos = adjustY(context.world, initPos, adjustRange)

    if ("id" !in drop) throw DropError("Missing entity ID")

    val id = when(val initId: String = drop["id"]) {
        "LightningBolt" -> "lightning_bolt"
        else -> initId
    }

    gameAPI.spawnEntity(
        world=context.world,
        id=id,
        pos=pos,
        nbt = drop["nbttag"],
        player = context.player,
        sourceId = context.sourceId,
        randomizeMob = drop["randomizeMob"],
        rotation=positiveMod(drop.get<Double>("facing") + drop.get<Int>("rotation"), 4.0),
    )
}