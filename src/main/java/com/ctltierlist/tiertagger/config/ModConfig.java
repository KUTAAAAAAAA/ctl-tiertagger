package com.ctltierlist.tiertagger.config;

import com.ctltierlist.tiertagger.CTLTierTagger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    
    // Default values
    private static boolean enabled = true;
    private static boolean showGamemode = true;
    private static long cacheTimeMinutes = 30;
    private static boolean debugMode = false;
    private static String selectedGamemode = "Sword";
    private static boolean showHighestTier = false;
    
    // Tier colors (Exact TierTagger colors)
    private static int colorHT1 = 0xe8ba3a;
    private static int colorLT1 = 0xd5b355;
    private static int colorHT2 = 0xc4d3e7;
    private static int colorLT2 = 0xa0a7b2;
    private static int colorHT3 = 0xf89f5a;
    private static int colorLT3 = 0xc67b42;
    private static int colorHT4 = 0x81749a;
    private static int colorLT4 = 0x655b79;
    private static int colorHT5 = 0x8f82a8;
    private static int colorLT5 = 0x655b79;
    
    // Gradient colors (second color for gradients)
    private static int gradientColorHT1 = 0xd5a830;
    private static int gradientColorLT1 = 0xc2a045;
    private static int gradientColorHT2 = 0xb0c0d7;
    private static int gradientColorLT2 = 0x8090a2;
    private static int gradientColorHT3 = 0xe88040;
    private static int gradientColorLT3 = 0xb06030;
    private static int gradientColorHT4 = 0x706080;
    private static int gradientColorLT4 = 0x554860;
    private static int gradientColorHT5 = 0x7d6f98;
    private static int gradientColorLT5 = 0x554860;
    
    // Gradient enable flags
    private static boolean gradientEnabledHT1 = false;
    private static boolean gradientEnabledLT1 = false;
    private static boolean gradientEnabledHT2 = false;
    private static boolean gradientEnabledLT2 = false;
    private static boolean gradientEnabledHT3 = false;
    private static boolean gradientEnabledLT3 = false;
    private static boolean gradientEnabledHT4 = false;
    private static boolean gradientEnabledLT4 = false;
    private static boolean gradientEnabledHT5 = false;
    private static boolean gradientEnabledLT5 = false;

    public static void init(Path configDir) {
        configFile = new File(configDir.toFile(), "ctl-tiertagger.json");
        load();
    }

    public static void load() {
        if (!configFile.exists()) {
            save(); // Create default config
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            
            if (json.has("enabled")) enabled = json.get("enabled").getAsBoolean();
            if (json.has("showGamemode")) showGamemode = json.get("showGamemode").getAsBoolean();
            if (json.has("cacheTimeMinutes")) cacheTimeMinutes = json.get("cacheTimeMinutes").getAsLong();
            if (json.has("debugMode")) debugMode = json.get("debugMode").getAsBoolean();
            if (json.has("selectedGamemode")) selectedGamemode = json.get("selectedGamemode").getAsString();
            if (json.has("showHighestTier")) showHighestTier = json.get("showHighestTier").getAsBoolean();
            
            // Load tier colors
            if (json.has("colorHT1")) colorHT1 = json.get("colorHT1").getAsInt();
            if (json.has("colorLT1")) colorLT1 = json.get("colorLT1").getAsInt();
            if (json.has("colorHT2")) colorHT2 = json.get("colorHT2").getAsInt();
            if (json.has("colorLT2")) colorLT2 = json.get("colorLT2").getAsInt();
            if (json.has("colorHT3")) colorHT3 = json.get("colorHT3").getAsInt();
            if (json.has("colorLT3")) colorLT3 = json.get("colorLT3").getAsInt();
            if (json.has("colorHT4")) colorHT4 = json.get("colorHT4").getAsInt();
            if (json.has("colorLT4")) colorLT4 = json.get("colorLT4").getAsInt();
            if (json.has("colorHT5")) colorHT5 = json.get("colorHT5").getAsInt();
            if (json.has("colorLT5")) colorLT5 = json.get("colorLT5").getAsInt();
            
            // Load gradient colors
            if (json.has("gradientColorHT1")) gradientColorHT1 = json.get("gradientColorHT1").getAsInt();
            if (json.has("gradientColorLT1")) gradientColorLT1 = json.get("gradientColorLT1").getAsInt();
            if (json.has("gradientColorHT2")) gradientColorHT2 = json.get("gradientColorHT2").getAsInt();
            if (json.has("gradientColorLT2")) gradientColorLT2 = json.get("gradientColorLT2").getAsInt();
            if (json.has("gradientColorHT3")) gradientColorHT3 = json.get("gradientColorHT3").getAsInt();
            if (json.has("gradientColorLT3")) gradientColorLT3 = json.get("gradientColorLT3").getAsInt();
            if (json.has("gradientColorHT4")) gradientColorHT4 = json.get("gradientColorHT4").getAsInt();
            if (json.has("gradientColorLT4")) gradientColorLT4 = json.get("gradientColorLT4").getAsInt();
            if (json.has("gradientColorHT5")) gradientColorHT5 = json.get("gradientColorHT5").getAsInt();
            if (json.has("gradientColorLT5")) gradientColorLT5 = json.get("gradientColorLT5").getAsInt();
            
            // Load gradient enable flags
            if (json.has("gradientEnabledHT1")) gradientEnabledHT1 = json.get("gradientEnabledHT1").getAsBoolean();
            if (json.has("gradientEnabledLT1")) gradientEnabledLT1 = json.get("gradientEnabledLT1").getAsBoolean();
            if (json.has("gradientEnabledHT2")) gradientEnabledHT2 = json.get("gradientEnabledHT2").getAsBoolean();
            if (json.has("gradientEnabledLT2")) gradientEnabledLT2 = json.get("gradientEnabledLT2").getAsBoolean();
            if (json.has("gradientEnabledHT3")) gradientEnabledHT3 = json.get("gradientEnabledHT3").getAsBoolean();
            if (json.has("gradientEnabledLT3")) gradientEnabledLT3 = json.get("gradientEnabledLT3").getAsBoolean();
            if (json.has("gradientEnabledHT4")) gradientEnabledHT4 = json.get("gradientEnabledHT4").getAsBoolean();
            if (json.has("gradientEnabledLT4")) gradientEnabledLT4 = json.get("gradientEnabledLT4").getAsBoolean();
            if (json.has("gradientEnabledHT5")) gradientEnabledHT5 = json.get("gradientEnabledHT5").getAsBoolean();
            if (json.has("gradientEnabledLT5")) gradientEnabledLT5 = json.get("gradientEnabledLT5").getAsBoolean();
            
            CTLTierTagger.LOGGER.info("Config loaded successfully");
        } catch (Exception e) {
            CTLTierTagger.LOGGER.error("Failed to load config: {}", e.getMessage());
        }
    }

    public static void save() {
        try {
            configFile.getParentFile().mkdirs();
            
            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            json.addProperty("showGamemode", showGamemode);
            json.addProperty("cacheTimeMinutes", cacheTimeMinutes);
            json.addProperty("debugMode", debugMode);
            json.addProperty("selectedGamemode", selectedGamemode);
            json.addProperty("showHighestTier", showHighestTier);
            
            // Save tier colors
            json.addProperty("colorHT1", colorHT1);
            json.addProperty("colorLT1", colorLT1);
            json.addProperty("colorHT2", colorHT2);
            json.addProperty("colorLT2", colorLT2);
            json.addProperty("colorHT3", colorHT3);
            json.addProperty("colorLT3", colorLT3);
            json.addProperty("colorHT4", colorHT4);
            json.addProperty("colorLT4", colorLT4);
            json.addProperty("colorHT5", colorHT5);
            json.addProperty("colorLT5", colorLT5);
            
            // Save gradient colors
            json.addProperty("gradientColorHT1", gradientColorHT1);
            json.addProperty("gradientColorLT1", gradientColorLT1);
            json.addProperty("gradientColorHT2", gradientColorHT2);
            json.addProperty("gradientColorLT2", gradientColorLT2);
            json.addProperty("gradientColorHT3", gradientColorHT3);
            json.addProperty("gradientColorLT3", gradientColorLT3);
            json.addProperty("gradientColorHT4", gradientColorHT4);
            json.addProperty("gradientColorLT4", gradientColorLT4);
            json.addProperty("gradientColorHT5", gradientColorHT5);
            json.addProperty("gradientColorLT5", gradientColorLT5);
            
            // Save gradient enable flags
            json.addProperty("gradientEnabledHT1", gradientEnabledHT1);
            json.addProperty("gradientEnabledLT1", gradientEnabledLT1);
            json.addProperty("gradientEnabledHT2", gradientEnabledHT2);
            json.addProperty("gradientEnabledLT2", gradientEnabledLT2);
            json.addProperty("gradientEnabledHT3", gradientEnabledHT3);
            json.addProperty("gradientEnabledLT3", gradientEnabledLT3);
            json.addProperty("gradientEnabledHT4", gradientEnabledHT4);
            json.addProperty("gradientEnabledLT4", gradientEnabledLT4);
            json.addProperty("gradientEnabledHT5", gradientEnabledHT5);
            json.addProperty("gradientEnabledLT5", gradientEnabledLT5);
            
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(json, writer);
            }
            
            CTLTierTagger.LOGGER.info("Config saved successfully");
        } catch (Exception e) {
            CTLTierTagger.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    // Getters
    public static boolean isEnabled() { return enabled; }
    public static boolean shouldShowGamemode() { return showGamemode; }
    public static long getCacheTime() { return cacheTimeMinutes * 60 * 1000; } // Convert to milliseconds
    public static boolean isDebugMode() { return debugMode; }
    public static String getSelectedGamemode() { return selectedGamemode; }
    public static boolean shouldShowHighestTier() { return showHighestTier; }
    
    // Color getters
    public static int getColorHT1() { return colorHT1; }
    public static int getColorLT1() { return colorLT1; }
    public static int getColorHT2() { return colorHT2; }
    public static int getColorLT2() { return colorLT2; }
    public static int getColorHT3() { return colorHT3; }
    public static int getColorLT3() { return colorLT3; }
    public static int getColorHT4() { return colorHT4; }
    public static int getColorLT4() { return colorLT4; }
    public static int getColorHT5() { return colorHT5; }
    public static int getColorLT5() { return colorLT5; }
    
    // Gradient color getters
    public static int getGradientColorHT1() { return gradientColorHT1; }
    public static int getGradientColorLT1() { return gradientColorLT1; }
    public static int getGradientColorHT2() { return gradientColorHT2; }
    public static int getGradientColorLT2() { return gradientColorLT2; }
    public static int getGradientColorHT3() { return gradientColorHT3; }
    public static int getGradientColorLT3() { return gradientColorLT3; }
    public static int getGradientColorHT4() { return gradientColorHT4; }
    public static int getGradientColorLT4() { return gradientColorLT4; }
    public static int getGradientColorHT5() { return gradientColorHT5; }
    public static int getGradientColorLT5() { return gradientColorLT5; }
    
    // Gradient enabled getters
    public static boolean isGradientEnabledHT1() { return gradientEnabledHT1; }
    public static boolean isGradientEnabledLT1() { return gradientEnabledLT1; }
    public static boolean isGradientEnabledHT2() { return gradientEnabledHT2; }
    public static boolean isGradientEnabledLT2() { return gradientEnabledLT2; }
    public static boolean isGradientEnabledHT3() { return gradientEnabledHT3; }
    public static boolean isGradientEnabledLT3() { return gradientEnabledLT3; }
    public static boolean isGradientEnabledHT4() { return gradientEnabledHT4; }
    public static boolean isGradientEnabledLT4() { return gradientEnabledLT4; }
    public static boolean isGradientEnabledHT5() { return gradientEnabledHT5; }
    public static boolean isGradientEnabledLT5() { return gradientEnabledLT5; }

    // Setters
    public static void setEnabled(boolean value) { enabled = value; save(); }
    public static void setShowGamemode(boolean value) { showGamemode = value; save(); }
    public static void setCacheTimeMinutes(long value) { cacheTimeMinutes = value; save(); }
    public static void setDebugMode(boolean value) { debugMode = value; save(); }
    public static void setSelectedGamemode(String value) { selectedGamemode = value; save(); }
    public static void setShowHighestTier(boolean value) { showHighestTier = value; save(); }
    
    // Color setters
    public static void setColorHT1(int value) { colorHT1 = value; save(); }
    public static void setColorLT1(int value) { colorLT1 = value; save(); }
    public static void setColorHT2(int value) { colorHT2 = value; save(); }
    public static void setColorLT2(int value) { colorLT2 = value; save(); }
    public static void setColorHT3(int value) { colorHT3 = value; save(); }
    public static void setColorLT3(int value) { colorLT3 = value; save(); }
    public static void setColorHT4(int value) { colorHT4 = value; save(); }
    public static void setColorLT4(int value) { colorLT4 = value; save(); }
    public static void setColorHT5(int value) { colorHT5 = value; save(); }
    public static void setColorLT5(int value) { colorLT5 = value; save(); }
    
    // Gradient color setters
    public static void setGradientColorHT1(int value) { gradientColorHT1 = value; save(); }
    public static void setGradientColorLT1(int value) { gradientColorLT1 = value; save(); }
    public static void setGradientColorHT2(int value) { gradientColorHT2 = value; save(); }
    public static void setGradientColorLT2(int value) { gradientColorLT2 = value; save(); }
    public static void setGradientColorHT3(int value) { gradientColorHT3 = value; save(); }
    public static void setGradientColorLT3(int value) { gradientColorLT3 = value; save(); }
    public static void setGradientColorHT4(int value) { gradientColorHT4 = value; save(); }
    public static void setGradientColorLT4(int value) { gradientColorLT4 = value; save(); }
    public static void setGradientColorHT5(int value) { gradientColorHT5 = value; save(); }
    public static void setGradientColorLT5(int value) { gradientColorLT5 = value; save(); }
    
    // Gradient enabled setters
    public static void setGradientEnabledHT1(boolean value) { gradientEnabledHT1 = value; save(); }
    public static void setGradientEnabledLT1(boolean value) { gradientEnabledLT1 = value; save(); }
    public static void setGradientEnabledHT2(boolean value) { gradientEnabledHT2 = value; save(); }
    public static void setGradientEnabledLT2(boolean value) { gradientEnabledLT2 = value; save(); }
    public static void setGradientEnabledHT3(boolean value) { gradientEnabledHT3 = value; save(); }
    public static void setGradientEnabledLT3(boolean value) { gradientEnabledLT3 = value; save(); }
    public static void setGradientEnabledHT4(boolean value) { gradientEnabledHT4 = value; save(); }
    public static void setGradientEnabledLT4(boolean value) { gradientEnabledLT4 = value; save(); }
    public static void setGradientEnabledHT5(boolean value) { gradientEnabledHT5 = value; save(); }
    public static void setGradientEnabledLT5(boolean value) { gradientEnabledLT5 = value; save(); }
    
    // Helper method to get tier color by name
    public static int getTierColor(String tier) {
        // Handle retired tiers (e.g., "RHT3" -> use retired color)
        if (tier.startsWith("R")) {
            return 0x808080; // TierTagger retired color (gray)
        }
        
        if (tier.startsWith("HT1")) return colorHT1;
        else if (tier.startsWith("LT1")) return colorLT1;
        else if (tier.startsWith("HT2")) return colorHT2;
        else if (tier.startsWith("LT2")) return colorLT2;
        else if (tier.startsWith("HT3")) return colorHT3;
        else if (tier.startsWith("LT3")) return colorLT3;
        else if (tier.startsWith("HT4")) return colorHT4;
        else if (tier.startsWith("LT4")) return colorLT4;
        else if (tier.startsWith("HT5")) return colorHT5;
        else if (tier.startsWith("LT5")) return colorLT5;
        return 0xD3D3D3; // Default gray
    }
    
    // Gamemode utilities
    public static String[] getAvailableGamemodes() {
        return new String[]{"Sword", "Crystal", "Netherite", "Potion", "Mace", "UHC", "Axe", "SMP"};
    }
    
    public static void cycleGamemode() {
        String[] gamemodes = getAvailableGamemodes();
        int currentIndex = 0;
        for (int i = 0; i < gamemodes.length; i++) {
            if (gamemodes[i].equals(selectedGamemode)) {
                currentIndex = i;
                break;
            }
        }
        currentIndex = (currentIndex + 1) % gamemodes.length;
        setSelectedGamemode(gamemodes[currentIndex]);
    }
    
    // Helper method to get gradient color by tier name
    public static int getGradientColor(String tier) {
        if (tier.startsWith("HT1")) return gradientColorHT1;
        else if (tier.startsWith("LT1")) return gradientColorLT1;
        else if (tier.startsWith("HT2")) return gradientColorHT2;
        else if (tier.startsWith("LT2")) return gradientColorLT2;
        else if (tier.startsWith("HT3")) return gradientColorHT3;
        else if (tier.startsWith("LT3")) return gradientColorLT3;
        else if (tier.startsWith("HT4")) return gradientColorHT4;
        else if (tier.startsWith("LT4")) return gradientColorLT4;
        else if (tier.startsWith("HT5")) return gradientColorHT5;
        else if (tier.startsWith("LT5")) return gradientColorLT5;
        return 0xAAAAAA; // Default gray
    }
    
    // Helper method to check if gradient is enabled for a tier
    public static boolean isGradientEnabled(String tier) {
        if (tier.startsWith("HT1")) return gradientEnabledHT1;
        else if (tier.startsWith("LT1")) return gradientEnabledLT1;
        else if (tier.startsWith("HT2")) return gradientEnabledHT2;
        else if (tier.startsWith("LT2")) return gradientEnabledLT2;
        else if (tier.startsWith("HT3")) return gradientEnabledHT3;
        else if (tier.startsWith("LT3")) return gradientEnabledLT3;
        else if (tier.startsWith("HT4")) return gradientEnabledHT4;
        else if (tier.startsWith("LT4")) return gradientEnabledLT4;
        else if (tier.startsWith("HT5")) return gradientEnabledHT5;
        else if (tier.startsWith("LT5")) return gradientEnabledLT5;
        return false;
    }
    
    // Reset all settings to defaults
    public static void resetToDefaults() {
        enabled = true;
        showGamemode = true;
        cacheTimeMinutes = 30;
        debugMode = false;
        selectedGamemode = "Sword";
        showHighestTier = false;
        resetColorsToDefaults();
        save();
    }
    
    // Reset colors to defaults
    public static void resetColorsToDefaults() {
        colorHT1 = 0xe8ba3a;
        colorLT1 = 0xd5b355;
        colorHT2 = 0xc4d3e7;
        colorLT2 = 0xa0a7b2;
        colorHT3 = 0xf89f5a;
        colorLT3 = 0xc67b42;
        colorHT4 = 0x81749a;
        colorLT4 = 0x655b79;
        colorHT5 = 0x8f82a8;
        colorLT5 = 0x655b79;
        
        gradientColorHT1 = 0xd5a830;
        gradientColorLT1 = 0xc2a045;
        gradientColorHT2 = 0xb0c0d7;
        gradientColorLT2 = 0x8090a2;
        gradientColorHT3 = 0xe88040;
        gradientColorLT3 = 0xb06030;
        gradientColorHT4 = 0x706080;
        gradientColorLT4 = 0x554860;
        gradientColorHT5 = 0x7d6f98;
        gradientColorLT5 = 0x554860;
        
        gradientEnabledHT1 = false;
        gradientEnabledLT1 = false;
        gradientEnabledHT2 = false;
        gradientEnabledLT2 = false;
        gradientEnabledHT3 = false;
        gradientEnabledLT3 = false;
        gradientEnabledHT4 = false;
        gradientEnabledLT4 = false;
        gradientEnabledHT5 = false;
        gradientEnabledLT5 = false;
        save();
    }
}
