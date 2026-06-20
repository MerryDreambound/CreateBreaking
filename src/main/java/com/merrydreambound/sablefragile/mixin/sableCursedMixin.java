package com.merrydreambound.sablefragile.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelStorageFile;
import org.jline.utils.OSUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Mixin(SubLevelStorageFile.class)
public class sableCursedMixin {

    @Shadow
    @Final
    private FileChannel file;

    @WrapOperation(method = "<init>(Ljava/nio/file/Path;Ljava/nio/file/Path;I)V", at = @At(value = "INVOKE", target = "Ljava/nio/channels/FileChannel;open(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/nio/channels/FileChannel;"))
    FileChannel sableOptimizationRealFRFR(Path path, OpenOption[] options, Operation<FileChannel> original) throws IOException {
        if (OSUtils.IS_WINDOWS) {
            return FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
        } else {
            return FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        }
    };


    @Inject(method = "write(ILjava/nio/ByteBuffer;)V", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/storage/region/SubLevelStorageFile;writeHeader()V", shift = At.Shift.AFTER))
    void sableWriteHeaderSync(int index, ByteBuffer byteBuffer, CallbackInfo ci) throws IOException {
        file.force(false);
    }
}
