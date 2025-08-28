package com.fluidtranslator.container;

import com.fluidtranslator.tileentity.TileEntitySimpleFluidTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class ContainerFluidTank extends Container {
    private TileEntitySimpleFluidTank tank;

    public ContainerFluidTank(InventoryPlayer playerInv, TileEntitySimpleFluidTank tank) {
        this.tank = tank;

        // add item slots here
        // addSlotToContainer(new Slot(...));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
