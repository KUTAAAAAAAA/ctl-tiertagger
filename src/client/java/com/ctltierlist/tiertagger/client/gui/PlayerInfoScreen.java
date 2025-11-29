package com.ctltierlist.tiertagger.client.gui;

import com.ctltierlist.tiertagger.api.TierListAPI;
import com.ctltierlist.tiertagger.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.*;

public class PlayerInfoScreen extends Screen {
    private final Screen parent;
    private final TierListAPI.PlayerTierData playerData;
    private final List<TierEntry> tierEntries = new ArrayList<>();
    private final PlayerSkinWidget skinWidget;

    public PlayerInfoScreen(Screen parent, TierListAPI.PlayerTierData playerData, PlayerSkinWidget skinWidget) {
        super(Text.literal("Player Info"));
        this.parent = parent;
        this.playerData = playerData;
        this.skinWidget = skinWidget;
        
        // Build and sort tier entries list
        for (Map.Entry<String, TierListAPI.TierInfo> entry : playerData.getAllTiers().entrySet()) {
            String gamemode = entry.getKey();
            String tier = entry.getValue().tier;
            boolean retired = entry.getValue().retired;
            if (!tier.equals("Unranked")) {
                tierEntries.add(new TierEntry(gamemode, tier, retired));
            }
        }
        
        // Sort by retired status first (active first), then by tier value
        tierEntries.sort(Comparator.comparing((TierEntry e) -> e.retired)
                                   .thenComparingInt(e -> getTierValue(e.tier)));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Done button at bottom
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.DONE,
            button -> MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(centerX - 100, this.height - 27, 200, 20).build());

        // Calculate layout
        int rankingHeight = tierEntries.size() * 11;
        int infoHeight = 56;
        int startY = (this.height - infoHeight - rankingHeight) / 2;
        int rankingY = startY + infoHeight;
        
        // Position and add the skin widget (60x144) - same positioning as original TierTagger
        if (this.skinWidget != null) {
            this.skinWidget.setPosition(this.width / 2 - 65, (this.height - 144) / 2);
            this.addDrawableChild(this.skinWidget);
        }

        // Add ranking text widgets
        for (TierEntry entry : tierEntries) {
            Text tierText = formatTier(entry);
            TextWidget textWidget = new TextWidget(tierText, this.textRenderer);
            textWidget.setX(centerX + 5);
            textWidget.setY(rankingY);
            
            String statusText = entry.retired ? "§cRetired" : "§aActive";
            Text tooltipText = Text.literal("Gamemode: " + entry.gamemode + "\n" + statusText).formatted(Formatting.GRAY);
            textWidget.setTooltip(Tooltip.of(tooltipText));
            
            this.addDrawableChild(textWidget);
            rankingY += 11;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;

        // Draw title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            playerData.playerName + "'s profile",
            centerX,
            20,
            0xFFFFFFFF
        );

        // Calculate positions
        int rankingHeight = tierEntries.size() * 11;
        int infoHeight = 56;
        int startY = (this.height - infoHeight - rankingHeight) / 2;
        


        // Draw player info on the right side
        context.drawTextWithShadow(this.textRenderer, getRegionText(), centerX + 5, startY, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, getPointsText(), centerX + 5, startY + 15, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, getRankText(), centerX + 5, startY + 30, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Rankings:", centerX + 5, startY + 45, 0xFFFFFFFF);
    }

    private Text formatTier(TierEntry entry) {
        // Format tier with R prefix if retired (e.g., "RHT3" or "RLT3")
        String tierString = entry.retired ? "R" + entry.tier : entry.tier;
        int primaryColor = ModConfig.getTierColor(tierString);
        
        // Get colored gamemode name with icon
        Text gamemodeText = getGamemodeStyledText(entry.gamemode);
        Text tierText = Text.literal(tierString).styled(s -> s.withColor(primaryColor));
        
        return Text.empty()
            .append(gamemodeText)
            .append(Text.literal(": ").formatted(Formatting.GRAY))
            .append(tierText);
    }
    
