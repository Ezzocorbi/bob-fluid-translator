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
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;

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

    private static final HashBiMap<FluidType, Fluid> customMappings = HashBiMap.create();

    public ModFluidRegistry() {

    }

    /**
     * Given a {@link FluidType} from HBM, this method registers a corresponding Forge Fluid ({@link Fluid})
     * @param fluidType HBM fluid
     * @return Returns the fluid block associated to the ForgeFluid
     */
    public CustomFluidBlock registerFluidType(FluidType fluidType) {
        String name = fluidType.getName().toLowerCase();
        Fluid forgeFluid = new Fluid(name);
        FluidRegistry.registerFluid(forgeFluid);

        LanguageRegistry.instance().addStringLocalization("fluid." + name, "en_US", StatCollector.translateToLocal(fluidType.getUnlocalizedName()));

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
        FluidType result = customMappings.inverse().get(fluid);
        if (result != null) return result;
        return Fluids.fromName(fluid.getName().toUpperCase());
    }

    /**
     * Returns the corresponding Forge fluid
     * @param fluidType HBM fluid
     * @return returns null if there is no correspondence (like for black-listed fluids)
     */
    public static Fluid getForgeFluid(FluidType fluidType) {
        Fluid result = customMappings.get(fluidType);
        if (result != null) return result;
        return FluidRegistry.getFluid(fluidType.getName().toLowerCase());
    }

    /**
     *
     * @param fluidType Fluid to check
     * @return Returns true if the fluid should not be registered
     */
    public static boolean isBlackListed(FluidType fluidType) {
        return ModConfig.fluidBlacklist.contains(fluidType.getName());
    }

    /**
     * Registers custom fluid mappings between NTM fluid types and Forge fluid instances.
     * <p>
     * This method takes a {@link Map} and resolves the fluid objects on both sides:
     * <ul>
     *   <li><b>Key</b>: NTM fluid name</li>
     *   <li><b>Value</b>: Forge fluid name</li>
     * </ul>
     * <p>
     * Each entry is validated to ensure both the NTM fluid and Forge fluid exist.
     * If any mapping refers to an unknown fluid, a {@link RuntimeException} is thrown.
     * <p>
     * Successfully resolved pairs are stored in the {@code customMappings} map,
     * linking NTM {@link FluidType} objects to Forge {@link Fluid} objects.
     *
     * @param mappings a {@link Map} where each key is an NTM fluid name and each value is the corresponding Forge fluid name
     * @throws RuntimeException if either the NTM fluid or the Forge fluid cannot be found
     */
    public static void setCustomMappings(Map<String, String> mappings) {
        mappings.keySet().forEach(fluidTypeName -> {
            String forgeFluidName = mappings.get(fluidTypeName);

            FluidType fluidType = Fluids.fromName(fluidTypeName);
            Fluid forgeFluid = FluidRegistry.getFluid(forgeFluidName);
            if (fluidType == null || fluidType.getID() == Fluids.NONE.getID()) throw new RuntimeException("Cannot find NTM fluid '" + fluidTypeName + "' defined in custom mappings.");
            if (forgeFluid == null) throw new RuntimeException("Cannot find Forge fluid '" + forgeFluidName + "' defined in custom mappings.");

            customMappings.put(fluidType, forgeFluid);
        });
    }
}
