package com.ctltierlist.tiertagger.client.gui;

import com.ctltierlist.tiertagger.api.TierListAPI;
import com.ctltierlist.tiertagger.client.util.SkinLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class PlayerSearchScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget searchField;
    private ButtonWidget searchButton;
    private boolean isSearching = false;
    private String errorMessage = null;

    public PlayerSearchScreen(Screen parent) {
        super(Text.literal("Search Players"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Centered search field
        this.searchField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150,
            centerY - 30,
            300,
            20,
            Text.literal("Search")
        );
        this.searchField.setPlaceholder(Text.literal("Enter player name..."));
        this.searchField.setMaxLength(16);
        this.addSelectableChild(this.searchField);
        this.setInitialFocus(this.searchField);
        
        // Search button
        this.searchButton = ButtonWidget.builder(
            Text.literal("Search"),
            button -> performSearch()
        ).dimensions(centerX - 75, centerY + 5, 150, 20).build();
        this.addDrawableChild(this.searchButton);
    }

    private void performSearch() {
        String query = this.searchField.getText().trim();
        
        if (query.isEmpty()) {
            this.errorMessage = "§cPlease enter a player name";
            return;
        }

        if (this.isSearching) {
            return;
        }

        this.isSearching = true;
        this.errorMessage = null;
        this.searchButton.active = false;

        // Directly fetch player data and open profile
        openPlayerProfile(query);
    }

    private void openPlayerProfile(String playerName) {
        if (this.client == null) return;

        // Load skin and player data
        CompletableFuture<PlayerSkinWidget> skinWidgetFuture = SkinLoader.loadSkinAndCreateWidget(
            playerName,
            this.client
        );
        CompletableFuture<TierListAPI.PlayerTierData> dataFuture = TierListAPI.fetchPlayerTier(playerName);

        // Wait for both to complete
        CompletableFuture.allOf(dataFuture, skinWidgetFuture).thenRun(() -> {
            TierListAPI.PlayerTierData data = dataFuture.join();
            PlayerSkinWidget skinWidget = skinWidgetFuture.join();

            if (this.client != null) {
                this.client.execute(() -> {
                    this.isSearching = false;
                    this.searchButton.active = true;
                    
                    if (data != null && skinWidget != null) {
                        this.client.setScreen(new PlayerInfoScreen(this, data, skinWidget));
                    } else {
                        this.errorMessage = "§cPlayer not found: " + playerName;
                    }
                });
            }
        }).exceptionally(throwable -> {
            if (this.client != null) {
                this.client.execute(() -> {
                    this.isSearching = false;
                    this.searchButton.active = true;
                    this.errorMessage = "§cFailed to load player data";
                });
            }
            return null;
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Draw semi-transparent background
        context.fillGradient(
            centerX - 160, centerY - 60,
            centerX + 160, centerY + 50,
            0xCC000000, 0xCC000000
        );

        // Draw title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§bSearch Player"),
            centerX,
            centerY - 50,
            0xFFFFFF
        );

        // Draw search field
        this.searchField.render(context, mouseX, mouseY, delta);

        // Draw status messages
        if (this.isSearching) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§7Searching..."),
                centerX,
                centerY + 35,
                0xFFFFFF
            );
        } else if (this.errorMessage != null) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(this.errorMessage),
                centerX,
                centerY + 35,
                0xFFFFFF
            );
        } else {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§7Press ESC to close"),
                centerX,
                centerY + 35,
                0x888888
            );
        }
    }



    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            this.close();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) { // ENTER or NUMPAD_ENTER key
            performSearch();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String text = this.searchField.getText();
        this.init(client, width, height);
        this.searchField.setText(text);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
