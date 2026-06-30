package com.ezzo.fluidtranslator;

import com.ezzo.fluidtranslator.blocks.fluid.CorrosiveFluidBlock;
import com.ezzo.fluidtranslator.blocks.fluid.CustomFluidBlock;
import com.ezzo.fluidtranslator.blocks.fluid.FlammableFluidBlock;
import com.ezzo.fluidtranslator.blocks.fluid.GasFluidBlock;
import com.ezzo.fluidtranslator.item.CustomFluidItemBlock;
import com.ezzo.fluidtranslator.item.GenericBucket;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class manages the registration and translation of fluids from the HBM mod
 * into the Forge fluid system.
 * <p>
 * Given an instance of {@link FluidType} (an HBM-defined fluid),
 * the class handles the registration of the corresponding {@link Fluid}
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

    public ModFluidRegistry() {

    }

    /**
     * Given a {@link FluidType} from HBM, this method registers a corresponding Forge Fluid ({@link Fluid})
     *
     * @param fluidType HBM fluid
     */
    public void registerFluidType(FluidType fluidType) {
        String name = ModConfig.customMappings.getOrDefault(
                fluidType.getName(),
                fluidType.getName().toLowerCase() + ModConfig.suffix);

        Fluid forgeFluid = new Fluid(name)
                .setTemperature(fluidType.temperature)
                .setGaseous(fluidType.hasTrait(FluidTraitSimple.FT_Gaseous.class));
        FluidRegistry.registerFluid(forgeFluid);

        Block block = Blocks.air;
        if(!isBlockBlackListed(fluidType))
        {
            if(fluidType.hasTrait(FT_Corrosive.class)){
                block = new CorrosiveFluidBlock(forgeFluid, Material.water, name);
            }
            else if (fluidType.hasTrait(FluidTraitSimple.FT_Gaseous.class) ||
                     fluidType.hasTrait(FluidTraitSimple.FT_Gaseous_ART.class)){
                block = new GasFluidBlock(forgeFluid, Material.water, name);
            }
            else if(fluidType.hasTrait(FT_Combustible.class) ||
                    fluidType.hasTrait(FT_Flammable.class)){
                block = new FlammableFluidBlock(forgeFluid, Material.water, name);
            }
            else {
                block = new CustomFluidBlock(forgeFluid, Material.water, name);
            }

            GameRegistry.registerBlock(block, CustomFluidItemBlock.class, name + "_block");
            forgeFluid.setBlock(block);
        }
        GenericBucket genericBucket = new GenericBucket(forgeFluid, block);
        GameRegistry.registerItem(genericBucket, name + "_bucket");

        FluidContainerRegistry.registerFluidContainer(
                new FluidStack(forgeFluid, FluidContainerRegistry.BUCKET_VOLUME),
                new ItemStack(genericBucket),
                new ItemStack(Items.bucket)
        );

    }

    /**
     * Returns the corresponding HBM fluid
     * @param fluid Forge fluid
     * @return returns NONE {@link FluidType} if there is no correspondence
     */
    public static FluidType getHBMFluid(Fluid fluid) {
        String fluidName = ModConfig.customMappings.inverse().getOrDefault(
                fluid.getName(),
                fluid.getName()
                        .replaceFirst(ModConfig.suffix + "$", "")
                        .toUpperCase());
        return Fluids.fromName(fluidName);
    }

    /**
     * Returns the corresponding Forge fluid
     * @param fluidType HBM fluid
     * @return returns null if there is no correspondence (like for black-listed fluids)
     */
    public static Fluid getForgeFluid(FluidType fluidType) {
        String fluidName = ModConfig.customMappings.getOrDefault(
                fluidType.getName(),
                fluidType.getName().toLowerCase() + ModConfig.suffix
        );
        return FluidRegistry.getFluid(fluidName);
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
     *
     * @param fluidType Fluid to check
     * @return Returns true if the fluid block should not be registered
     */
    public static boolean isBlockBlackListed(FluidType fluidType) {
        return ModConfig.blockBlacklist.contains(fluidType.getName());
    }
    /**
     * Returns all registered {@link FluidType}s that are not blacklisted.
     *
     * @return an {@link Iterable} of valid, non-blacklisted {@link FluidType}s
     */
    public static Iterable<FluidType> validFluids() {
        List<FluidType> result = new ArrayList<>();

        Arrays.stream(Fluids.getAll())
                .filter(f -> !ModFluidRegistry.isBlackListed(f))
                .forEach(result::add);

        return result;
    }

    /**
     *
     * @param fluid Fluid to check
     * @return True if a texture exists for the fluid {@code fluid}, false otherwise
     */
    public static boolean textureExists(FluidType fluid) {
        try {
            Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(fluid.getTexture());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
