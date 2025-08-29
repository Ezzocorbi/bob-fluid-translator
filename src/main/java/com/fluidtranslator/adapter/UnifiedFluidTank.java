package com.fluidtranslator.adapter;

import com.fluidtranslator.CustomFluidRegistry;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

/**
 * UnifiedFluidTank provides a unified abstraction for fluid tanks,
 * bridging the differences between HBM’s {@link com.hbm.inventory.fluid.tank.FluidTank}
 * and Forge’s {@link net.minecraftforge.fluids.FluidTank}.
 *
 * Internally, this class uses an HBM {@link FluidTank} as the "source of truth".
 * Conversions to Forge-compatible tanks are handled on demand via
 * {@link CustomFluidRegistry}.
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
        FluidStack forgeFluidStack = new FluidStack(CustomFluidRegistry.getForgeFluid(hbmTank.getTankType()), getFill());
        return new net.minecraftforge.fluids.FluidTank(forgeFluidStack, getCapacity());
    }

    public UnifiedFluidTank readFromNBT(NBTTagCompound nbt) {
        return null;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return null;
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

        UnifiedFluidStack stack = UnifiedFluidStack.fromHBM(hbmTank.getTankType(), drained);
        if (doDrain)
        {
            setFill(getFill() - drained);
            if (getFill() < 0) {
                setFill(0);
            }
        }
        return stack;
    }

    public void setFluid(UnifiedFluid fluid) {
        hbmTank.setTankType(fluid.toHBM());
    }
}
