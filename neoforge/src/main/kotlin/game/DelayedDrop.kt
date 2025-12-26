package mod.lucky.neoforge.game

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.serialization.Codec
import mod.lucky.common.GAME_API
import mod.lucky.common.attribute.*
import mod.lucky.common.drop.DropContext
import mod.lucky.java.fromAttr
import mod.lucky.neoforge.*
import mod.lucky.java.game.*
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import kotlin.jvm.optionals.getOrNull

class DelayedDrop(
    type: EntityType<DelayedDrop> = ForgeLuckyRegistry.delayedDrop.get(),
    world: MCWorld,
    private var data: DelayedDropData = DelayedDropData.createDefault(world),
) : MCEntity(type, world) {
    override fun defineSynchedData(builder: SynchedEntityData.Builder) {}

    override fun tick() {
        super.tick()
        data.tick(level())
        if (data.ticksRemaining <= 0) remove(RemovalReason.DISCARDED)
    }

    override fun hurtServer(p0: ServerLevel, p1: DamageSource, p2: Float): Boolean {
        return false
    }

    override fun readAdditionalSaveData(tag: ValueInput) {
        try {
            val contextTag = tag.child("context").get()
            val playerUUID = contextTag.getString("playerUUID").getOrNull()
            val hitEntityUUID = contextTag.getString("hitEntityUUID").getOrNull()

            val dropContextAttr = dictAttrOf(
                "dropPos" to contextTag.list("dropPos", Codec.DOUBLE).getOrNull()?.toList()?.let {
                    val pos = it.map { v -> doubleAttrOf(v) }
                    if (pos.size != 3) null else ListAttr(pos)
                },
                "bowPower" to doubleAttrOf(contextTag.getDoubleOr("bowPower", 0.0)),
                "playerUUID" to playerUUID?.let { stringAttrOf(it) },
                "hitEntityUUID" to hitEntityUUID?.let { stringAttrOf(it) },
                "sourceId" to stringAttrOf(contextTag.getStringOr("sourceId", ""))
            )

            data = DelayedDropData(
                singleDropString = tag.getString("drop").getOrNull(),
                ticksRemaining = tag.getIntOr("ticksRemaining", 0),
                context = DropContext.fromAttr(dropContextAttr, level())
            )
        } catch (e: Exception) {
            GAME_API.logError("Failed to parse DelayedDrop: $e")
            data = DelayedDropData.createDefault(level())
        }
    }
    override fun addAdditionalSaveData(tag: ValueOutput) {
        val parentTag = CompoundTag()
        data.writeToTag(parentTag)
        tag.store(parentTag)
    }
}

@OnlyIn(Dist.CLIENT)
open class DelayedDropRenderState : EntityRenderState() {}

@OnlyInClient
class DelayedDropRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<DelayedDrop, DelayedDropRenderState>(
    ctx) {
    override fun render(
        renderState: DelayedDropRenderState,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {}

    override fun createRenderState(): DelayedDropRenderState {
        return DelayedDropRenderState()
    }
}
