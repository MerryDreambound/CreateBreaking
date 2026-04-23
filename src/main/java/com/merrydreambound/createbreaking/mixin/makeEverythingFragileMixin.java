package com.merrydreambound.createbreaking.mixin;

import com.mojang.serialization.Codec;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import foundry.veil.platform.registry.RegistryObject;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PhysicsBlockPropertyTypes.class)
public class makeEverythingFragileMixin {
    @Shadow
    private static <T> RegistryObject<PhysicsBlockPropertyTypes.PhysicsBlockPropertyType<T>> register(ResourceLocation id, Codec<T> codec, T defaultValue) {
        throw new AssertionError();
    }
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/config/block_properties/PhysicsBlockPropertyTypes;register(Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/serialization/Codec;Ljava/lang/Object;)Lfoundry/veil/platform/registry/RegistryObject;", ordinal = 5))
    private static RegistryObject<PhysicsBlockPropertyTypes.PhysicsBlockPropertyType<Boolean>> redirectFragileRegister(ResourceLocation id, Codec<Boolean> codec, Object defaultValue) {
        return register(id, codec, true);
    }
}
