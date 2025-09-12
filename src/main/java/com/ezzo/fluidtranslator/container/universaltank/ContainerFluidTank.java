package com.ezzo.fluidtranslator.container.universaltank;

import com.ezzo.fluidtranslator.tileentity.TileEntityUniversalTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFluidTank extends Container {

    public ContainerFluidTank(InventoryPlayer playerInv, TileEntityUniversalTank tank) {

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

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstackCopy = null;
        Slot slot = (Slot)inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstackCopy = itemstack1.copy();

            int containerSlots = inventorySlots.size() - player.inventory.mainInventory.length;

            if (index < containerSlots) { // Shift click from container
                if (!this.mergeItemStack(itemstack1, containerSlots, inventorySlots.size(), true)) {
                    return null; // Return null when nothing is moved
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 1, false)) { // Shift click from inventory
                return null; // Return null when nothing is moved
            }

            if (itemstack1.stackSize == 0) { // Whole stack has been moved -> set clicked slot to empty
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstackCopy.stackSize) { // Nothing has been moved -> return null
                return null;
            }
        }
        return itemstackCopy;
    }
}
