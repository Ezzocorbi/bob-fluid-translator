package com.fluidtranslator.tileentity;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.fluidtranslator.CustomFluidRegistry;
import com.fluidtranslator.TankModes;
import com.fluidtranslator.adapter.UnifiedFluidStack;
import com.fluidtranslator.adapter.UnifiedFluidTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.HashSet;

public class TileEntityUniversalTank extends TileEntityMachineBase implements IFluidStandardTransceiverMK2, IFluidHandler {

    protected FluidNode node;
    protected FluidType lastType = Fluids.NONE;
    public UnifiedFluidTank tank;
    public short mode = 0;

    public TileEntityUniversalTank() {
        super(1);
        slots = new ItemStack[2];
        tank = new UnifiedFluidTank(4000);
    }

    /// HBM-RELEVANT IMPLEMENTATION ///

    @Override
    public FluidTank[] getSendingTanks() {
        if (mode == TankModes.BUFFER.ordinal || mode == TankModes.SENDER.ordinal)
            return new FluidTank[] {tank.toHBM()};
        else
            return new FluidTank[0];
    }

    @Override
    public FluidTank[] getReceivingTanks() {
        if (mode == TankModes.BUFFER.ordinal || mode == TankModes.RECEIVER.ordinal)
            return new FluidTank[] {tank.toHBM()};
        else
            return new FluidTank[0];
    }

    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { tank.toHBM() };
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if(this.mode == TankModes.SENDER.ordinal || this.mode == TankModes.DISABLED.ordinal) return 0;
        if(tank.toHBM().getPressure() != pressure) return 0;
        return type == tank.toHBM().getTankType() ? tank.getCapacity() - tank.getFill() : 0;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getName() {
        return "hbmFluidTankTest";
    }

    public long transferFluid(FluidType type, int pressure, long amount) {
        long toTransfer = Math.min(getDemand(type, pressure), amount);
        tank.setFill(tank.getFill() + (int) toTransfer);
        return amount - toTransfer;
    }

