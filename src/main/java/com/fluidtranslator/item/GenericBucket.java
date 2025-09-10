package com.fluidtranslator.item;

import com.fluidtranslator.CustomFluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.HashMap;

public class GenericBucket extends ItemBucket {

    private CustomFluid fluid;
    private static final HashMap<Fluid, GenericBucket> fluidToBucket = new HashMap<Fluid, GenericBucket>();

    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public GenericBucket(CustomFluid fluid, Block block) {
        super(block);
        this.setUnlocalizedName(fluid.getName() + "bucket");
        this.setContainerItem(Items.bucket);
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.fluid = fluid;
        registerBucket(fluid, this);
    }

    private static void registerBucket(Fluid fluid, GenericBucket bucket) {
        fluidToBucket.put(fluid, bucket);
    }

    public static GenericBucket getBuckerForFluid(Fluid fluid) {
        return fluidToBucket.get(fluid);
    }

    @Override
    public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return fluid.getLocalizedName(FluidContainerRegistry.getFluidForFilledItem(stack)) + " Bucket";
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        return icon;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        return this.getIcon(stack, renderPass);
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        return icon;
    }

    public void setIcon(IIcon icon) {
        this.icon = icon;
    }
}
