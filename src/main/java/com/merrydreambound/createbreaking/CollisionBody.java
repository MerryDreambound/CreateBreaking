package com.merrydreambound.createbreaking;

import net.minecraft.core.BlockPos;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.UUID;

public class CollisionBody {
    public UUID id;
    public BlockPos pos;
    public Vector3d hitBlock;
    public double impactVelocity;
    public CollisionBody(UUID id, BlockPos pos, Vector3d hitBlock,double impactVelocity) {
        this.id = Objects.requireNonNullElseGet(id, () -> new UUID(0, 0));
        this.pos = pos;
        this.hitBlock = hitBlock;
        this.impactVelocity = impactVelocity;
    }

}
