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
    private static final Map<String, java.util.UUID> uuidCache = new ConcurrentHashMap<>();
    
    /**
     * Fetch real UUID from Mojang API
     */
    private static java.util.UUID fetchMojangUUID(String playerName) {
        // Check cache first
        if (uuidCache.containsKey(playerName.toLowerCase())) {
            return uuidCache.get(playerName.toLowerCase());
        }
        
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse JSON: {"id":"uuid-without-dashes","name":"PlayerName"}
                String json = response.body();
                String id = json.split("\"id\":\"")[1].split("\"")[0];
                // Insert dashes into UUID
                String uuidStr = id.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
                );
                java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                uuidCache.put(playerName.toLowerCase(), uuid);
                CTLTierTagger.LOGGER.info("Fetched Mojang UUID for {}: {}", playerName, uuid);
                return uuid;
            }
        } catch (Exception e) {
            CTLTierTagger.LOGGER.warn("Failed to fetch Mojang UUID for {}: {}", playerName, e.getMessage());
        }
        return null;
    }
    
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
                        // Try Mojang API first for real UUID, fallback to pseudo UUID
                        java.util.UUID realUuid = fetchMojangUUID(playerName);
                        if (realUuid != null) {
                            profile = new com.mojang.authlib.GameProfile(realUuid, playerName);
                            CTLTierTagger.LOGGER.info("Using Mojang UUID for {}: {}", playerName, realUuid);
                        } else {
                            java.util.UUID pseudoUuid = java.util.UUID.nameUUIDFromBytes(
                                ("OfflinePlayer:" + playerName).getBytes(java.nio.charset.StandardCharsets.UTF_8)
                            );
                            profile = new com.mojang.authlib.GameProfile(pseudoUuid, playerName);
                            CTLTierTagger.LOGGER.info("Using pseudo UUID for {}: {}", playerName, pseudoUuid);
                        }
                    }
                } else {
                    // Try Mojang API first for real UUID, fallback to pseudo UUID
                    java.util.UUID realUuid = fetchMojangUUID(playerName);
                    if (realUuid != null) {
                        profile = new com.mojang.authlib.GameProfile(realUuid, playerName);
                        CTLTierTagger.LOGGER.info("Using Mojang UUID for {}: {}", playerName, realUuid);
                    } else {
                        java.util.UUID pseudoUuid = java.util.UUID.nameUUIDFromBytes(
                            ("OfflinePlayer:" + playerName).getBytes(java.nio.charset.StandardCharsets.UTF_8)
                        );
                        profile = new com.mojang.authlib.GameProfile(pseudoUuid, playerName);
                        CTLTierTagger.LOGGER.info("Using pseudo UUID for {}: {}", playerName, pseudoUuid);
                    }
                }
                
                // Fetch skin textures from Mojang (async) - this actually fetches, not just returns cached
                final var finalProfile = profile;
                var skinTexturesFuture = client.getSkinProvider().fetchSkinTextures(finalProfile);
                var skinTextures = skinTexturesFuture.join(); // Wait for fetch to complete
                
                // Create PlayerSkinWidget with correct size (60x144 like original TierTagger)
                PlayerSkinWidget widget = new PlayerSkinWidget(
                    60, 144,
                    client.getEntityModelLoader(),
                    () -> skinTextures != null ? skinTextures : client.getSkinProvider().getSkinTextures(finalProfile)
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
