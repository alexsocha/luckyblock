package mod.lucky.java.game

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.DropContext
import mod.lucky.common.drop.WeightedDrop
import mod.lucky.common.drop.runRandomDrop
import mod.lucky.java.*

data class LuckyProjectileData(
    val trailFreqPerTick: Double = 1.0,
    val trailDrops: List<WeightedDrop>? = null,
    val impactDrops: List<WeightedDrop>? = null,
    val sourceId: String = JavaLuckyRegistry.blockId,
) {
    companion object {
        val attrKeys = listOf("trail", "impact", "sourceId")
    }
}

fun LuckyProjectileData.tick(world: World, entity: Entity, shooter: Entity?, ticksExisted: Int) {
    try {
        if (ticksExisted < 2) return
        if (trailDrops == null) return

        if (trailFreqPerTick < 1.0 && trailFreqPerTick > 0) {
            val amount = (1.0 / trailFreqPerTick).toInt()
            val velocity = JAVA_GAME_API.getEntityVelocity(entity)
            val pos = GAME_API.getEntityPos(entity)
            for (i in 0 until amount) {
                val context = DropContext(
                    world = world,
                    player = shooter,
                    pos = Vec3d(
                        pos.x + velocity.x * i / amount,
                        pos.y + velocity.y * i / amount,
                        pos.z + velocity.z * i / amount
                    ),
                    sourceId = sourceId,
                )
                runRandomDrop(trailDrops, luck = 0, context = context, showOutput = false)
            }
        } else if ((ticksExisted - 2) % trailFreqPerTick.toInt() == 0) {
            val context = DropContext(
                world = world,
                player = shooter,
                pos = GAME_API.getEntityPos(entity),
                sourceId = sourceId,
            )
            runRandomDrop(trailDrops, 0, context = context, showOutput = false)
        }
    } catch (e: java.lang.Exception) {
        GAME_API.logError("Error in lucky_projectile tick", e)
    }
}

fun LuckyProjectileData.onImpact(world: World, entity: Entity, user: Entity?, hitEntity: Entity?) {
    try {
        if (impactDrops == null) return
        val context = DropContext(
            world = world,
            player = user,
            hitEntity = hitEntity,
            pos = if (hitEntity != null) GAME_API.getEntityPos(hitEntity) else GAME_API.getEntityPos(entity),
            sourceId = sourceId,
        )
        runRandomDrop(impactDrops, luck = 0, context = context, showOutput = true)
    } catch (e: java.lang.Exception) {
        GAME_API.logError("Error in lucky_projectile impact", e)
    }
}

fun LuckyProjectileData.toAttr(): DictAttr {
    return dictAttrOf(
        "sourceId" to stringAttrOf(sourceId),
        "trail" to trailDrops?.let { dictAttrOf(
            "frequency" to doubleAttrOf(trailFreqPerTick),
            "drops" to dropsToAttrList(it)
        )},
        "impact" to impactDrops?.let { dropsToAttrList(it) },
    )
}

fun LuckyProjectileData.Companion.fromAttr(attr: DictAttr): LuckyProjectileData {
    return try {
        LuckyProjectileData(
            trailFreqPerTick = (attr["trail"] as DictAttr?)?.getValue("frequency") ?: 0.0,
            trailDrops = (attr["trail"] as DictAttr?)?.let { dropsFromAttrList(it.getList("drops")) },
            impactDrops = (attr["impact"] as ListAttr?)?.let { dropsFromAttrList(it) },
            sourceId = attr.getOptionalValue<String>("sourceId") ?: JavaLuckyRegistry.blockId,
        )
    } catch (e: java.lang.Exception) {
        GAME_API.logError("Error loading lucky_projectile", e)
        LuckyProjectileData()
    }
}

fun LuckyProjectileData.writeToTag(tag: NBTTag) {
    writeNBTKeys(tag, toAttr())
}
fun LuckyProjectileData.Companion.readFromTag(tag: NBTTag): LuckyProjectileData {
    return fromAttr(readNBTKeys(tag, attrKeys))
}