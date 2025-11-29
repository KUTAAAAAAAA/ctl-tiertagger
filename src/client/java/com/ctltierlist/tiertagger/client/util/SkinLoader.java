package com.ctltierlist.tiertagger.client.util;

import com.ctltierlist.tiertagger.CTLTierTagger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SkinLoader {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final String SKIN_DOWNLOAD_URL = "https://mineskin.eu/download/";
    private static final String HEAD_URL = "https://mineskin.eu/helm/";
    
    private static final Map<String, Identifier> skinCache = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> headCache = new ConcurrentHashMap<>();
    
    /**
     * Load full skin texture for 3D rendering
     */
    public static CompletableFuture<Identifier> loadSkinTexture(String playerName) {
        // Check cache first
        if (skinCache.containsKey(playerName.toLowerCase())) {
            return CompletableFuture.completedFuture(skinCache.get(playerName.toLowerCase()));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SKIN_DOWNLOAD_URL + playerName;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    try (InputStream inputStream = response.body()) {
                        NativeImage image = NativeImage.read(inputStream);
                        
                        // Register texture on client thread
                        Identifier textureId = Identifier.of("ctl-tiertagger", "skins/" + playerName.toLowerCase());
                        
                        // Wait for texture registration to complete
                        CompletableFuture<Void> registrationFuture = new CompletableFuture<>();
                        MinecraftClient.getInstance().execute(() -> {
                            try {
                                MinecraftClient.getInstance().getTextureManager()
                                        .registerTexture(textureId, new NativeImageBackedTexture(image));
                                registrationFuture.complete(null);
                            } catch (Exception e) {
                                registrationFuture.completeExceptionally(e);
                            }
                        });
                        
                        registrationFuture.join(); // Wait for registration
                        
                        skinCache.put(playerName.toLowerCase(), textureId);
                        CTLTierTagger.LOGGER.info("Loaded skin for {}: {}", playerName, textureId);
                        
                        return textureId;
                    }
                } else {
                    CTLTierTagger.LOGGER.warn("Failed to load skin for {}: HTTP {}", playerName, response.statusCode());
                    return null;
                }
            } catch (Exception e) {
                CTLTierTagger.LOGGER.error("Error loading skin for {}: {}", playerName, e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Load skin and create a PlayerSkinWidget with correct size (60x144)
     * Uses Minecraft's native skin provider system like working TierTagger
     */
    public static CompletableFuture<PlayerSkinWidget> loadSkinAndCreateWidget(String playerName, MinecraftClient client) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create a proper GameProfile - first try to resolve the real UUID
                com.mojang.authlib.GameProfile profile;
                
                // Try to get the profile from online players first for better accuracy
                if (client.getNetworkHandler() != null) {
                    var onlinePlayer = client.getNetworkHandler().getPlayerList().stream()
                            .filter(entry -> entry.getProfile().getName().equalsIgnoreCase(playerName))
                            .findFirst();
                    
                    if (onlinePlayer.isPresent()) {
                        profile = onlinePlayer.get().getProfile();
                        CTLTierTagger.LOGGER.info("Found online player profile for {}: {}", playerName, profile.getId());
                    } else {
                        // Create profile with pseudo UUID - Minecraft will resolve the skin by name
                        java.util.UUID pseudoUuid = java.util.UUID.nameUUIDFromBytes(
                            ("OfflinePlayer:" + playerName).getBytes(java.nio.charset.StandardCharsets.UTF_8)
                        );
                        profile = new com.mojang.authlib.GameProfile(pseudoUuid, playerName);
                        CTLTierTagger.LOGGER.info("Created offline profile for {} with pseudo UUID: {}", playerName, pseudoUuid);
                    }
                } else {
                    // Create profile with pseudo UUID - Minecraft will resolve the skin by name
                    java.util.UUID pseudoUuid = java.util.UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + playerName).getBytes(java.nio.charset.StandardCharsets.UTF_8)
                    );
                    profile = new com.mojang.authlib.GameProfile(pseudoUuid, playerName);
                    CTLTierTagger.LOGGER.info("Created offline profile for {} with pseudo UUID: {}", playerName, pseudoUuid);
                }
                
                // Get skin textures from Minecraft's skin provider using correct 1.21.1 API
                final var finalProfile = profile;
                var skinSupplier = client.getSkinProvider().getSkinTextures(finalProfile);
                
                // Create PlayerSkinWidget with correct size (60x144 like original TierTagger)
                // Use getEntityModelLoader() for 1.21.1
                PlayerSkinWidget widget = new PlayerSkinWidget(
                    60, 144,  // width, height - same as original TierTagger!
                    client.getEntityModelLoader(),
                    () -> skinSupplier  // Wrap in supplier lambda for 1.21.1
                );
                
                CTLTierTagger.LOGGER.info("Created PlayerSkinWidget for {} with profile: {}", playerName, finalProfile.getId());
                return widget;
            } catch (Exception e) {
                CTLTierTagger.LOGGER.error("Error creating skin widget for {}: {}", playerName, e.getMessage());
                return null;
            }
        });
    }

    /**
     * Load head texture for 2D rendering (search results)
     */
    public static CompletableFuture<Identifier> loadHeadTexture(String playerName) {
        // Check cache first
        if (headCache.containsKey(playerName.toLowerCase())) {
            return CompletableFuture.completedFuture(headCache.get(playerName.toLowerCase()));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = HEAD_URL + playerName;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    try (InputStream inputStream = response.body()) {
                        NativeImage image = NativeImage.read(inputStream);
                        
                        // Register texture on client thread
                        Identifier textureId = Identifier.of("ctl-tiertagger", "heads/" + playerName.toLowerCase());
                        
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().getTextureManager()
                                    .registerTexture(textureId, new NativeImageBackedTexture(image));
                        });
                        
                        headCache.put(playerName.toLowerCase(), textureId);
                        CTLTierTagger.LOGGER.info("Loaded head for {}", playerName);
                        return textureId;
                    }
                } else {
                    CTLTierTagger.LOGGER.warn("Failed to load head for {}: HTTP {}", playerName, response.statusCode());
                    return null;
                }
            } catch (Exception e) {
                CTLTierTagger.LOGGER.error("Error loading head for {}: {}", playerName, e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Clear all cached textures
     */
    public static void clearCache() {
        skinCache.clear();
        headCache.clear();
    }
}
