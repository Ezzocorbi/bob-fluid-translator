package com.ezzo.fluidtranslator;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hbm.inventory.fluid.Fluids;
import net.minecraftforge.common.config.Configuration;


import java.util.*;

public class ModConfig {

    public static Configuration config;

    /** This list contains NTM fluids for which we should
     * not register a Forge fluid.
     */
    public static Set<String> fluidBlacklist;

    public static BiMap<String, String> customMappings;

    /**
     * Loads configs from file and sets their values in game.
     * Finally, saves the configs if they have changed.
     */
    public static void syncConfig() {
        config.load();

        String[] blacklist = config.getStringList("fluidBlacklist",
                "conversion",
                new String[] {
                        Fluids.NONE.getName(), "CUSTOM_DEMO"
                },
                "Fluids in the blacklist do not receive an automatic mapping.\n" +
                        "For more info visit https://github.com/Ezzocorbi/bob-fluid-translator/wiki/Configs\n");
        fluidBlacklist = new HashSet<String>(Arrays.asList(blacklist));

        String[] stringMappings = config.getStringList(
                "mappings",
                "conversion",
                new String[] {
                        "WATZ=mud_fluid"
                },
                "Overrides the automatic mapping by defining custom NTM to Forge fluid associations.\n" +
                        "These take precedence over automatic mapping.\n" +
                        "For more info visit https://github.com/Ezzocorbi/bob-fluid-translator/wiki/Configs\n"
        );

        customMappings = parseMappings(stringMappings);
        if (config.hasChanged()) config.save();
    }

    /**
     * Parses an array of fluid mapping definitions and converts them into a key-value map.
     * <p>
     * Each element of the {@code mappings} array must be in the format:
     * <pre>
     * "NTM Fluid Name=Forge Fluid Name"
     * </pre>
     * For example:
     * <pre>
     * {"WATER=water", "HELIUM=helium"}
     * </pre>
     * would produce a map where:
     * <ul>
     *   <li>{@code "WATER"} maps to {@code "water"}</li>
     *   <li>{@code "HELIUM"} maps to {@code "helium"}</li>
     * </ul>
     * <p>
     * The method performs basic validation and will throw an exception if any entry does not match
     * the expected format (i.e. if it doesn't split into exactly two parts separated by {@code "="}).
     *
     * @param raw an array of strings representing fluid name mappings in the format
     *                 {@code "NTM Fluid=Forge Fluid"}
     * @return a {@link Map} where the key is the NTM fluid name and the value is the Forge fluid name
     * @throws RuntimeException if any mapping string is invalid or malformed
     */
    private static BiMap<String, String> parseMappings(String[] raw) {
        BiMap<String, String> customMappings = HashBiMap.create();
        for(String s: raw) {
            String[] parts = s.split("=", 2);
            if (parts.length != 2) throw new RuntimeException("Invalid mapping in config: " + s);
            customMappings.put(parts[0].trim(), parts[1].trim());
        }
        return customMappings;
    }
}
