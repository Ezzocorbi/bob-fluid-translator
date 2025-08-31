package com.fluidtranslator.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class TileEntityForgeFluidTank extends TileEntity implements IFluidHandler, IInventory {

    private final FluidTank forgeTank = new FluidTank(4000);
    private final ItemStack[] inventoryStacks = new ItemStack[2];

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound tankTag = new NBTTagCompound();
        forgeTank.writeToNBT(tankTag);
        tag.setTag("Tank", tankTag);
     }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Tank")) {
            forgeTank.readFromNBT(tag.getCompoundTag("Tank"));
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        int filled = forgeTank.fill(resource, doFill);
        if(filled > 0 && doFill) {
            this.markDirtyAndUpdate();
        }
        return filled;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        FluidStack drained = forgeTank.drain(resource.amount, doDrain);
        if(drained != null && doDrain) {
            this.markDirtyAndUpdate();
        }
        return drained;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        FluidStack drained = forgeTank.drain(maxDrain, doDrain);
        if(drained != null && doDrain) {
            this.markDirtyAndUpdate();
        }
        return drained;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{forgeTank.getInfo()};
    }

    public void markDirtyAndUpdate() {
        this.markDirty();
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public int getSizeInventory() {
        return inventoryStacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < inventoryStacks.length) {
            return inventoryStacks[slot];
        } else {
            return null;
        }
    }

    /**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     */
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (this.inventoryStacks[slot] != null) {
            ItemStack stack;

            if (this.inventoryStacks[slot].stackSize <= amount) {
                // If the stack is smaller than the requested amount
                stack = this.inventoryStacks[slot];
                this.inventoryStacks[slot] = null;
                return stack;
            } else {
                stack = this.inventoryStacks[slot].splitStack(amount);

                if (this.inventoryStacks[slot].stackSize == 0) {
                    this.inventoryStacks[slot] = null;
                }

                return stack;
            }
        } else {
            return null;
        }
    }

    /**
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     */
    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if(inventoryStacks[slot] != null) {
            ItemStack stack = inventoryStacks[slot];
            inventoryStacks[slot] = null;
            return stack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack) {
        inventoryStacks[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return "forgeFluidTankInventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return false;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
        return true;
    }

    @Override
    public void updateEntity() {
        ItemStack stackIn = this.getStackInSlot(0);
        ItemStack stackOut = this.getStackInSlot(1);
        if (stackIn == null || stackOut != null || stackIn.stackSize > 1) {
            return;
        }

        if (FluidContainerRegistry.isBucket(stackIn)) { // Handle buckets
            FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stackIn);
            if (fluidStack != null) {
                // Bucket is full: attempt transfer bucket -> tank
                int filled = forgeTank.fill(fluidStack, true);
                if (filled > 0) {
                    this.setInventorySlotContents(0, null);
                    this.setInventorySlotContents(1, new ItemStack(Items.bucket));
                }
            } else {
                // Bucket is empty: attempt transfer tank -> bucket
                if (forgeTank.getFluidAmount() < FluidContainerRegistry.BUCKET_VOLUME) return;
                FluidStack drained = forgeTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
                if (drained == null) return;
                if (drained.amount > 0) {
                    this.setInventorySlotContents(0, null);
                    stackIn = FluidContainerRegistry.fillFluidContainer(drained, stackIn);
                    if (stackIn == null) return;
                    this.setInventorySlotContents(1, stackIn);
                }
            }
        } else if (stackIn.getItem() instanceof IFluidContainerItem) {
            // Container has fluid: attempt transfer container -> tank
            IFluidContainerItem containerItem = (IFluidContainerItem)stackIn.getItem();
            FluidStack fluidStack = containerItem.getFluid(stackIn);
            if (fluidStack != null) {
                int filled = forgeTank.fill(fluidStack, true);
                containerItem.drain(stackIn, filled, true);
                if (filled > 0) {
                    this.setInventorySlotContents(0, null);
                    this.setInventorySlotContents(1, stackIn);
                }
            } else {
                // Container is empty: attempt transfer tank -> container
                FluidStack drained = forgeTank.drain(containerItem.getCapacity(stackIn), true);
                this.setInventorySlotContents(0, null);
                containerItem.fill(stackIn, drained, true);
                this.setInventorySlotContents(1, stackIn);
            }
        }
    }
}
