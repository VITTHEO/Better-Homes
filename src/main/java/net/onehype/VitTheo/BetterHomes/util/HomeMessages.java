package net.onehype.VitTheo.BetterHomes.util;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.HomeManager;
import org.bukkit.command.CommandSender;

public final class HomeMessages {
    private HomeMessages() {
    }

    public static void sendSetResult(BetterHomes plugin, CommandSender sender, HomeManager.SetHomeResult result) {
        if (result == null) {
            plugin.messages().send(sender, "general.error");
            return;
        }
        if (result.type == HomeManager.SetHomeResultType.CREATED) {
            plugin.messages().send(sender, "homes.created", MessageService.placeholders("home", result.home.getName()));
        } else if (result.type == HomeManager.SetHomeResultType.UPDATED) {
            plugin.messages().send(sender, "homes.updated", MessageService.placeholders("home", result.home.getName()));
        } else if (result.type == HomeManager.SetHomeResultType.ALREADY_EXISTS) {
            plugin.messages().send(sender, "homes.already-exists",
                    MessageService.placeholders("home", result.home == null ? "" : result.home.getName()));
        } else if (result.type == HomeManager.SetHomeResultType.LIMIT_REACHED) {
            plugin.messages().send(sender, "homes.limit-reached",
                    MessageService.placeholders("limit", String.valueOf(result.limit)));
        } else {
            sendNameOrWorldError(plugin, sender, result.type, null);
        }
    }

    public static void sendRenameResult(BetterHomes plugin, CommandSender sender, String oldName,
                                        HomeManager.RenameResult result) {
        if (result.type == HomeManager.SetHomeResultType.UPDATED) {
            plugin.messages().send(sender, "homes.renamed",
                    MessageService.placeholders("old", oldName, "new", result.home.getName()));
            return;
        }
        if (result.type == HomeManager.SetHomeResultType.ALREADY_EXISTS) {
            plugin.messages().send(sender, "homes.already-exists",
                    MessageService.placeholders("home", result.home == null ? "" : result.home.getName()));
            return;
        }
        sendNameOrWorldError(plugin, sender, result.type, oldName);
    }

    public static void sendNameOrWorldError(BetterHomes plugin, CommandSender sender,
                                            HomeManager.SetHomeResultType type, String homeName) {
        if (type == HomeManager.SetHomeResultType.INVALID_NAME) {
            plugin.messages().send(sender, "homes.invalid-name",
                    MessageService.placeholders("allowed", plugin.settings().allowedNameCharactersMessage));
        } else if (type == HomeManager.SetHomeResultType.NAME_TOO_LONG) {
            plugin.messages().send(sender, "homes.name-too-long",
                    MessageService.placeholders("max", String.valueOf(plugin.settings().maxNameLength)));
        } else if (type == HomeManager.SetHomeResultType.WORLD_BLOCKED) {
            String world = sender instanceof org.bukkit.entity.Player
                    ? ((org.bukkit.entity.Player) sender).getWorld().getName() : "";
            plugin.messages().send(sender, "teleport.world-blocked", MessageService.placeholders("world", world));
        } else if (type == HomeManager.SetHomeResultType.WORLD_PERMISSION) {
            String world = sender instanceof org.bukkit.entity.Player
                    ? ((org.bukkit.entity.Player) sender).getWorld().getName() : "";
            plugin.messages().send(sender, "teleport.world-permission", MessageService.placeholders("world", world));
        } else if (type == HomeManager.SetHomeResultType.NOT_FOUND) {
            plugin.messages().send(sender, "homes.not-found",
                    MessageService.placeholders("home", homeName == null ? "" : homeName));
        } else {
            plugin.messages().send(sender, "general.error");
        }
    }

    public static void sendHomeNotFound(BetterHomes plugin, CommandSender sender, String homeName) {
        plugin.messages().send(sender, "homes.not-found",
                MessageService.placeholders("home", homeName == null ? plugin.settings().defaultHomeName : homeName));
    }
}
