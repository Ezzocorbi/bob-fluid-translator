package com.ezzo.fluidtranslator;

import net.minecraftforge.common.config.Configuration;

public class ModConfig {

    public static Configuration config;

    public static boolean enableFeatureX;
    public static int spawnRate;

    public static void syncConfig() {
        try {
            config.load();

            enableFeatureX = config.getBoolean("enableFeatureX", "general", true, "comment1");
            spawnRate = config.getInt("spawnRate", "world", 10, 1, 100, "comment2");

        } catch (Exception e) {
            System.err.println("Error while loading config file");
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) config.save();
        }
    }
}
