package com.merrydreambound.createbreaking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class BlockBreakingProgress extends SavedData {
    private final Map<BlockPos, Integer> blockProgressArray = new HashMap<>();


    public static BlockBreakingProgress create() {
        return new BlockBreakingProgress();
    }

    public static BlockBreakingProgress load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        BlockBreakingProgress data = BlockBreakingProgress.create();
        ListTag list = tag.getList("blockProgressArray", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
            int progress = entry.getInt("progress");
            data.blockProgressArray.put(pos, progress);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, Integer> entry : blockProgressArray.entrySet()) {
            CompoundTag tag = new CompoundTag();

            BlockPos pos = entry.getKey();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putInt("progress", entry.getValue());

            list.add(tag);
        }
        compoundTag.put("blockProgressArray", list);
        return compoundTag;
    }

    public static SavedData.Factory<BlockBreakingProgress> factory() {
        return new SavedData.Factory<>(BlockBreakingProgress::new, BlockBreakingProgress::load);
    }

    public void resetProgress(BlockPos position) {
        this.blockProgressArray.put(position, 0);
        this.setDirty();
    }

    public void setProgress(BlockPos position, int progress) {
        this.blockProgressArray.put(position, progress);
        this.setDirty();
    }

    public int getProgress(BlockPos position) {
        return this.blockProgressArray.getOrDefault(position, 0);
    }

    public static BlockBreakingProgress get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), "blockProgressArray");
    }
}

