package net.onehype.VitTheo.BetterHomes;

import net.onehype.VitTheo.BetterHomes.command.BetterHomesCommand;
import net.onehype.VitTheo.BetterHomes.command.DelHomeCommand;
import net.onehype.VitTheo.BetterHomes.command.HomeCommand;
import net.onehype.VitTheo.BetterHomes.command.HomesCommand;
import net.onehype.VitTheo.BetterHomes.command.SetHomeCommand;
import net.onehype.VitTheo.BetterHomes.compat.CompatService;
import net.onehype.VitTheo.BetterHomes.config.ConfigManager;
import net.onehype.VitTheo.BetterHomes.config.PluginSettings;
import net.onehype.VitTheo.BetterHomes.gui.HomeGuiManager;
import net.onehype.VitTheo.BetterHomes.gui.NamePromptManager;
import net.onehype.VitTheo.BetterHomes.home.HomeManager;
import net.onehype.VitTheo.BetterHomes.listener.GuiListener;
import net.onehype.VitTheo.BetterHomes.listener.PromptListener;
import net.onehype.VitTheo.BetterHomes.listener.TeleportListener;
import net.onehype.VitTheo.BetterHomes.storage.HomeStorage;
import net.onehype.VitTheo.BetterHomes.storage.YamlHomeStorage;
import net.onehype.VitTheo.BetterHomes.teleport.SafeLocationFinder;
import net.onehype.VitTheo.BetterHomes.teleport.TeleportManager;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;

public class BetterHomes extends JavaPlugin {
    private ConfigManager configManager;
    private PluginSettings settings;
    private MessageService messages;
    private CompatService compat;
    private HomeStorage storage;
    private HomeManager homeManager;
    private SafeLocationFinder safeLocationFinder;
    private TeleportManager teleportManager;
    private HomeGuiManager guiManager;
    private NamePromptManager promptManager;
    private BukkitTask autoSaveTask;

    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();
        settings = new PluginSettings();
        settings.reload(getConfig());
        messages = new MessageService(configManager);
        compat = new CompatService(this);

        storage = new YamlHomeStorage(this, settings.storageFile);
        homeManager = new HomeManager(this, storage);
        try {
            homeManager.load();
        } catch (IOException exception) {
            getLogger().severe("Unable to load homes: " + exception.getMessage());
        }

        safeLocationFinder = new SafeLocationFinder(this);
        teleportManager = new TeleportManager(this, safeLocationFinder);
        promptManager = new NamePromptManager(this);
        guiManager = new HomeGuiManager(this);

        registerCommands();
        registerListeners();
        scheduleAutoSave();
        getLogger().info("Better Homes enabled. Loaded " + homeManager.loadedHomeCount() + " homes.");
    }

    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }
        saveHomesQuietly();
        getLogger().info("Better Homes disabled.");
    }

    public void reloadPlugin() {
        saveHomesQuietly();
        configManager.load();
        settings.reload(getConfig());
        scheduleAutoSave();
    }

    public void saveHomesQuietly() {
        if (homeManager == null) {
            return;
        }
        try {
            homeManager.save();
        } catch (IOException exception) {
            getLogger().severe("Unable to save homes: " + exception.getMessage());
        }
    }

    private void scheduleAutoSave() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        long ticks = Math.max(1L, settings.autoSaveMinutes) * 60L * 20L;
        autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                saveHomesQuietly();
            }
        }, ticks, ticks);
    }

    private void registerCommands() {
        register("sethome", new SetHomeCommand(this));
        register("home", new HomeCommand(this));
        register("delhome", new DelHomeCommand(this));
        register("homes", new HomesCommand(this));
        register("betterhomes", new BetterHomesCommand(this));
    }

    private void register(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command not found in plugin.yml: " + name);
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PromptListener(this), this);
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public PluginSettings settings() {
        return settings;
    }

    public MessageService messages() {
        return messages;
    }

    public CompatService compat() {
        return compat;
    }

    public HomeManager homeManager() {
        return homeManager;
    }

    public TeleportManager teleportManager() {
        return teleportManager;
    }

    public HomeGuiManager guiManager() {
        return guiManager;
    }

    public NamePromptManager promptManager() {
        return promptManager;
    }
}
