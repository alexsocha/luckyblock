package mod.lucky.java

import mod.lucky.common.*
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.registerVec3TemplateVar
import java.util.*

fun registerJavaTemplateVars() {
    LuckyRegistry.registerTemplateVar("pUUID") { _, context ->
        stringAttrOf(context.dropContext!!.player?.let { JAVA_GAME_API.getEntityUUID(it) } ?: "")
    }

    LuckyRegistry.registerTemplateVar("pUUIDArray") { _, context ->
        ValueAttr(AttrType.INT_ARRAY, context.dropContext!!.player?.let {
            val uuid = UUID.fromString(JAVA_GAME_API.getEntityUUID(it))
            intArrayOf(
                (uuid.mostSignificantBits shr 32).toInt(),
                uuid.mostSignificantBits.toInt(),
                (uuid.leastSignificantBits shr 32).toInt(),
                uuid.leastSignificantBits.toInt(),
            )
        } ?: intArrayOf())
    }

    registerVec3TemplateVar("bowPos", AttrType.DOUBLE) { _, context ->
        val dropContext = context.dropContext!!
        dropContext.player?.let {
            JAVA_GAME_API.getArrowPosAndVelocity(dropContext.world, it, dropContext.bowPower).first
        }
    }

    val bowMotionSpec = TemplateVarSpec(
        listOf(
            Pair("power", ValueSpec(AttrType.DOUBLE)),
            Pair("inaccuracyDeg", ValueSpec(AttrType.DOUBLE))
        ),
        argRange = 0..2
    )
    registerVec3TemplateVar("bowMotion", AttrType.DOUBLE, bowMotionSpec) { templateVar, context ->
        val power = templateVar.args.getOptionalValue(0) ?: 1.0
        val inaccuracyDeg = templateVar.args.getOptionalValue(1) ?: 0.0
        val dropContext = context.dropContext!!
        dropContext.player?.let {
            JAVA_GAME_API.getArrowPosAndVelocity(
                world = dropContext.world,
                player = it,
                bowPower = dropContext.bowPower * power,
                yawOffsetDeg = context.random.randDouble(-inaccuracyDeg, inaccuracyDeg),
                pitchOffsetDeg = context.random.randDouble(-inaccuracyDeg, inaccuracyDeg)
            ).second
        }
    }

    val chestLootSpec = TemplateVarSpec(listOf("lootTableId" to ValueSpec(AttrType.STRING)))
    LuckyRegistry.registerTemplateVar("chestLootTable", chestLootSpec) { templateVar, context ->
        JAVA_GAME_API.generateChestLoot(
            world=context.dropContext!!.world,
            pos=context.dropContext.pos.floor(),
            lootTableId=(templateVar.args[0] as ValueAttr).value as String,
            random=context.random,

        )
    }

    // compatibility
    LuckyRegistry.registerTemplateVar("chestVillageArmorer") { _, context ->
        JAVA_GAME_API.generateChestLoot(
            world=context.dropContext!!.world,
            pos=context.dropContext.pos.floor(),
            "chests/village/village_armorer",
            random=context.random,
        )
    }
    LuckyRegistry.registerTemplateVar("chestBonusChest") { _, context ->
        JAVA_GAME_API.generateChestLoot(
            world=context.dropContext!!.world,
            pos=context.dropContext.pos.floor(),
            lootTableId="chests/spawn_bonus_chest",
            random=context.random
        )
    }
    LuckyRegistry.registerTemplateVar("chestDungeonChest") { _, context ->
        JAVA_GAME_API.generateChestLoot(
            world=context.dropContext!!.world,
            pos=context.dropContext.pos.floor(),
            lootTableId="chests/simple_dungeon",
            random=context.random
        )
    }
}
