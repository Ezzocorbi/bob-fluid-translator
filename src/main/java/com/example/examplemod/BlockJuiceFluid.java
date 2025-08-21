package com.example.examplemod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockJuiceFluid extends BlockFluidClassic {
    @SideOnly(Side.CLIENT)
    protected IIcon stillIcon;
    @SideOnly(Side.CLIENT)
    protected IIcon flowingIcon;

    public BlockJuiceFluid(Fluid fluid, Material material) {
        super(fluid, material);
        setBlockName("juiceFluid");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        stillIcon = iconRegister.registerIcon(ExampleMod.MODID+":juice_still");
        flowingIcon = iconRegister.registerIcon(ExampleMod.MODID+":juice_flow");
        // Assegna le icone al fluido
        getFluid().setIcons(stillIcon, flowingIcon);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return (side == 0 || side == 1) ? stillIcon : flowingIcon;
    }
}
