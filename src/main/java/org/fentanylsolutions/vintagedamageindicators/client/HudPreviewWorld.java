package org.fentanylsolutions.vintagedamageindicators.client;

import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;

import com.cleanroommc.modularui.utils.fakeworld.DummyWorld;

public final class HudPreviewWorld extends DummyWorld {

    public static final HudPreviewWorld INSTANCE = new HudPreviewWorld();
    private boolean forceDark;

    private HudPreviewWorld() {
        if (this.difficultySetting == null) {
            this.difficultySetting = EnumDifficulty.NORMAL;
        }
    }

    @Override
    public float getLightBrightness(int x, int y, int z) {
        if (this.forceDark) {
            return 0.0F;
        }
        return 1.0F;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue) {
        if (this.forceDark) {
            return 0;
        }
        return 15728880;
    }

    @Override
    public int getBlockLightValue(int x, int y, int z) {
        if (this.forceDark) {
            return 0;
        }
        return 15;
    }

    @Override
    public int getBlockLightValue_do(int x, int y, int z, boolean useNeighborBrightness) {
        if (this.forceDark) {
            return 0;
        }
        return 15;
    }

    @Override
    public int getSkyBlockTypeBrightness(EnumSkyBlock type, int x, int y, int z) {
        if (this.forceDark) {
            return 0;
        }
        return 15;
    }

    @Override
    public int getSavedLightValue(EnumSkyBlock type, int x, int y, int z) {
        if (this.forceDark) {
            return 0;
        }
        return 15;
    }

    public boolean isForceDark() {
        return this.forceDark;
    }

    public void setForceDark(boolean forceDark) {
        this.forceDark = forceDark;
    }
}
