package net.onehype.VitTheo.BetterHomes.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PluginSettings {
    public String storageFile;
    public int autoSaveMinutes;

    public String defaultHomeName;
    public int defaultLimit;
    public boolean useLimitPermission;
    public boolean allowOverwrite;
    public String overwritePermission;
    public boolean perWorldHomes;
    public int maxNameLength;
    public Pattern namePattern;
    public String allowedNameCharactersMessage;

    public String listingMode;
    public int entriesPerChatPage;

    public String worldMode;
    public Set<String> worldList;
    public boolean requireWorldPermission;
    public String worldPermissionTemplate;

    public int teleportDelaySeconds;
    public int teleportCooldownSeconds;
    public boolean cancelOnMove;
    public boolean cancelOnDamage;
    public boolean cancelOnCombat;
    public int combatTagSeconds;
    public boolean centerOnBlock;
    public boolean safeTeleport;
    public int safeSearchHorizontal;
    public int safeSearchVertical;
    public boolean warmupTitle;

    public boolean sharedHomesEnabled;
    public boolean publicHomesEnabled;
    public boolean allowOwnerShareCommand;
    public boolean allowOwnerPublicToggle;

    public boolean delhomeRequiresConfirmation;
    public boolean homeWithoutNameUsesFirstIfOnlyOne;
    public boolean homesCommandOpensGui;
    public boolean homesCommandShowsChatList;

    public boolean guiEnabled;
    public int guiRows;
    public boolean guiFillEmptySlots;

    public boolean soundsEnabled;
    public boolean effectsEnabled;

    public void reload(FileConfiguration config) {
        storageFile = config.getString("storage.file", "homes.yml");
        autoSaveMinutes = Math.max(1, config.getInt("storage.auto-save-minutes", 5));

        defaultHomeName = config.getString("homes.default-name", "home");
        defaultLimit = Math.max(0, config.getInt("homes.default-limit", 5));
        useLimitPermission = config.getBoolean("homes.use-limit-permission", true);
        allowOverwrite = config.getBoolean("homes.allow-overwrite", false);
        overwritePermission = config.getString("homes.allow-overwrite-with-permission", "betterhomes.sethome.overwrite");
        perWorldHomes = config.getBoolean("homes.per-world-homes", false);
        maxNameLength = Math.max(1, config.getInt("homes.max-name-length", 24));
        allowedNameCharactersMessage = config.getString("homes.allowed-name-characters-message", "A-Z, a-z, 0-9, _ and -");

        String regex = config.getString("homes.name-regex", "^[A-Za-z0-9_-]+$");
        try {
            namePattern = Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            namePattern = Pattern.compile("^[A-Za-z0-9_-]+$");
        }

        listingMode = config.getString("listing.mode", "both").toLowerCase(Locale.ENGLISH);
        entriesPerChatPage = Math.max(1, config.getInt("listing.entries-per-chat-page", 8));

        worldMode = config.getString("worlds.mode", "none").toLowerCase(Locale.ENGLISH);
        worldList = new HashSet<String>();
        for (String world : config.getStringList("worlds.list")) {
            worldList.add(world.toLowerCase(Locale.ENGLISH));
        }
        requireWorldPermission = config.getBoolean("worlds.require-permission", false);
        worldPermissionTemplate = config.getString("worlds.permission-template", "betterhomes.world.%world%");

        teleportDelaySeconds = Math.max(0, config.getInt("teleport.delay-seconds", 5));
        teleportCooldownSeconds = Math.max(0, config.getInt("teleport.cooldown-seconds", 20));
        cancelOnMove = config.getBoolean("teleport.cancel-on-move", true);
        cancelOnDamage = config.getBoolean("teleport.cancel-on-damage", true);
        cancelOnCombat = config.getBoolean("teleport.cancel-on-combat", true);
        combatTagSeconds = Math.max(0, config.getInt("teleport.combat-tag-seconds", 12));
        centerOnBlock = config.getBoolean("teleport.center-on-block", true);
        safeTeleport = config.getBoolean("teleport.safe-teleport", true);
        safeSearchHorizontal = Math.max(0, config.getInt("teleport.search-radius-horizontal", 4));
        safeSearchVertical = Math.max(0, config.getInt("teleport.search-radius-vertical", 5));
        warmupTitle = config.getBoolean("teleport.warmup-title", true);

        sharedHomesEnabled = config.getBoolean("shared-homes.enabled", true);
        publicHomesEnabled = config.getBoolean("shared-homes.public-enabled", true);
        allowOwnerShareCommand = config.getBoolean("shared-homes.allow-owner-share-command", true);
        allowOwnerPublicToggle = config.getBoolean("shared-homes.allow-owner-public-toggle", true);

        homeWithoutNameUsesFirstIfOnlyOne = config.getBoolean("commands.home-without-name-uses-first-if-only-one", true);
        delhomeRequiresConfirmation = config.getBoolean("commands.delhome-requires-confirmation", true);
        homesCommandOpensGui = config.getBoolean("commands.homes-command-opens-gui", true);
        homesCommandShowsChatList = config.getBoolean("commands.homes-command-shows-chat-list", true);

        guiEnabled = config.getBoolean("gui.enabled", true);
        guiRows = Math.min(6, Math.max(1, config.getInt("gui.rows", 6)));
        guiFillEmptySlots = config.getBoolean("gui.fill-empty-slots", true);

        soundsEnabled = config.getBoolean("sounds.enabled", true);
        effectsEnabled = config.getBoolean("effects.enabled", true);
    }

    public boolean chatListingEnabled() {
        return "chat".equals(listingMode) || "both".equals(listingMode);
    }

    public boolean guiListingEnabled() {
        return "gui".equals(listingMode) || "both".equals(listingMode);
    }

    public boolean worldAllowed(String worldName) {
        if (worldName == null) {
            return false;
        }
        String normalized = worldName.toLowerCase(Locale.ENGLISH);
        if ("whitelist".equals(worldMode)) {
            return worldList.contains(normalized);
        }
        if ("blacklist".equals(worldMode)) {
            return !worldList.contains(normalized);
        }
        return true;
    }
}
