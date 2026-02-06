package com.ezzo.fluidtranslator;

import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @Override
    public void registerTanslations() {
        ModFluidRegistry.validFluids().forEach(f -> {
            LanguageRegistry.instance().addStringLocalization(
                    "fluid." + f.getName().toLowerCase(),
                    "en_US",
                    f.getLocalizedName());
        });
    }
}
