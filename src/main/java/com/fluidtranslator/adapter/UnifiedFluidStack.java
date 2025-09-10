package com.fluidtranslator.adapter;

import com.fluidtranslator.ModFluidRegistry;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraftforge.fluids.Fluid;

/**
 * UnifiedFluidStack represents a fluid-stack in a unified way,
 * bridging the gap between two incompatible systems:
 * <ul>
 *     <li><b>HBM</b>: uses {@link com.hbm.inventory.FluidStack}</li>
 *     <li><b>Forge</b>: uses {@link net.minecraftforge.fluids.FluidStack}</li>
 * </ul>
 *
 * Internally, it stores the HBM {@link FluidStack} as the "source of truth".
 * The Forge counterpart is created on demand via {@link ModFluidRegistry}.
 *
 * This class ensures that both systems can work with the same abstraction,
 * without the need for external conversion logic every time fluid data
 * needs to be transferred.
 *
 * Typical usage:
 * <ul>
 *     <li>Create a new stack from Forge with {@link #fromForge(Fluid, int)}</li>
 *     <li>Create a new stack from HBM with {@link #fromHBM(FluidType, int)}</li>
 *     <li>Convert back with {@link #toForge()} or {@link #toHBM()}</li>
 * </ul>
 *
 * An "empty" stack is represented by {@link Fluids#NONE} and amount 0,
 * accessible through {@link #emptyStack()}.
 */
public class UnifiedFluidStack {
    private com.hbm.inventory.FluidStack hbmFluidStack;

    private UnifiedFluidStack(UnifiedFluid fluid, int amount) {
        this.hbmFluidStack = new FluidStack(fluid.toHBM(), amount);
    }

    public static UnifiedFluidStack emptyStack() {
        return new UnifiedFluidStack(UnifiedFluid.fromHBM(Fluids.NONE), 0);
    }

    public static UnifiedFluidStack fromForge(Fluid forgeFluid, int amount) {
        return new UnifiedFluidStack(UnifiedFluid.fromForge(forgeFluid), amount);
    }

    public static UnifiedFluidStack fromHBM(FluidType hbmFluid, int amount) {
        return new UnifiedFluidStack(UnifiedFluid.fromHBM(hbmFluid), amount);
    }

    public static UnifiedFluidStack fromForge(net.minecraftforge.fluids.FluidStack forgeFluidStack, int amount) {
        return new UnifiedFluidStack(UnifiedFluid.fromForge(forgeFluidStack.getFluid()), amount);
    }

    public static UnifiedFluidStack fromForge(net.minecraftforge.fluids.FluidStack forgeFluidStack) {
        return new UnifiedFluidStack(UnifiedFluid.fromForge(forgeFluidStack.getFluid()), forgeFluidStack.amount);
    }

    public static UnifiedFluidStack fromHBM(FluidStack hbmFluidStack, int amount) {
        return new UnifiedFluidStack(UnifiedFluid.fromHBM(hbmFluidStack.type), amount);
    }

    public static UnifiedFluidStack fromHBM(FluidStack hbmFluidStack) {
        return new UnifiedFluidStack(UnifiedFluid.fromHBM(hbmFluidStack.type), hbmFluidStack.fill);
    }

    public FluidStack toHBM() {
        return hbmFluidStack;
    }

    public net.minecraftforge.fluids.FluidStack toForge() {
        if(isEmpty()) {
            return null;
        }
        return new net.minecraftforge.fluids.FluidStack(ModFluidRegistry.getForgeFluid(hbmFluidStack.type), hbmFluidStack.fill);
    }

    public UnifiedFluid getFluid() {
        return UnifiedFluid.fromHBM(this.hbmFluidStack.type);
    }

    public int amount() {
        return hbmFluidStack.fill;
    }

    public boolean isEmpty() {
        return hbmFluidStack.fill == 0 || hbmFluidStack.type.getID() == Fluids.NONE.getID();
    }

    public boolean isSameFluid(UnifiedFluidStack stack) {
        return hbmFluidStack.type.getID() == stack.toHBM().type.getID();
    }
}
