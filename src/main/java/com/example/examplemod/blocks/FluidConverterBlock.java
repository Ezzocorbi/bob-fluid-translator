package com.example.examplemod.blocks;

import com.example.examplemod.tileentity.FluidConverterTank;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public class FluidConverterBlock extends BlockContainer {
    public FluidConverterBlock() {
        super(Material.rock);
        setBlockName("fluidConverter");
        setBlockTextureName("minecraft:glass");
        setHardness(2.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new FluidConverterTank();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof FluidConverterTank) {
            FluidConverterTank teTank = (FluidConverterTank)te;
            FluidTankInfo tankInfo = teTank.getTankInfo(ForgeDirection.getOrientation(side))[0];
            FluidStack fluidStack = tankInfo.fluid;
            if (fluidStack != null) {
                player.addChatMessage(new ChatComponentText(fluidStack.getFluid().getName() + " | " + fluidStack.amount));
            } else {
                player.addChatMessage(new ChatComponentText("Empty"));
            }
        }
        return true;
    }
}
