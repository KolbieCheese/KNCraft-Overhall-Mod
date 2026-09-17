package com.beautyinblocks.tents.mixin;

import com.beautyinblocks.tents.TentPortals;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class PongMixin {
    // Explicit Forge 1.20.1 SRG name: handlePong. No client addon is required.
    @Inject(method = "m_142110_", at = @At("HEAD"), remap = false)
    private void acknowledge(ServerboundPongPacket packet, CallbackInfo ci) {
        var player = ((ServerGamePacketListenerImpl)(Object)this).player;
        player.getServer().execute(() -> TentPortals.acknowledge(player, packet.getId()));
    }
}
