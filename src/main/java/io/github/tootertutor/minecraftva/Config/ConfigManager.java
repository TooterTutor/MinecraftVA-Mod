package io.github.tootertutor.minecraftva.Config;

import java.util.Random;

import io.github.tootertutor.minecraftva.MinecraftVA;
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
        MinecraftVA.LOGGER.info("Getting port: randomPort = " + randomPort);
        if (randomPort) {
            Random random = new Random();
            return random.nextInt(1024, 49151); // Random port between 1024 and 49151
        }
        else {
            return port;
        }
    }
}
