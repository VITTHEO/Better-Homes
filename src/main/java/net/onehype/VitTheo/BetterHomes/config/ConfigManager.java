package net.onehype.VitTheo.BetterHomes.config;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private final BetterHomes plugin;
    private File messagesFile;
    private YamlConfiguration messages;

    public ConfigManager(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        loadMessages();
    }

    public FileConfiguration config() {
        return plugin.getConfig();
    }

    public YamlConfiguration messages() {
        return messages;
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
        InputStream defaults = plugin.getResource("messages.yml");
        if (defaults != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaults, StandardCharsets.UTF_8));
            messages.setDefaults(defaultConfig);
            messages.options().copyDefaults(true);
            try {
                messages.save(messagesFile);
            } catch (IOException exception) {
                plugin.getLogger().warning("Unable to save messages.yml defaults: " + exception.getMessage());
            }
        }
    }
}
