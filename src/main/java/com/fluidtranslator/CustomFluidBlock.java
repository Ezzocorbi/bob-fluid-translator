package com.fluidtranslator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class CustomFluidBlock extends BlockFluidClassic {
    @SideOnly(Side.CLIENT)
    protected IIcon stillIcon;

    private final Fluid fluid;

    public CustomFluidBlock(Fluid fluid, Material material, String name) {
        super(fluid, material);
        setBlockName(name);
        this.fluid = fluid;
    }

    @Override
    public String getLocalizedName() {
        return ModFluidRegistry.getHBMFluid(fluid).getLocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return stillIcon;
    }

    public void setIcons(IIcon icon) {
        getFluid().setIcons(icon);
        this.stillIcon = icon;
    }

    public Fluid getFluid() {
        return this.fluid;
    }
}
