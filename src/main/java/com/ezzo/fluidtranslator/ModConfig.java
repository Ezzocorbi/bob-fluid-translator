package com.ezzo.fluidtranslator;

import com.google.common.collect.HashBiMap;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.*;

public class ModConfig {

    public static Configuration config;

    /** This list contains fluids that shouldn't get a translation.
     * It's used by other classes to check if a fluid has a translation handled by
     * this registry.
     */
    public static Set<String> fluidBlacklist;

    public static Map<String, String> customMappings;

    /**
     * Loads configs from file and sets their values in game
     * Finally, saves the configs if they have changed.
     */
    public static void syncConfig() {
        config.load();

        String[] blacklist = config.getStringList("fluidBlacklist",
                "conversion",
                new String[] {
                        Fluids.NONE.getName(), Fluids.WATZ.getName(), "CUSTOM_DEMO"
                },
                "Fluids from NTM that should not be translated to Forge fluids.\n" +
                        "For more info visit https://github.com/Ezzocorbi/bob-fluid-translator/wiki/Configs\n");
        fluidBlacklist = new HashSet<String>(Arrays.asList(blacklist));

        String[] stringMappings = config.getStringList(
                "mappings",
                "conversion",
                new String[0],
                "Custom mappings.\n" +
                        "For more info visit https://github.com/Ezzocorbi/bob-fluid-translator/wiki/Configs\n"
        );

        customMappings = parseMappings(stringMappings);
//        mappings.keySet().forEach(k -> System.out.println(k.getName() + " - " + mappings.get(k).getName())); // TODO Remove this log

        if (config.hasChanged()) config.save();
    }

    /**
     * Parses an array of fluid mapping definitions and converts them into a key-value map.
     * <p>
     * Each element of the {@code mappings} array must be in the format:
     * <pre>
     * "NTM Fluid Name - Forge Fluid Name"
     * </pre>
     * For example:
     * <pre>
     * {"WATER - water", "HELIUM - helium"}
     * </pre>
     * would produce a map where:
     * <ul>
     *   <li>{@code "WATER"} maps to {@code "water"}</li>
     *   <li>{@code "HELIUM"} maps to {@code "helium"}</li>
     * </ul>
     * <p>
     * The method performs basic validation and will throw an exception if any entry does not match
     * the expected format (i.e. if it doesn't split into exactly two parts separated by {@code " - "}).
     *
     * @param mappings an array of strings representing fluid name mappings in the format
     *                 {@code "NTM Fluid - Forge Fluid"}
     * @return a {@link Map} where the key is the NTM fluid name and the value is the Forge fluid name
     * @throws RuntimeException if any mapping string is invalid or malformed
     */
    private static Map<String, String> parseMappings(String[] mappings) {
        Map<String, String> result = new HashMap<>();
        for(String mapping: mappings) {
            String[] fluids = mapping.split(" - ");
            if (fluids.length != 2) throw new RuntimeException("Invalid mapping found in config: " + mapping);
            result.put(fluids[0], fluids[1]);
        }
        return result;
    }
}
