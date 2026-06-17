package com.merrydreambound.sablefragile.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.merrydreambound.sablefragile.BlockBreakingProgress;
import com.merrydreambound.sablefragile.CollisionBody;
import com.merrydreambound.sablefragile.SableFragile;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineProvider;
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
import java.util.Objects;
import java.util.UUID;

@Mixin(FragileBlockCallback.class)
public class TriggerVelocityMixin {

    @WrapWithCondition(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean disableBreaking(ServerLevel instance, BlockPos pos, boolean b) {
        return false;
    }

    @WrapOperation(method = "sable$onCollision", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/callback/FragileBlockCallback;onHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lorg/joml/Vector3d;)Ldev/ryanhcode/sable/api/physics/callback/BlockSubLevelCollisionCallback$CollisionResult;"))
    private BlockSubLevelCollisionCallback.CollisionResult wrapOnCollision(FragileBlockCallback instance, ServerLevel level, BlockPos pos, BlockState state, Vector3d hitPos, Operation<BlockSubLevelCollisionCallback.CollisionResult> original, @Local(argsOnly = true) double impactVelocity, @Local(argsOnly = true,ordinal = 1) BlockPos otherHitBlockPos) {

        LogUtils.getLogger().info(String.valueOf(otherHitBlockPos) + String.valueOf(pos));
        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.getCurrentlySteppingSystem();
        final ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
//        double mass = PhysicsBlockPropertyHelper.getMass(level, pos, state);
        if (otherHitBlockPos == null) {
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }
        LogUtils.getLogger().info("NOT NULL");

        CollisionBody bodyA = new CollisionBody(getServerSubLevelUUID(level, new Vector3d(pos.getX(), pos.getY(), pos.getZ())), pos, impactVelocity);
        CollisionBody bodyB = new CollisionBody(getServerSubLevelUUID(level, new Vector3d(otherHitBlockPos.getX(), otherHitBlockPos.getY(), otherHitBlockPos.getZ())), otherHitBlockPos, impactVelocity);


        LogUtils.getLogger().info(bodyA.toString());
        LogUtils.getLogger().info(bodyB.toString());
        BlockSubLevelCollisionCallback.CollisionResult collisionResult;


        if (Objects.equals(bodyA.id, new UUID(0, 0))) {
            // bodyA is world
            // bodyB is contraption
            collisionResult = worldContraptionCollision(level, impactVelocity, container, system, bodyA, bodyB);
        } else {
            if (Objects.equals(bodyB.id, new UUID(0, 0))) {
                //BodyB is world
                //BodyA is contraption
                collisionResult = worldContraptionCollision(level, impactVelocity, container, system, bodyB, bodyA);
            } else {
                // bodyA is contraption, body B is contraption
                if (bodyA.id != bodyB.id){
                    collisionResult = contraptionContraptionCollision(level, impactVelocity, container, system, bodyA, bodyB);
                }else{
                    return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
                }
            }

        }
        return collisionResult;
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
        if (contraptionBlockMass == 0){
            contraptionBlockMass = 1;
        }
        if (worldBlockMass == 0){
            worldBlockMass = 1;
        }
        double conptrationBlockBounciness = ((BlockStateExtension) contraptionBlockState).sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());

        if (container == null) return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        ServerSubLevel contraptionSubLevel = (ServerSubLevel) container.getSubLevel(contraption.id);
        if (contraptionSubLevel == null) return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        double contraptionMass = contraptionSubLevel.getMassTracker().getMass();

        if (contraptionMass== 0){
            contraptionMass = 1;
        }
        RigidBodyHandle handle = system.getPhysicsHandle(contraptionSubLevel);
        if (handle == null) {
            LogUtils.getLogger().info("HANDLER IS NULL");
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);

        }

        double effectiveMass = Math.min(worldBlockMass, contraptionBlockMass);
        double contraptionRatio = effectiveMass / contraptionBlockMass;
        double worldRatio = effectiveMass / worldBlockMass;


    // Crash happens here
//        Vector3d currentVelocity = impactVelocity;
        Vector3d currentVelocity = null;
        handle.getLinearVelocity(currentVelocity);
        // Crash happens here ^^



