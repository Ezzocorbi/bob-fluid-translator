package com.example.examplemod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class GenericBucket extends ItemBucket {

    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public GenericBucket(Block block) {
        super(block);
        this.setUnlocalizedName("genericBucket");
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

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return this.icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("FluidColor")) {
//            System.out.println(stack.getTagCompound().getInteger("FluidColor"));
            return stack.getTagCompound().getInteger("FluidColor");
        }
        return 0xFFFFFFFF; // bianco se vuoto
    }
}
