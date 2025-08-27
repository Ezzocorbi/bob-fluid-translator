package com.example.examplemod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

@Mod(modid = ExampleMod.MODID, version = ExampleMod.VERSION)
public class ExampleMod
{
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Juice juice = new Juice("juice");
        FluidRegistry.registerFluid(juice);

        BlockJuiceFluid juiceBlock = new BlockJuiceFluid(juice, Material.water);
        juiceBlock.setBlockName("juiceBlock");

        GameRegistry.registerBlock(juiceBlock, "juiceBlock");
        juice.setBlock(juiceBlock);
        juiceBlock.setCreativeTab(CreativeTabs.tabBrewing);

        ItemJuiceBucket juiceBucket = new ItemJuiceBucket(juiceBlock);
        GameRegistry.registerItem(juiceBucket, "juiceBucket");

//        GenericBucket genericBucket = new GenericBucket(Blocks.air);
//        GameRegistry.registerItem(genericBucket, "genericBucket");

//        ItemStack juiceBucketStack = new ItemStack(genericBucket);
//        NBTTagCompound tag = new NBTTagCompound();
//        tag.setInteger("FluidColor", juice.getColor());
//        juiceBucketStack.setTagCompound(tag);

        FluidContainerRegistry.registerFluidContainer(
                new FluidStack(juice, FluidContainerRegistry.BUCKET_VOLUME),
                new ItemStack(juiceBucket),
                new ItemStack(Items.bucket)
        );
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
        System.out.println(FluidRegistry.getFluid("juice").getUnlocalizedName());
    }
}
