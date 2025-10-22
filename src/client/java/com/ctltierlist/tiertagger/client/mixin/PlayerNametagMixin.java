package com.ctltierlist.tiertagger.client.mixin;

import com.ctltierlist.tiertagger.api.TierListAPI;
import com.ctltierlist.tiertagger.cache.TierCache;
import com.ctltierlist.tiertagger.config.ModConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerNametagMixin {
    
    @Shadow
    public abstract String getNameForScoreboard();
    
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Text modifyDisplayName(Text original) {
        if (!ModConfig.isEnabled()) {
            return original;
        }
        
        String playerName = this.getNameForScoreboard();
        TierListAPI.PlayerTierData tierData = TierCache.getTierData(playerName);
        
        if (tierData == null) {
            return original;
        }
        
        // Determine which tier to show based on config
        String displayTier;
        String displayGamemode;
        boolean isRetired;
        
        if (ModConfig.shouldShowHighestTier()) {
            // Show highest tier across all gamemodes
            displayTier = tierData.getHighestTier();
            displayGamemode = tierData.getHighestTierGamemode();
            
            if (displayTier.equals("Unranked") || displayGamemode == null) {
                return original;
            }
            
            isRetired = tierData.isRetired(displayGamemode);
        } else {
            // Filter by selected gamemode
            String selectedGamemode = ModConfig.getSelectedGamemode();
            
            if (!tierData.hasTierForGamemode(selectedGamemode)) {
                return original;
            }
            
            displayTier = tierData.getTierForGamemode(selectedGamemode);
            displayGamemode = selectedGamemode;
            isRetired = tierData.isRetired(selectedGamemode);
        }
        
        // Add R prefix for retired tiers (e.g., "RHT3")
        if (isRetired) {
            displayTier = "R" + displayTier;
        }
        
        // Build tier text in TierTagger format: [ICON] TIER | PlayerName
        Text result = Text.empty();
        
        // Add gamemode icon if enabled
        if (ModConfig.shouldShowGamemode()) {
            String icon = getGamemodeIcon(displayGamemode);
            if (!icon.isEmpty()) {
                result = result.copy().append(Text.literal(icon + " "));
            }
        }
        
        // Add colored tier from config (with optional gradient)
        if (ModConfig.isGradientEnabled(displayTier)) {
            // Apply gradient
            int startColor = ModConfig.getTierColor(displayTier);
            int endColor = ModConfig.getGradientColor(displayTier);
            result = result.copy().append(createGradientText(displayTier, startColor, endColor));
        } else {
            // Single color
            int tierColor = ModConfig.getTierColor(displayTier);
            result = result.copy().append(Text.literal(displayTier).styled(s -> s.withColor(tierColor)));
        }
        
        // Add pipe separator
        result = result.copy().append(Text.literal(" | ").styled(s -> s.withColor(0x808080)));
        
        // Add original player name
        result = result.copy().append(original);
        
        return result;
    }
    
    /**
     * Get gamemode icon (using custom font characters)
     * These map to textures defined in assets/minecraft/font/default.json
     * Falls back to text abbreviations if custom fonts don't render in nametags
     */
    private static String getGamemodeIcon(String gamemode) {
        // Try custom font character, fallback to text if needed
        return switch (gamemode.toLowerCase()) {
            case "sword" -> "\uE801"; // sword icon
            case "cpvp", "crystal" -> "\uE800"; // crystal icon
            case "netherite", "nethpot" -> "\uE803"; // netherite icon
            case "pot", "potion" -> "\uE802"; // pot icon
            case "mace", "macepvp" -> "\uE807"; // mace icon
            case "uhc" -> "\uE804"; // uhc icon
            case "axe", "axepvp" -> "\uE805"; // axe icon
            case "smp", "smpkit" -> "\uE806"; // smp icon
            default -> "";
        };
    }
    
    /**
     * Create gradient text by interpolating colors between characters
     */
    private static Text createGradientText(String text, int startColor, int endColor) {
        if (text.length() <= 1) {
            return Text.literal(text).styled(s -> s.withColor(startColor));
        }
        
        Text result = Text.empty();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            // Calculate interpolation factor (0.0 to 1.0)
            float factor = (float) i / (length - 1);
            
            // Interpolate RGB components
            int r1 = (startColor >> 16) & 0xFF;
            int g1 = (startColor >> 8) & 0xFF;
            int b1 = startColor & 0xFF;
            
            int r2 = (endColor >> 16) & 0xFF;
            int g2 = (endColor >> 8) & 0xFF;
            int b2 = endColor & 0xFF;
            
            int r = (int) (r1 + (r2 - r1) * factor);
            int g = (int) (g1 + (g2 - g1) * factor);
            int b = (int) (b1 + (b2 - b1) * factor);
            
            int interpolatedColor = (r << 16) | (g << 8) | b;
            
            // Add character with interpolated color
            result = result.copy().append(Text.literal(String.valueOf(text.charAt(i))).styled(s -> s.withColor(interpolatedColor)));
        }
        
        return result;
    }

}
