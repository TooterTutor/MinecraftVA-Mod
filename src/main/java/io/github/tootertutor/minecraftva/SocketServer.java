package io.github.tootertutor.minecraftva;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.tootertutor.minecraftva.Config.ConfigManager;
import io.github.tootertutor.minecraftva.Data.MethodMapper;

public class SocketServer {
    private final ConfigManager configManager; // Instance of ConfigManager
    private final int PORT; // Declare PORT as an instance variable
    private final Consumer<String> methodInvoker;
    private final MethodMapper methodMapper;
    private volatile boolean running;
    private final Gson gson = new Gson();
    private ServerSocket serverSocket;
    private ExecutorService executor;

    private static final int VOICE_ATTACK_PORT = 28463; // Fallback port for VoiceAttack
    private boolean randomPortMode; // This should be set based on your configuration logic
    private int defaultPort; // Store the default port for comparison

    public SocketServer(ConfigManager configManager, Consumer<String> methodInvoker, MethodMapper methodMapper) {
        this.methodInvoker = methodInvoker;
        this.methodMapper = methodMapper;
        this.executor = Executors.newSingleThreadExecutor();
        this.configManager = configManager; // Initialize the configManager
        this.defaultPort = this.configManager.getPort(); // Initialize defaultPort
        this.PORT = this.defaultPort; // Initialize PORT in the constructor
        this.randomPortMode = false; // Set this based on your configuration logic
    }

    public void start() {
        running = true;
        MinecraftVA.LOGGER.info("SocketServer starting...");
        executor.submit(this::run);

        // Start a separate thread to send handshake to VoiceAttack
        new Thread(this::sendHandshake).start();
    }

    private void sendHandshake() {
        try {
            // Create a socket to connect to VoiceAttack
            Socket voiceAttackSocket = new Socket("localhost", VOICE_ATTACK_PORT);
            PrintWriter out = new PrintWriter(voiceAttackSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(voiceAttackSocket.getInputStream()));

            // Create handshake JSON object
            JsonObject handshakeCommand = new JsonObject();
            handshakeCommand.addProperty("command", "handshake");
            handshakeCommand.addProperty("port", PORT);

            // Send handshake command to VoiceAttack
            out.println(gson.toJson(handshakeCommand));
            MinecraftVA.LOGGER.info("Sent handshake command: " + gson.toJson(handshakeCommand));

            // Wait for acknowledgment from VoiceAttack
            String response = in.readLine();
            MinecraftVA.LOGGER.info("Received acknowledgment.");

            // Handle the acknowledgment if needed
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            if (jsonResponse.has("status") && "acknowledged".equals(jsonResponse.get("status").getAsString())) {
                MinecraftVA.LOGGER.info("Handshake acknowledged by VoiceAttack.");
            } else {
                MinecraftVA.LOGGER.warn("Handshake not acknowledged.");
            }

            // Close the socket after communication
            voiceAttackSocket.close();
        } catch (Exception e) {
            MinecraftVA.LOGGER.error("Error in handshake listener", e);
        }
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            MinecraftVA.LOGGER.info("SocketServer started and listening on port " + PORT);
            while (running) {
                MinecraftVA.LOGGER.info("Waiting for client connection...");
                try (Socket clientSocket = serverSocket.accept()) {
                    MinecraftVA.LOGGER.info("Client connected: " + clientSocket.getInetAddress());
                    handleClientConnection(clientSocket);
                }
            }
        } catch (Exception e) {
            if (running) {
                MinecraftVA.LOGGER.error("Error in SocketServer", e);
            }
        } finally {
            closeServerSocket();
        }
    }

    private void handleClientConnection(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine = in.readLine();
            MinecraftVA.LOGGER.info("Received command: " + inputLine);

            // Validate input before processing
            if (inputLine == null || inputLine.trim().isEmpty()) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("message", "Received empty command");
                out.println(gson.toJson(errorResponse));
                return;
            }

            String result = handleCommand(inputLine);
            MinecraftVA.LOGGER.info("Sending response: " + result);
            out.println(result);
        } catch (Exception e) {
            MinecraftVA.LOGGER.error("Error handling client connection", e);
        }
    }

    private String handleCommand(String command) {
        try {
            JsonObject jsonCommand = gson.fromJson(command, JsonObject.class);
            String action = jsonCommand.get("action").getAsString();

            switch (action) {
                case "executeMethod":
                    String translationKey = jsonCommand.has("translationKey")
                            ? jsonCommand.get("translationKey").getAsString()
                            : null;

                    if (translationKey == null || translationKey.isEmpty()) {
                        JsonObject errorResponse = new JsonObject();
                        errorResponse.addProperty("success", false);
                        errorResponse.addProperty("message", "Missing or empty translationKey");
                        return gson.toJson(errorResponse);
                    }

                    try {
                        methodInvoker.accept(translationKey);
                        JsonObject response = new JsonObject();
                        response.addProperty("success", true);
                        response.addProperty("message", "Method invoked: " + translationKey);
                        return gson.toJson(response);
                    } catch (Exception e) {
                        JsonObject errorResponse = new JsonObject();
                        errorResponse.addProperty("success", false);
                        errorResponse.addProperty("message",
                                "Failed to invoke method: " + translationKey + ". Error: " + e.getMessage());
                        return gson.toJson(errorResponse);
                    }
                case "getMappings":
                    return gson.toJson(methodMapper.getMappings());
                default:
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("success", false);
                    errorResponse.addProperty("message", "Unknown command: " + action);
                    return gson.toJson(errorResponse);
            }
        } catch (Exception e) {
            MinecraftVA.LOGGER.error("Error handling command: " + command, e);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("message", "Error handling command: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }

    public void stop() {
        running = false;
        closeServerSocket();
        shutdownExecutor();
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            MinecraftVA.LOGGER.error("Error closing server socket", e);
        }
    }

    private void shutdownExecutor() {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                MinecraftVA.LOGGER.warn("Executor did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
