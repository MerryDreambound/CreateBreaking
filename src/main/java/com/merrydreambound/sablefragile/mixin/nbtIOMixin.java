package com.merrydreambound.sablefragile.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(NbtIo.class)
public class nbtIOMixin {

    @Shadow
    public static void write(CompoundTag compoundTag, Path path) throws IOException {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    /**
     * @author
     * @reason
     */
    @Inject(method = "writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V", at = @At("HEAD"), cancellable = true)
    private static void writeThing(CompoundTag compoundTag, Path path, CallbackInfo ci) throws IOException {
        LogUtils.getLogger().info(String.valueOf(path));
        if (!path.endsWith("level.dat")){
            write(compoundTag,path);
            ci.cancel();
        }

    }

}
