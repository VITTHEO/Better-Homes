package net.onehype.VitTheo.BetterHomes.home;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.storage.HomeStorage;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import net.onehype.VitTheo.BetterHomes.util.PermissionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
    private final BetterHomes plugin;
    private final HomeStorage storage;
    private Map<UUID, Map<String, Home>> homes = new HashMap<UUID, Map<String, Home>>();

    public HomeManager(BetterHomes plugin, HomeStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public synchronized void load() throws IOException {
        homes = reindex(storage.load());
    }

    public void save() throws IOException {
        storage.save(snapshot());
    }

    public synchronized Map<UUID, Map<String, Home>> snapshot() {
        Map<UUID, Map<String, Home>> copy = new HashMap<UUID, Map<String, Home>>();
        for (Map.Entry<UUID, Map<String, Home>> playerEntry : homes.entrySet()) {
            Map<String, Home> playerHomes = new HashMap<String, Home>();
            for (Map.Entry<String, Home> homeEntry : playerEntry.getValue().entrySet()) {
                playerHomes.put(homeEntry.getKey(), new Home(homeEntry.getValue()));
            }
            copy.put(playerEntry.getKey(), playerHomes);
        }
        return copy;
    }

    public SetHomeResult setHome(Player player, String rawName, boolean forceOverwrite, boolean bypassLimit) {
        if (player == null || player.getLocation() == null || player.getLocation().getWorld() == null) {
            return new SetHomeResult(SetHomeResultType.INVALID_LOCATION, null, 0);
        }
        return setHome(player.getUniqueId(), player.getName(), player.getLocation(), player, rawName, forceOverwrite, bypassLimit);
    }

    public SetHomeResult setHome(UUID ownerId, String ownerName, Location location, Player permissionSource,
                                 String rawName, boolean forceOverwrite, boolean bypassLimit) {
        if (location == null || location.getWorld() == null) {
            return new SetHomeResult(SetHomeResultType.INVALID_LOCATION, null, 0);
        }

        NameCheck nameCheck = checkName(rawName);
        if (nameCheck.type != SetHomeResultType.OK) {
            return new SetHomeResult(nameCheck.type, null, 0);
        }

        WorldCheck worldCheck = checkWorld(permissionSource, location.getWorld().getName());
        if (worldCheck == WorldCheck.BLOCKED) {
            return new SetHomeResult(SetHomeResultType.WORLD_BLOCKED, null, 0);
        }
        if (worldCheck == WorldCheck.PERMISSION) {
            return new SetHomeResult(SetHomeResultType.WORLD_PERMISSION, null, 0);
        }

        synchronized (this) {
            Map<String, Home> playerHomes = homes.get(ownerId);
            if (playerHomes == null) {
                playerHomes = new HashMap<String, Home>();
                homes.put(ownerId, playerHomes);
            }

            String key = key(nameCheck.name, location.getWorld().getName());
            Home existing = playerHomes.get(key);
            if (existing != null && !canOverwrite(permissionSource, forceOverwrite)) {
                return new SetHomeResult(SetHomeResultType.ALREADY_EXISTS, new Home(existing), 0);
            }

            if (existing == null && !bypassLimit && permissionSource != null) {
                int limit = getLimit(permissionSource, location.getWorld().getName());
                if (limit >= 0 && countHomes(ownerId, location.getWorld().getName()) >= limit) {
                    return new SetHomeResult(SetHomeResultType.LIMIT_REACHED, null, limit);
                }
            }

            Home home;
            SetHomeResultType resultType;
            if (existing == null) {
                home = new Home(ownerId, ownerName, nameCheck.name, location);
                playerHomes.put(key, home);
                resultType = SetHomeResultType.CREATED;
            } else {
                existing.setOwnerName(ownerName);
                existing.updateLocation(location);
                home = existing;
                resultType = SetHomeResultType.UPDATED;
            }
            return new SetHomeResult(resultType, new Home(home), 0);
        }
    }

    public synchronized Home getHome(UUID ownerId, String rawName, String currentWorldName) {
        Home home = getHomeInternal(ownerId, rawName, currentWorldName);
        return home == null ? null : new Home(home);
    }

    public synchronized Home getFirstHomeIfOnlyOne(UUID ownerId) {
        Map<String, Home> map = homes.get(ownerId);
        if (map == null || map.size() != 1) {
            return null;
        }
        return new Home(map.values().iterator().next());
    }

    public synchronized List<Home> getHomes(UUID ownerId) {
        Map<String, Home> map = homes.get(ownerId);
        List<Home> list = new ArrayList<Home>();
        if (map != null) {
            for (Home home : map.values()) {
                list.add(new Home(home));
            }
        }
        Collections.sort(list, HOME_COMPARATOR);
        return list;
    }

    public synchronized boolean deleteHome(UUID ownerId, String rawName, String currentWorldName) {
        Map<String, Home> map = homes.get(ownerId);
        if (map == null) {
            return false;
        }
        Home home = getHomeInternal(ownerId, rawName, currentWorldName);
        if (home == null) {
            return false;
        }
        map.remove(key(home.getName(), home.getWorldName()));
        if (map.isEmpty()) {
            homes.remove(ownerId);
        }
        return true;
    }

    public synchronized boolean clearHomes(UUID ownerId) {
        return homes.remove(ownerId) != null;
    }

    public synchronized boolean moveHome(UUID ownerId, String rawName, String currentWorldName, Location location) {
        Home home = getHomeInternal(ownerId, rawName, currentWorldName);
        if (home == null || location == null || location.getWorld() == null) {
            return false;
        }
        Map<String, Home> map = homes.get(ownerId);
        map.remove(key(home.getName(), home.getWorldName()));
        home.updateLocation(location);
        map.put(key(home.getName(), home.getWorldName()), home);
        return true;
    }

    public synchronized RenameResult renameHome(UUID ownerId, String rawName, String currentWorldName, String newName) {
        NameCheck nameCheck = checkName(newName);
        if (nameCheck.type != SetHomeResultType.OK) {
            return new RenameResult(nameCheck.type, null);
        }

        Map<String, Home> map = homes.get(ownerId);
        if (map == null) {
            return new RenameResult(SetHomeResultType.NOT_FOUND, null);
        }
        Home home = getHomeInternal(ownerId, rawName, currentWorldName);
        if (home == null) {
            return new RenameResult(SetHomeResultType.NOT_FOUND, null);
        }
        String newKey = key(nameCheck.name, home.getWorldName());
        if (map.containsKey(newKey)) {
            return new RenameResult(SetHomeResultType.ALREADY_EXISTS, new Home(map.get(newKey)));
        }
        map.remove(key(home.getName(), home.getWorldName()));
        home.setName(nameCheck.name);
        map.put(newKey, home);
        return new RenameResult(SetHomeResultType.UPDATED, new Home(home));
    }

    public synchronized boolean setPublic(UUID ownerId, String rawName, String currentWorldName, boolean publicHome) {
        Home home = getHomeInternal(ownerId, rawName, currentWorldName);
        if (home == null) {
            return false;
        }
        home.setPublicHome(publicHome);
        return true;
    }

    public synchronized boolean share(UUID ownerId, String rawName, String currentWorldName, UUID target) {
        Home home = getHomeInternal(ownerId, rawName, currentWorldName);
        if (home == null) {
            return false;
        }
        home.shareWith(target);
        return true;
    }

    public synchronized boolean unshare(UUID ownerId, String rawName, String currentWorldName, UUID target) {
        Home home = getHomeInternal(ownerId, rawName, currentWorldName);
        if (home == null) {
            return false;
        }
        home.unshareWith(target);
        return true;
    }

    public synchronized UUID findOwnerIdByName(String ownerName) {
        if (ownerName == null) {
            return null;
        }
        for (Map.Entry<UUID, Map<String, Home>> entry : homes.entrySet()) {
            for (Home home : entry.getValue().values()) {
                if (home.getOwnerName() != null && home.getOwnerName().equalsIgnoreCase(ownerName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public synchronized int countHomes(UUID ownerId, String worldName) {
        Map<String, Home> map = homes.get(ownerId);
        if (map == null) {
            return 0;
        }
        if (!plugin.settings().perWorldHomes || worldName == null) {
            return map.size();
        }
        int count = 0;
        for (Home home : map.values()) {
            if (worldName.equalsIgnoreCase(home.getWorldName())) {
                count++;
            }
        }
        return count;
    }

    public synchronized int loadedPlayerCount() {
        return homes.size();
    }

    public synchronized int loadedHomeCount() {
        int count = 0;
        for (Map<String, Home> playerHomes : homes.values()) {
            count += playerHomes.size();
        }
        return count;
    }

    public int getLimit(Player player, String worldName) {
        if (player == null) {
            return plugin.settings().defaultLimit;
        }
        if (PermissionUtil.has(player, PermissionNodes.BYPASS_LIMIT)) {
            return Integer.MAX_VALUE;
        }
        int limit = plugin.settings().defaultLimit;
        if (plugin.settings().useLimitPermission) {
            limit = PermissionUtil.highestNumericPermission(player, "betterhomes.limit.", limit);
        }
        return limit;
    }

    public boolean canAccess(Player viewer, Home home) {
        if (viewer == null || home == null) {
            return false;
        }
        if (viewer.getUniqueId().equals(home.getOwnerId())) {
            return true;
        }
        if (PermissionUtil.has(viewer, PermissionNodes.OTHERS)) {
            return true;
        }
        if (plugin.settings().publicHomesEnabled && home.isPublicHome()) {
            return true;
        }
        return plugin.settings().sharedHomesEnabled && home.isSharedWith(viewer.getUniqueId());
    }

    public WorldCheck checkWorld(Player player, String worldName) {
        if (!plugin.settings().worldAllowed(worldName)) {
            return WorldCheck.BLOCKED;
        }
        if (player != null && plugin.settings().requireWorldPermission) {
            String permission = plugin.settings().worldPermissionTemplate.replace("%world%", worldName);
            if (!PermissionUtil.has(player, permission) && !PermissionUtil.has(player, "betterhomes.world.*")) {
                return WorldCheck.PERMISSION;
            }
        }
        return WorldCheck.OK;
    }

    public NameCheck checkName(String rawName) {
        String name = rawName == null ? plugin.settings().defaultHomeName : rawName.trim();
        if (name.length() == 0) {
            name = plugin.settings().defaultHomeName;
        }
        if (name.length() > plugin.settings().maxNameLength) {
            return new NameCheck(SetHomeResultType.NAME_TOO_LONG, name);
        }
        if (!plugin.settings().namePattern.matcher(name).matches()) {
            return new NameCheck(SetHomeResultType.INVALID_NAME, name);
        }
        return new NameCheck(SetHomeResultType.OK, name);
    }

    private boolean canOverwrite(Player player, boolean forceOverwrite) {
        if (forceOverwrite || plugin.settings().allowOverwrite) {
            return true;
        }
        return player != null && (PermissionUtil.has(player, plugin.settings().overwritePermission)
                || PermissionUtil.has(player, PermissionNodes.ADMIN));
    }

    private Home getHomeInternal(UUID ownerId, String rawName, String currentWorldName) {
        Map<String, Home> map = homes.get(ownerId);
        if (map == null) {
            return null;
        }
        String name = rawName == null || rawName.trim().length() == 0 ? plugin.settings().defaultHomeName : rawName.trim();
        if (plugin.settings().perWorldHomes && currentWorldName != null) {
            Home sameWorld = map.get(key(name, currentWorldName));
            if (sameWorld != null) {
                return sameWorld;
            }
        }
        Home direct = map.get(normalize(name));
        if (direct != null) {
            return direct;
        }
        for (Home home : map.values()) {
            if (home.getName().equalsIgnoreCase(name)) {
                return home;
            }
        }
        return null;
    }

    private Map<UUID, Map<String, Home>> reindex(Map<UUID, Map<String, Home>> source) {
        Map<UUID, Map<String, Home>> result = new HashMap<UUID, Map<String, Home>>();
        for (Map.Entry<UUID, Map<String, Home>> entry : source.entrySet()) {
            Map<String, Home> indexed = new HashMap<String, Home>();
            for (Home home : entry.getValue().values()) {
                indexed.put(key(home.getName(), home.getWorldName()), home);
            }
            result.put(entry.getKey(), indexed);
        }
        return result;
    }

    private String key(String name, String worldName) {
        if (plugin.settings().perWorldHomes && worldName != null) {
            return worldName.toLowerCase(Locale.ENGLISH) + "/" + normalize(name);
        }
        return normalize(name);
    }

    private String normalize(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ENGLISH);
    }

    private static final Comparator<Home> HOME_COMPARATOR = new Comparator<Home>() {
        public int compare(Home first, Home second) {
            int name = first.getName().compareToIgnoreCase(second.getName());
            if (name != 0) {
                return name;
            }
            return first.getWorldName().compareToIgnoreCase(second.getWorldName());
        }
    };

    public enum SetHomeResultType {
        OK,
        CREATED,
        UPDATED,
        INVALID_NAME,
        NAME_TOO_LONG,
        ALREADY_EXISTS,
        LIMIT_REACHED,
        WORLD_BLOCKED,
        WORLD_PERMISSION,
        INVALID_LOCATION,
        NOT_FOUND
    }

    public enum WorldCheck {
        OK,
        BLOCKED,
        PERMISSION
    }

    public static class NameCheck {
        public final SetHomeResultType type;
        public final String name;

        public NameCheck(SetHomeResultType type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public static class SetHomeResult {
        public final SetHomeResultType type;
        public final Home home;
        public final int limit;

        public SetHomeResult(SetHomeResultType type, Home home, int limit) {
            this.type = type;
            this.home = home;
            this.limit = limit;
        }
    }

    public static class RenameResult {
        public final SetHomeResultType type;
        public final Home home;

        public RenameResult(SetHomeResultType type, Home home) {
            this.type = type;
            this.home = home;
        }
    }
}
