package com.fluidtranslator;

import com.fluidtranslator.blocks.BlockForgeFluidTank;
import com.fluidtranslator.blocks.BlockHBMFluidTank;
import com.fluidtranslator.blocks.BlockHBMWrapperFluidTank;
import com.fluidtranslator.blocks.BlockUniversalTank;
import com.fluidtranslator.gui.GuiHandler;
import com.fluidtranslator.proxy.CommonProxy;
import com.fluidtranslator.tileentity.TileEntityForgeFluidTank;
import com.fluidtranslator.tileentity.TileEntityHBMFluidTank;
import com.fluidtranslator.tileentity.TileEntityHBMWrapper;
import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.ArrayList;

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
        ArrayList<String> fluidBlacklist = new ArrayList<String>();
        fluidBlacklist.add("none");
        fluidBlacklist.add("custom_demo");

        // Register HBM's fluids
        CustomFluidRegistry ft = new CustomFluidRegistry();
        for(FluidType f : Fluids.getAll()) {
            if(fluidBlacklist.contains(f.getName().toLowerCase())) {
                continue;
            }
            ft.registerFluidType(f);
        }

        // Register blocks
        GameRegistry.registerBlock(new BlockForgeFluidTank(), "simpleFluidTank");
        GameRegistry.registerBlock(new BlockHBMWrapperFluidTank(), "hbmToForgeTank");
        GameRegistry.registerBlock(new BlockUniversalTank(), "universalTank");

        // Register tile entities
        GameRegistry.registerTileEntity(TileEntityForgeFluidTank.class, "teSimpleFluidTank");
        GameRegistry.registerTileEntity(TileEntityHBMWrapper.class, "teHBMWrapperTank");
        GameRegistry.registerTileEntity(TileEntityUniversalTank.class, "teUniversalTank");

        // HBM specific stuff
        GameRegistry.registerBlock(new BlockHBMFluidTank(), "simpleHBMTank");
        GameRegistry.registerTileEntity(TileEntityHBMFluidTank.class, "teSimpleHBMTank");
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

}
