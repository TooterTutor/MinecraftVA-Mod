package io.github.tootertutor.minecraftva;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataExporter {
    private static final String EXPORT_FILE_NAME = "voiceattack_keybinds.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Map<String, String> lastExportedMappings = null;

    public DataExporter() {
        // Schedule periodic exports
        scheduler.scheduleAtFixedRate(this::exportIfChanged, 1, 5, TimeUnit.MINUTES);
    }

    public void exportData(Map<String, String> mappings) {
        if (!mappings.equals(lastExportedMappings)) {
            try {
                Path exportPath = getExportPath();
                try (FileWriter writer = new FileWriter(exportPath.toFile())) {
                    gson.toJson(mappings, writer);
                }
                lastExportedMappings = Map.copyOf(mappings);
                MinecraftVA.LOGGER.info("Exported keybind mappings to " + exportPath);
            } catch (IOException e) {
                MinecraftVA.LOGGER.error("Failed to export keybind mappings", e);
            }
        }
    }

    private void exportIfChanged() {
        if (lastExportedMappings != null) {
            exportData(lastExportedMappings);
        }
    }

    private Path getExportPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(EXPORT_FILE_NAME);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}