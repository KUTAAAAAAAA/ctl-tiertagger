package com.ctltierlist.tiertagger.api;

import com.ctltierlist.tiertagger.CTLTierTagger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TierListAPI {
    private static final String API_BASE_URL = "https://ctltierlist-api-b2s8.vercel.app";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson GSON = new Gson();

    /**
     * Fetch player profile data from the API
     * @param playerName The player's in-game name
     * @return CompletableFuture with PlayerTierData
     */
    public static CompletableFuture<PlayerTierData> fetchPlayerTier(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = API_BASE_URL + "/api/search_profile/" + playerName;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    return parsePlayerData(json, playerName);
                } else {
                    CTLTierTagger.LOGGER.warn("Failed to fetch tier for {}: HTTP {}", playerName, response.statusCode());
                    return null;
                }
            } catch (Exception e) {
                CTLTierTagger.LOGGER.error("Error fetching tier for {}: {}", playerName, e.getMessage());
                return null;
            }
        });
    }

    /**
     * Search for players by name prefix
     * @param query The search query (player name prefix)
     * @return CompletableFuture with list of PlayerSearchResult
     */
    public static CompletableFuture<List<PlayerSearchResult>> searchPlayers(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = API_BASE_URL + "/api/search_profile/" + query.replace(" ", "%20");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    List<PlayerSearchResult> players = new ArrayList<>();
                    
                    try {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        
                        if (json.has("profile")) {
                            JsonObject profile = json.getAsJsonObject("profile");
                            String playerName = profile.has("name") ? profile.get("name").getAsString() : query;
                            String uuid = profile.has("uuid") ? profile.get("uuid").getAsString() : "";
                            
                            // Get highest tier from ranks
                            String tier = "Unranked";
                            if (json.has("ranks")) {
                                JsonObject ranks = json.getAsJsonObject("ranks");
                                int bestTierValue = 999;
                                
                                for (String gamemode : ranks.keySet()) {
                                    JsonObject rankData = ranks.getAsJsonObject(gamemode);
                                    if (rankData.has("rank")) {
                                        String rank = rankData.get("rank").getAsString();
                                        int tierValue = getTierValue(rank);
                                        if (tierValue < bestTierValue) {
                                            bestTierValue = tierValue;
                                            tier = rank;
                                        }
                                    }
                                }
                            }
                            
                            players.add(new PlayerSearchResult(playerName, uuid, tier));
                        }
                    } catch (Exception parseError) {
                        CTLTierTagger.LOGGER.error("Error parsing search result: {}", parseError.getMessage());
                    }
                    
                    return players;
                } else {
                    CTLTierTagger.LOGGER.warn("Failed to search player {}: HTTP {}", query, response.statusCode());
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                CTLTierTagger.LOGGER.error("Error searching players: {}", e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    /**
     * Parse player data from JSON response
     */
    private static PlayerTierData parsePlayerData(JsonObject json, String playerName) {
        try {
            JsonObject profile = json.has("profile") ? json.getAsJsonObject("profile") : null;
            JsonObject ranks = json.has("ranks") ? json.getAsJsonObject("ranks") : null;

            String region = profile != null && profile.has("region") ? profile.get("region").getAsString() : "Unknown";
            String uuid = profile != null && profile.has("uuid") ? profile.get("uuid").getAsString() : "";
            String avatar = profile != null && profile.has("avatar") ? profile.get("avatar").getAsString() : "";
            
            int totalPoints = json.has("totalPoints") ? json.get("totalPoints").getAsInt() : 0;
            String title = json.has("title") ? json.get("title").getAsString() : "Unranked";
            int overallRank = json.has("rank") ? json.get("rank").getAsInt() : 0;
            
            PlayerTierData tierData = new PlayerTierData(playerName, region, uuid, avatar, totalPoints, title, overallRank);

            // Store tier for each gamemode
            if (ranks != null) {
                for (String gamemode : ranks.keySet()) {
                    JsonObject rankData = ranks.getAsJsonObject(gamemode);
                    if (rankData.has("rank")) {
                        String rank = rankData.get("rank").getAsString();
                        boolean retired = rankData.has("retired") && rankData.get("retired").getAsBoolean();
                        tierData.setTierForGamemode(normalizeGamemode(gamemode), rank, retired);
                    }
                }
            }

            return tierData;
        } catch (Exception e) {
            CTLTierTagger.LOGGER.error("Error parsing player data: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Normalize gamemode names to match config names
     */
    private static String normalizeGamemode(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "sword", "swd" -> "Sword";
            case "crystal", "cpvp" -> "Crystal";
            case "netherite", "nethpot" -> "Netherite";
            case "pot", "potion" -> "Potion";
            case "mace", "macepvp" -> "Mace";
            case "uhc" -> "UHC";
            case "axe", "axepvp" -> "Axe";
            case "smp", "smpkit" -> "SMP";
            default -> gamemode;
        };
    }

    /**
     * Convert tier string to numeric value for comparison (lower is better)
     */
    private static int getTierValue(String tier) {
        if (tier == null || tier.equals("Unranked")) return 999;
        
        // Extract tier number and type (HT/LT)
        if (tier.startsWith("HT")) {
            int num = Integer.parseInt(tier.substring(2));
            return (num * 2) - 1; // HT1=1, HT2=3, HT3=5, etc.
        } else if (tier.startsWith("LT")) {
            int num = Integer.parseInt(tier.substring(2));
            return num * 2; // LT1=2, LT2=4, LT3=6, etc.
        }
        return 999;
    }

    /**
     * Data class to hold player tier information
     */
    public static class PlayerTierData {
        public final String playerName;
        public final String region;
        public final String uuid;
        public final String avatarUrl;
        public final int totalPoints;
        public final String title;
        public final int overallRank;
        public final long fetchTime;
        private final Map<String, TierInfo> gamemodeTiers;

        public PlayerTierData(String playerName, String region, String uuid, String avatarUrl, 
                            int totalPoints, String title, int overallRank) {
            this.playerName = playerName;
            this.region = region;
            this.uuid = uuid;
            this.avatarUrl = avatarUrl;
            this.totalPoints = totalPoints;
            this.title = title;
            this.overallRank = overallRank;
            this.fetchTime = System.currentTimeMillis();
            this.gamemodeTiers = new ConcurrentHashMap<>();
        }

        public void setTierForGamemode(String gamemode, String tier, boolean retired) {
            gamemodeTiers.put(gamemode, new TierInfo(tier, retired));
        }

        public String getTierForGamemode(String gamemode) {
            TierInfo info = gamemodeTiers.get(gamemode);
            return info != null ? info.tier : "Unranked";
        }

        public boolean isRetired(String gamemode) {
            TierInfo info = gamemodeTiers.get(gamemode);
            return info != null && info.retired;
        }

        public boolean hasTierForGamemode(String gamemode) {
            TierInfo info = gamemodeTiers.get(gamemode);
            return info != null && !info.tier.equals("Unranked");
        }
        
        public Map<String, TierInfo> getAllTiers() {
            return gamemodeTiers;
        }

        public String getHighestTier() {
            String highestTier = "Unranked";
            int highestTierValue = 999;
            
            for (Map.Entry<String, TierInfo> entry : gamemodeTiers.entrySet()) {
                String tier = entry.getValue().tier;
                int tierValue = TierListAPI.getTierValue(tier);
                if (tierValue < highestTierValue) {
                    highestTierValue = tierValue;
                    highestTier = tier;
                }
            }
            
            return highestTier;
        }

        public String getHighestTierGamemode() {
            String highestGamemode = null;
            int highestTierValue = 999;
            
            for (Map.Entry<String, TierInfo> entry : gamemodeTiers.entrySet()) {
                String tier = entry.getValue().tier;
                int tierValue = TierListAPI.getTierValue(tier);
                if (tierValue < highestTierValue) {
                    highestTierValue = tierValue;
                    highestGamemode = entry.getKey();
                }
            }
            
            return highestGamemode;
        }

        public boolean isExpired(long cacheTimeMs) {
            return System.currentTimeMillis() - fetchTime > cacheTimeMs;
        }

        @Override
        public String toString() {
            return String.format("Player: %s, Region: %s, Points: %d, Rank: #%d, Tiers: %s", 
                playerName, region, totalPoints, overallRank, gamemodeTiers);
        }
    }
    
    /**
     * Inner class to hold tier info with retired status
     */
    public static class TierInfo {
        public final String tier;
        public final boolean retired;
        
        public TierInfo(String tier, boolean retired) {
            this.tier = tier;
            this.retired = retired;
        }
        
        @Override
        public String toString() {
            return tier + (retired ? " (Retired)" : "");
        }
    }

    /**
     * Simple search result class for player search
     */
    public static class PlayerSearchResult {
        public final String playerName;
        public final String uuid;
        public final String tier;
        
        public PlayerSearchResult(String playerName, String uuid, String tier) {
            this.playerName = playerName;
            this.uuid = uuid;
            this.tier = tier;
        }
    }
}
