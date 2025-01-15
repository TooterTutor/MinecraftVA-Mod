package io.github.tootertutor.minecraftva.Keybinds;

import io.github.tootertutor.minecraftva.MinecraftVA;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class KeybindManager {
    public Map<String, KeyBinding> registeredKeybinds = new HashMap<>();

    public void updateKeybinds() {
        registeredKeybinds.clear();
        for (KeyBinding keyBinding : MinecraftClient.getInstance().options.allKeys) {
            String translationKey = keyBinding.getTranslationKey();
            registeredKeybinds.put(translationKey, keyBinding);
            MinecraftVA.LOGGER.debug("Registered keybind: " + translationKey);
        }
        MinecraftVA.LOGGER.info("Updated keybinds. Total registered: " + registeredKeybinds.size());
    }

    public KeyBinding getKeybindByTranslationKey(String translationKey) {
        return registeredKeybinds.get(translationKey);
    }
}
