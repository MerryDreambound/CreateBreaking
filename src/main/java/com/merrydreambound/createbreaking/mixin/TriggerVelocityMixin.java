package com.merrydreambound.createbreaking.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.merrydreambound.createbreaking.BlockBreakingProgress;
import com.merrydreambound.createbreaking.CollisionBody;
import com.merrydreambound.createbreaking.CreateBreaking;
import com.merrydreambound.createbreaking.config.CreateBreakingConfig;
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

import java.util.Objects;
import java.util.UUID;

@Mixin(FragileBlockCallback.class)
public class TriggerVelocityMixin {

    @Unique
    private CollisionBody bodyA;
    @Unique
    private CollisionBody bodyB;



    @WrapWithCondition(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean disableBreaking(ServerLevel instance, BlockPos pos, boolean b) {
        return false;
    }

    @WrapOperation(method = "sable$onCollision", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/callback/FragileBlockCallback;onHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lorg/joml/Vector3d;)Ldev/ryanhcode/sable/api/physics/callback/BlockSubLevelCollisionCallback$CollisionResult;"))
    private BlockSubLevelCollisionCallback.CollisionResult wrapOnCollision(FragileBlockCallback instance, ServerLevel level, BlockPos pos, BlockState state, Vector3d hitPos, Operation<BlockSubLevelCollisionCallback.CollisionResult> original, @Local(argsOnly = true) double impactVelocity) {

        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.getCurrentlySteppingSystem();
        final ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
//        double mass = PhysicsBlockPropertyHelper.getMass(level, pos, state);
        if (bodyA == null) {
            bodyA = new CollisionBody(getServerSubLevelUUID(level, hitPos), pos, hitPos, impactVelocity);
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }

        bodyB = new CollisionBody(getServerSubLevelUUID(level, hitPos), pos, hitPos, impactVelocity);






        if (Objects.equals(bodyA.id, new UUID(0, 0))) {
            // bodyA is world
            // bodyB is contraption
            BlockSubLevelCollisionCallback.CollisionResult collisionResult = worldContraptionCollision(level, impactVelocity, container, system, bodyA, bodyB);
            bodyA = null;
            bodyB = null;
            return collisionResult;
        } else {
            if (Objects.equals(bodyB.id, new UUID(0, 0))) {
                //BodyB is world
                //BodyA is contraption
                BlockSubLevelCollisionCallback.CollisionResult collisionResult = worldContraptionCollision(level, impactVelocity, container, system, bodyB, bodyA);
                bodyA = null;
                bodyB = null;
                return collisionResult;

            } else {
                // bodyA is contraption, body B is contraption
            }

        }

        return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
    }

    /**
     * @param level Level instance
     * @param impactVelocity Impact velocity between the 2 bodies
     * @param container Sable container
     * @param system Sable system
     * @param world CollisionBody world instance
     * @param contraption CollisionBody contraption instance
     */
    private BlockSubLevelCollisionCallback.CollisionResult worldContraptionCollision(ServerLevel level, double impactVelocity, ServerSubLevelContainer container, SubLevelPhysicsSystem system, CollisionBody world, CollisionBody contraption) {

        double gravity = DimensionPhysicsData.getGravity(level).length();

        BlockPos worldHitPos = world.pos;
        BlockState worldBlockState = level.getBlockState(worldHitPos);
        double worldBlockMass = ((BlockStateExtension) worldBlockState).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
        double worldBlockBounciness = ((BlockStateExtension) worldBlockState).sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());
        BlockPos contraptionHitPos = contraption.pos;
        BlockState contraptionBlockState = level.getBlockState(contraptionHitPos);
        double contraptionBlockMass = ((BlockStateExtension) contraptionBlockState).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
        double conptrationBlockBounciness = ((BlockStateExtension) contraptionBlockState).sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());


        if (container == null) return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        ServerSubLevel contraptionSubLevel = (ServerSubLevel) container.getSubLevel(contraption.id);
        if (contraptionSubLevel == null) return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        double contraptionMass = contraptionSubLevel.getMassTracker().getMass();
        // Avoid making 1 block falls dig through the whole world
        double minKineticEnergy = gravity * contraptionMass * CreateBreaking.CONFIG.MinHeight.get();

