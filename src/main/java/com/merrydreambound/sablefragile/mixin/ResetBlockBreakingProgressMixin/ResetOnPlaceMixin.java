package com.merrydreambound.sablefragile.mixin.ResetBlockBreakingProgressMixin;


import com.merrydreambound.sablefragile.BlockBreakingProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEvent.EntityPlaceEvent.class)
public class ResetOnPlaceMixin {

    @Inject(method = "<init>", at = @At(value = "HEAD"))
    private static void resetOnPlace(BlockSnapshot blockSnapshot, BlockState placedAgainst, Entity entity, CallbackInfo ci){
        Level level = entity.level();
        if (level instanceof ServerLevel){
            BlockPos pos = blockSnapshot.getPos();
            BlockBreakingProgress blockBreakingProgress = BlockBreakingProgress.get((ServerLevel) level);
            blockBreakingProgress.resetProgress(pos);
            level.destroyBlockProgress(pos.hashCode(),pos,-1);
        }
    }
}
