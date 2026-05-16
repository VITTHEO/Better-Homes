package net.onehype.VitTheo.BetterHomes.listener;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeleportListener implements Listener {
    private final BetterHomes plugin;

    public TeleportListener(BetterHomes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        plugin.teleportManager().handleMove(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        plugin.teleportManager().handleDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        plugin.teleportManager().handleCombat(event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.teleportManager().cancelPending(event.getPlayer().getUniqueId(), null);
    }
}
