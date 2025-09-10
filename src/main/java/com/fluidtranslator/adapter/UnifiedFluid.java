package com.fluidtranslator.adapter;

import com.fluidtranslator.ModFluidRegistry;
import com.hbm.inventory.fluid.FluidType;
import net.minecraftforge.fluids.Fluid;

/**
 * UnifiedFluid is an abstraction layer that represents a fluid
 * in both incompatible systems:
 * <ul>
 *     <li><b>HBM</b>: uses {@link com.hbm.inventory.fluid.FluidType}</li>
 *     <li><b>Forge</b>: uses {@link net.minecraftforge.fluids.Fluid}</li>
 * </ul>
 *
 * This class allows working with a single unified object regardless
 * of the underlying system.
 *
 * Internally, it stores the HBM {@link FluidType} as the "source of truth",
 * while the Forge counterpart is retrieved from {@link ModFluidRegistry}
 * only when needed.
 *
 * The {@code amount} field represents the fluid volume, shared between
 * both systems.
 */
public class UnifiedFluid {
    private FluidType hbmFluid;

    private UnifiedFluid(FluidType hbmFluid) {
        this.hbmFluid = hbmFluid;
    }

    public static UnifiedFluid fromForge(Fluid forgeFluid) {
        return new UnifiedFluid(ModFluidRegistry.getHBMFluid(forgeFluid));
    }

    public static UnifiedFluid fromHBM(FluidType hbmFluid) {
        return new UnifiedFluid(hbmFluid);
    }

    public FluidType toHBM() {
        return hbmFluid;
    }

    public Fluid toForge() {
        return ModFluidRegistry.getForgeFluid(hbmFluid);
    }
}
