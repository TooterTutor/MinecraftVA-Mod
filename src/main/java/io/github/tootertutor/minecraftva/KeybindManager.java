package io.github.tootertutor.minecraftva;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class KeybindManager {
    private Map<String, KeyBinding> registeredKeybinds = new HashMap<>();

    public void updateKeybinds() {
        registeredKeybinds.clear();
        for (KeyBinding keyBinding : MinecraftClient.getInstance().options.allKeys) {
            registeredKeybinds.put(keyBinding.getTranslationKey(), keyBinding);
        }
        MinecraftVA.LOGGER.info("Updated keybinds. Total registered: " + registeredKeybinds.size());
    }

    public Map<String, KeyBinding> getRegisteredKeybinds() {
        return new HashMap<>(registeredKeybinds);
    }

    public KeyBinding getKeybindByTranslationKey(String translationKey) {
        return registeredKeybinds.get(translationKey);
    }
}