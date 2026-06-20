package com.merrydreambound.sablefragile.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(SavedData.class)
public class UncompressedSavingMixin {
    @WrapWithCondition(method = "lambda$save$0", at = @At(value = "INVOKE",target="Lnet/neoforged/neoforge/common/IOUtilities;writeNbtCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"))
    private boolean uncompressedDataSave(CompoundTag tag, Path path){
        LogUtils.getLogger().info(String.valueOf(path));
        if (path.endsWith("blockProgressArray.dat") || path.endsWith("level.dat")){
            try{
                LogUtils.getLogger().info("Let's fucking gooo");

                net.neoforged.neoforge.common.IOUtilities.writeNbt(tag, path);
            } catch (IOException e) {
                LogUtils.getLogger().error("PANIC! Failed to save data");
                return true;
            }
            return false;
        }else{
            return true;
        }
    }
}
