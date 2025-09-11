package com.fluidtranslator;

import com.fluidtranslator.blocks.*;
import com.fluidtranslator.container.GuiHandler;
import com.fluidtranslator.item.HBMAdapterItemBlock;
import com.fluidtranslator.item.UniversalTankItemBlock;
import com.fluidtranslator.network.ModNetwork;
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
import net.minecraftforge.fluids.FluidRegistry;

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
        // Init network wrapper
        ModNetwork.init();

        // Register HBM's fluids
        ModFluidRegistry ft = new ModFluidRegistry();
        for(FluidType f : Fluids.getAll()) {
            if (ModFluidRegistry.isBlackListed(f)) continue;
            if (FluidRegistry.getFluid(f.getName().toLowerCase() + "_fluid") != null) continue;
            ft.registerFluidType(f);
        }

        // Register blocks
        GameRegistry.registerBlock(new BlockUniversalTank(8000), UniversalTankItemBlock.class, "universalTank");
        GameRegistry.registerBlock(new BlockUniversalTank(16000), UniversalTankItemBlock.class, "universalTankLarge");
        GameRegistry.registerBlock(new BlockHBMAdapter(), HBMAdapterItemBlock.class, "ntmAdapter");

        // Register tile entities
        GameRegistry.registerTileEntity(TileEntityUniversalTank.class, "teUniversalTank");
        GameRegistry.registerTileEntity(TileEntityHBMAdapter.class, "teNTMAdapter");

    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
    }

}
