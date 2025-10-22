package com.ctltierlist.tiertagger.client.gui;

import com.ctltierlist.tiertagger.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private int centerX;
    private int spacing = 25;
    private ConfigCategory currentCategory = ConfigCategory.GENERAL;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int contentHeight = 0;
    
    private enum ConfigCategory {
        GENERAL("General"),
        COLOURS("Colours");
        
        private final String displayName;
        
        ConfigCategory(String displayName) {
            this.displayName = displayName;
        }
    }

    public ConfigScreen(Screen parent) {
        super(Text.literal("CTL TierTagger Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        centerX = this.width / 2;
        scrollOffset = 0;
        
        // Category tabs at the top
        int tabY = 45;
        int tabWidth = 100;
        int tabSpacing = 5;
        int startTabX = centerX - (ConfigCategory.values().length * (tabWidth + tabSpacing)) / 2;
        
        for (int i = 0; i < ConfigCategory.values().length; i++) {
            ConfigCategory category = ConfigCategory.values()[i];
            int tabX = startTabX + i * (tabWidth + tabSpacing);
            boolean isSelected = category == this.currentCategory;
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal(isSelected ? "§l" + category.displayName : category.displayName),
                button -> {
                    this.currentCategory = category;
                    this.clearAndInit();
                }
            ).dimensions(tabX, tabY, tabWidth, 20).build());
        }
        
        // Show widgets based on current category
        if (this.currentCategory == ConfigCategory.GENERAL) {
            this.initGeneralCategory();
        } else if (this.currentCategory == ConfigCategory.COLOURS) {
            this.initColoursCategory();
        }
        
        // Done button (always visible)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(parent);
                }
            }
        ).dimensions(centerX - 100, this.height - 30, 200, 20).build());
    }
    
    private void initGeneralCategory() {
        int startY = 80;

        // Toggle mod enabled/disabled
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Mod Status: " + (ModConfig.isEnabled() ? "§aEnabled" : "§cDisabled")),
            button -> {
                ModConfig.setEnabled(!ModConfig.isEnabled());
                button.setMessage(Text.literal("Mod Status: " + (ModConfig.isEnabled() ? "§aEnabled" : "§cDisabled")));
            }
        ).dimensions(centerX - 100, startY, 200, 20)
         .tooltip(Tooltip.of(Text.literal("Toggle tier display on/off")))
         .build());

        // Toggle gamemode display
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Gamemode Icons: " + (ModConfig.shouldShowGamemode() ? "§aShown" : "§cHidden")),
            button -> {
                ModConfig.setShowGamemode(!ModConfig.shouldShowGamemode());
                button.setMessage(Text.literal("Gamemode Icons: " + (ModConfig.shouldShowGamemode() ? "§aShown" : "§cHidden")));
            }
        ).dimensions(centerX - 100, startY + spacing, 200, 20)
         .tooltip(Tooltip.of(Text.literal("Toggle gamemode icons before tier display")))
         .build());

        // Gamemode selector button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Gamemode: " + ModConfig.getSelectedGamemode()),
            button -> {
                ModConfig.cycleGamemode();
                button.setMessage(Text.literal("Gamemode: " + ModConfig.getSelectedGamemode()));
            }
        ).dimensions(centerX - 100, startY + spacing * 2, 200, 20)
         .tooltip(Tooltip.of(Text.literal("Select your current gamemode (Press G to cycle in-game)")))
         .build());

        // Toggle highest tier mode
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(ModConfig.shouldShowHighestTier() ? "Show Highest Tier: ON" : "Show Highest Tier: OFF"),
            button -> {
                ModConfig.setShowHighestTier(!ModConfig.shouldShowHighestTier());
                button.setMessage(Text.literal(ModConfig.shouldShowHighestTier() ? "Show Highest Tier: ON" : "Show Highest Tier: OFF"));
            }
        ).dimensions(centerX - 100, startY + spacing * 3, 200, 20)
         .tooltip(Tooltip.of(Text.literal("ON: Show highest tier across all gamemodes | OFF: Filter by selected gamemode")))
         .build());

        // Toggle debug mode
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(ModConfig.isDebugMode() ? "Disable Debug" : "Enable Debug"),
            button -> {
                ModConfig.setDebugMode(!ModConfig.isDebugMode());
                button.setMessage(Text.literal(ModConfig.isDebugMode() ? "Disable Debug" : "Enable Debug"));
            }
        ).dimensions(centerX - 100, startY + spacing * 4, 200, 20)
         .tooltip(Tooltip.of(Text.literal("Enable debug logging")))
         .build());
        
        // Player search button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§bSearch Player"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new PlayerSearchScreen(this));
                }
            }
        ).dimensions(centerX - 100, startY + spacing * 5, 200, 20)
         .tooltip(Tooltip.of(Text.literal("Search for a player's tier information")))
         .build());
        
        // Reset to defaults button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§cReset All Settings"),
            button -> {
                ModConfig.resetToDefaults();
                this.clearAndInit();
            }
        ).dimensions(centerX - 100, startY + spacing * 6, 200, 20)
         .tooltip(Tooltip.of(Text.literal("Reset all settings to default values")))
         .build());
    }
    
    private void initColoursCategory() {
        int startY = 75;
        int rowHeight = 26;
        int row = 0;
        
        int availableHeight = this.height - 120; // Space between tabs and buttons
        int totalContentHeight = rowHeight * 10; // 10 tier rows
        maxScroll = Math.max(0, totalContentHeight - availableHeight);
        
        // HT1
        addTierColorRow("HT1", centerX - 180, startY + rowHeight * row++ - scrollOffset, 
            ModConfig.getColorHT1(), ModConfig::setColorHT1,
            ModConfig.getGradientColorHT1(), ModConfig::setGradientColorHT1,
            ModConfig.isGradientEnabledHT1(), ModConfig::setGradientEnabledHT1);
        
        // LT1
        addTierColorRow("LT1", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorLT1(), ModConfig::setColorLT1,
            ModConfig.getGradientColorLT1(), ModConfig::setGradientColorLT1,
            ModConfig.isGradientEnabledLT1(), ModConfig::setGradientEnabledLT1);
        
        // HT2
        addTierColorRow("HT2", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorHT2(), ModConfig::setColorHT2,
            ModConfig.getGradientColorHT2(), ModConfig::setGradientColorHT2,
            ModConfig.isGradientEnabledHT2(), ModConfig::setGradientEnabledHT2);
        
        // LT2
        addTierColorRow("LT2", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorLT2(), ModConfig::setColorLT2,
            ModConfig.getGradientColorLT2(), ModConfig::setGradientColorLT2,
            ModConfig.isGradientEnabledLT2(), ModConfig::setGradientEnabledLT2);
        
        // HT3
        addTierColorRow("HT3", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorHT3(), ModConfig::setColorHT3,
            ModConfig.getGradientColorHT3(), ModConfig::setGradientColorHT3,
            ModConfig.isGradientEnabledHT3(), ModConfig::setGradientEnabledHT3);
        
        // LT3
        addTierColorRow("LT3", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorLT3(), ModConfig::setColorLT3,
            ModConfig.getGradientColorLT3(), ModConfig::setGradientColorLT3,
            ModConfig.isGradientEnabledLT3(), ModConfig::setGradientEnabledLT3);
        
        // HT4
        addTierColorRow("HT4", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorHT4(), ModConfig::setColorHT4,
            ModConfig.getGradientColorHT4(), ModConfig::setGradientColorHT4,
            ModConfig.isGradientEnabledHT4(), ModConfig::setGradientEnabledHT4);
        
        // LT4
        addTierColorRow("LT4", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorLT4(), ModConfig::setColorLT4,
            ModConfig.getGradientColorLT4(), ModConfig::setGradientColorLT4,
            ModConfig.isGradientEnabledLT4(), ModConfig::setGradientEnabledLT4);
        
        // HT5
        addTierColorRow("HT5", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorHT5(), ModConfig::setColorHT5,
            ModConfig.getGradientColorHT5(), ModConfig::setGradientColorHT5,
            ModConfig.isGradientEnabledHT5(), ModConfig::setGradientEnabledHT5);
        
        // LT5
        addTierColorRow("LT5", centerX - 180, startY + rowHeight * row++ - scrollOffset,
            ModConfig.getColorLT5(), ModConfig::setColorLT5,
            ModConfig.getGradientColorLT5(), ModConfig::setGradientColorLT5,
            ModConfig.isGradientEnabledLT5(), ModConfig::setGradientEnabledLT5);
        
        // Reset colors button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§cReset All Colors"),
            button -> {
                ModConfig.resetColorsToDefaults();
                this.clearAndInit();
            }
        ).dimensions(centerX - 90, this.height - 55, 180, 22)
         .tooltip(Tooltip.of(Text.literal("Reset all tier colors and gradients to default")))
         .build());
    }
    
    private void addTierColorRow(String tierName, int x, int y, 
                                  int primaryColor, java.util.function.Consumer<Integer> onPrimaryChange,
                                  int gradientColor, java.util.function.Consumer<Integer> onGradientChange,
                                  boolean gradientEnabled, java.util.function.Consumer<Boolean> onGradientToggle) {
        // Check if this row is visible in the viewport
        int topBound = 70;
        int bottomBound = this.height - 60;
        boolean isVisible = y >= topBound && y + 25 <= bottomBound;
        
        if (!isVisible) return; // Skip rendering if out of viewport
        
        // Tier label
        this.addDrawable((context, mouseX, mouseY, delta) -> {
            context.drawText(this.textRenderer, Text.literal(tierName), x, y + 5, Colors.WHITE, false);
        });
        
        // Primary color label
        this.addDrawable((context, mouseX, mouseY, delta) -> {
            context.drawText(this.textRenderer, Text.literal("Color:"), x + 40, y + 5, 0xAAAAAA, false);
        });
        
        // Primary color picker
        ColorPickerWidget primaryPicker = new ColorPickerWidget(x + 80, y + 2, 32, 20, primaryColor, onPrimaryChange);
        this.addDrawableChild(primaryPicker);
        
        // Gradient toggle button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(gradientEnabled ? "§aGrad" : "§7Grad"),
            button -> {
                onGradientToggle.accept(!gradientEnabled);
                this.clearAndInit();
            }
        ).dimensions(x + 120, y + 2, 45, 20)
         .tooltip(Tooltip.of(Text.literal(gradientEnabled ? "Gradient enabled" : "Gradient disabled")))
         .build());
        
        // Gradient color picker (only if enabled)
        if (gradientEnabled) {
            this.addDrawable((context, mouseX, mouseY, delta) -> {
                context.drawText(this.textRenderer, Text.literal("→"), x + 172, y + 5, 0x888888, false);
            });
            ColorPickerWidget gradientPicker = new ColorPickerWidget(x + 190, y + 2, 32, 20, gradientColor, onGradientChange);
            this.addDrawableChild(gradientPicker);
        }
    }
    
    private void addColorPicker(String label, int x, int y, int initialColor, java.util.function.Consumer<Integer> onColorChange) {
        // Draw label
        this.addDrawable((context, mouseX, mouseY, delta) -> {
            context.drawText(this.textRenderer, Text.literal(label), x, y + 5, Colors.WHITE, false);
        });
        
        // Add color picker widget
        ColorPickerWidget picker = new ColorPickerWidget(x + 150, y, 40, 20, initialColor, onColorChange);
        this.addDrawableChild(picker);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Draw title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            centerX,
            20,
            Colors.WHITE
        );
        
        // Draw info text based on category
        if (this.currentCategory == ConfigCategory.GENERAL) {
            int infoY = this.height - 70;
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§7Cache Time: §f" + ModConfig.getCacheTime() / 60000 + " minutes"),
                centerX,
                infoY,
                Colors.WHITE
            );
            
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§7Keybind: §fPress G to cycle gamemode"),
                centerX,
                infoY + 12,
                Colors.WHITE
            );
        } else if (this.currentCategory == ConfigCategory.COLOURS) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§7Click color boxes to customize • Toggle Grad to enable gradients"),
                centerX,
                this.height - 80,
                0xAAAAAA
            );
            
            // Draw scroll indicators
            if (maxScroll > 0) {
                if (scrollOffset > 0) {
                    context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        Text.literal("§7▲ Scroll Up"),
                        centerX,
                        68,
                        0xFFFFFF
                    );
                }
                if (scrollOffset < maxScroll) {
                    context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        Text.literal("§7▼ Scroll Down"),
                        centerX,
                        this.height - 95,
                        0xFFFFFF
                    );
                }
            }
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.currentCategory == ConfigCategory.COLOURS && maxScroll > 0) {
            int scrollAmount = (int) (verticalAmount * 20);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollAmount));
            this.clearAndInit();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
