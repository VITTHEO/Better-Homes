package net.onehype.VitTheo.BetterHomes.teleport;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SafeLocationFinder {
    private final BetterHomes plugin;

    public SafeLocationFinder(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public Location findSafe(Location original) {
        if (original == null || original.getWorld() == null) {
            return null;
        }
        if (isSafe(original)) {
            return centered(original);
        }

        World world = original.getWorld();
        int baseX = original.getBlockX();
        int baseY = original.getBlockY();
        int baseZ = original.getBlockZ();
        int horizontal = plugin.settings().safeSearchHorizontal;
        int vertical = plugin.settings().safeSearchVertical;

        for (int dy = -vertical; dy <= vertical; dy++) {
            for (int radius = 0; radius <= horizontal; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                            continue;
                        }
                        Location candidate = new Location(world, baseX + dx + 0.5D, baseY + dy, baseZ + dz + 0.5D,
                                original.getYaw(), original.getPitch());
                        if (isSafe(candidate)) {
                            return centered(candidate);
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean isSafe(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        World world = location.getWorld();
        int y = location.getBlockY();
        if (y < 1 || y >= world.getMaxHeight() - 1) {
            return false;
        }

        Block feet = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
        Block head = world.getBlockAt(location.getBlockX(), y + 1, location.getBlockZ());
        Block floor = world.getBlockAt(location.getBlockX(), y - 1, location.getBlockZ());
        return isPassable(feet.getType()) && isPassable(head.getType()) && isFloorSafe(floor.getType());
    }

    private Location centered(Location location) {
        if (!plugin.settings().centerOnBlock) {
            return location;
        }
        return new Location(location.getWorld(), location.getBlockX() + 0.5D, location.getY(),
                location.getBlockZ() + 0.5D, location.getYaw(), location.getPitch());
    }

    private boolean isPassable(Material material) {
        if (material == null) {
            return false;
        }
        return !material.isSolid() && !isDangerous(material);
    }

    private boolean isFloorSafe(Material material) {
        if (material == null) {
            return false;
        }
        return material.isSolid() && !isDangerous(material);
    }

    private boolean isDangerous(Material material) {
        String name = material.name();
        return name.contains("LAVA")
                || name.contains("FIRE")
                || name.contains("CACTUS")
                || name.contains("MAGMA")
                || name.contains("CAMPFIRE")
                || name.contains("SWEET_BERRY")
                || name.contains("POWDER_SNOW");
    }
}
