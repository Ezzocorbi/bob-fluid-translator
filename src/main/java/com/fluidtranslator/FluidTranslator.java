package com.fluidtranslator;

import com.fluidtranslator.blocks.*;
import com.fluidtranslator.container.GuiHandler;
import com.fluidtranslator.proxy.CommonProxy;
import com.fluidtranslator.tileentity.*;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = FluidTranslator.MODID, version = FluidTranslator.VERSION, dependencies = "required-after:hbm")
public class FluidTranslator
{
    public static final String MODID = "bobfluidtranslator";
    public static final String VERSION = "1.0";

    @Mod.Instance(FluidTranslator.MODID)
    public static FluidTranslator instance;

    @SidedProxy(clientSide = "com.fluidtranslator.proxy.ClientProxy",
                serverSide = "com.fluidtranslator.proxy.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        // Register HBM's fluids
        CustomFluidRegistry ft = new CustomFluidRegistry();
        for(FluidType f : Fluids.getAll()) {
            if (CustomFluidRegistry.isBlackListed(f)) continue;
            ft.registerFluidType(f);
        }

        // Register blocks
        GameRegistry.registerBlock(new BlockUniversalTank(), "universalTank");
        GameRegistry.registerBlock(new BlockHBMAdapter(), "hbmInterface");

        // Register tile entities
        GameRegistry.registerTileEntity(TileEntityUniversalTank.class, "teUniversalTank");
        GameRegistry.registerTileEntity(TileEntityHBMAdapter.class, "teHBMInterface");

    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new CustomEventHandler());
    }

}
