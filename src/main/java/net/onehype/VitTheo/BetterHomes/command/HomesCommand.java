package net.onehype.VitTheo.BetterHomes.command;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class HomesCommand implements TabExecutor {
    private final BetterHomes plugin;

    public HomesCommand(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null || !CommandUtils.has(plugin, sender, PermissionNodes.USE)) {
            return true;
        }
        int page = CommandUtils.page(args, 0);
        List<Home> homes = plugin.homeManager().getHomes(player.getUniqueId());

        if (plugin.settings().homesCommandShowsChatList && plugin.settings().chatListingEnabled()) {
            if (CommandUtils.has(plugin, sender, PermissionNodes.LIST)) {
                CommandUtils.sendHomeList(plugin, player, player.getName(), homes, page);
            }
        }
        if (plugin.settings().homesCommandOpensGui && plugin.settings().guiListingEnabled()) {
            if (CommandUtils.has(plugin, sender, PermissionNodes.GUI)) {
                plugin.guiManager().openMain(player, page);
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
