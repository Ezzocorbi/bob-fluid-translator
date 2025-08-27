package com.example.examplemod;

import com.example.examplemod.blocks.FluidConverterBlock;
import com.example.examplemod.tileentity.FluidConverterTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.ArrayList;

@Mod(modid = ExampleMod.MODID, version = ExampleMod.VERSION, dependencies = "required-after:hbm")
public class ExampleMod
{
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ArrayList<String> fluidBlacklist = new ArrayList<String>();
        fluidBlacklist.add("none");
        fluidBlacklist.add("custom_demo");

        // Register HBM's fluids
        FluidTranslator ft = new FluidTranslator();
        for(FluidType f : Fluids.getAll()) {
            if(fluidBlacklist.contains(f.getName().toLowerCase())) {
                continue;
            }
            ft.registerFluidType(f);
        }

        // Register blocks
        GameRegistry.registerBlock(new FluidConverterBlock(), "fluidConverter");

        // Register tile entities
        GameRegistry.registerTileEntity(FluidConverterTank.class, "fluidConverterTank");
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

    }
}