        if (currentVelocity.length() <= 0.0001){
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }
        double kineticEnergy = 0.5 * impactVelocity * impactVelocity * contraptionMass;
        double speedCost = effectiveMass * (1.0 - worldBlockBounciness) * SableFragile.CONFIG.SpeedCost.get();
        double penetrationDepthCost = worldBlockMass * SableFragile.CONFIG.PenetrationCost.get();
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
        double minFallSpeed = (Math.sqrt(gravity * 2) * SableFragile.CONFIG.MinHeight.get());
        boolean oneBlockFall = currentVelocity.y() >= -minFallSpeed && currentVelocity.y() < 0;
        boolean canPenetrate = newEnergy >= (penetrationDepthCost*contraptionRatio);
        Vector3d deltaVelocity = new Vector3d(new Vector3d(currentVelocity).normalize()).mul(-massNewton);

        boolean contraptionBroken = false;
        double contraptionDamageDone;
        if (SableFragile.CONFIG.ExtraFragileWorld.get()){
            contraptionDamageDone = Math.max(((kineticEnergy / penetrationDepthCost * (10 * worldRatio)) * contraptionRatio), 1);
        }else{
            contraptionDamageDone = Math.max(((kineticEnergy / penetrationDepthCost * (10)) * contraptionRatio), 1);
        }

