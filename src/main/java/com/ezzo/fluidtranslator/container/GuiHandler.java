package com.ezzo.fluidtranslator.container;

import com.ezzo.fluidtranslator.container.universaltank.ContainerFluidTank;
import com.ezzo.fluidtranslator.container.universaltank.GuiFluidTank;
import com.ezzo.fluidtranslator.container.hbmadapter.ContainerHBMAdapter;
import com.ezzo.fluidtranslator.container.hbmadapter.GuiHBMAdapter;
import com.ezzo.fluidtranslator.tileentity.TileEntityHBMAdapter;
import com.ezzo.fluidtranslator.tileentity.TileEntityUniversalTank;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityUniversalTank) {
            return new ContainerFluidTank(player.inventory, (TileEntityUniversalTank) te);
        } else if (te instanceof TileEntityHBMAdapter) {
            return new ContainerHBMAdapter(player.inventory, (TileEntityHBMAdapter) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityUniversalTank) {
            return new GuiFluidTank(player.inventory, (TileEntityUniversalTank) te);
        } else if (te instanceof TileEntityHBMAdapter) {
            return new GuiHBMAdapter(player.inventory, (TileEntityHBMAdapter) te);
        }
        return null;
    }
}
