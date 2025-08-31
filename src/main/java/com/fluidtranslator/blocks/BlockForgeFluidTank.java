package com.fluidtranslator.blocks;

import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.tileentity.TileEntityForgeFluidTank;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockForgeFluidTank extends BlockContainer {
    public BlockForgeFluidTank() {
        super(Material.rock);
        setBlockName("simpleFluidTank");
        setBlockTextureName("minecraft:glass");
        setHardness(2.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityForgeFluidTank();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(FluidTranslator.instance, 0, world, x, y, z); // 0 = GUI ID
        }
        return true;
    }
}
