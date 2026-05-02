//package com.merrydreambound.createbreaking.mixin.ResetBlockBreakingProgressMixin;
//
//import com.merrydreambound.createbreaking.BlockBreakingProgress;
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.level.LevelWriter;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(LevelWriter.class)
//public interface ResetInterfaceMixin {
//    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z",at = @At(value = "HEAD"))
//    default void reset(BlockPos pos, boolean dropBlock, CallbackInfoReturnable<Boolean> cir){
////        if (level instanceof ServerLevel){
////            BlockBreakingProgress blockBreakingProgress = BlockBreakingProgress.get((ServerLevel) level);
////            blockBreakingProgress.resetProgress(pos);
////            level.destroyBlockProgress(pos.hashCode(),pos,-1);
////        }
//    }
//
//    @Inject(method = "removeBlock")
//}
