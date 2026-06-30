package com.ezzo.fluidtranslator.blocks.fluid;

import com.hbm.entity.effect.EntityMist;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import java.util.Random;

public class GasFluidBlock extends CustomFluidBlock {
    public GasFluidBlock(Fluid fluid, Material material, String name) {
        super(fluid, material, name);
    }
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand){
        super.updateTick(world, x, y, z, rand);
        rad(world, x, y, z);
        world.setBlock(x,y,z, Blocks.air);
        EntityMist mist = new EntityMist(world);
        mist.setType(this.hbmFluid);
        mist.setPosition(x, y, z);
        mist.setArea(10, 5);
        mist.setDuration(80);
        world.spawnEntityInWorld(mist);
    }
}
