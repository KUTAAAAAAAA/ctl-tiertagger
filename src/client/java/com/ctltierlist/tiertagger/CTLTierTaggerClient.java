package com.ctltierlist.tiertagger;

import com.ctltierlist.tiertagger.client.gui.PlayerSearchScreen;
import com.ctltierlist.tiertagger.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CTLTierTaggerClient implements ClientModInitializer {
    private static KeyBinding gamemodeKeybind;
    private static KeyBinding searchKeybind;
    
    @Override
    public void onInitializeClient() {
        CTLTierTagger.init();
        
        // Initialize config
        ModConfig.init(FabricLoader.getInstance().getConfigDir());
        
        // Register gamemode keybind
        gamemodeKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.ctl-tiertagger.cycle_gamemode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.ctl-tiertagger.controls"
        ));
        
        // Register search keybind
        searchKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.ctl-tiertagger.search_player",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "category.ctl-tiertagger.controls"
        ));
        
        // Register keybind handlers
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Gamemode cycle
            while (gamemodeKeybind.wasPressed()) {
                ModConfig.cycleGamemode();
                if (client.player != null) {
                    String gamemode = ModConfig.getSelectedGamemode();
                    String icon = getGamemodeIcon(gamemode);
                    String message = icon + " §7Current Gamemode: §f" + gamemode;
                    client.player.sendMessage(Text.literal(message), true);
                }
            }
            
            // Open search screen
            while (searchKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PlayerSearchScreen(null));
                }
            }
        });
        
        CTLTierTagger.LOGGER.info("CTL TierTagger Client initialized!");
        CTLTierTagger.LOGGER.info("Nametag rendering enabled - tiers will show above players");
    }
    
    private static String getGamemodeIcon(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "sword" -> "\uE801";
            case "crystal" -> "\uE800";
            case "netherite" -> "\uE803";
            case "potion" -> "\uE802";
            case "mace" -> "\uE807";
            case "uhc" -> "\uE804";
            case "axe" -> "\uE805";
            case "smp" -> "\uE806";
            default -> "";
        };
    }
}
