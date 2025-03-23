package io.github.tootertutor.minecraftva.Data;

import io.github.tootertutor.minecraftva.MinecraftVA;
import net.minecraft.client.option.KeyBinding;

import java.util.HashMap;
import java.util.Map;

public class MethodMapper {
    private Map<String, String> mappings = new HashMap<>();

    public void updateMappings(Map<String, KeyBinding> keybinds) {
        mappings.clear();
        for (Map.Entry<String, KeyBinding> entry : keybinds.entrySet()) {
            mappings.put(entry.getValue().getBoundKeyLocalizedText().getString(), entry.getKey());
        }
        MinecraftVA.LOGGER.info("Updated method mappings. Total mapped: " + mappings.size());
    }

    public Map<String, String> getMappings() {
        return new HashMap<>(mappings);
    }
}