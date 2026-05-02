package com.merrydreambound.createbreaking.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.merrydreambound.createbreaking.BlockBreakingProgress;
import com.merrydreambound.createbreaking.CreateBreaking;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;

import javax.annotation.Nullable;

@Mixin(FragileBlockCallback.class)
public class TriggerVelocityMixin {

    @Unique
    private BlockPos[] hitPositionsToCheck = new BlockPos[1];

    private static @Nullable ServerSubLevel getServerSubLevel(final Level level, final Vector3dc pos) {
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
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, true);
        }
        ServerSubLevel serverSubLevel = getServerSubLevel(level, hitPos);
        if (serverSubLevel == null){
            hitPositionsToCheck[0] = pos;
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, true);

        }else {
            if (serverSubLevel instanceof ServerSubLevel) {
                double mass = serverSubLevel.getMassTracker().getMass();

                // Avoid making 1 block falls dig through the whole world
                double minVelocity = Math.sqrt(DimensionPhysicsData.getGravity(level).length() * 2) * mass;
//                if (impactVelocity < minVelocity) {
//                    return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
//                }

                double bounciness = ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());
                RigidBodyHandle handle = system.getPhysicsHandle(serverSubLevel);
                if (handle == null){
                    LogUtils.getLogger().info("HANDLER IS NULL");
                    return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, true);
                }
                Vector3d currentVelocity = handle.getLinearVelocity(new Vector3d());
                BlockPos hitBlockPos = hitPositionsToCheck[0];
                BlockState hitBlockState = level.getBlockState(hitBlockPos);
                double kineticEnergy = 0.5 * impactVelocity * impactVelocity * mass;
                double hitBlockMass = ((BlockStateExtension) hitBlockState).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
                double speedCost = hitBlockMass * (1.0-bounciness) * CreateBreaking.CONFIG.SpeedCost.get();
                BlockBreakingProgress blockBreakingProgress = BlockBreakingProgress.get(level);
                int progress = blockBreakingProgress.getProgress(hitBlockPos);

                if (kineticEnergy<speedCost){
                    int breakingState = (int) ((kineticEnergy/speedCost) * 10);
                    progress += Math.clamp(breakingState,0,9);
                    blockBreakingProgress.setProgress(hitBlockPos,progress);
                    level.destroyBlockProgress(hitBlockPos.hashCode(),hitBlockPos,progress);
                    if (progress >= 10){
                        blockBreakingProgress.resetProgress(hitBlockPos);

                        level.destroyBlock(hitBlockPos, false);
                        handle.applyLinearAndAngularImpulse(new Vector3d(currentVelocity.normalize()).mul(-(currentVelocity.length() *mass*2)), JOMLConversion.ZERO,true);
                        return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, true);

                    }
                    handle.applyLinearAndAngularImpulse(new Vector3d(currentVelocity.normalize()).mul(-(currentVelocity.length() *mass)), JOMLConversion.ZERO,true);
                    return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, true);
                }
                double newEnergy = Math.max(0.0, kineticEnergy - speedCost);
                double newSpeed = Math.sqrt(2.0 * newEnergy / mass);
                double massNewton = mass * (impactVelocity - newSpeed);
                Vector3d deltaVelocity = new Vector3d(currentVelocity.normalize()).mul(-massNewton);

                //Calculate penetration
                double penetrationDepthCost = hitBlockMass * CreateBreaking.CONFIG.PenetrationCost.get();
                if (penetrationDepthCost == 0) {
                    penetrationDepthCost = 0.125;
                }
                double penetrationDepth = newEnergy / penetrationDepthCost;
                if (penetrationDepth >= 1.0) {
                    level.destroyBlock(hitBlockPos, false);
                    level.destroyBlockProgress(pos.hashCode(),pos,9);
                    handle.applyLinearAndAngularImpulse(deltaVelocity, JOMLConversion.ZERO,true);
                }
            }
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, true);
        }
    }
}