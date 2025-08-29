package com.fluidtranslator.blocks;

import com.fluidtranslator.tileentity.TileEntityHBMWrapper;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class BlockHBMWrapperFluidTank extends BlockContainer {
    public BlockHBMWrapperFluidTank() {
        super(Material.rock);
        setBlockName("hbmToForgeTank");
        setBlockTextureName("minecraft:glass");
        setHardness(2.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHBMWrapper();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityHBMWrapper) {
            TileEntityHBMWrapper tank = (TileEntityHBMWrapper) te;
            FluidStack fluidStack = tank.getTankInfo(ForgeDirection.getOrientation(side))[0].fluid;
            if (fluidStack != null) {
                player.addChatMessage(new ChatComponentText(fluidStack.getFluid().getName() + " | " + fluidStack.amount));
            } else {
                player.addChatMessage(new ChatComponentText("Empty"));
            }
        }

        return true;
    }
}
