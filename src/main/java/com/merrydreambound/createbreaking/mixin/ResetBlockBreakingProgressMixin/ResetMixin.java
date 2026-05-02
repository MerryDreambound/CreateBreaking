package com.merrydreambound.createbreaking.mixin.ResetBlockBreakingProgressMixin;

import com.merrydreambound.createbreaking.BlockBreakingProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEvent.BreakEvent.class)
public abstract class ResetMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "TAIL"))
    private void resetProgress(Level level, BlockPos pos, BlockState state, Player player, CallbackInfo ci){
        if (level instanceof ServerLevel){
            BlockBreakingProgress blockBreakingProgress = BlockBreakingProgress.get((ServerLevel) level);
            blockBreakingProgress.resetProgress(pos);
            level.destroyBlockProgress(pos.hashCode(),pos,-1);
        }
    }
}
