package com.fluidtranslator;

import com.hbm.inventory.fluid.FluidType;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * This class is used to match the localization of HBM's fluids
 */
public class CustomFluid extends Fluid {
    public CustomFluid(String fluidName) {
        super(fluidName);
    }

    @Override
    public String getLocalizedName(FluidStack stack) {
        FluidType fluidType = ModFluidRegistry.getHBMFluid(stack.getFluid());
        if (fluidType != null) return fluidType.getLocalizedName();
        else return super.getLocalizedName(stack);
    }

    // I don't want to write the full lang file for every fluid of every fork of NTM, so I
    // just use this method to force other mods to use the english localized name from NTM
    @Override
    public String getUnlocalizedName() {
        return this.getLocalizedName(new FluidStack(this, 0));
    }
}
