package com.ezzo.fluidtranslator.blocks.fluid;

import com.hbm.extprop.HbmLivingProps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import java.util.Random;

import static com.hbm.blocks.gas.BlockGasFlammable.fireSources;

public class FlammableFluidBlock extends CustomFluidBlock {
    public FlammableFluidBlock(Fluid fluid, Material material, String name) {
        super(fluid, material, name);
    }
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        rad(world, x, y, z);
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            Block b = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
            if(isFireSource(b)) {
                combust(world, x, y, z);
                return;
            }
        }
        super.updateTick(world, x, y, z, rand);
    }
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        affect(entity);
        if(entity.isBurning()) {
            this.combust(world, x, y, z);
            return;
        }
        if(entity instanceof EntityLivingBase){
            HbmLivingProps.setOil((EntityLivingBase) entity,300);
        }
    }
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {

        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            Block b = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

            if(isFireSource(b)) {
                world.scheduleBlockUpdate(x, y, z, this, 2);
            }
        }
    }
    protected void combust(World world, int x, int y, int z) {
        world.setBlock(x, y, z, Blocks.fire);
    }

    public boolean isFireSource(Block b) {
        return fireSources.contains(b);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return true;
    }
}
