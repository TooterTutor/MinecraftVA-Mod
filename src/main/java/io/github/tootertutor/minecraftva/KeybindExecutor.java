package io.github.tootertutor.minecraftva;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KeybindExecutor {
    private final MinecraftClient client;
    private final Map<String, Method> methodCache = new HashMap<>();

    public KeybindExecutor(MinecraftClient client) {
        this.client = client;
    }

    public boolean executeMethod(String translationKey) {
        try {
            KeyBinding keyBinding = findKeyBinding(translationKey);
            if (keyBinding == null) {
                MinecraftVA.LOGGER.error("No KeyBinding found for translation key: " + translationKey);
                return false;
            }

            Method method = findMethodForKeyBinding(keyBinding);
            if (method == null) {
                MinecraftVA.LOGGER.error("No method found for KeyBinding: " + translationKey);
                return false;
            }

            client.execute(() -> {
                try {
                    Object targetObject = getTargetObject(method);
                    method.setAccessible(true);
                    method.invoke(targetObject);
                } catch (Exception e) {
                    MinecraftVA.LOGGER.error("Error executing method for " + translationKey, e);
                }
            });

            return true;
        } catch (Exception e) {
            MinecraftVA.LOGGER.error("Error in executeMethod for " + translationKey, e);
            return false;
        }
    }

    private KeyBinding findKeyBinding(String translationKey) {
        return Arrays.stream(client.options.allKeys)
                .filter(kb -> kb.getTranslationKey().equals(translationKey))
                .findFirst()
                .orElse(null);
    }

    private Method findMethodForKeyBinding(KeyBinding keyBinding) {
        String cacheKey = keyBinding.getTranslationKey();
        if (methodCache.containsKey(cacheKey)) {
            return methodCache.get(cacheKey);
        }

        String methodName = deriveMethodName(keyBinding.getTranslationKey());
        Method method = findMethodInHierarchy(client.getClass(), methodName);

        if (method == null) {
            // If not found in client, search in loaded mods
            method = searchMethodInLoadedMods(methodName);
        }

        if (method != null) {
            methodCache.put(cacheKey, method);
        }

        return method;
    }

    private String deriveMethodName(String translationKey) {
        // Convert translation key to a likely method name
        String baseName = translationKey.replace("key.", "").replace("gui.", "");
        return "on" + Arrays.stream(baseName.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .reduce("", String::concat);
    }

    private Method findMethodInHierarchy(Class<?> clazz, String methodName) {
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Method searchMethodInLoadedMods(String methodName) {
        // This is a placeholder. Implement based on your mod loader.
        // Example for Fabric:
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            try {
                Class<?> modClass = Class.forName(mod.getMetadata().getId());
                Method method = findMethodInHierarchy(modClass, methodName);
                if (method != null) {
                    return method;
                }
            } catch (ClassNotFoundException e) {
                MinecraftVA.LOGGER.error("Error searching for method in mod: " + mod.getMetadata().getId(), e);
            }
        }
        return null;
    }

    private Object getTargetObject(Method method) {
        // This is a simplified approach. You might need to adjust this
        // based on where the method is actually defined.
        if (method.getDeclaringClass().isInstance(client)) {
            return client;
        }
        // For methods not in the client, you might need to find or create
        // the appropriate instance. This could involve looking up mod instances.
        return null;
    }
}