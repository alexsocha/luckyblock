package mod.lucky.common

import kotlin.math.*

val colorNames = listOf(
    "black",
    "blue",
    "brown",
    "cyan",
    "gray",
    "green",
    "green",
    "light_blue",
    "light_gray",
    "lime",
    "magenta",
    "orange",
    "pink",
    "purple",
    "red",
    "white",
    "yellow"
)

@Suppress("UNCHECKED_CAST")
fun <T : Number>add(n1: T, n2: T): T {
    return when {
        n1 is Double && n2 is Double -> (n1 + n2) as T
        n1 is Int && n2 is Int -> (n1 + n2) as T
        else -> throw Exception()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Number>subtract(n1: T, n2: T): T {
    return when {
        n1 is Double && n2 is Double -> (n1 - n2) as T
        n1 is Int && n2 is Int -> (n1 - n2) as T
        else -> throw Exception()
    }
}

data class Vec3<T : Number>(val x: T, val y: T, val z: T) {
    constructor(pos: Vec3<T>): this(pos.x, pos.y, pos.z)
    fun toDouble(): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
    fun toFloat(): Vec3<Float> = Vec3<Float>(x.toFloat(), y.toFloat(), z.toFloat())
    fun floor(): Vec3i = Vec3i(floor(x.toDouble()).toInt(), floor(y.toDouble()).toInt(), floor(z.toDouble())
        .toInt())
    operator fun plus(vec: Vec3<T>): Vec3<T> = Vec3(add(this.x, vec.x), add(this.y, vec.y), add(this.z, vec.z))
    operator fun minus(vec: Vec3<T>): Vec3<T> = Vec3(subtract(this.x, vec.x), subtract(this.y, vec.y), subtract(this.z, vec.z))
}

typealias Vec3i = Vec3<Int>
typealias Vec3d = Vec3<Double>
typealias BlockPos = Vec3i
val zeroVec3d = Vec3d(0.0, 0.0, 0.0)

interface Random {
    fun randInt(range: IntRange): Int
    fun nextDouble(): Double

    fun randDouble(min: Double, max: Double): Double {
        return min + (max - min) * nextDouble()
    }
}

class DefaultRandom(
    private val random: kotlin.random.Random = kotlin.random.Random.Default,
) : Random {
    override fun randInt(range: IntRange): Int = range.random(random)
    override fun nextDouble(): Double = random.nextDouble()
}

val DEFAULT_RANDOM = DefaultRandom()

fun <K, V>mapOfNotNull(vararg pairs: Pair<K, V?>): Map<K, V> {
    @Suppress("UNCHECKED_CAST")
    return mapOf(*(pairs.filter { (_, v) -> v != null } as List<Pair<K, V>>).toTypedArray())
}

fun positiveMod(n: Int, m: Int): Int {
    val modN = n.rem(m)
    return if (modN < 0) modN + m else modN
}

fun positiveMod(n: Double, m: Double): Double {
    val modN = n.rem(m)
    return if (modN < 0) modN + m else modN
}

fun degToRad(deg: Double): Double {
    return deg / 180.0 * PI
}

fun distanceBetween(vec1: Vec3d, vec2: Vec3d): Double {
    val dx: Double = vec1.x - vec2.x
    val dy: Double = vec1.y - vec2.y
    val dz: Double = vec1.z - vec2.z
    return sqrt(dx * dx + dy * dy + dz * dz)
}

fun <T> chooseRandomFrom(random: Random, list: List<T>): T {
    return list[random.randInt(list.indices)]
}

fun directionToVelocity(yawRad: Double, pitchRad: Double, speed: Double): Vec3d {
    return Vec3d(
        -sin(yawRad) * cos(pitchRad) * speed,
        -sin(pitchRad) * speed,
        cos(yawRad) * cos(pitchRad) * speed,
    )
}

fun rotateVec3d(vec: Vec3d, rotationRad: Double): Vec3d {
    return Vec3d(
        vec.x * cos(rotationRad) - vec.z * sin(rotationRad),
        vec.y,
        vec.x * sin(rotationRad) + vec.z * cos(rotationRad),
    )
}

fun <T>chooseMultiRandomFrom(random: Random, list: List<T>, amountRange: IntRange): List<T> {
    val removeAmount = list.size - random.randInt(amountRange)
    val newList = ArrayList(list)
    (0 until removeAmount).forEach { _ -> newList.removeAt(random.randInt(newList.indices)) }
    return newList
}

fun rotatePos(pos: Vec3d, centerOffset: Vec3d, rotation: Int): Vec3d {
    val modRotation = positiveMod(rotation, 4)
    var posX: Double = pos.x - centerOffset.x
    val posY: Double = pos.y - centerOffset.y
    var posZ: Double = centerOffset.z - pos.z
    for (i in 0 until modRotation) {
        val x = posX
        val z = posZ
        posX = z
        posZ = -x
    }
    return Vec3d(posX + centerOffset.x, posY + centerOffset.y, centerOffset.z - posZ)
}

fun getWorldPos(
    posOffset: Vec3d, centerOffset: Vec3d, worldCenter: Vec3d, rotation: Int
): Vec3d {
    return rotatePos(
        (worldCenter + posOffset) - centerOffset,
        worldCenter, rotation
    )
}
