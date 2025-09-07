package com.fluidtranslator.container;

import com.fluidtranslator.container.forgefluidtank.ContainerFluidTank;
import com.fluidtranslator.container.forgefluidtank.GuiFluidTank;
import com.fluidtranslator.container.hbmadapter.ContainerHBMAdapter;
import com.fluidtranslator.container.hbmadapter.GuiHBMAdapter;
import com.fluidtranslator.tileentity.TileEntityForgeFluidTank;
import com.fluidtranslator.tileentity.TileEntityHBMAdapter;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityForgeFluidTank) {
            return new ContainerFluidTank(player.inventory, (TileEntityForgeFluidTank) te);
        } else if (te instanceof TileEntityHBMAdapter) {
            return new ContainerHBMAdapter(player.inventory, (TileEntityHBMAdapter) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityForgeFluidTank) {
            return new GuiFluidTank(player.inventory, (TileEntityForgeFluidTank) te);
        } else if (te instanceof TileEntityHBMAdapter) {
            return new GuiHBMAdapter(player.inventory, (TileEntityHBMAdapter) te);
        }
        return null;
    }
}
