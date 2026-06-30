package com.ezzo.fluidtranslator.blocks.fluid;

import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.lib.ModDamageSource;

import com.hbm.util.ArmorUtil;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import java.util.*;

public class CorrosiveFluidBlock extends CustomFluidBlock {
    public Random rand = new Random();
    private int rating;

    public ArrayList<Material> corrosibleBlock = new ArrayList<>(Arrays.asList(Material.wood,
            Material.cactus,
            Material.cake,
            Material.circuits,
            Material.cloth,
            Material.coral,
            Material.craftedSnow,
            Material.glass,
            Material.gourd,
            Material.ice,
            Material.leaves,
            Material.packedIce,
            Material.piston,
            Material.plants,
            Material.portal,
            Material.redstoneLight,
            Material.snow,
            Material.sponge,
            Material.vine,
            Material.web));

    public CorrosiveFluidBlock(Fluid fluid, Material material, String name) {
        super(fluid, material, name);
        this.rating = this.hbmFluid.getTrait(FT_Corrosive.class).getRating();
    }
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        affect(entity);
        if (entity instanceof EntityItem) {
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;

            if(entity.ticksExisted % 20 == 0 && !world.isRemote) {
                entity.attackEntityFrom(ModDamageSource.acid, this.rating * 0.01F);
            }
            if(entity.ticksExisted % 5 == 0) {
                world.spawnParticle("cloud", entity.posX, entity.posY, entity.posZ, 0.0, 0.0, 0.0);
            }
        }
        else  {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, ModDamageSource.acid, this.rating / 60F);
            if(entity instanceof EntityLivingBase) {
                for (int i = 0; i < 4; i++) {
                    ArmorUtil.damageSuit((EntityLivingBase) entity, i, this.rating / 50);
                }
            }
        }
        if(entity.ticksExisted % 5 == 0) {
            world.playSoundAtEntity(entity, "random.fizz", 0.2F, 1F);
        }
    }
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);
        rad(world, x, y, z);
            if (this.rating > 50) {
                reactToBlocks2(world, x + 1, y, z);
                reactToBlocks2(world, x - 1, y, z);
                reactToBlocks2(world, x, y + 1, z);
                reactToBlocks2(world, x, y - 1, z);
                reactToBlocks2(world, x, y, z + 1);
                reactToBlocks2(world, x, y, z - 1);
            }
    }
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);

        reactToBlocks(world, x + 1, y, z);
        reactToBlocks(world, x - 1, y, z);
        reactToBlocks(world, x, y + 1, z);
        reactToBlocks(world, x, y - 1, z);
        reactToBlocks(world, x, y, z + 1);
        reactToBlocks(world, x, y, z - 1);
    }

    public void reactToBlocks(World world, int x, int y, int z) {
        if(!Objects.equals(world.getBlock(x, y, z).getUnlocalizedName(), this.getUnlocalizedName())) {
            Block block = world.getBlock(x, y, z);

            if(block.getMaterial().isLiquid()) {
                world.setBlock(x, y, z, Blocks.air);
            }
        }
    }
    public void reactToBlocks2(World world, int x, int y, int z) {
        if (!Objects.equals(world.getBlock(x, y, z).getUnlocalizedName(), this.getUnlocalizedName())) {
            Block block = world.getBlock(x, y, z);

            if (block == Blocks.stone ||
                    block == Blocks.stone_brick_stairs ||
                    block == Blocks.stonebrick ||
                    block == Blocks.stone_slab) {
                if (rand.nextInt(20) == 0)
                    world.setBlock(x, y, z, Blocks.cobblestone);
            } else if (block == Blocks.cobblestone) {
                if (rand.nextInt(15) == 0)
                    world.setBlock(x, y, z, Blocks.gravel);
            } else if (block == Blocks.sandstone) {
                if (rand.nextInt(5) == 0)
                    world.setBlock(x, y, z, Blocks.sand);
            } else if (block == Blocks.hardened_clay ||
                    block == Blocks.stained_hardened_clay) {
                if (rand.nextInt(10) == 0)
                    world.setBlock(x, y, z, Blocks.clay);
            } else if (corrosibleBlock.contains(block.getMaterial()) ||
                    block.getExplosionResistance(null) < 1.2F) {
                world.setBlock(x, y, z, Blocks.air);
            }
        }
    }
}
