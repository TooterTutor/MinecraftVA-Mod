package io.github.tootertutor.minecraftva.Keybinds;

import io.github.tootertutor.minecraftva.MinecraftVA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeybindMethodInvoker {
    private final MinecraftClient client;
    private final KeybindManager keybindManager;

    public KeybindMethodInvoker(MinecraftClient client, KeybindManager keybindManager) {
        this.client = client;
        this.keybindManager = keybindManager;
    }

    public void invokeMethod(String translationKey) {
        MinecraftVA.LOGGER.debug("Attempting to invoke method for translation key: " + translationKey);
        KeyBinding keyBinding = keybindManager.getKeybindByTranslationKey(translationKey);
        if (keyBinding != null) {
            try {
                InputUtil.Key oldKey = keyBinding.boundKey;
                InputUtil.Key tmp = InputUtil.fromTranslationKey("key.keyboard.f22");
                keyBinding.setBoundKey(tmp);
                KeyBinding.updateKeysByCode();
                KeyBinding.onKeyPressed(tmp);
                keyBinding.setBoundKey(oldKey);
                KeyBinding.updateKeysByCode();
                MinecraftVA.LOGGER.info("Invoked method for keybind: " + translationKey);
            } catch (Exception e) {
                MinecraftVA.LOGGER.error("Failed to invoke method for keybind: " + translationKey, e);
            }
        } else {
            MinecraftVA.LOGGER.error("KeyBinding not found for translation key: " + translationKey);
        }
    }
}

