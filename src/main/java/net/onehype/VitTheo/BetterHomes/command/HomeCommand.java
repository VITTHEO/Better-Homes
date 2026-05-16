package net.onehype.VitTheo.BetterHomes.command;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import net.onehype.VitTheo.BetterHomes.util.HomeMessages;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeCommand implements TabExecutor {
    private final BetterHomes plugin;

    public HomeCommand(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null || !CommandUtils.has(plugin, sender, PermissionNodes.USE)
                || !CommandUtils.has(plugin, sender, PermissionNodes.HOME)) {
            return true;
        }

        Home home;
        if (args.length == 0) {
            home = plugin.homeManager().getHome(player.getUniqueId(), plugin.settings().defaultHomeName, player.getWorld().getName());
            if (home == null && plugin.settings().homeWithoutNameUsesFirstIfOnlyOne) {
                home = plugin.homeManager().getFirstHomeIfOnlyOne(player.getUniqueId());
            }
        } else if (args[0].contains(":")) {
            home = resolveForeignHome(player, args[0]);
            if (home == null) {
                return true;
            }
        } else {
            home = plugin.homeManager().getHome(player.getUniqueId(), args[0], player.getWorld().getName());
        }

        if (home == null) {
            HomeMessages.sendHomeNotFound(plugin, player, args.length == 0 ? plugin.settings().defaultHomeName : args[0]);
            plugin.compat().playSound(player, "sounds.error");
            return true;
        }
        plugin.teleportManager().requestTeleport(player, home);
        return true;
    }

    private Home resolveForeignHome(Player player, String spec) {
        String[] parts = spec.split(":", 2);
        if (parts.length != 2 || parts[0].length() == 0 || parts[1].length() == 0) {
            plugin.messages().send(player, "general.usage", MessageService.placeholders("usage", "/home <player>:<home>"));
            return null;
        }
        UUID ownerId = plugin.homeManager().findOwnerIdByName(parts[0]);
        if (ownerId == null) {
            plugin.messages().send(player, "general.unknown-player", MessageService.placeholders("player", parts[0]));
            plugin.compat().playSound(player, "sounds.error");
            return null;
        }
        Home home = plugin.homeManager().getHome(ownerId, parts[1], player.getWorld().getName());
        if (home == null) {
            HomeMessages.sendHomeNotFound(plugin, player, parts[1]);
            plugin.compat().playSound(player, "sounds.error");
            return null;
        }
        if (!plugin.homeManager().canAccess(player, home)) {
            plugin.messages().send(player, "homes.no-access");
            plugin.compat().playSound(player, "sounds.error");
            return null;
        }
        return home;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<String>();
        if (!(sender instanceof Player) || args.length != 1) {
            return suggestions;
        }
        Player player = (Player) sender;
        for (Home home : plugin.homeManager().getHomes(player.getUniqueId())) {
            if (home.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                suggestions.add(home.getName());
            }
        }
        return suggestions;
    }
}
