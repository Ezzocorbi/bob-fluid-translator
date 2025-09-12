package com.fluidtranslator.adapter;

import com.fluidtranslator.ModFluidRegistry;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * UnifiedFluidTank provides a unified abstraction for fluid tanks,
 * bridging the differences between HBM’s {@link com.hbm.inventory.fluid.tank.FluidTank}
 * and Forge’s {@link net.minecraftforge.fluids.FluidTank}.
 *
 * Internally, this class uses an HBM {@link FluidTank} as the "source of truth".
 * Conversions to Forge-compatible tanks are handled on demand via
 * {@link ModFluidRegistry}.
 *
 * Key features:
 * <ul>
 *     <li>Acts as a fluid container with configurable capacity.</li>
 *     <li>Supports filling with {@link UnifiedFluidStack}, ensuring compatibility
 *     with both Forge and HBM fluid systems.</li>
 *     <li>Supports draining, correctly handling empty states using {@link Fluids#NONE}.</li>
 *     <li>Provides conversion methods: {@link #toHBM()} for HBM and {@link #toForge()} for Forge.</li>
 * </ul>
 *
 * Usage example:
 * <pre>
 * UnifiedFluidTank tank = new UnifiedFluidTank(4000);
 * tank.fill(UnifiedFluidStack.fromForge(waterFluid, 1000), true);
 * UnifiedFluidStack drained = tank.drain(500, true);
 * </pre>
 *
 * This class makes it possible for game mechanics, GUIs, and machines to
 * interact seamlessly with either Forge fluids or HBM fluids, without
 * needing to manage separate tank implementations manually.
 */
public class UnifiedFluidTank {
    private final FluidTank hbmTank;

    public UnifiedFluidTank(int capacity) {
        hbmTank = new FluidTank(Fluids.NONE, capacity);
    }

    public FluidTank toHBM() {
        return hbmTank;
    }

    public net.minecraftforge.fluids.FluidTank toForge() {
        Fluid forgeFluid = ModFluidRegistry.getForgeFluid(hbmTank.getTankType());
        if (forgeFluid == null) {
            return new net.minecraftforge.fluids.FluidTank(null, 0); // empty fluid tank
        }
        FluidStack forgeFluidStack = new FluidStack(forgeFluid, getFill());
        return new net.minecraftforge.fluids.FluidTank(forgeFluidStack, getCapacity());
    }

    public int getFill() {
        if (hbmTank.getTankType() == Fluids.NONE) {
            return 0;
        } else {
            return hbmTank.getFill();
        }
    }

    public void setFill(int fill) {
        hbmTank.setFill(fill);
    }

    public int getCapacity() {
        return hbmTank.getMaxFill();
    }

    /**
     * Attempts tank capacity and returns the difference between the old capacity and the new capacity.
     * Does not change capacity of old capacity is greater, in which case it returns zero.
     * @param newSize New capacity
     * @return Difference between old capacity and new capacity
     */
    public int changeTankSize(int newSize) {
        return hbmTank.changeTankSize(newSize);
    }

    public int fill(UnifiedFluidStack resource, boolean doFill) {
        if (resource == null)
        {
            return 0;
        }

        if (!doFill)
        {
            if (hbmTank.getTankType() == Fluids.NONE)
            {
                return Math.min(getCapacity(), resource.amount());
            }

            if (hbmTank.getTankType().getID() != resource.toHBM().type.getID())
            {
                return 0;
            }

            return Math.min(getCapacity() - getFill(), resource.amount());
        }

        if (hbmTank.getTankType() == Fluids.NONE)
        {
            hbmTank.conform(resource.toHBM());
            hbmTank.setFill(Math.min(getCapacity(), resource.amount()));
            return getFill();
        }

        if (hbmTank.getTankType().getID() != resource.toHBM().type.getID())
        {
            return 0;
        }
        int filled = getCapacity() - getFill();

        if (resource.amount() < filled)
        {
            setFill(getFill() + resource.amount());
            filled = resource.amount();
        }
        else
        {
            setFill(getCapacity());
        }

        return filled;
    }

    public UnifiedFluidStack drain(int maxDrain, boolean doDrain) {
        if (hbmTank.getTankType() == Fluids.NONE)
        {
            return UnifiedFluidStack.emptyStack();
        }

        int drained = maxDrain;
        if (getFill() < drained)
        {
            drained = getFill();
        }

        UnifiedFluidStack stackDrained = UnifiedFluidStack.fromHBM(hbmTank.getTankType(), drained);
        if (doDrain)
        {
            setFill(getFill() - drained);
            if (getFill() < 0) {
                setFill(0);
            }
        }
        return stackDrained;
    }

    /**
     * Changes fluid type of the tank and <b>resets fill to zero</b>
     * @param fluid New fluid
     */
    public void setFluid(UnifiedFluid fluid) {
        hbmTank.setTankType(fluid.toHBM());
    }

    /**
     * Attempts to set the tank's fluid type <b>only if the tank is empty</b>.
     *
     * @param fluid The new fluid type
     * @return {@code true} if the fluid type was successfully changed,
     *         or if the tank already contains the same fluid type;
     *         {@code false} if the tank is not empty and holds a different fluid.
     */
    public boolean setFluidSafe(UnifiedFluid fluid) {
        if (this.toHBM().getTankType().getID() == fluid.toHBM().getID()) return true;
        if (this.getFill() > 0) return false;
        else hbmTank.setTankType(fluid.toHBM());
        return true;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("fluidId", hbmTank.getTankType().getID());
        tag.setInteger("maxFill", this.getCapacity());
        tag.setInteger("fill", this.getFill());
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("fluidId")) {
            int fluidId = tag.getInteger("fluidId");
            UnifiedFluid fluid = UnifiedFluid.fromHBM(Fluids.fromID(fluidId));
            this.setFluid(fluid);
        }

        if (tag.hasKey("maxFill")) {
            hbmTank.changeTankSize(tag.getInteger("maxFill"));
        }

        if (tag.hasKey("fill")) {
            int fill = tag.getInteger("fill");
            this.setFill(fill);
        }
    }
}
