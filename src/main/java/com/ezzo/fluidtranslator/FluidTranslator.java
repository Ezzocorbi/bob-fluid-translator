package com.ezzo.fluidtranslator;

import com.ezzo.fluidtranslator.blocks.ModBlocks;
import com.ezzo.fluidtranslator.container.GuiHandler;
import com.ezzo.fluidtranslator.item.HBMAdapterItemBlock;
import com.ezzo.fluidtranslator.item.UniversalTankItemBlock;
import com.ezzo.fluidtranslator.network.ModNetwork;
import com.ezzo.fluidtranslator.tileentity.TileEntityHBMAdapter;
import com.ezzo.fluidtranslator.tileentity.TileEntityUniversalTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;

@Mod(modid = FluidTranslator.MODID, version = FluidTranslator.VERSION, dependencies = "required-after:hbm")
public class FluidTranslator
{
    public static final String MODID = "bobfluidtranslator";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(FluidTranslator.MODID)
    public static FluidTranslator instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        // Init network wrapper
        ModNetwork.init();

        // Register NTM's fluids
        ModFluidRegistry ft = new ModFluidRegistry();
        for(FluidType f : Fluids.getAll()) {
            if (ModFluidRegistry.isBlackListed(f)) continue;
            if (FluidRegistry.getFluid(f.getName().toLowerCase().replaceFirst("_fluid$", "")) != null) continue;
            ft.registerFluidType(f);
        }

        // Init blocks
        ModBlocks.initBlocks();

        // Register blocks
        GameRegistry.registerBlock(ModBlocks.universalTank, UniversalTankItemBlock.class, "universalTank");
        GameRegistry.registerBlock(ModBlocks.universalTankLarge, UniversalTankItemBlock.class, "universalTankLarge");
        GameRegistry.registerBlock(ModBlocks.hbmAdapter, HBMAdapterItemBlock.class, "ntmAdapter");

        // Register tile entities
        GameRegistry.registerTileEntity(TileEntityUniversalTank.class, "teUniversalTank");
        GameRegistry.registerTileEntity(TileEntityHBMAdapter.class, "teNTMAdapter");

    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
        addRecipes();
    }

    public void addRecipes() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.universalTank),
                "XLX",
                "XCX",
                "XLX",
                'X', ModItems.plate_polymer,
                'L', ModItems.plate_lead,
                'C', ModItems.fluid_tank_empty
        );

        GameRegistry.addRecipe(new ItemStack(ModBlocks.universalTankLarge),
                "XLX",
                "XCX",
                "XLX",
                'X', ModItems.plate_polymer,
                'L', ModItems.plate_lead,
                'C', ModItems.fluid_barrel_empty
        );

        GameRegistry.addRecipe(new ItemStack(ModBlocks.hbmAdapter),
                "XWX",
                "ZYZ",
                "XWX",
                'X', ModItems.plate_steel,
                'Y', new ItemStack(ModItems.circuit, 1, 8),
                'W', Items.comparator,
                'Z', new ItemStack(com.hbm.blocks.ModBlocks.fluid_duct_neo)
        );
    }
}
