package com.fluidtranslator.gui;

import com.fluidtranslator.container.ContainerFluidTank;
import com.fluidtranslator.container.GuiFluidTank;
import com.fluidtranslator.tileentity.TileEntitySimpleFluidTank;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntitySimpleFluidTank) {
            return new ContainerFluidTank(player.inventory, (TileEntitySimpleFluidTank) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntitySimpleFluidTank) {
            return new GuiFluidTank(player.inventory, (TileEntitySimpleFluidTank) te);
        }
        return null;
    }
}