        if (!canPenetrate){
            int worldDamage = blockBreakingProgress.getDamage(world.pos);
            worldDamage += (int) Math.min(((kineticEnergy / penetrationDepthCost * 10) * worldRatio),9);
            checkToBreak(blockBreakingProgress,level,world.pos,worldDamage);
            checkToBreak(blockBreakingProgress, level, contraption.pos,(int)contraptionDamageDone);
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }else{
            if (worldRatio < 1){
                contraptionBroken = checkToBreak(blockBreakingProgress, level, contraption.pos,(int)contraptionDamageDone);
            }else{
                level.destroyBlock(world.pos, false);
            }

            if (!contraptionBroken){
                handle.applyLinearAndAngularImpulse(deltaVelocity, JOMLConversion.ZERO, true);
            }

            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);

        }
    }

    /**
     * @param level Level instance
     * @param impactVelocity Impact velocity between the 2 bodies
     * @param container Sable container
     * @param system Sable system
     * @param contraptionA CollisionBody contraptionA instance
     * @param contraptionB CollisionBody contraptionB instance
     */
    private BlockSubLevelCollisionCallback.CollisionResult contraptionContraptionCollision(ServerLevel level, double impactVelocity, ServerSubLevelContainer container, SubLevelPhysicsSystem system, CollisionBody contraptionA, CollisionBody contraptionB) {

        double gravity = DimensionPhysicsData.getGravity(level).length();

        BlockPos contraptionAHitPos = contraptionA.pos;
        BlockState contraptionABlockState = level.getBlockState(contraptionAHitPos);
        double contraptionABlockMass = ((BlockStateExtension) contraptionABlockState).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
        double contraptionABlockBounciness = ((BlockStateExtension) contraptionABlockState).sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());
        BlockPos contraptionBHitPos = contraptionB.pos;
        BlockState contraptionBBlockState = level.getBlockState(contraptionBHitPos);
        double contraptionBBlockMass = ((BlockStateExtension) contraptionBBlockState).sable$getProperty(PhysicsBlockPropertyTypes.MASS.get());
        double conptrationBBlockBounciness = ((BlockStateExtension) contraptionBBlockState).sable$getProperty(PhysicsBlockPropertyTypes.RESTITUTION.get());


        if (container == null) return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        ServerSubLevel contraptionBSubLevel = (ServerSubLevel) container.getSubLevel(contraptionB.id);
        ServerSubLevel contraptionASubLevel = (ServerSubLevel) container.getSubLevel(contraptionA.id);
        if (contraptionASubLevel == null) return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        if (contraptionBSubLevel == null) return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        double contraptionAMass = contraptionASubLevel.getMassTracker().getMass();
        double contraptionBMass = contraptionBSubLevel.getMassTracker().getMass();
        RigidBodyHandle contraptionAHandle = system.getPhysicsHandle(contraptionASubLevel);
        RigidBodyHandle contraptionBHandle = system.getPhysicsHandle(contraptionBSubLevel);
        if (contraptionBHandle == null || contraptionAHandle == null) {
            LogUtils.getLogger().info("HANDLER IS NULL");
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);

        }

        double effectiveMass = Math.min(contraptionABlockMass, contraptionBBlockMass);
        double contraptionRatio = effectiveMass / contraptionBBlockMass;
        double worldRatio = effectiveMass / contraptionABlockMass;



        Vector3d currentVelocityB = contraptionBHandle.getLinearVelocity(new Vector3d());
        Vector3d currentVelocityA = contraptionAHandle.getLinearVelocity(new Vector3d());

        double kineticEnergy = 0.5 * impactVelocity * impactVelocity * contraptionBMass;
        double speedCost = effectiveMass * (1.0 - contraptionABlockBounciness) * SableFragile.CONFIG.SpeedCost.get();
        double penetrationDepthCost = contraptionABlockMass * SableFragile.CONFIG.PenetrationCost.get();
        //Calculate penetration
        if (penetrationDepthCost == 0) {
            penetrationDepthCost = 0.125;
        }
        if (speedCost == 0) {
            speedCost = 0.125;
        }
        double newEnergy = Math.max(0.0, kineticEnergy - speedCost);
        double newSpeed = Math.sqrt(2.0 * newEnergy / contraptionBMass);
        double massBNewton = contraptionBMass * (impactVelocity - newSpeed);
        double massANewton = contraptionAMass * (impactVelocity - newSpeed);

        BlockBreakingProgress blockBreakingProgress = BlockBreakingProgress.get(level);
        double minFallSpeed = (Math.sqrt(gravity * 2) * SableFragile.CONFIG.MinHeight.get());
        boolean oneBlockFall = currentVelocityB.y() >= -minFallSpeed && currentVelocityB.y() < 0;
        boolean canPenetrate = newEnergy >= (penetrationDepthCost*contraptionRatio);


        Vector3d deltaVelocityB = new Vector3d(new Vector3d(currentVelocityB).normalize()).mul(-massBNewton);
        Vector3d deltaVelocityA = new Vector3d(new Vector3d(currentVelocityA).normalize()).mul(-massANewton);

        boolean brokenB = false;
        boolean brokenA = false;
        double contraptionDamageDone;
        if (SableFragile.CONFIG.ExtraFragileWorld.get()){
            contraptionDamageDone = Math.max(((kineticEnergy / penetrationDepthCost * (10 * worldRatio)) * contraptionRatio), 1);
        }else{
            contraptionDamageDone = Math.max(((kineticEnergy / penetrationDepthCost * (10)) * contraptionRatio), 1);
        }
        if (!canPenetrate){
            int worldDamage = blockBreakingProgress.getDamage(contraptionA.pos);
            worldDamage += (int) Math.min(((kineticEnergy / penetrationDepthCost * 10) * worldRatio),9);
            brokenA = checkToBreak(blockBreakingProgress,level,contraptionA.pos,worldDamage);
            brokenB = checkToBreak(blockBreakingProgress, level, contraptionB.pos,(int)contraptionDamageDone);
            if (!brokenB){
                contraptionBHandle.applyLinearAndAngularImpulse(deltaVelocityB, JOMLConversion.ZERO, true);
            }
            if (!brokenA){
                contraptionAHandle.applyLinearAndAngularImpulse(deltaVelocityA, JOMLConversion.ZERO, true);
            }
            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);
        }else{


            if (worldRatio < 1){
                brokenB = checkToBreak(blockBreakingProgress, level, contraptionB.pos,(int)contraptionDamageDone);
            }else{
                level.destroyBlock(contraptionA.pos, false);
            }
            if (!brokenB){
                contraptionBHandle.applyLinearAndAngularImpulse(deltaVelocityB, JOMLConversion.ZERO, true);
            }
            if (!brokenA){
                contraptionAHandle.applyLinearAndAngularImpulse(deltaVelocityA, JOMLConversion.ZERO, true);
            }

            return new BlockSubLevelCollisionCallback.CollisionResult(JOMLConversion.ZERO, false);

        }
    }

    @Unique
    private static UUID getServerSubLevelUUID(final Level level, final Vector3dc pos) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel instanceof ServerSubLevel serverSubLevel) {
            return serverSubLevel.getUniqueId();
        }
        return new UUID(0,0);
    }

    @Unique
    private boolean checkToBreak(BlockBreakingProgress breakingProgress, Level level, BlockPos pos, int newDamage) {

        int blockProgress = breakingProgress.getDamage(pos);
        blockProgress += newDamage;
        boolean broken = blockProgress >= 10;
        if (broken) {
            breakingProgress.resetProgress(pos);
            level.destroyBlock(pos, false);
            return true;
        } else {
            breakingProgress.setDamage(pos, blockProgress);
            level.destroyBlockProgress(pos.hashCode(), pos, blockProgress);
            return false;
        }
    }

}