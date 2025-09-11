package com.fluidtranslator.blocks;

import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.container.GuiIds;
import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockUniversalTank extends BlockContainer {

    private final int capacity;

    public BlockUniversalTank(int capacity) {
        super(Material.rock);
        setBlockName("universalFluidTank");
        setBlockTextureName("minecraft:glass");
        setHardness(4.0F);
        this.capacity = capacity;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityUniversalTank(this.capacity);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityUniversalTank) {
                player.openGui(FluidTranslator.instance, GuiIds.UNIVERSAL_TANK.ordinal, world, x, y, z);
            }
        }
        return true;
    }

    public int getCapacity() {
        return capacity;
    }
}
