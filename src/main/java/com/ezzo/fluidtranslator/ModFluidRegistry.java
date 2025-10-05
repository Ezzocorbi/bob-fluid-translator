package com.ezzo.fluidtranslator;

import com.ezzo.fluidtranslator.blocks.CustomFluidBlock;
import com.ezzo.fluidtranslator.item.CustomFluidItemBlock;
import com.ezzo.fluidtranslator.item.GenericBucket;
import com.google.common.collect.HashBiMap;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashSet;
import java.util.Set;

/**
 * This class manages the registration and translation of fluids from the HBM mod
 * into the Forge fluid system.
 * <p>
 * Given an instance of {@link com.hbm.inventory.fluid.FluidType} (an HBM-defined fluid),
 * the class handles the registration of the corresponding {@link net.minecraftforge.fluids.Fluid}
 * through the Forge API, automatically creating the associated block and bucket.
 * </p>
 *
 * <p>
 * The conversion (lookup) between HBM fluids and Forge fluids is based on a naming convention:
 * <ul>
 *   <li><b>HBM</b> uses the format: <code>"FLUID_NAME"</code> (uppercase, no suffix)</li>
 *   <li><b>Forge</b> uses the format: <code>"fluid_name_fluid"</code> (lowercase, with a <code>_fluid</code> suffix)</li>
 * </ul>
 * Custom exceptions to this naming rule are handled through an internal lookup table.
 *
 */
public class ModFluidRegistry {

    /** This list contains fluids that shouldn't get a translation.
     * It's used by other classes to check if a fluid has a translation handled by
     * this registry.
     */
    private static final Set<String> blackList = new HashSet<String>();

    // This look up table is used to match fluids that don't follow the naming convention
    private static final HashBiMap<Fluid, FluidType> lookUpTable = HashBiMap.create();

    public ModFluidRegistry() {
        blackList.add(Fluids.NONE.getName());
        blackList.add(Fluids.WATER.getName());
        blackList.add(Fluids.LAVA.getName());
        blackList.add(Fluids.WATZ.getName());
        blackList.add("CUSTOM_DEMO");

        lookUpTable.put(FluidRegistry.getFluid("mud_fluid"), Fluids.WATZ);
        lookUpTable.put(FluidRegistry.getFluid("water"), Fluids.WATER);
        lookUpTable.put(FluidRegistry.getFluid("lava"), Fluids.LAVA);
    }

    /**
     * Given a {@link FluidType} from HBM, this method registers a corresponding Forge Fluid ({@link Fluid})
     * @param fluidType HBM fluid
     * @return Returns the fluid block associated to the ForgeFluid
     */
    public CustomFluidBlock registerFluidType(FluidType fluidType) {
        String name = fluidType.getName().toLowerCase() + "_fluid";
        Fluid forgeFluid = new Fluid(name);
        FluidRegistry.registerFluid(forgeFluid);

        LanguageRegistry.instance().addStringLocalization("fluid." + name, "en_US", fluidType.getName());

        CustomFluidBlock block = new CustomFluidBlock(forgeFluid, Material.water, name);
        GameRegistry.registerBlock(block, CustomFluidItemBlock.class, name + "_block");
        forgeFluid.setBlock(block);

        GenericBucket genericBucket = new GenericBucket(forgeFluid, block);
        GameRegistry.registerItem(genericBucket, name + "_bucket");

        FluidContainerRegistry.registerFluidContainer(
                new FluidStack(forgeFluid, FluidContainerRegistry.BUCKET_VOLUME),
                new ItemStack(genericBucket),
                new ItemStack(Items.bucket)
        );

        return block;
    }

    /**
     * Returns the corresponding HBM fluid
     * @param fluid Forge fluid
     * @return returns null if there is no correspondence
     */
    public static FluidType getHBMFluid(Fluid fluid) {
        FluidType result = lookUpTable.get(fluid);
        if (result != null) return result;

        return Fluids.fromName(fluid.getName()
                .replaceFirst("_fluid$", "") // remove "_fluid" at the end of the string
                .toUpperCase());
    }

    /**
     * Returns the corresponding Forge fluid
     * @param fluidType HBM fluid
     * @return returns null if there is no correspondence (like for black-listed fluids)
     */
    public static Fluid getForgeFluid(FluidType fluidType) {
        Fluid result = lookUpTable.inverse().get(fluidType);
        if (result != null) return result;
        return FluidRegistry.getFluid(fluidType.getName().toLowerCase() + "_fluid");
    }

    /**
     *
     * @param fluidType Fluid to check
     * @return Returns true if the fluid should not be registered
     */
    public static boolean isBlackListed(FluidType fluidType) {
        return blackList.contains(fluidType.getName());
    }
}
