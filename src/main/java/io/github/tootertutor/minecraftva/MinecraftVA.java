package io.github.tootertutor.minecraftva;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class MinecraftVA implements ModInitializer {
    public static final String MOD_ID = "voiceattackapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private KeybindManager keybindManager;
    private MethodMapper methodMapper;
    private DataExporter dataExporter;
    private KeyBinding updateKeybindMapping;
    private KeyBinding restartSocketServer;
    private KeybindMethodInvoker keybindMethodInvoker;
    private SocketServer socketServer;
    private boolean initialized = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing VoiceAttack API Mod");

        this.keybindManager = new KeybindManager();
        this.methodMapper = new MethodMapper();
        this.dataExporter = new DataExporter();
        this.keybindMethodInvoker = new KeybindMethodInvoker(MinecraftClient.getInstance(), this.keybindManager);

        // Create a Consumer<String> that will be passed to the SocketServer
        Consumer<String> methodInvoker = this.keybindMethodInvoker::invokeMethod;
        this.socketServer = new SocketServer(methodInvoker, methodMapper);

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
                    client.player.sendMessage(Text.literal("Updated keybind mappings"), false);
                }

                if (restartSocketServer.wasPressed()) {
                    restartSocketServer();
                    client.player.sendMessage(Text.literal("Restarted socket server"), false);
                }
            }
        });

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }

    private void updateKeybinds() {
        keybindManager.updateKeybinds();
        methodMapper.updateMappings(keybindManager.registeredKeybinds);
        keybindMethodInvoker.updateMethods(keybindManager.registeredKeybinds);
        dataExporter.exportData(methodMapper.getMappings());
    }

    private void restartSocketServer() {
        LOGGER.info("Restarting SocketServer");
        if (socketServer != null) {
            socketServer.stop();
        }
        Consumer<String> methodInvoker = this.keybindMethodInvoker::invokeMethod;
        socketServer = new SocketServer(methodInvoker, methodMapper);
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