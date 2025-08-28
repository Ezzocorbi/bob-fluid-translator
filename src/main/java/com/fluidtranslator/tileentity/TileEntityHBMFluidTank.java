package com.fluidtranslator.tileentity;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;

public class TileEntityHBMFluidTank extends TileEntityMachineBase implements IFluidStandardTransceiverMK2 {

    protected FluidNode node;
    protected FluidType lastType = Fluids.NONE;
    public FluidTank tank;
    public short mode = 0;

    public TileEntityHBMFluidTank() {
        super(1);
        tank = new FluidTank(Fluids.NONE, 2000);
    }

    // mode 0 = receiver
    // mode 1 = buffer
    // mode 2 = sender
    // mode 3 = disabled
    @Override
    public FluidTank[] getSendingTanks() {
        return (mode == 1 || mode == 2) ? new FluidTank[] {tank} : new FluidTank[0];
    }

    @Override
    public FluidTank[] getReceivingTanks() {
        return (mode == 0 || mode == 1) ? new FluidTank[] {tank} : new FluidTank[0];
    }

    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { tank };
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if(this.mode == 2 || this.mode == 3) return 0;
        if(tank.getPressure() != pressure) return 0;
        return type == tank.getTankType() ? tank.getMaxFill() - tank.getFill() : 0;
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
        markDirtyAndUpdate();
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
        if(!worldObj.isRemote) {
            // In buffer mode, acts like a pipe block, providing fluid to its own node
            // otherwise, it is a regular providing/receiving machine, blocking further propagation
            if(mode == 1) {
                if(this.node == null || this.node.expired || tank.getTankType() != lastType) {

                    this.node = (FluidNode) UniNodespace.getNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider());

                    if(this.node == null || this.node.expired || tank.getTankType() != lastType) {
                        this.node = this.createNode(tank.getTankType());
                        UniNodespace.createNode(worldObj, this.node);
                        lastType = tank.getTankType();
                    }
                }

                if(node != null && node.hasValidNet()) {
                    node.net.addProvider(this);
                    node.net.addReceiver(this);
                }
            } else {
                if(this.node != null) {
                    UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider());
                    this.node = null;
                }

                for(DirPos pos : getConPos()) {
                    FluidNode dirNode = (FluidNode) UniNodespace.getNode(worldObj, pos.getX(), pos.getY(), pos.getZ(), tank.getTankType().getNetworkProvider());

                    if(mode == 2) {
                        tryProvide(tank, worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
                    } else {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeProvider(this);
                    }

                    if(mode == 0) {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.addReceiver(this);
                    } else {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeReceiver(this);
                    }
                }
            }

//            if(this.node != null) {
//                UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider());
//                this.node = null;
//            }
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

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag, "tank");
        tag.setTag("Tank", tankTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Tank")) {
            tank.readFromNBT(tag.getCompoundTag("Tank"), "tank");
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

    public void markDirtyAndUpdate() {
        this.markDirty();
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }
}
