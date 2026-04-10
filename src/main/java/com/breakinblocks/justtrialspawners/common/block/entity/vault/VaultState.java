package com.breakinblocks.justtrialspawners.common.block.entity.vault;

import net.minecraft.util.StringRepresentable;

public enum VaultState implements StringRepresentable {
    INACTIVE("inactive", 6),
    ACTIVE("active", 12),
    UNLOCKING("unlocking", 12),
    EJECTING("ejecting", 12);

    private final String name;
    private final int lightLevel;

    VaultState(String name, int lightLevel) {
        this.name = name;
        this.lightLevel = lightLevel;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
