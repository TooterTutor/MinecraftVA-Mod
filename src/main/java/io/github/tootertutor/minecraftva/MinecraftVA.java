package io.github.tootertutor.minecraftva;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecraftVA implements ModInitializer {
    public static final String MOD_ID = "voiceattackapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private KeybindManager keybindManager;
    private MethodMapper methodMapper;
    private DataExporter dataExporter;
    private KeyBinding updateKeybindMapping;
    private KeyBinding restartSocketServer;
    private KeybindExecutor keybindExecutor;
    private SocketServer socketServer;
    private boolean initialized = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing VoiceAttack API Mod");

        this.keybindManager = new KeybindManager();
        this.methodMapper = new MethodMapper();
        this.dataExporter = new DataExporter();
        this.keybindExecutor = new KeybindExecutor(MinecraftClient.getInstance());
        this.socketServer = new SocketServer(keybindExecutor, methodMapper);

        // Register the update keybind mapping
        updateKeybindMapping = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voiceattackapi.update_mappings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.voiceattackapi.general"
        ));

        // Register the restart socket server keybind
        restartSocketServer = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voiceattackapi.restart_socket_server",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.voiceattackapi.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!initialized && client.player != null) {
                updateKeybinds();
                socketServer.start();
                initialized = true;
            }

            if (initialized) {
                if (updateKeybindMapping.wasPressed()) {
                    updateKeybinds();
                    client.player.sendMessage(net.minecraft.text.Text.literal("VoiceAttack API: Keybind mappings updated"), false);
                }

                if (restartSocketServer.wasPressed()) {
                    restartSocketServer();
                    client.player.sendMessage(net.minecraft.text.Text.literal("VoiceAttack API: SocketServer restarted"), false);
                }
            }
        });

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }

    private void updateKeybinds() {
        keybindManager.updateKeybinds();
        methodMapper.updateMappings(keybindManager.getRegisteredKeybinds());
        dataExporter.exportData(methodMapper.getMappings());
    }

    private void restartSocketServer() {
        LOGGER.info("Restarting SocketServer");
        if (socketServer != null) {
            socketServer.stop();
        }
        socketServer = new SocketServer(keybindExecutor, methodMapper);
        socketServer.start();
    }

    private void onShutdown() {
        LOGGER.info("Shutting down VoiceAttack API Mod");
        if (socketServer != null) {
            socketServer.stop();
        }
        if (dataExporter != null) {
            dataExporter.shutdown();
        }
    }
}