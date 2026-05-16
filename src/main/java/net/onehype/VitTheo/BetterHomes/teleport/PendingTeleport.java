package net.onehype.VitTheo.BetterHomes.teleport;

import net.onehype.VitTheo.BetterHomes.home.Home;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public class PendingTeleport {
    private final Home home;
    private final Location origin;
    private final BukkitTask task;

    public PendingTeleport(Home home, Location origin, BukkitTask task) {
        this.home = home;
        this.origin = origin;
        this.task = task;
    }

    public Home getHome() {
        return home;
    }

    public Location getOrigin() {
        return origin;
    }

    public BukkitTask getTask() {
        return task;
    }
}
