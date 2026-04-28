package com.merrydreambound.createbreaking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jline.utils.Log;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;

import javax.annotation.Nullable;

@Mixin(FragileBlockCallback.class)
public class TriggerVelocityMixin {

    @Shadow
    @Final
    public static FragileBlockCallback INSTANCE;

    private static @Nullable ServerSubLevel getServerSubLevel(final Level level, final BlockPos pos) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel instanceof ServerSubLevel serverSubLevel) {
            return serverSubLevel;
        }
        return null;
    }

    @WrapWithCondition(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean disableBreaking(ServerLevel instance, BlockPos pos, boolean b) {
        return false;
    }

    @WrapOperation(method = "sable$onCollision", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/callback/FragileBlockCallback;onHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lorg/joml/Vector3d;)Ldev/ryanhcode/sable/api/physics/callback/BlockSubLevelCollisionCallback$CollisionResult;"))
    private BlockSubLevelCollisionCallback.CollisionResult wrapOnCollision(FragileBlockCallback instance, ServerLevel level, BlockPos pos, BlockState state, Vector3d hitPos, Operation<BlockSubLevelCollisionCallback.CollisionResult> original, @Local(argsOnly = true) double impactVelocity) {

        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.getCurrentlySteppingSystem();
        final ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
//        double mass = PhysicsBlockPropertyHelper.getMass(level, pos, state);
        if (container == null){
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }
        ServerSubLevel sublevel = getServerSubLevel(level, pos);
        if (sublevel == null){
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }
        if (sublevel instanceof ServerSubLevel) {
            double mass = sublevel.getMassTracker().getMass();
            double bounciness = ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());

            RigidBodyHandle handle = system.getPhysicsHandle(sublevel);
            Vector3d currentVelocity = handle.getLinearVelocity(new Vector3d());
            double currentSpeed = impactVelocity;
            BlockPos hitBlockPos = new BlockPos((int) (sublevel.logicalPose().position().x() - 0.5 + Math.clamp(currentVelocity.x(), -1.51, 1.51)),
                    (int) (sublevel.logicalPose().position().y() + Math.clamp(currentVelocity.y(), -1.51, 1.51)),
                    (int) (sublevel.logicalPose().position().z() + Math.clamp(currentVelocity.z(), -1.51, 1.51)));
            BlockState hitBlockState = level.getBlockState(hitBlockPos);


            double hitBlockMass = ((BlockStateExtension) hitBlockState).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
            double speedCost = hitBlockMass * hitBlockMass * (1.0 - bounciness);
            double newSpeed = Math.max(0.0, currentSpeed - speedCost);
            double velocityRatioLoss;

            if (currentSpeed != 0){
                velocityRatioLoss = newSpeed / currentSpeed;
            }else{
                velocityRatioLoss = 1;
            }
            Vector3d velocityLoss = currentVelocity.mul(-(1.0 - velocityRatioLoss), new Vector3d());
            handle.addLinearAndAngularVelocity(velocityLoss, JOMLConversion.ZERO);

            //Calculate penetration
            double kineticEnergy = 0.5 * currentSpeed * currentSpeed * mass;
            double penetrationDepthCost = hitBlockMass * hitBlockMass;
            if (penetrationDepthCost == 0) {
                penetrationDepthCost = 0.125;
            }
            double penetrationDepth = kineticEnergy / penetrationDepthCost;
            if (penetrationDepth >= 1.0) {
                level.destroyBlock(hitBlockPos, true);
            }
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }
        return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
    }
}