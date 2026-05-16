package net.onehype.VitTheo.BetterHomes.teleport;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import net.onehype.VitTheo.BetterHomes.home.HomeManager;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import net.onehype.VitTheo.BetterHomes.util.PermissionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private final BetterHomes plugin;
    private final SafeLocationFinder safeLocationFinder;
    private final Map<UUID, PendingTeleport> pending = new HashMap<UUID, PendingTeleport>();
    private final Map<UUID, Long> cooldownUntil = new HashMap<UUID, Long>();
    private final Map<UUID, Long> combatUntil = new HashMap<UUID, Long>();

    public TeleportManager(BetterHomes plugin, SafeLocationFinder safeLocationFinder) {
        this.plugin = plugin;
        this.safeLocationFinder = safeLocationFinder;
    }

    public void requestTeleport(final Player player, final Home home) {
        if (player == null || home == null) {
            return;
        }

        Location target = home.toLocation();
        if (target == null || target.getWorld() == null) {
            plugin.messages().send(player, "teleport.world-missing",
                    MessageService.placeholders("world", home.getWorldName()));
            plugin.compat().playSound(player, "sounds.error");
            return;
        }

        HomeManager.WorldCheck worldCheck = plugin.homeManager().checkWorld(player, target.getWorld().getName());
        if (worldCheck == HomeManager.WorldCheck.BLOCKED) {
            plugin.messages().send(player, "teleport.world-blocked",
                    MessageService.placeholders("world", target.getWorld().getName()));
            plugin.compat().playSound(player, "sounds.error");
            return;
        }
        if (worldCheck == HomeManager.WorldCheck.PERMISSION) {
            plugin.messages().send(player, "teleport.world-permission",
                    MessageService.placeholders("world", target.getWorld().getName()));
            plugin.compat().playSound(player, "sounds.error");
            return;
        }

        if (isCoolingDown(player)) {
            long seconds = Math.max(1L, (cooldownUntil.get(player.getUniqueId()) - System.currentTimeMillis() + 999L) / 1000L);
            plugin.messages().send(player, "teleport.cooldown",
                    MessageService.placeholders("seconds", String.valueOf(seconds)));
            plugin.compat().playSound(player, "sounds.error");
            return;
        }

        if (plugin.settings().cancelOnCombat && isInCombat(player)) {
            plugin.messages().send(player, "teleport.cancelled-combat");
            plugin.compat().playSound(player, "sounds.error");
            return;
        }

        int delay = plugin.settings().teleportDelaySeconds;
        if (delay <= 0 || PermissionUtil.has(player, PermissionNodes.TELEPORT_INSTANT)) {
            performTeleport(player, home);
            return;
        }

        cancelPending(player.getUniqueId(), null);
        plugin.messages().send(player, "teleport.starting",
                MessageService.placeholders("home", home.getName(), "seconds", String.valueOf(delay)));
        if (plugin.settings().warmupTitle) {
            plugin.compat().sendTitle(player, "&a" + home.getName(), "&7Teleport in " + delay + "s", 5, 35, 10);
        }
        plugin.compat().playSound(player, "sounds.click");

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                pending.remove(player.getUniqueId());
                if (!player.isOnline()) {
                    return;
                }
                if (plugin.settings().cancelOnCombat && isInCombat(player)) {
                    plugin.messages().send(player, "teleport.cancelled-combat");
                    plugin.compat().playSound(player, "sounds.error");
                    return;
                }
                performTeleport(player, home);
            }
        }, delay * 20L);
        pending.put(player.getUniqueId(), new PendingTeleport(home, player.getLocation(), task));
    }

    public void handleMove(PlayerMoveEvent event) {
        if (!plugin.settings().cancelOnMove || event.getTo() == null) {
            return;
        }
        PendingTeleport teleport = pending.get(event.getPlayer().getUniqueId());
        if (teleport == null) {
            return;
        }
        Location from = teleport.getOrigin();
        Location to = event.getTo();
        if (from.getWorld() != null && to.getWorld() != null
                && from.getWorld().equals(to.getWorld())
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        cancelPending(event.getPlayer().getUniqueId(), "teleport.cancelled-move");
    }

    public void handleDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (plugin.settings().cancelOnDamage && pending.containsKey(player.getUniqueId())) {
            cancelPending(player.getUniqueId(), "teleport.cancelled-damage");
        }
    }

    public void handleCombat(EntityDamageByEntityEvent event) {
        if (!plugin.settings().cancelOnCombat || event.isCancelled()) {
            return;
        }
        Player victim = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        Player attacker = playerFromDamager(event.getDamager());
        if (victim != null) {
            tagCombat(victim);
            if (pending.containsKey(victim.getUniqueId())) {
                cancelPending(victim.getUniqueId(), "teleport.cancelled-combat");
            }
        }
        if (attacker != null) {
            tagCombat(attacker);
            if (pending.containsKey(attacker.getUniqueId())) {
                cancelPending(attacker.getUniqueId(), "teleport.cancelled-combat");
            }
        }
    }

    public void cancelPending(UUID playerId, String messagePath) {
        PendingTeleport teleport = pending.remove(playerId);
        if (teleport == null) {
            return;
        }
        teleport.getTask().cancel();
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && messagePath != null) {
            plugin.messages().send(player, messagePath);
            plugin.compat().playSound(player, "sounds.error");
        }
    }

    public void cancelAll() {
        for (UUID uuid : new HashMap<UUID, PendingTeleport>(pending).keySet()) {
            cancelPending(uuid, null);
        }
    }

    public boolean hasPending(UUID playerId) {
        return pending.containsKey(playerId);
    }

    private void performTeleport(Player player, Home home) {
        Location target = home.toLocation();
        if (target == null) {
            plugin.messages().send(player, "teleport.world-missing",
                    MessageService.placeholders("world", home.getWorldName()));
            return;
        }

        if (plugin.settings().safeTeleport && PermissionUtil.has(player, PermissionNodes.TELEPORT_SAFELY)) {
            target = safeLocationFinder.findSafe(target);
            if (target == null) {
                plugin.messages().send(player, "teleport.unsafe");
                plugin.compat().playSound(player, "sounds.error");
                return;
            }
        }

        if (player.teleport(target)) {
            if (!PermissionUtil.has(player, PermissionNodes.BYPASS_COOLDOWN)
                    && plugin.settings().teleportCooldownSeconds > 0) {
                cooldownUntil.put(player.getUniqueId(),
                        System.currentTimeMillis() + plugin.settings().teleportCooldownSeconds * 1000L);
            }
            plugin.compat().playEffect(target, "effects.teleport");
            plugin.compat().playSound(player, "sounds.teleport");
            plugin.messages().send(player, "teleport.success",
                    MessageService.placeholders("home", home.getName()));
        } else {
            plugin.messages().send(player, "general.error");
        }
    }

    private boolean isCoolingDown(Player player) {
        if (PermissionUtil.has(player, PermissionNodes.BYPASS_COOLDOWN)) {
            return false;
        }
        Long until = cooldownUntil.get(player.getUniqueId());
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            cooldownUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private void tagCombat(Player player) {
        combatUntil.put(player.getUniqueId(), System.currentTimeMillis() + plugin.settings().combatTagSeconds * 1000L);
    }

    private boolean isInCombat(Player player) {
        Long until = combatUntil.get(player.getUniqueId());
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            combatUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private Player playerFromDamager(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            Object shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }
}
