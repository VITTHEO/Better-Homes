package net.onehype.VitTheo.BetterHomes.storage;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class YamlHomeStorage implements HomeStorage {
    private final BetterHomes plugin;
    private final File file;

    public YamlHomeStorage(BetterHomes plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    public Map<UUID, Map<String, Home>> load() throws IOException {
        Map<UUID, Map<String, Home>> loaded = new HashMap<UUID, Map<String, Home>>();
        if (!file.exists()) {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new IOException("Unable to create plugin data folder");
            }
            if (!file.createNewFile()) {
                return loaded;
            }
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) {
            return loaded;
        }

        for (String uuidKey : players.getKeys(false)) {
            UUID ownerId;
            try {
                ownerId = UUID.fromString(uuidKey);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Ignoring invalid UUID in homes.yml: " + uuidKey);
                continue;
            }

            ConfigurationSection playerSection = players.getConfigurationSection(uuidKey);
            if (playerSection == null) {
                continue;
            }
            String ownerName = playerSection.getString("name", "Unknown");
            ConfigurationSection homesSection = playerSection.getConfigurationSection("homes");
            if (homesSection == null) {
                continue;
            }

            Map<String, Home> homes = new HashMap<String, Home>();
            for (String homeId : homesSection.getKeys(false)) {
                ConfigurationSection section = homesSection.getConfigurationSection(homeId);
                if (section == null) {
                    continue;
                }
                Home home = deserialize(ownerId, ownerName, section);
                if (home == null) {
                    continue;
                }
                String key = section.getString("key", home.getName().toLowerCase());
                homes.put(key, home);
            }
            loaded.put(ownerId, homes);
        }
        return loaded;
    }

    public void save(Map<UUID, Map<String, Home>> homes) throws IOException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IOException("Unable to create plugin data folder");
        }
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Home>> playerEntry : homes.entrySet()) {
            String playerPath = "players." + playerEntry.getKey().toString();
            String ownerName = "Unknown";
            int index = 0;
            for (Map.Entry<String, Home> homeEntry : playerEntry.getValue().entrySet()) {
                Home home = homeEntry.getValue();
                ownerName = home.getOwnerName();
                String path = playerPath + ".homes." + index;
                serialize(yaml, path, homeEntry.getKey(), home);
                index++;
            }
            yaml.set(playerPath + ".name", ownerName);
        }
        yaml.save(file);
    }

    private Home deserialize(UUID ownerId, String ownerName, ConfigurationSection section) {
        String name = section.getString("name");
        String worldName = section.getString("world");
        if (name == null || worldName == null) {
            return null;
        }

        Home home = new Home(ownerId, ownerName, name, worldName, section.getDouble("x"), section.getDouble("y"),
                section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
        home.setCreatedAt(section.getLong("created-at", System.currentTimeMillis()));
        home.setUpdatedAt(section.getLong("updated-at", home.getCreatedAt()));
        home.setPublicHome(section.getBoolean("public", false));
        Set<UUID> shared = new HashSet<UUID>();
        for (String uuid : section.getStringList("shared-with")) {
            try {
                shared.add(UUID.fromString(uuid));
            } catch (IllegalArgumentException ignored) {
            }
        }
        home.setSharedWith(shared);
        home.setUpdatedAt(section.getLong("updated-at", home.getUpdatedAt()));
        return home;
    }

    private void serialize(YamlConfiguration yaml, String path, String key, Home home) {
        yaml.set(path + ".key", key);
        yaml.set(path + ".name", home.getName());
        yaml.set(path + ".world", home.getWorldName());
        yaml.set(path + ".x", home.getX());
        yaml.set(path + ".y", home.getY());
        yaml.set(path + ".z", home.getZ());
        yaml.set(path + ".yaw", home.getYaw());
        yaml.set(path + ".pitch", home.getPitch());
        yaml.set(path + ".created-at", home.getCreatedAt());
        yaml.set(path + ".updated-at", home.getUpdatedAt());
        yaml.set(path + ".public", home.isPublicHome());
        Set<String> shared = new HashSet<String>();
        for (UUID uuid : home.getSharedWith()) {
            shared.add(uuid.toString());
        }
        yaml.set(path + ".shared-with", shared.toArray(new String[shared.size()]));
    }

}