    /**
     * Get styled gamemode text with icon and color (like TierTagger)
     */
    private Text getGamemodeStyledText(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "axe", "axepvp" -> Text.empty()
                .append(Text.literal("⚔ ").styled(s -> s.withColor(0x00FF00)))
                .append(Text.literal("Axe").styled(s -> s.withColor(0x00FF00)));
            case "mace", "macepvp" -> Text.empty()
                .append(Text.literal("🔨 ").styled(s -> s.withColor(0xAAAAAA)))
                .append(Text.literal("Mace").styled(s -> s.withColor(0xAAAAAA)));
            case "netherite", "nethpot" -> Text.empty()
                .append(Text.literal("⬛ ").styled(s -> s.withColor(0x7d4a40)))
                .append(Text.literal("Netherite").styled(s -> s.withColor(0x7d4a40)));
            case "pot", "potion" -> Text.empty()
                .append(Text.literal("🧪 ").styled(s -> s.withColor(0xFF0000)))
                .append(Text.literal("Potion").styled(s -> s.withColor(0xFF0000)));
            case "smp", "smpkit" -> Text.empty()
                .append(Text.literal("🏠 ").styled(s -> s.withColor(0xeccb45)))
                .append(Text.literal("SMP").styled(s -> s.withColor(0xeccb45)));
            case "sword" -> Text.empty()
                .append(Text.literal("⚔ ").styled(s -> s.withColor(0xa4fdf0)))
                .append(Text.literal("Sword").styled(s -> s.withColor(0xa4fdf0)));
            case "uhc" -> Text.empty()
                .append(Text.literal("❤ ").styled(s -> s.withColor(0xFF0000)))
                .append(Text.literal("UHC").styled(s -> s.withColor(0xFF0000)));
            case "cpvp", "crystal", "vanilla" -> Text.empty()
                .append(Text.literal("💎 ").styled(s -> s.withColor(0xFF55FF)))
                .append(Text.literal("Crystal").styled(s -> s.withColor(0xFF55FF)));
            case "diasmp" -> Text.empty()
                .append(Text.literal("💎 ").styled(s -> s.withColor(0x55FFFF)))
                .append(Text.literal("DiaSMP").styled(s -> s.withColor(0x55FFFF)));
            default -> Text.literal(gamemode).formatted(Formatting.WHITE);
        };
    }

    private Text getRegionText() {
        int regionColor = getRegionColor(playerData.region);
        return Text.empty()
            .append(Text.literal("Region: "))
            .append(Text.literal(playerData.region).styled(s -> s.withColor(regionColor)));
    }

    private Text getPointsText() {
        int pointColor = getTitleColor(playerData.title);
        return Text.empty()
            .append(Text.literal("Points: "))
            .append(Text.literal(playerData.totalPoints + " ").styled(s -> s.withColor(pointColor)))
            .append(Text.literal("(" + playerData.title + ")").styled(s -> s.withColor(pointColor | 0x404040)));
    }

    private Text getRankText() {
        int rankColor = switch (playerData.overallRank) {
            case 1 -> 0xe5ba43;
            case 2 -> 0x808c9c;
            case 3 -> 0xb56326;
            default -> 0x1e2634;
        };
        
        return Text.empty()
            .append(Text.literal("Global rank: "))
            .append(Text.literal("#" + playerData.overallRank).styled(s -> s.withColor(rankColor)));
    }

    private int getRegionColor(String region) {
        return switch (region.toUpperCase()) {
            case "NA" -> 0xff6a6e;
            case "EU" -> 0x6aff6e;
            case "SA" -> 0xff9900;
            case "AS", "AS/AU" -> 0xc27ba0;
            case "AU" -> 0xf6b26b;
            case "ME" -> 0xffd966;
            case "AF" -> 0x674ea7;
            default -> 0xFFFFFF;
        };
    }

    private int getTitleColor(String title) {
        return switch (title) {
            case "Combat Grandmaster" -> 0xE6C622;
            case "Combat Master" -> 0xFBB03B;
            case "Combat Ace" -> 0xCD285C;
            case "Combat Specialist" -> 0xAD78D8;
            case "Combat Cadet" -> 0x9291D9;
            case "Combat Novice" -> 0x9291D9;
            case "Rookie" -> 0x6C7178;
            default -> 0xFFFFFF;
        };
    }

    private int getTierValue(String tier) {
        if (tier == null || tier.equals("Unranked")) return 999;
        
        // Remove R prefix for retired tiers
        String cleanTier = tier.startsWith("R") ? tier.substring(1) : tier;
        
        if (cleanTier.startsWith("HT")) {
            int num = Integer.parseInt(cleanTier.substring(2));
            return (num * 2) - 1;
        } else if (cleanTier.startsWith("LT")) {
            int num = Integer.parseInt(cleanTier.substring(2));
            return num * 2;
        }
        return 999;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private static class TierEntry {
        final String gamemode;
        final String tier;
        final boolean retired;

        TierEntry(String gamemode, String tier, boolean retired) {
            this.gamemode = gamemode;
            this.tier = tier;
            this.retired = retired;
        }
    }
}
