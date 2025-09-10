package com.fluidtranslator.network;

import com.fluidtranslator.FluidTranslator;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class ModNetwork {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(FluidTranslator.MODID);

    public static void init() {
        INSTANCE.registerMessage(MessageSetOperationMode.Handler.class, MessageSetOperationMode.class, 0, Side.SERVER);
        INSTANCE.registerMessage(MessageSetTankIndex.Handler.class, MessageSetTankIndex.class, 1, Side.SERVER);
    }
}
