package net.heavy.leashingutils.mixin;

import com.mojang.datafixers.util.Either;
import net.heavy.leashingutils.events.AttachEvents;
import net.heavy.leashingutils.interfaces.IMobEntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(MobEntity.class)
public class MobEntityMixin implements IMobEntityMixin {
    @Shadow
    private @Nullable Entity holdingEntity;
    @Shadow private Either<UUID, BlockPos> leashNbt;
    @Shadow private int holdingEntityId;
    private boolean manualDetachTriggered = false;

    /**
     * emits ATTACH_LEASH at the end of attachLeash from MobEntity
     */
    @Inject(
            method = "attachLeash",
            at = @At("TAIL")
    )
    private void onAttachLeash(Entity entity, boolean sendPacket, CallbackInfo ci) {
        if (((MobEntity)(Object)this).getWorld().isClient) {
            if (entity.isPlayer()) {
                AttachEvents.ATTACH_LEASH.invoker().run();
            }
        }
    }

    /**
     * emits DETACH_LEASH at the end of detachLeash from MobEntity
     */
    @Inject(
            method = "detachLeash",
            at = @At("HEAD")
    )
    private void onDetachLeash(boolean sendPacket, boolean dropItem, CallbackInfo ci) {
        if (((MobEntity)(Object)this).getWorld().isClient) {
            if (this.holdingEntity != null && this.holdingEntity.isPlayer()) {
                AttachEvents.DETACH_LEASH.invoker().run();
                this.manualDetachTriggered = true;
            }
        }
    }

    /**
     * replaces the call to detachLeash from MobEntity to setHoldingEntityId by heavyTutorialMod$detachLeashFromsetHoldingEntityId
     * needed so that it doesn't trigger the mixin onDetachLeash from that function
     * it also then emits the DETACH_LEASH event if the holdingEntity isn't the player anymore (changed to a fence)
     */
    @Redirect(
            method = "setHoldingEntityId",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/MobEntity;detachLeash(ZZ)V"
            )
    )
    private void onSetHoldingEntityId(MobEntity instance, boolean sendPacket, boolean dropItem) {
        this.heavyTutorialMod$detachLeashFromsetHoldingEntityId(sendPacket, dropItem);
    }

    public void heavyTutorialMod$detachLeashFromsetHoldingEntityId(boolean sendPacket, boolean dropItem) {
        Entity newHoldingEntity = ((MobEntity)(Object)this).getWorld().getEntityById(this.holdingEntityId);

        /**
         * If detach event has been triggered by a manual action (right click on an attached mod by the player)
         * newHoldingEntity is null thus will trigger the detach event again from here
         * manualDetachTriggered prevent that behaviour
         */
        if (manualDetachTriggered) {
            this.manualDetachTriggered = false;
        } else {
            if (newHoldingEntity == null || (newHoldingEntity != null && !newHoldingEntity.isPlayer())) {
                AttachEvents.DETACH_LEASH.invoker().run();
                this.manualDetachTriggered = false;
            }
        }

        if (this.holdingEntity != null) {
            this.holdingEntity = null;
            this.leashNbt = null;
            ((MobEntity)(Object)this).clearPositionTarget();
            if (!((MobEntity)(Object)this).getWorld().isClient && dropItem) {
                ((MobEntity)(Object)this).dropItem(Items.LEAD);
            }
            if (!((MobEntity)(Object)this).getWorld().isClient && sendPacket && ((MobEntity)(Object)this).getWorld() instanceof ServerWorld) {
                ((ServerWorld)((MobEntity)(Object)this).getWorld()).getChunkManager().sendToOtherNearbyPlayers(((MobEntity)(Object)this), new EntityAttachS2CPacket(((MobEntity)(Object)this), null));
            }
        }
    }
}
