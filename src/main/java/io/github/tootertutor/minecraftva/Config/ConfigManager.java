package io.github.tootertutor.minecraftva.Config;

import java.util.Random;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Config(name = "VoiceAttackAPI")
@Config.Gui.Background("minecraft:textures/block/gray_stained_glass.png")
@Environment(EnvType.CLIENT)
public class ConfigManager implements ConfigData {
    public boolean randomPort = false;
    public int port = 28463;

    public int getPort() {
        if (randomPort) {
            Random random = new Random();
            return random.nextInt(65535 - 1024) + 1024; // Random port between 1024 and 65535
        }
        return port;
    }
}
