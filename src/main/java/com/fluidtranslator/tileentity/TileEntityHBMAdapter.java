package com.fluidtranslator.tileentity;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.fluidtranslator.ModFluidRegistry;
import com.fluidtranslator.adapter.UnifiedFluidStack;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityHBMAdapter extends TileEntity implements IFluidHandler, IInventory {

    private ForgeDirection targetDirection; // Direction where the fluid transceiver is located
    private IFluidStandardTransceiverMK2 fluidHandler;
    private int tankIndex = 0;
    private final ItemStack[] inventoryStacks = new ItemStack[2];
    private boolean initialized = false;

    /**
     * If this is true, the adapter will reset the {@link FluidType}
     * of the connected tank to {@code Fluids.NONE} when it empties the tank
     */
    private boolean resetFluidType = true;

    public TileEntityHBMAdapter() {

    }

    public void setTankIndex(int index) {
        this.tankIndex = index;
        markDirtyAndUpdate();
    }

    public int getTankIndex() {
        return this.tankIndex;
    }

    public void shouldResetFluidType(boolean set) {
        this.resetFluidType = set;
        markDirtyAndUpdate();
    }

    public boolean doesResetFluidType() {
        return this.resetFluidType;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("tankIndex", tankIndex);
        tag.setBoolean("resetFluidType", resetFluidType);
        if (targetDirection != null) {
            tag.setInteger("fluidHandlerDirection", targetDirection.ordinal());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("tankIndex")) {
            tankIndex = tag.getInteger("tankIndex");
        }
        if (tag.hasKey("resetFluidType")) {
            resetFluidType = tag.getBoolean("resetFluidType");
        }
        if (tag.hasKey("fluidHandlerDirection")) {
            targetDirection = ForgeDirection.getOrientation(tag.getInteger("fluidHandlerDirection"));
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
        NBTTagCompound tag = pkt.func_148857_g();
        this.readFromNBT(tag);
        if (tag.hasKey("fluidHandlerDirection")) {
            ForgeDirection dir = ForgeDirection.getOrientation(tag.getInteger("fluidHandlerDirection"));
            findAndConnectTank(dir);
        }
    }

    /**
     * Sets the fluid transceiver of this adapter to the tile entity adjacent to this one,
     * in the specified direction.
     *
     * The fluid transceiver is updated only if there is a valid one at the new location.
     *
     * Steps:
     * - Uses this tile entity's coordinates as the starting point,
     * - Calculates the position of the neighboring tile entity in the given direction,
     * - If that tile entity implements {@link IFluidStandardTransceiverMK2},
     *   it is set as the new fluid transceiver.
     *
     * @param direction The direction relative to this tile entity where the neighbor is located
     * @return Returns true if the tank was updated, false if no tank was found at the new location.
     */
    public boolean setTankAtDirection(ForgeDirection direction) {
        boolean tankChanged = findAndConnectTank(direction);
        markDirtyAndUpdate();
        return tankChanged;
    }

    /**
     * Attempts to locate and connect to a fluid transceiver in the given direction.
     *
     * Search order:
     * 1. Check if the adjacent tile entity is directly a fluid transceiver.
     * 2. If not, check if the adjacent block is part of a multiblock machine ({@link BlockDummyable}).
     *    - Find the "core" tile entity of the multiblock.
     *    - Verify that the block at the given position can connect fluids.
     *    - If valid, set it as the transceiver.
     *
     * @param direction The direction to search relative to this tile entity
     * @return true if a valid fluid transceiver was found and connected, false otherwise
     */
    private boolean findAndConnectTank(ForgeDirection direction) {
        BlockPos currentPos = new BlockPos(this.xCoord, this.yCoord, this.zCoord);
        BlockPos offset = new BlockPos(direction.offsetX, direction.offsetY, direction.offsetZ);
        BlockPos neighborPos = currentPos.add(offset);

        // Try to get the tile entity at the neighbor position
        TileEntity neighborTile = worldObj.getTileEntity(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ());

        // Case 1: Neighbor is directly a fluid transceiver
        if (neighborTile instanceof IFluidStandardTransceiverMK2) {
            connectToTransceiver((IFluidStandardTransceiverMK2) neighborTile, direction);
            return true;
        }

        // Case 2: Neighbor is part of a multiblock machine (BlockDummyable)
        Block neighborBlock = worldObj.getBlock(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ());
        if (neighborBlock instanceof BlockDummyable) {
            BlockDummyable dummy = (BlockDummyable) neighborBlock;

            // Get the coordinates of the multiblock's core
            int[] corePos = dummy.findCore(worldObj, neighborPos.getX(), neighborPos.getY(), neighborPos.getZ());
            TileEntity coreTile = worldObj.getTileEntity(corePos[0], corePos[1], corePos[2]);

            if (coreTile instanceof IFluidStandardTransceiverMK2) {
                IFluidStandardTransceiverMK2 transceiver = (IFluidStandardTransceiverMK2) coreTile;

                // Verify if the multiblock can connect fluids at this position
                FluidType fluid = transceiver.getAllTanks()[tankIndex].getTankType();
                boolean canConnect = Library.canConnectFluid(
                        worldObj, neighborPos.getX(), neighborPos.getY(), neighborPos.getZ(), direction, fluid
                );

                if (canConnect) {
                    connectToTransceiver(transceiver, direction);
                    return true;
                }
            }
        }

        // No valid transceiver found
        return false;
    }

    /**
     * Helper method: sets the fluid handler and direction for this adapter.
     */
    private void connectToTransceiver(IFluidStandardTransceiverMK2 transceiver, ForgeDirection direction) {
        this.setFluidHandler(transceiver);
        this.setTargetDirection(direction);
    }

    /**
     *
     * @return Returns all tanks of the connected machine. Returns {@code null} if there is no machine connected.
     */
    public FluidTank[] getAllTanks() {
        if (fluidHandler == null) return null;
        else return fluidHandler.getAllTanks();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null) return 0;

        if (fluidHandler == null) return 0;
        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];

        // Look for corresponding fluid type from HBM
        FluidType type = ModFluidRegistry.getHBMFluid(resource.getFluid());

        if (!canFill(from, resource.getFluid())) return 0; // Incompatible fluids

        int toFill = Math.min(resource.amount, tank.getMaxFill() - tank.getFill());
        if (doFill) {
            tank.setTankType(type);
            tank.setFill(tank.getFill() + toFill);
            markDirtyAndUpdate();
        }
        return toFill;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.getFluid() == null) return null;
        if (fluidHandler == null) return null;
        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];
        if (tank.getTankType() != ModFluidRegistry.getHBMFluid(resource.getFluid())) return null;
        return drain(from, resource.amount, doDrain);

    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (fluidHandler == null) return null;
        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];
        if (tank.getFill() <= 0) return null;

        int drained = Math.min(maxDrain, tank.getFill());
        Fluid fluid = ModFluidRegistry.getForgeFluid(tank.getTankType());
        if (fluid == null) return null; // No correspondence to any Forge fluid, don't drain

        FluidStack fs = new FluidStack(fluid, drained);
        if (doDrain) {
            tank.setFill(tank.getFill() - drained);
            if (resetFluidType && tank.getFill() <= 0) tank.setTankType(Fluids.NONE);
            markDirtyAndUpdate();
        }
        return fs;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        if (fluidHandler == null) return false;
        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];
        FluidType incomingFluid = ModFluidRegistry.getHBMFluid(fluid);
        FluidType storedFluid = tank.getTankType();

        if (incomingFluid == null) return false;
        if (incomingFluid.getID() == Fluids.NONE.getID()) return false;
        if (storedFluid.getID() == Fluids.NONE.getID()) return true;
        if (storedFluid.getID() == incomingFluid.getID()) return true;
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        if (fluidHandler == null) return false;
        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];
        return tank.getTankType().getID() == ModFluidRegistry.getHBMFluid(fluid).getID();
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        if (fluidHandler == null) return null;
        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];
        UnifiedFluidStack fluidStack = UnifiedFluidStack.fromHBM(tank.getTankType(), tank.getFill());
        if (fluidStack.isEmpty()) {
            return new FluidTankInfo[]{ new FluidTankInfo(null, tank.getMaxFill()) };
        } else {
            return new FluidTankInfo[]{ new FluidTankInfo(fluidStack.toForge(), tank.getMaxFill()) };
        }
    }

    @Override
    public void updateEntity() {
        if (!initialized && worldObj != null && targetDirection != null) {
            findAndConnectTank(targetDirection);
        }
        this.initialized = true;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    /**
     * Set the direction where the fluid transceiver is located
     * @param direction The direction relative to this tile entity where the neighbor is located
     */
    public void setTargetDirection(ForgeDirection direction) {
        this.targetDirection = direction;
    }

    public ForgeDirection getTargetDirection() {
        return this.targetDirection;
    }

    public void setFluidHandler(IFluidStandardTransceiverMK2 handler) {
        this.fluidHandler = handler;
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
        return "hbmAdapterInventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
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
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return false;
    }
}
