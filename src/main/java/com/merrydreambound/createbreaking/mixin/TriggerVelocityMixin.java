package com.merrydreambound.createbreaking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.merrydreambound.createbreaking.CreateBreaking;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinition;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;

@Mixin(FragileBlockCallback.class)
public class TriggerVelocityMixin {
    @ModifyExpressionValue(method = "sable$onCollision", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/callback/FragileBlockCallback;getTriggerVelocity()D"))
    private double getTriggerVelocityMassBased(double triggerVelocity, BlockPos pos) {

        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.getCurrentlySteppingSystem();
        final ServerLevel level = system.getLevel();
        final BlockState state = level.getBlockState(pos);

        double mass = ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());


        if (mass == 0) {
            mass = CreateBreaking.CONFIG.Weightless_TriggerSpeed.get();
        } else if (mass == 0.25) {
            mass = CreateBreaking.CONFIG.Super_Light_TriggerSpeed.get();
        } else if (mass == 0.5) {
            mass = CreateBreaking.CONFIG.Light_TriggerSpeed.get();
        } else if (mass == 1) {
            mass = CreateBreaking.CONFIG.Default_TriggerSpeed.get();
        } else if (mass == 2) {
            mass = CreateBreaking.CONFIG.Heavy_TriggerSpeed.get();
        } else if (mass == 4) {
            mass = CreateBreaking.CONFIG.Super_Heavy_TriggerSpeed.get();
        }
        double triggerVelocityMixin = 4 * mass;
        return triggerVelocityMixin;
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"), index = 1)
    private boolean removeBlockDrops(boolean par2) {
        return false;
    }

//    @ModifyReturnValue(method = "Ldev/ryanhcode/sable/physics/callback/FragileBlockCallback;onHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lorg/joml/Vector3d;)Ldev/ryanhcode/sable/api/physics/callback/BlockSubLevelCollisionCallback$CollisionResult;", at = @At(value = "RETURN"))
//    private BlockSubLevelCollisionCallback.CollisionResult addFeedbackForce(BlockSubLevelCollisionCallback.CollisionResult original,final ServerLevel level, final BlockPos pos, final BlockState state, final Vector3d hitPos,@Local double impactVelocity){
//        Vector3d normal = new Vector3d(hitPos).sub(pos.getX(), pos.getY(), pos.getZ());
//        // Bounciness? Opposite force? I have no idea lmao
//        double impulseMagnitude = impactVelocity * 0.05;
//        Vector3d fedbackForce = normal.mul(impulseMagnitude);
//        return new BlockSubLevelCollisionCallback.CollisionResult(fedbackForce, original.removeCollision());
//    }
//    @WrapOperation(method = "sable$onCollision", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/callback/FragileBlockCallback;onHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lorg/joml/Vector3d;)Ldev/ryanhcode/sable/api/physics/callback/BlockSubLevelCollisionCallback$CollisionResult;"))
//    private BlockSubLevelCollisionCallback.CollisionResult wrapOnCollision(FragileBlockCallback instance, ServerLevel level, BlockPos pos, BlockState state, Vector3d hitPos, Operation<BlockSubLevelCollisionCallback.CollisionResult> original, @Local(argsOnly = true) double impactVelocity){
//        BlockSubLevelCollisionCallback.CollisionResult result = original.call(instance, level, pos, state, hitPos);
//
//        return new BlockSubLevelCollisionCallback.CollisionResult(newSpeed, result.removeCollision());
//    }
}