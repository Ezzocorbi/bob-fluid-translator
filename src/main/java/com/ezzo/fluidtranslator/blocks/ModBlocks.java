package com.ezzo.fluidtranslator.blocks;

import net.minecraft.block.Block;

public class ModBlocks {
    public static Block universalTank;
    public static Block universalTankLarge;
    public static Block hbmAdapter;

    public static void initBlocks() {
        universalTank = new BlockUniversalTank(8000);
        universalTankLarge = new BlockUniversalTank(16000);
        hbmAdapter = new BlockHBMAdapter();
    }
}
