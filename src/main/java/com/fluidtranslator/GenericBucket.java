package com.fluidtranslator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class GenericBucket extends ItemBucket {

    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public GenericBucket(Fluid fluid, Block block) {
        super(block);
        this.setUnlocalizedName(fluid.getName() + "Bucket");
        this.setContainerItem(Items.bucket);
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.icon = iconRegister.registerIcon("minecraft:bucket_empty");
    }
}
