package com.fluidtranslator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class CustomFluidBlock extends BlockFluidClassic {
    @SideOnly(Side.CLIENT)
    protected IIcon stillIcon;
    @SideOnly(Side.CLIENT)
    protected IIcon flowingIcon;

    public CustomFluidBlock(Fluid fluid, Material material, String name) {
        super(fluid, material);
        setBlockName(name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
//        String stillName = RefStrings.MODID + ":" + getFluid().getName();
//        String flowingName = RefStrings.MODID + ":" + getFluid().getName();
//
//        System.out.println(stillName);
//        System.out.println(flowingName);
        stillIcon = iconRegister.registerIcon(FluidTranslator.MODID + ":copy/" + getFluid().getName());
        flowingIcon = iconRegister.registerIcon(FluidTranslator.MODID + ":copy/" + getFluid().getName());

        getFluid().setIcons(stillIcon, flowingIcon);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return (side == 0 || side == 1) ? stillIcon : flowingIcon;
    }
}
