package net.onehype.VitTheo.BetterHomes.command;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CommandUtils {
    private CommandUtils() {
    }

    public static Player requirePlayer(BetterHomes plugin, CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.messages().send(sender, "general.player-only");
            return null;
        }
        return (Player) sender;
    }

    public static boolean has(BetterHomes plugin, CommandSender sender, String permission) {
        if (sender.hasPermission(permission) || sender.hasPermission(PermissionNodes.ADMIN)) {
            return true;
        }
        plugin.messages().send(sender, "general.no-permission");
        return false;
    }

    public static String homeName(BetterHomes plugin, String[] args, int index) {
        if (args.length <= index || args[index].trim().length() == 0) {
            return plugin.settings().defaultHomeName;
        }
        return args[index].trim();
    }

    public static int page(String[] args, int index) {
        if (args.length <= index) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(args[index]));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    public static void sendHomeList(BetterHomes plugin, CommandSender sender, String ownerName, List<Home> homes, int page) {
        if (homes.isEmpty()) {
            plugin.messages().send(sender, "homes.no-homes");
            return;
        }
        int perPage = plugin.settings().entriesPerChatPage;
        int pages = Math.max(1, (int) Math.ceil(homes.size() / (double) perPage));
        page = Math.max(1, Math.min(page, pages));

        plugin.messages().send(sender, "homes.list-header",
                MessageService.placeholders("player", ownerName, "page", String.valueOf(page), "pages", String.valueOf(pages)));

        int start = (page - 1) * perPage;
        for (int i = start; i < start + perPage && i < homes.size(); i++) {
            Home home = homes.get(i);
            Map<String, String> placeholders = MessageService.placeholders(
                    "home", home.getName(),
                    "world", home.getWorldName(),
                    "x", String.valueOf((int) Math.floor(home.getX())),
                    "y", String.valueOf((int) Math.floor(home.getY())),
                    "z", String.valueOf((int) Math.floor(home.getZ())));
            sender.sendMessage(plugin.messages().format(plugin.configManager().messages().getString("homes.list-entry", ""), placeholders));
        }
        plugin.messages().send(sender, "homes.list-footer");
    }

    public static List<String> onlineNames(BetterHomes plugin, String prefix) {
        List<String> names = new ArrayList<String>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (prefix == null || player.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                names.add(player.getName());
            }
        }
        return names;
    }
}
