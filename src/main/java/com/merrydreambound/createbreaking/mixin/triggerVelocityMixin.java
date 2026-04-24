package com.merrydreambound.createbreaking.mixin;

import com.merrydreambound.createbreaking.CreateBreaking;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinition;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;

@Mixin(FragileBlockCallback.class)
public class triggerVelocityMixin {
    @ModifyVariable(method = "sable$onCollision", at = @At("STORE"), name = "triggerVelocity")
    private double getTriggerVelocityMassBased(double triggerVelocity, BlockPos pos) {


        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.getCurrentlySteppingSystem();
        final ServerLevel level = system.getLevel();
        final BlockState state = level.getBlockState(pos);

        final ObjectList<PhysicsBlockPropertiesDefinition> definitions = new ObjectArrayList<>();
//        ((BlockStateExtension) state).sable$loadProperties(state.getBlock().getStateDefinition(), definitions.get(1));
        double mass = ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());


        if (mass == 0) {
            mass = CreateBreaking.CONFIG.Weightless_TriggerSpeed.get();
        } else if (mass == 0.25) {
            mass = CreateBreaking.CONFIG.Super_Light_TriggerSpeed.get() * 4;
        } else if (mass == 0.5) {
            mass = CreateBreaking.CONFIG.Light_TriggerSpeed.get() * 4;
        } else if (mass == 1) {
            mass = CreateBreaking.CONFIG.Default_TriggerSpeed.get() * 4;
        } else if (mass == 2) {
            mass = CreateBreaking.CONFIG.Heavy_TriggerSpeed.get() * 4;
        } else if (mass == 4) {
            mass = CreateBreaking.CONFIG.Super_Heavy_TriggerSpeed.get() * 4;
        }
        double triggerVelocityMixin = 4 * mass;
//        LogUtils.getLogger().info("Config super heavy:" + String.valueOf(CreateBreaking.CONFIG.Super_Heavy_TriggerSpeed.get()));
        return triggerVelocityMixin;
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"), index = 1)
    private boolean removeBlockDrops(boolean par2) {
        return false;
    }
}