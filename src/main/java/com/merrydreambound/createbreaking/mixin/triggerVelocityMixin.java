package com.merrydreambound.createbreaking.mixin;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.BlockStateConditionSet;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinition;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinitionLoader;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;

import java.util.Map;
import java.util.Optional;

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
            mass = 0.125;
        } else if (mass == 0.25) {
            mass = 0.25*4;
        } else if (mass == 0.5) {
            mass = 0.5*4;
        } else if (mass == 1) {
            mass = 1*4;
        } else if (mass == 4) {
            mass = 4*4;
        }
        double velocity = 4 * mass;
//        LogUtils.getLogger().info(String.valueOf(velocity));
        return velocity;
    }
}