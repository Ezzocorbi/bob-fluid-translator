package com.fluidtranslator.container;

import com.fluidtranslator.tileentity.TileEntityForgeFluidTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerFluidTank extends Container {
    private TileEntityForgeFluidTank tank;

    public ContainerFluidTank(InventoryPlayer playerInv, TileEntityForgeFluidTank tank) {
        this.tank = tank;

        addSlotToContainer(new Slot(tank, 0, 23, 16));
        addSlotToContainer(new Slot(tank, 1, 23, 57));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
