package net.onehype.VitTheo.BetterHomes.command;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import net.onehype.VitTheo.BetterHomes.home.HomeManager;
import net.onehype.VitTheo.BetterHomes.util.HomeMessages;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import net.onehype.VitTheo.BetterHomes.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BetterHomesCommand implements TabExecutor {
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "info", "sethomeplayer",
            "delhomeplayer", "listhomes", "clearhomes", "givehome", "rename", "move", "share", "unshare", "public");

    private final BetterHomes plugin;

    public BetterHomesCommand(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "info".equalsIgnoreCase(args[0])) {
            return info(sender);
        }

        String sub = args[0].toLowerCase();
        if ("reload".equals(sub)) {
            return reload(sender);
        }
        if ("sethomeplayer".equals(sub) || "givehome".equals(sub)) {
            return setHomePlayer(sender, args, "/" + label + " " + sub + " <player> <name>");
        }
        if ("delhomeplayer".equals(sub)) {
            return delHomePlayer(sender, args, "/" + label + " delhomeplayer <player> <name>");
        }
        if ("listhomes".equals(sub)) {
            return listHomes(sender, args, "/" + label + " listhomes <player> [page]");
        }
        if ("clearhomes".equals(sub)) {
            return clearHomes(sender, args, "/" + label + " clearhomes <player>");
        }
        if ("rename".equals(sub)) {
            return rename(sender, args, "/" + label + " rename <old> <new>");
        }
        if ("move".equals(sub)) {
            return move(sender, args, "/" + label + " move <home>");
        }
        if ("share".equals(sub) || "unshare".equals(sub)) {
            return share(sender, args, "/" + label + " " + sub + " <home> <player>", "share".equals(sub));
        }
        if ("public".equals(sub)) {
            return publicHome(sender, args, "/" + label + " public <home> <on|off>");
        }

        plugin.messages().send(sender, "general.usage",
                MessageService.placeholders("usage", "/" + label + " <" + joinSubcommands() + ">"));
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!CommandUtils.has(plugin, sender, PermissionNodes.RELOAD)) {
            return true;
        }
        plugin.reloadPlugin();
        plugin.messages().send(sender, "general.reload");
        return true;
    }

    private boolean info(CommandSender sender) {
        if (!CommandUtils.has(plugin, sender, PermissionNodes.ADMIN)) {
            return true;
        }
        plugin.messages().sendList(sender, "admin.info", MessageService.placeholders(
                "version", plugin.getDescription().getVersion(),
                "players", String.valueOf(plugin.homeManager().loadedPlayerCount()),
                "homes", String.valueOf(plugin.homeManager().loadedHomeCount())));
        return true;
    }

    private boolean setHomePlayer(CommandSender sender, String[] args, String usage) {
        if (!CommandUtils.has(plugin, sender, PermissionNodes.OTHERS)) {
            return true;
        }
        if (args.length < 3) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID targetId = target.getUniqueId();
        if (targetId == null) {
            plugin.messages().send(sender, "general.unknown-player", MessageService.placeholders("player", args[1]));
            return true;
        }

        Location location = null;
        Player permissionSource = sender instanceof Player ? (Player) sender : null;
        if (permissionSource != null) {
            location = permissionSource.getLocation();
        } else {
            Player onlineTarget = Bukkit.getPlayer(args[1]);
            if (onlineTarget != null) {
                location = onlineTarget.getLocation();
            }
        }
        if (location == null) {
            plugin.messages().send(sender, "general.player-only");
            return true;
        }

        String ownerName = target.getName() == null ? args[1] : target.getName();
        HomeManager.SetHomeResult result = plugin.homeManager()
                .setHome(targetId, ownerName, location, permissionSource, args[2], true, true);
        HomeMessages.sendSetResult(plugin, sender, result);
        return true;
    }

    private boolean delHomePlayer(CommandSender sender, String[] args, String usage) {
        if (!CommandUtils.has(plugin, sender, PermissionNodes.OTHERS)) {
            return true;
        }
        if (args.length < 3) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }
        UUID ownerId = resolveOwnerId(args[1]);
        if (ownerId == null) {
            plugin.messages().send(sender, "general.unknown-player", MessageService.placeholders("player", args[1]));
            return true;
        }
        boolean deleted = plugin.homeManager().deleteHome(ownerId, args[2], null);
        if (deleted) {
            plugin.messages().send(sender, "homes.deleted", MessageService.placeholders("home", args[2]));
        } else {
            HomeMessages.sendHomeNotFound(plugin, sender, args[2]);
        }
        return true;
    }

    private boolean listHomes(CommandSender sender, String[] args, String usage) {
        if (!CommandUtils.has(plugin, sender, PermissionNodes.OTHERS)) {
            return true;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }
        UUID ownerId = resolveOwnerId(args[1]);
        if (ownerId == null) {
            plugin.messages().send(sender, "general.unknown-player", MessageService.placeholders("player", args[1]));
            return true;
        }
        CommandUtils.sendHomeList(plugin, sender, args[1], plugin.homeManager().getHomes(ownerId),
                CommandUtils.page(args, 2));
        return true;
    }

    private boolean clearHomes(CommandSender sender, String[] args, String usage) {
        if (!CommandUtils.has(plugin, sender, PermissionNodes.OTHERS)) {
            return true;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }
        UUID ownerId = resolveOwnerId(args[1]);
        if (ownerId == null) {
            plugin.messages().send(sender, "general.unknown-player", MessageService.placeholders("player", args[1]));
            return true;
        }
        plugin.homeManager().clearHomes(ownerId);
        plugin.messages().send(sender, "homes.cleared", MessageService.placeholders("player", args[1]));
        return true;
    }

    private boolean rename(CommandSender sender, String[] args, String usage) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null || !CommandUtils.has(plugin, sender, PermissionNodes.RENAME)) {
            return true;
        }
        if (args.length < 3) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }
        HomeManager.RenameResult result = plugin.homeManager()
                .renameHome(player.getUniqueId(), args[1], player.getWorld().getName(), args[2]);
        HomeMessages.sendRenameResult(plugin, sender, args[1], result);
        return true;
    }

    private boolean move(CommandSender sender, String[] args, String usage) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null || !CommandUtils.has(plugin, sender, PermissionNodes.SET_HOME)) {
            return true;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }
        HomeManager.WorldCheck worldCheck = plugin.homeManager().checkWorld(player, player.getWorld().getName());
        if (worldCheck == HomeManager.WorldCheck.BLOCKED) {
            plugin.messages().send(player, "teleport.world-blocked",
                    MessageService.placeholders("world", player.getWorld().getName()));
            return true;
        }
        if (worldCheck == HomeManager.WorldCheck.PERMISSION) {
            plugin.messages().send(player, "teleport.world-permission",
                    MessageService.placeholders("world", player.getWorld().getName()));
            return true;
        }
        boolean moved = plugin.homeManager().moveHome(player.getUniqueId(), args[1], player.getWorld().getName(), player.getLocation());
        if (moved) {
            plugin.messages().send(player, "homes.moved", MessageService.placeholders("home", args[1]));
        } else {
            HomeMessages.sendHomeNotFound(plugin, player, args[1]);
        }
        return true;
    }

    private boolean share(CommandSender sender, String[] args, String usage, boolean share) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null) {
            return true;
        }
        if (!plugin.settings().sharedHomesEnabled || !plugin.settings().allowOwnerShareCommand) {
            plugin.messages().send(player, "general.no-permission");
            return true;
        }
        if (args.length < 3) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        if (target.getUniqueId() == null) {
            plugin.messages().send(sender, "general.unknown-player", MessageService.placeholders("player", args[2]));
            return true;
        }
        boolean success = share
                ? plugin.homeManager().share(player.getUniqueId(), args[1], player.getWorld().getName(), target.getUniqueId())
                : plugin.homeManager().unshare(player.getUniqueId(), args[1], player.getWorld().getName(), target.getUniqueId());
        if (!success) {
            HomeMessages.sendHomeNotFound(plugin, player, args[1]);
            return true;
        }
        plugin.messages().send(player, share ? "homes.shared" : "homes.unshared",
                MessageService.placeholders("home", args[1], "player", target.getName() == null ? args[2] : target.getName()));
        return true;
    }

    private boolean publicHome(CommandSender sender, String[] args, String usage) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null) {
            return true;
        }
        if (!plugin.settings().publicHomesEnabled || !plugin.settings().allowOwnerPublicToggle) {
            plugin.messages().send(player, "general.no-permission");
            return true;
        }
        if (args.length < 3) {
            plugin.messages().send(sender, "general.usage", MessageService.placeholders("usage", usage));
            return true;
        }
        boolean state = "on".equalsIgnoreCase(args[2]) || "true".equalsIgnoreCase(args[2]) || "yes".equalsIgnoreCase(args[2]);
        boolean success = plugin.homeManager().setPublic(player.getUniqueId(), args[1], player.getWorld().getName(), state);
        if (!success) {
            HomeMessages.sendHomeNotFound(plugin, player, args[1]);
            return true;
        }
        plugin.messages().send(player, state ? "homes.public-enabled" : "homes.public-disabled",
                MessageService.placeholders("home", args[1]));
        return true;
    }

    private UUID resolveOwnerId(String playerName) {
        UUID loaded = plugin.homeManager().findOwnerIdByName(playerName);
        if (loaded != null) {
            return loaded;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
        return offline == null ? null : offline.getUniqueId();
    }

    private String joinSubcommands() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < SUBCOMMANDS.size(); i++) {
            if (i > 0) {
                builder.append("|");
            }
            builder.append(SUBCOMMANDS.get(i));
        }
        return builder.toString();
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<String>();
        if (args.length == 1) {
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }
        String sub = args[0].toLowerCase();
        if (args.length == 2 && ("sethomeplayer".equals(sub) || "delhomeplayer".equals(sub)
                || "listhomes".equals(sub) || "clearhomes".equals(sub) || "givehome".equals(sub))) {
            return CommandUtils.onlineNames(plugin, args[1]);
        }
        if (sender instanceof Player && args.length == 2 && ("rename".equals(sub) || "move".equals(sub)
                || "share".equals(sub) || "unshare".equals(sub) || "public".equals(sub))) {
            Player player = (Player) sender;
            for (Home home : plugin.homeManager().getHomes(player.getUniqueId())) {
                if (home.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    suggestions.add(home.getName());
                }
            }
            return suggestions;
        }
        if (args.length == 3 && ("share".equals(sub) || "unshare".equals(sub))) {
            return CommandUtils.onlineNames(plugin, args[2]);
        }
        if (args.length == 3 && "public".equals(sub)) {
            if ("on".startsWith(args[2].toLowerCase())) {
                suggestions.add("on");
            }
            if ("off".startsWith(args[2].toLowerCase())) {
                suggestions.add("off");
            }
        }
        return suggestions;
    }
}
