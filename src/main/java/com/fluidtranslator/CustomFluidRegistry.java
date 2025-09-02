package com.fluidtranslator;

import com.fluidtranslator.item.GenericBucket;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashSet;
import java.util.Set;

public class CustomFluidRegistry {

    private static final Set<String> blackList = new HashSet<String>();

    public CustomFluidRegistry() {
        blackList.add(Fluids.NONE.getName());
        blackList.add(Fluids.LAVA.getName());
        blackList.add(Fluids.WATER.getName());
        blackList.add("CUSTOM_DEMO");
    }

    public CustomFluidBlock registerFluidType(FluidType fluidType) {
        String name = fluidType.getName().toLowerCase();
        Fluid forgeFluid = new Fluid(name);
        FluidRegistry.registerFluid(forgeFluid);

        CustomFluidBlock block = new CustomFluidBlock(forgeFluid, Material.water, name);
        GameRegistry.registerBlock(block, name + "_block");
        forgeFluid.setBlock(block);

        GenericBucket genericBucket = new GenericBucket(forgeFluid, block);
        GameRegistry.registerItem(genericBucket, name + "_bucket");

        FluidContainerRegistry.registerFluidContainer(
                new FluidStack(forgeFluid, FluidContainerRegistry.BUCKET_VOLUME),
                new ItemStack(genericBucket),
                new ItemStack(Items.bucket)
        );

        return block;
    }

    /**
     * Returns the corresponding HBM fluid
     * @param fluid Forge fluid
     * @return returns null if there is no correspondence
     */
    public static FluidType getHBMFluid(Fluid fluid) {
        return Fluids.fromName(fluid.getName().toUpperCase());
    }

    /**
     * Returns the corresponding Forge fluid
     * @param fluidType HBM fluid
     * @return returns null if there is no correspondence (like for black-listed fluids)
     */
    public static Fluid getForgeFluid(FluidType fluidType) {
        return FluidRegistry.getFluid(fluidType.getName().toLowerCase());
    }

    /**
     *
     * @param fluidType Fluid to check
     * @return Returns true if the fluid should not be registered
     */
    public static boolean isBlackListed(FluidType fluidType) {
        return blackList.contains(fluidType.getName());
    }
}
