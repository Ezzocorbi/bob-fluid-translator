package com.fluidtranslator.tileentity;

import com.fluidtranslator.CustomFluidRegistry;
import com.fluidtranslator.FluidTranslator;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class TileEntityHBMWrapper extends TileEntity implements IFluidHandler {

    // Internal HBM tank
    public FluidTank tank;

    public TileEntityHBMWrapper() {
        super();
        this.tank = new FluidTank(Fluids.NONE, 16000);
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

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null) return 0;

        // Look for corresponding fluid type from HBM
        FluidType type = CustomFluidRegistry.getHBMFluid(resource.getFluid());
        if (type == null) return 0; // No correspondence to any HBM fluid

        int toFill = Math.min(resource.amount, tank.getMaxFill() - tank.getFill());
        if (doFill && canFill(from, resource.getFluid())) {
            tank.setTankType(type);
            tank.setFill(tank.getFill() + toFill);
            markDirtyAndUpdate();
        }
        return toFill;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (tank.getFill() <= 0) return null;

        int drained = Math.min(maxDrain, tank.getFill());
        FluidStack fs = new FluidStack(CustomFluidRegistry.getForgeFluid(tank.getTankType()), drained);

        if (doDrain) {
            tank.setFill(tank.getFill() - drained);
            if (tank.getFill() <= 0) tank.setTankType(Fluids.NONE);
            markDirtyAndUpdate();
        }

        return fs;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.getFluid() == null) return null;
        if (tank.getTankType() != CustomFluidRegistry.getHBMFluid(resource.getFluid())) return null;
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        FluidType incomingFluid = CustomFluidRegistry.getHBMFluid(fluid);
        FluidType storedFluid = tank.getTankType();
        if (incomingFluid == null) return false;
        if (storedFluid.getID() == Fluids.NONE.getID()) return true;
        if (storedFluid.getID() == incomingFluid.getID()) return true;
        return false;
    }


    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return tank.getTankType() == CustomFluidRegistry.getHBMFluid(fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        Fluid forgeFluid = CustomFluidRegistry.getForgeFluid(tank.getTankType());
        FluidStack fluidStack;
        if (forgeFluid == null) {
            fluidStack = null;
        } else {
            fluidStack = new FluidStack(forgeFluid, tank.getFill());
        }
        return new FluidTankInfo[] {
                new FluidTankInfo(fluidStack, tank.getMaxFill())
        };
    }

    public void markDirtyAndUpdate() {
        this.markDirty();
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }
}

