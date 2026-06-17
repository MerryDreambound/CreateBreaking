package com.merrydreambound.sablefragile;

import net.minecraft.core.BlockPos;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.UUID;

public class CollisionBody {
    public UUID id;
    public BlockPos pos;
    public double impactVelocity;
    public CollisionBody(UUID id, BlockPos pos,double impactVelocity) {
        this.id = Objects.requireNonNullElseGet(id, () -> new UUID(0, 0));
        this.pos = pos;
        this.impactVelocity = impactVelocity;
    }

}
