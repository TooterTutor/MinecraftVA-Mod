package io.github.tootertutor.minecraftva;

import net.minecraft.client.option.KeyBinding;

import java.util.HashMap;
import java.util.Map;

public class MethodMapper {
    private Map<String, String> mappings = new HashMap<>();

    public void updateMappings(Map<String, KeyBinding> keybinds) {
        mappings.clear();
        for (Map.Entry<String, KeyBinding> entry : keybinds.entrySet()) {
            mappings.put(entry.getKey(), entry.getKey());
        }
        MinecraftVA.LOGGER.info("Updated method mappings. Total mapped: " + mappings.size());
    }

    public Map<String, String> getMappings() {
        return new HashMap<>(mappings);
    }

    public String getMethodNameForTranslationKey(String translationKey) {
        return mappings.get(translationKey);
    }
}