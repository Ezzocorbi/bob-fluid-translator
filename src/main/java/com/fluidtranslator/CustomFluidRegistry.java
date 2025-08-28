package com.fluidtranslator;

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

public class CustomFluidRegistry {

    public CustomFluidRegistry() {

    }

    public CustomFluidBlock registerFluidType(FluidType fluidType) {
        String name = fluidType.getName().toLowerCase();
        Fluid forgeFluid = new Fluid(name);
        FluidRegistry.registerFluid(forgeFluid);

        CustomFluidBlock block = new CustomFluidBlock(forgeFluid, Material.water, name);
        GameRegistry.registerBlock(block, name + "block");
        forgeFluid.setBlock(block);

        GenericBucket genericBucket = new GenericBucket(forgeFluid, block);
        GameRegistry.registerItem(genericBucket, name + "bucket");

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
     * @param fluid HBM fluid
     * @return returns null if there is no correspondence (like for black-listed fluids)
     */
    public static Fluid getForgeFluid(FluidType fluidType) {
        return FluidRegistry.getFluid(fluidType.getName().toLowerCase());
    }
}
