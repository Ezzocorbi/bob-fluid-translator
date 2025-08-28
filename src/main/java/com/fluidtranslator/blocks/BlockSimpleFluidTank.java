package com.fluidtranslator.blocks;

import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.tileentity.TileEntitySimpleFluidTank;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class BlockSimpleFluidTank extends BlockContainer {
    public BlockSimpleFluidTank() {
        super(Material.rock);
        setBlockName("simpleFluidTank");
        setBlockTextureName("minecraft:glass");
        setHardness(2.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySimpleFluidTank();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(FluidTranslator.instance, 0, world, x, y, z); // 0 = GUI ID
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntitySimpleFluidTank) {
            TileEntitySimpleFluidTank tank = (TileEntitySimpleFluidTank) te;
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
