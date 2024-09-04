package io.github.tootertutor.minecraftva;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.KeybindTextContent;

import java.util.Map;

public class KeybindMethodInvoker {
    private final MinecraftClient client;
    private final KeybindManager keybindManager;

    public KeybindMethodInvoker(MinecraftClient client, KeybindManager keybindManager) {
        this.client = client;
        this.keybindManager = keybindManager;
    }

    public void updateMethods(Map<String, KeyBinding> keybinds) {
        // No need to do anything here
    }

    public void invokeMethod(String translationKey) {
        MinecraftVA.LOGGER.debug("Attempting to invoke method for translation key: " + translationKey);
        KeyBinding keyBinding = keybindManager.getKeybindByTranslationKey(translationKey);
        if (keyBinding != null) {
            try {
                KeyBinding.onKeyPressed(keyBinding.boundKey);
                MinecraftVA.LOGGER.info("Invoked method for keybind: " + translationKey);
            } catch (Exception e) {
                MinecraftVA.LOGGER.error("Failed to invoke method for keybind: " + translationKey, e);
            }
        } else {
            MinecraftVA.LOGGER.error("KeyBinding not found for translation key: " + translationKey);
        }
    }
}

