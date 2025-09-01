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


    public CustomFluidBlock(Fluid fluid, Material material, String name) {
        super(fluid, material);
        setBlockName(name);
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerBlockIcons(IIconRegister iconRegister) {
//        if(iconRegister instanceof TextureMap) {
//            TextureMap map = (TextureMap) iconRegister;
//            CustomAtlasSprite sprite = new CustomAtlasSprite("bobfluidtranslator:tile.honeyTexture");
//            if (map.setTextureEntry(sprite.getIconName(), sprite)) {
//                System.out.println("[Code 1338] Sprite in registerBlockIcons: " + sprite.toString());
//            }
//            setIcons(sprite);
//            getFluid().setIcons(sprite);
//        }
//    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return stillIcon;
    }

    public void setIcons(IIcon icon) {
        getFluid().setIcons(icon);
        this.stillIcon = icon;
    }
}