        RigidBodyHandle handle = system.getPhysicsHandle(contraptionSubLevel);
        if (handle == null) {
            LogUtils.getLogger().info("HANDLER IS NULL");
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);

        }

        double effectiveMass = Math.min(worldBlockMass, contraptionBlockMass);
        double contraptionRatio = effectiveMass / contraptionBlockMass;
        double worldRatio = effectiveMass / worldBlockMass;



        Vector3d currentVelocity = handle.getLinearVelocity(new Vector3d());
        double kineticEnergy = 0.5 * impactVelocity * impactVelocity * contraptionMass;
        double speedCost = effectiveMass * (1.0 - worldBlockBounciness) * CreateBreaking.CONFIG.SpeedCost.get();
        double penetrationDepthCost = worldBlockMass * CreateBreaking.CONFIG.PenetrationCost.get();
        //Calculate penetration
        if (penetrationDepthCost == 0) {
            penetrationDepthCost = 0.125;
        }
        if (speedCost == 0) {
            speedCost = 0.125;
        }
        double newEnergy = Math.max(0.0, kineticEnergy - speedCost);
        double newSpeed = Math.sqrt(2.0 * newEnergy / contraptionMass);
        double massNewton = contraptionMass * (impactVelocity - newSpeed);
        BlockBreakingProgress blockBreakingProgress = BlockBreakingProgress.get(level);
        double minFallSpeed = (Math.sqrt(gravity * 2) * CreateBreaking.CONFIG.MinHeight.get());
        boolean oneBlockFall = currentVelocity.y() >= -minFallSpeed && currentVelocity.y() < 0;
        if (newEnergy < penetrationDepthCost || oneBlockFall){
            int worldDamage = blockBreakingProgress.getDamage(world.pos);
            worldDamage += (int) Math.min(((kineticEnergy / penetrationDepthCost * 10) * worldRatio),9);
            int contraptionDamage = blockBreakingProgress.getDamage(contraption.pos);
            contraptionDamage += (int) ((kineticEnergy / penetrationDepthCost * 10) * contraptionRatio);
            blockBreakingProgress.setDamage(contraption.pos, contraptionDamage);
            checkToBreak(blockBreakingProgress, level, contraption.pos);
            return getCollisionResult(level, contraptionMass, handle, new Vector3d(currentVelocity), world.pos, blockBreakingProgress, worldDamage);
        }else{
            if (worldRatio < 1){
                int contraptionDamage = blockBreakingProgress.getDamage(contraption.pos);
                contraptionDamage += (int) Math.max((kineticEnergy / penetrationDepthCost * (10*worldRatio)) * contraptionRatio,1);
                blockBreakingProgress.setDamage(contraption.pos, contraptionDamage);
                checkToBreak(blockBreakingProgress, level, contraption.pos);
            }else{
                level.destroyBlock(world.pos, false);
            }
            Vector3d deltaVelocity = new Vector3d(new Vector3d(currentVelocity).normalize()).mul(-massNewton);

            handle.applyLinearAndAngularImpulse(deltaVelocity, JOMLConversion.ZERO, true);
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);

        }
    }

    @Unique
    private BlockSubLevelCollisionCallback.CollisionResult getCollisionResult(ServerLevel level, double contraptionMass, RigidBodyHandle handle, Vector3d currentVelocity, BlockPos hitBlockPos, BlockBreakingProgress blockBreakingProgress, int progress) {
        blockBreakingProgress.setDamage(hitBlockPos, progress);
        checkToBreak(blockBreakingProgress, level, hitBlockPos);
//        handle.applyLinearAndAngularImpulse(new Vector3d(currentVelocity.normalize()).mul(-(currentVelocity.length() * contraptionMass )), JOMLConversion.ZERO, true);
        return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
    }
    private static UUID getServerSubLevelUUID(final Level level, final Vector3dc pos) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel instanceof ServerSubLevel serverSubLevel) {
            return serverSubLevel.getUniqueId();
        }
        return new UUID(0,0);
    }

    @Unique
    private void checkToBreak(BlockBreakingProgress breakingProgress, Level level, BlockPos pos) {
        int blockProgress = breakingProgress.getDamage(pos);
        boolean broken = blockProgress >= 10;
        if (broken) {
            breakingProgress.resetProgress(pos);
            level.destroyBlock(pos, false);
        } else {
            breakingProgress.setDamage(pos, blockProgress);
            level.destroyBlockProgress(pos.hashCode(), pos, blockProgress);
        }
    }

}