    public long getFluidAvailable(FluidType type, int pressure) {
        long amount = 0;
        for(FluidTank tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) amount += tank.getFill();
        }
        return amount;
    }

    public void useUpFluid(FluidType type, int pressure, long amount) {
        int tanks = 0;
        for(FluidTank tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) tanks++;
        }
        if(tanks > 1) {
            int firstRound = (int) Math.floor((double) amount / (double) tanks);
            for(FluidTank tank : getSendingTanks()) {
                if(tank.getTankType() == type && tank.getPressure() == pressure) {
                    int toRem = Math.min(firstRound, tank.getFill());
                    tank.setFill(tank.getFill() - toRem);
                    amount -= toRem;
                }
            }
        }
        if(amount > 0) for(FluidTank tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) {
                int toRem = (int) Math.min(amount, tank.getFill());
                tank.setFill(tank.getFill() - toRem);
                amount -= toRem;
            }
        }
    }

    @Override
    public void updateEntity() {
        if(mode == TankModes.DISABLED.ordinal && this.node != null) {
            UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, tank.toHBM().getTankType().getNetworkProvider());
            this.node = null;
            return;
        }

        if(!worldObj.isRemote) {
            if(mode == TankModes.BUFFER.ordinal) {
                if(this.node == null || this.node.expired || tank.toHBM().getTankType() != lastType) {

                    this.node = (FluidNode) UniNodespace.getNode(worldObj, xCoord, yCoord, zCoord, tank.toHBM().getTankType().getNetworkProvider());

                    if(this.node == null || this.node.expired || tank.toHBM().getTankType() != lastType) {
                        this.node = this.createNode(tank.toHBM().getTankType());
                        UniNodespace.createNode(worldObj, this.node);
                        lastType = tank.toHBM().getTankType();
                    }
                }

                if(node != null && node.hasValidNet()) {
                    node.net.addProvider(this);
                    node.net.addReceiver(this);
                }
            } else {
                if(this.node != null) {
                    UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, tank.toHBM().getTankType().getNetworkProvider());
                    this.node = null;
                }

                for(DirPos pos : getConPos()) {
                    FluidNode dirNode = (FluidNode) UniNodespace.getNode(worldObj, pos.getX(), pos.getY(), pos.getZ(), tank.toHBM().getTankType().getNetworkProvider());

                    if(mode == TankModes.SENDER.ordinal) {
                        tryProvide(tank.toHBM(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
                    } else {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeProvider(this);
                    }

                    if(mode == TankModes.RECEIVER.ordinal) {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.addReceiver(this);
                    } else {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeReceiver(this);
                    }
                }
            }
            this.networkPackNT(50);
        }
    }

    protected DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(xCoord + 1, yCoord, zCoord, Library.POS_X),
                new DirPos(xCoord - 1, yCoord, zCoord, Library.NEG_X),
                new DirPos(xCoord, yCoord + 1, zCoord, Library.POS_Y),
                new DirPos(xCoord, yCoord - 1, zCoord, Library.NEG_Y),
                new DirPos(xCoord, yCoord, zCoord + 1, Library.POS_Z),
                new DirPos(xCoord, yCoord, zCoord - 1, Library.NEG_Z)
        };
    }

    protected FluidNode createNode(FluidType type) {
        DirPos[] conPos = getConPos();

        HashSet<BlockPos> posSet = new HashSet<BlockPos>();
        posSet.add(new BlockPos(this));
        for(DirPos pos : conPos) {
            ForgeDirection dir = pos.getDir();
            posSet.add(new BlockPos(pos.getX() - dir.offsetX, pos.getY() - dir.offsetY, pos.getZ() - dir.offsetZ));
        }

        return new FluidNode(type.getNetworkProvider(), posSet.toArray(new BlockPos[posSet.size()])).setConnections(conPos);
    }

    /// FORGE-RELEVANT IMPLEMENTATION ///

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound tankTag = tank.writeToNBT();
        tag.setTag("tank", tankTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("tank")) {
            tank.readFromNBT(tag.getCompoundTag("tank"));
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        System.out.println("Sending packet");
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        System.out.println("Received packet");
        this.readFromNBT(pkt.func_148857_g());
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        int filled = tank.fill(UnifiedFluidStack.fromForge(resource), doFill);
        if(filled > 0 && doFill) {
            this.markDirtyAndUpdate();
        }
        return filled;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        UnifiedFluidStack drained = tank.drain(resource.amount, doDrain);
        if(drained != null && doDrain) {
            this.markDirtyAndUpdate();
        }
        return drained.toForge();
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        UnifiedFluidStack drained = tank.drain(maxDrain, doDrain);
        if(drained != null && doDrain) {
            this.markDirtyAndUpdate();
        }
        return drained.toForge();
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return tank.toHBM().getTankType() == CustomFluidRegistry.getHBMFluid(fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{tank.toForge().getInfo()};
    }

    ///  INVENTORY HANDLING ///

    @Override
    public int getSizeInventory() {
        return slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < slots.length) {
            return slots[slot];
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
        if (this.slots[slot] != null) {
            ItemStack stack;

            if (this.slots[slot].stackSize <= amount) {
                // If the stack is smaller than the requested amount
                stack = this.slots[slot];
                this.slots[slot] = null;
                return stack;
            } else {
                stack = this.slots[slot].splitStack(amount);

                if (this.slots[slot].stackSize == 0) {
                    this.slots[slot] = null;
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
        if(slots[slot] != null) {
            ItemStack stack = slots[slot];
            slots[slot] = null;
            return stack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack) {
        slots[slot] = itemStack;
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
    public int getInventoryStackLimit() {
        return 1;
    }

    public void markDirtyAndUpdate() {
        this.markDirty();
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }
}
