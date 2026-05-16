package net.onehype.VitTheo.BetterHomes.command;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.util.HomeMessages;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DelHomeCommand implements TabExecutor {
    private final BetterHomes plugin;
    private final Map<UUID, String> confirmations = new HashMap<UUID, String>();

    public DelHomeCommand(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null || !CommandUtils.has(plugin, sender, PermissionNodes.USE)
                || !CommandUtils.has(plugin, sender, PermissionNodes.DELETE_HOME)) {
            return true;
        }

        String name = CommandUtils.homeName(plugin, args, 0);
        boolean confirmed = args.length >= 2 && "confirm".equalsIgnoreCase(args[1]);
        String key = name.toLowerCase(Locale.ENGLISH);
        if (plugin.settings().delhomeRequiresConfirmation && !confirmed) {
            confirmations.put(player.getUniqueId(), key);
            plugin.messages().send(player, "homes.delete-confirm", MessageService.placeholders("home", name));
            return true;
        }
        if (plugin.settings().delhomeRequiresConfirmation && !key.equals(confirmations.get(player.getUniqueId()))) {
            plugin.messages().send(player, "homes.delete-confirm", MessageService.placeholders("home", name));
            confirmations.put(player.getUniqueId(), key);
            return true;
        }
        confirmations.remove(player.getUniqueId());

        boolean deleted = plugin.homeManager().deleteHome(player.getUniqueId(), name, player.getWorld().getName());
        if (deleted) {
            plugin.messages().send(player, "homes.deleted", MessageService.placeholders("home", name));
            plugin.compat().playSound(player, "sounds.success");
        } else {
            HomeMessages.sendHomeNotFound(plugin, player, name);
            plugin.compat().playSound(player, "sounds.error");
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<String>();
        if (!(sender instanceof Player) || args.length != 1) {
            return suggestions;
        }
        Player player = (Player) sender;
        for (net.onehype.VitTheo.BetterHomes.home.Home home : plugin.homeManager().getHomes(player.getUniqueId())) {
            if (home.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                suggestions.add(home.getName());
            }
        }
        return suggestions;
    }
}
