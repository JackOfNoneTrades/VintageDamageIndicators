package org.fentanylsolutions.vintagedamageindicators.client;

import com.cleanroommc.modularui.utils.fakeworld.DummyWorld;

public final class HudPreviewWorld extends DummyWorld {

    public static final HudPreviewWorld INSTANCE = new HudPreviewWorld();

    private HudPreviewWorld() {}

    @Override
    public float getLightBrightness(int x, int y, int z) {
        return 1.0F;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue) {
        return 15728880;
    }

    @Override
    public int getBlockLightValue(int x, int y, int z) {
        return 15;
    }
}
