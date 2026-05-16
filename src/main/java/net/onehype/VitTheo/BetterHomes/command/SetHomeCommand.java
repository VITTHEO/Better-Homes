package net.onehype.VitTheo.BetterHomes.command;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.HomeManager;
import net.onehype.VitTheo.BetterHomes.util.HomeMessages;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetHomeCommand implements TabExecutor {
    private final BetterHomes plugin;

    public SetHomeCommand(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.requirePlayer(plugin, sender);
        if (player == null || !CommandUtils.has(plugin, sender, PermissionNodes.USE)
                || !CommandUtils.has(plugin, sender, PermissionNodes.SET_HOME)) {
            return true;
        }

        String name = CommandUtils.homeName(plugin, args, 0);
        HomeManager.SetHomeResult result = plugin.homeManager().setHome(player, name, false, false);
        HomeMessages.sendSetResult(plugin, player, result);
        plugin.compat().playSound(player, result.type == HomeManager.SetHomeResultType.CREATED
                || result.type == HomeManager.SetHomeResultType.UPDATED ? "sounds.success" : "sounds.error");
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
