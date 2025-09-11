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
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityHBMAdapter extends TileEntity implements IFluidHandler, IInventory {

    private IFluidStandardTransceiverMK2 fluidHandler;
    private TileEntity lastConnectedMachine;
    private int tankIndex = 0;
    private final ItemStack[] inventoryStacks = new ItemStack[2];
    private boolean initialized = false;
    private BlockPos targetPos;
    private boolean connected = false;

    /**
     * @return Returns true if the adapter is connected to a machine
     */
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

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
        tag.setBoolean("connected", connected);
        if (targetPos != null) {
            tag.setIntArray("targetPos", new int[] {targetPos.getX(), targetPos.getY(), targetPos.getZ()});
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
        if (tag.hasKey("targetPos")) {
            int[] pos = tag.getIntArray("targetPos");
            targetPos = new BlockPos(pos[0], pos[1], pos[2]);
        }
        if (tag.hasKey("connected")) {
            connected = tag.getBoolean("connected");
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
        if (targetPos != null && isConnected())
            findAndConnectTank(targetPos);
    }

    /**
     * When the adjacent block changes we check
     * 1. is a tile entity present at that position?
     * 2. is the tile entity a fluid handler we can connect to?
     * @param world World where the neighbor was placed
     * @param neighbor Neighbor block
     */
    public void onNeighborChanged(World world, Block neighbor) {
        if (targetPos == null) return;

        TileEntity newMachine = world.getTileEntity(targetPos.getX(), targetPos.getY(), targetPos.getZ());
         if (newMachine == null) {
            this.fluidHandler = null;
            setConnected(false);
            markDirtyAndUpdate();
        } else if (lastConnectedMachine == null || !lastConnectedMachine.equals(newMachine)) {
            if (findAndConnectTank(targetPos)) markDirtyAndUpdate();
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
        BlockPos currentPos = new BlockPos(this.xCoord, this.yCoord, this.zCoord);
        BlockPos offset = new BlockPos(direction.offsetX, direction.offsetY, direction.offsetZ);
        BlockPos searchPos = currentPos.add(offset);

        boolean tankChanged = findAndConnectTank(searchPos);
        if (tankChanged) markDirtyAndUpdate();
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
     * @param target The position of the tile entity to search
     * @return true if a valid fluid transceiver was found and connected, false otherwise
     */
    private boolean findAndConnectTank(BlockPos target) {
        // Try to get the tile entity at the neighbor position
        TileEntity neighborTile = worldObj.getTileEntity(target.getX(), target.getY(), target.getZ());

        // Case 1: Neighbor is directly a fluid transceiver
        if (neighborTile instanceof IFluidStandardTransceiverMK2) {
            connectToTransceiver((IFluidStandardTransceiverMK2) neighborTile, target);
            this.lastConnectedMachine = neighborTile;
            return true;
        }

        // Case 2: Neighbor is part of a multiblock machine (BlockDummyable)
        Block neighborBlock = worldObj.getBlock(target.getX(), target.getY(), target.getZ());
        if (neighborBlock instanceof BlockDummyable) {
            BlockDummyable dummy = (BlockDummyable) neighborBlock;

            // Get the coordinates of the multiblock's core
            int[] corePos = dummy.findCore(worldObj, target.getX(), target.getY(), target.getZ());
            TileEntity coreTile = worldObj.getTileEntity(corePos[0], corePos[1], corePos[2]);

            if (coreTile instanceof IFluidStandardTransceiverMK2) {
                IFluidStandardTransceiverMK2 transceiver = (IFluidStandardTransceiverMK2) coreTile;

                // Verify if the multiblock can connect fluids at this position
                FluidType fluid = transceiver.getAllTanks()[tankIndex].getTankType();
                ForgeDirection direction = getForgeDirection(target);

                boolean canConnect = Library.canConnectFluid(
                        worldObj, target.getX(), target.getY(), target.getZ(), direction, fluid
                );

                if (canConnect) {
                    connectToTransceiver(transceiver, target);
                    this.lastConnectedMachine = coreTile;
                    return true;
                }
            }
        }

        // No valid transceiver found
        return false;
    }

    /**
     * Determines the {@link ForgeDirection} from this tile entity to the given target position.
     * <p>
     * This method calculates the difference in coordinates between the current
     * tile entity and the target {@link BlockPos}, then matches it against the
     * known offset vectors of {@link ForgeDirection}.
     * <p>
     * Works only for directly adjacent blocks (offset of ±1 on exactly one axis).
     *
     * @param target The target position to compare with this tile entity's coordinates
     * @return The corresponding {@link ForgeDirection}, or {@link ForgeDirection#UNKNOWN}
     *         if the target is not a direct neighbor
     */
    private ForgeDirection getForgeDirection(BlockPos target) {
        int dx = target.getX() - this.xCoord;
        int dy = target.getY() - this.yCoord;
        int dz = target.getZ() - this.zCoord;

        for (ForgeDirection dir : ForgeDirection.values()) {
            if (dx == dir.offsetX &&
                dy == dir.offsetY &&
                dz == dir.offsetZ) {
                return dir;
            }
        }
        return ForgeDirection.UNKNOWN;
    }

    /**
     * Helper method: sets the fluid handler and direction for this adapter.
     */
    private void connectToTransceiver(IFluidStandardTransceiverMK2 transceiver, BlockPos targetPos) {
        this.setFluidHandler(transceiver);
        this.setTargetPosition(targetPos);
        this.connected = true;
    }

    /**
     *
     * @return Returns all tanks of the connected machine. Returns {@code null} if there is no machine connected.
     */
    public FluidTank[] getAllTanks() {
        if (!isConnected()) return null;
        else return fluidHandler.getAllTanks();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (!isConnected()) return 0;
        if (resource == null) return 0;
        if (!canFill(from, resource.getFluid())) return 0;

        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];
        // Look for corresponding fluid type from HBM
        FluidType type = ModFluidRegistry.getHBMFluid(resource.getFluid());

        int toFill = Math.min(resource.amount, tank.getMaxFill() - tank.getFill());
        if (doFill) {
            if (tank.getTankType().getID() == Fluids.NONE.getID()) {
                tank.setTankType(type);
            }
            tank.setFill(tank.getFill() + toFill);
            markDirtyAndUpdate();
        }
        return toFill;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (!isConnected()) return null;
        if (resource == null || resource.getFluid() == null) return null;
        if (!canDrain(from, resource.getFluid())) return null;

        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (!isConnected()) return null;
        if (maxDrain <= 0) return null;

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
        if (!isConnected()) return false;
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
        if (!isConnected()) return false;
        if (fluid == null) return false;
        FluidTank tank = fluidHandler.getAllTanks()[tankIndex];
        return tank.getTankType().getID() == ModFluidRegistry.getHBMFluid(fluid).getID();
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        if (!isConnected())
            return new FluidTankInfo[] {new FluidTankInfo(null, 0)};

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
        if (!initialized && worldObj != null) {
            if (targetPos != null) {
                findAndConnectTank(targetPos);
            }
            this.initialized = true;
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    /**
     * Set the direction where the fluid transceiver is located
     * @param position The position where neighbor is located
     */
    public void setTargetPosition(BlockPos position) {
        this.targetPos = position;
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
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
        return false;
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
