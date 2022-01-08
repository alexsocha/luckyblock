package mod.lucky.java.loader

import mod.lucky.common.Vec3d
import mod.lucky.common.Vec3i
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.SingleDrop
import mod.lucky.common.drop.DropStructure
import mod.lucky.common.LOGGER
import mod.lucky.java.JAVA_GAME_API
import java.io.InputStream
import java.lang.Exception

fun readLegacySchematic(stream: InputStream): DropStructure {
    val nbt = try {
        JAVA_GAME_API.readCompressedNBT(stream) as DictAttr
    } catch (e: Exception) {
        LOGGER.logError("Error reading legacy schematic structure", e)
        DictAttr()
    }

    // In schematics, length is z and width is x. Here it is reversed.
    val size = Vec3i(
        nbt.getValue<Short>("Width").toInt(),
        nbt.getValue<Short>("Height").toInt(),
        nbt.getValue<Short>("Length").toInt(),
    )

    val blockIdBytes = nbt.getValue<ByteArray>("Blocks")
    val blockDataBytes = nbt.getValue<ByteArray>("Data")
    var x = 0
    var y = 0
    var z = 0
    val blockDrops = ArrayList<SingleDrop>()
    for (i in blockIdBytes.indices) {
        val blockIdInt = blockIdBytes[i].toInt()
        val blockDataInt = blockDataBytes[i].toInt()

        val blockId = JAVA_GAME_API.convertLegacyItemId(blockIdInt, blockDataInt) ?: "minecraft:stone"
        if (blockId != "minecraft:air") {
            blockDrops.add(SingleDrop("block", dictAttrOf(
                "id" to stringAttrOf(blockId),
                "posOffset" to vec3AttrOf(AttrType.DOUBLE, Vec3i(x, y, z).toDouble()),
            )))
        }
        x++
        if (x >= size.x) {
            x = 0
            z++
        }
        if (z >= size.z) {
            z = 0
            y++
        }
    }

    val entityDrops = nbt.getList("Entities").children.map {
        val posList = ((it as DictAttr)["Position"] as ListAttr?) ?: vec3AttrOf(AttrType.DOUBLE, Vec3d(0.0, 0.0, 0.0))
        SingleDrop("entity", dictAttrOf(
            "posOffset" to posList,
            "nbttag" to it,
        ))
    }

    return DropStructure(
        defaultProps = dictAttrOf("size" to vec3AttrOf(AttrType.DOUBLE, size.toDouble())),
        drops = blockDrops + entityDrops,
    )
}
