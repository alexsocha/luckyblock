package mod.lucky.common.drop.action

import mod.lucky.common.*
import mod.lucky.common.attribute.DictAttr
import mod.lucky.common.drop.*
import kotlin.math.atan2

private fun applyKnockbackEffect(drop: SingleDrop, dropPos: Vec3d, targetEntity: Entity) {
    val entityPos = gameAPI.getEntityPos(targetEntity)

    val isClose = distanceBetween(dropPos, gameAPI.getEntityPos(targetEntity)) < 0.01

    val yawRad = if ("directionYaw" in drop) degToRad(drop["directionYaw"]) else atan2(
        (entityPos.x - dropPos.x) * -1,
        entityPos.z - dropPos.z
    )
    val pitchRad = degToRad(if ("target" !in drop && isClose) -90.0 else drop["directionPitch"])
    val power = if ("target" !in drop && isClose) drop.get<Double>("power") * 0.5 else drop["power"]

    val motion = directionToVelocity(yawRad, pitchRad, power)
    gameAPI.setEntityMotion(targetEntity, motion)
}

fun applyEffect(drop: SingleDrop, dropPos: Vec3d, targetEntity: Entity, effectId: String) {
    when (effectId) {
        "special_fire" -> gameAPI.setEntityOnFire(targetEntity, drop["duration"])
        "special_knockback" -> applyKnockbackEffect(drop, dropPos, targetEntity)
        else -> gameAPI.applyStatusEffect(targetEntity, effectId, drop["duration"], drop["amplifier"])
    }
}

fun doEffectDrop(drop: SingleDrop, context: DropContext) {
    val dropPos = drop.getPos(context.pos)
    val targetEntity = when {
        "target" in drop && "range" !in drop -> when (drop.get<String>("target")) {
            "player" -> context.player
            "hitEntity" -> context.hitEntity
            else -> null
        }
        "target" !in drop && "range" !in drop -> context.player
        else -> null
    }
    if (drop.getOrNull<String>("target") == "hitEntity" && targetEntity == null) return

    val effectId: String = when (val dropId = drop.get<String>("id")) {
        "special_fire" -> dropId
        "special_knockback" -> dropId
        else -> {
            try {
                val intId = dropId.toInt()
                gameAPI.convertStatusEffectId(intId) ?: dropId
            } catch (e: NumberFormatException) {
                dropId
            }
        }
    }

    if (targetEntity != null) {
        applyEffect(drop, dropPos, targetEntity, effectId)
    } else {
        val effectBoxMin = dropPos - dropPos
        val effectBoxMax = dropPos + dropPos
        val entities = gameAPI.getLivingEntitiesInBox(context.world, effectBoxMin, effectBoxMax)

        val range: Double = drop["range"]
        for (entity in entities) {
            if (drop["excludePlayer"] && entity == context.player) continue
            val distance = distanceBetween(dropPos, gameAPI.getEntityPos(entity))
            if (distance <= range) {
                applyEffect(drop, dropPos, entity, effectId)
            }
        }
    }
}
