package io.github.tootertutor.minecraftva;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer {
    private static final int PORT = 25565;
    private final KeybindExecutor keybindExecutor;
    private final MethodMapper methodMapper;
    private volatile boolean running;
    private final Gson gson = new Gson();
    private ServerSocket serverSocket;
    private ExecutorService executor;

    public SocketServer(KeybindExecutor keybindExecutor, MethodMapper methodMapper) {
        this.keybindExecutor = keybindExecutor;
        this.methodMapper = methodMapper;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        running = true;
        MinecraftVA.LOGGER.info("SocketServer starting...");
        executor.submit(this::run);
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            MinecraftVA.LOGGER.info("SocketServer started and listening on port " + PORT);
            while (running) {
                MinecraftVA.LOGGER.info("Waiting for client connection...");
                try (Socket clientSocket = serverSocket.accept()) {
                    MinecraftVA.LOGGER.info("Client connected: " + clientSocket.getInetAddress());
                    try (
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                    ) {
                        String inputLine = in.readLine();
                        MinecraftVA.LOGGER.info("Received command: " + inputLine);
                        String result = handleCommand(inputLine);
                        MinecraftVA.LOGGER.info("Sending response: " + result);
                        out.println(result);
                    }
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

    private String handleCommand(String command) {
        try {
            JsonObject jsonCommand = gson.fromJson(command, JsonObject.class);
            String action = jsonCommand.get("action").getAsString();

            switch (action) {
                case "executeMethod":
                    String translationKey = jsonCommand.has("translationKey") ?
                            jsonCommand.get("translationKey").getAsString() : null;

                    if (translationKey == null || translationKey.isEmpty()) {
                        JsonObject errorResponse = new JsonObject();
                        errorResponse.addProperty("success", false);
                        errorResponse.addProperty("message", "Missing or empty translationKey");
                        return gson.toJson(errorResponse);
                    }

                    boolean success = keybindExecutor.executeMethod(translationKey);
                    JsonObject response = new JsonObject();
                    response.addProperty("success", success);
                    response.addProperty("message", success ? "Method executed: " + translationKey : "Failed to execute method: " + translationKey);
                    return gson.toJson(response);
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
