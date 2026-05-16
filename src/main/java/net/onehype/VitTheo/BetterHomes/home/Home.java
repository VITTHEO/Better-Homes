package net.onehype.VitTheo.BetterHomes.home;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Home {
    private final UUID ownerId;
    private String ownerName;
    private String name;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private long createdAt;
    private long updatedAt;
    private boolean publicHome;
    private final Set<UUID> sharedWith;

    public Home(UUID ownerId, String ownerName, String name, Location location) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.sharedWith = new HashSet<UUID>();
        updateLocation(location);
    }

    public Home(UUID ownerId, String ownerName, String name, String worldName,
                double x, double y, double z, float yaw, float pitch) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.sharedWith = new HashSet<UUID>();
        setRawLocation(worldName, x, y, z, yaw, pitch);
    }

    public Home(Home other) {
        this.ownerId = other.ownerId;
        this.ownerName = other.ownerName;
        this.name = other.name;
        this.worldName = other.worldName;
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.yaw = other.yaw;
        this.pitch = other.pitch;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.publicHome = other.publicHome;
        this.sharedWith = new HashSet<UUID>(other.sharedWith);
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        touch();
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPublicHome() {
        return publicHome;
    }

    public void setPublicHome(boolean publicHome) {
        this.publicHome = publicHome;
        touch();
    }

    public Set<UUID> getSharedWith() {
        return Collections.unmodifiableSet(sharedWith);
    }

    public void setSharedWith(Set<UUID> values) {
        sharedWith.clear();
        if (values != null) {
            sharedWith.addAll(values);
        }
        touch();
    }

    public boolean isSharedWith(UUID playerId) {
        return sharedWith.contains(playerId);
    }

    public void shareWith(UUID playerId) {
        sharedWith.add(playerId);
        touch();
    }

    public void unshareWith(UUID playerId) {
        sharedWith.remove(playerId);
        touch();
    }

    public void updateLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        setRawLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public void setRawLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        touch();
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }
}
