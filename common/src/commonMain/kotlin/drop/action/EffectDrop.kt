package mod.lucky.common.drop.action

import mod.lucky.common.*
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.drop.*
import kotlin.math.atan2

private fun applyKnockbackEffect(drop: SingleDrop, dropPos: Vec3d, targetEntity: Entity) {
    val entityPos = GAME_API.getEntityPos(targetEntity)

    val isClose = distanceBetween(dropPos, GAME_API.getEntityPos(targetEntity)) < 0.01

    val yawRad = if ("directionYaw" in drop) degToRad(drop["directionYaw"]) else atan2(
        (entityPos.x - dropPos.x) * -1,
        entityPos.z - dropPos.z
    )
    val pitchRad = degToRad(if ("target" !in drop && isClose) -90.0 else drop["directionPitch"])
    val power = if ("target" !in drop && isClose) drop.get<Double>("power") * 0.5 else drop["power"]

    val motion = directionToVelocity(yawRad, pitchRad, power)
    GAME_API.setEntityMotion(targetEntity, motion)
}

fun applyEffect(drop: SingleDrop, dropPos: Vec3d, target: String?, targetEntity: Entity?, effectId: String) {
    when (effectId) {
        "special_fire" -> {
            if (targetEntity == null) throw Exception("Entity required")
            GAME_API.setEntityOnFire(targetEntity, drop["duration"])
        }
        "special_knockback" -> {
            if (targetEntity == null) throw Exception("Entity required")
            applyKnockbackEffect(drop, dropPos, targetEntity)
        }
        else -> GAME_API.applyStatusEffect(
            target=target,
            targetEntity=targetEntity,
            effectId=effectId,
            durationSeconds=drop["duration"],
            amplifier=drop["amplifier"]
        )
    }
}

fun doEffectDrop(drop: SingleDrop, context: DropContext) {
    val pos = calculatePos(drop, context.pos, context.world)
    val (target, targetEntity) = when {
        "target" in drop && "range" !in drop -> when (drop.get<String>("target")) {
            "player" -> Pair("player", context.player)
            "hitEntity" -> Pair("hitEntity", context.hitEntity)
            else -> Pair("player", null)
        }
        "target" !in drop && "range" !in drop -> Pair("player", context.player)
        else -> Pair(null, null)
    }
    if (target == "hitEntity" && targetEntity == null) return

    val effectId: String = when (val dropId = drop.get<String>("id")) {
        "special_fire" -> dropId
        "special_knockback" -> dropId
        else -> {
            try {
                val intId = dropId.toInt()
                GAME_API.convertStatusEffectId(intId) ?: dropId
            } catch (e: NumberFormatException) {
                dropId
            }
        }
    }

    if (target != null || targetEntity != null) {
        applyEffect(
            drop=drop,
            dropPos=pos,
            target=target,
            targetEntity=targetEntity,
            effectId=effectId
        )
    } else {
        val range: Double = drop["range"]
        val effectBoxMin = pos - Vec3d(range, range, range)
        val effectBoxMax = pos + Vec3d(range, range, range)
        val entities = GAME_API.getLivingEntitiesInBox(context.world, effectBoxMin, effectBoxMax)

        for (entity in entities) {
            if (drop["excludePlayer"] && entity == context.player) continue
            val distance = distanceBetween(pos, GAME_API.getEntityPos(entity))
            if (distance <= range) {
                applyEffect(
                    drop=drop,
                    dropPos=pos,
                    target=target,
                    targetEntity=targetEntity,
                    effectId=effectId
                )
            }
        }
    }
}
