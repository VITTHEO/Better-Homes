package net.onehype.VitTheo.BetterHomes.gui;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import net.onehype.VitTheo.BetterHomes.home.HomeManager;
import net.onehype.VitTheo.BetterHomes.util.HomeMessages;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import net.onehype.VitTheo.BetterHomes.util.PermissionUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NamePromptManager {
    private final BetterHomes plugin;
    private final Map<UUID, Prompt> prompts = new HashMap<UUID, Prompt>();

    public NamePromptManager(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public void beginCreate(Player player) {
        prompts.put(player.getUniqueId(), new Prompt(PromptType.CREATE, null, null));
        player.closeInventory();
        plugin.messages().send(player, "gui.create-prompt");
    }

    public void beginRename(Player player, Home home) {
        prompts.put(player.getUniqueId(), new Prompt(PromptType.RENAME, home.getName(), home.getWorldName()));
        player.closeInventory();
        plugin.messages().send(player, "gui.rename-prompt");
    }

    public boolean hasPrompt(UUID playerId) {
        return prompts.containsKey(playerId);
    }

    public void cancel(Player player) {
        prompts.remove(player.getUniqueId());
        plugin.messages().send(player, "gui.prompt-cancelled");
    }

    public void clear(UUID playerId) {
        prompts.remove(playerId);
    }

    public void handleInput(final Player player, final String input) {
        final Prompt prompt = prompts.remove(player.getUniqueId());
        if (prompt == null) {
            return;
        }

        if ("cancel".equalsIgnoreCase(input)) {
            plugin.messages().send(player, "gui.prompt-cancelled");
            return;
        }

        if (prompt.type == PromptType.CREATE) {
            HomeManager.SetHomeResult result = plugin.homeManager().setHome(player, input, false, false);
            HomeMessages.sendSetResult(plugin, player, result);
            if (result.type == HomeManager.SetHomeResultType.CREATED || result.type == HomeManager.SetHomeResultType.UPDATED) {
                plugin.compat().playSound(player, "sounds.success");
                plugin.guiManager().openMain(player, 1);
            } else {
                plugin.compat().playSound(player, "sounds.error");
            }
            return;
        }

        if (prompt.type == PromptType.RENAME) {
            if (!PermissionUtil.has(player, PermissionNodes.RENAME)) {
                plugin.messages().send(player, "general.no-permission");
                return;
            }
            HomeManager.RenameResult result = plugin.homeManager()
                    .renameHome(player.getUniqueId(), prompt.homeName, prompt.worldName, input);
            HomeMessages.sendRenameResult(plugin, player, prompt.homeName, result);
            if (result.type == HomeManager.SetHomeResultType.UPDATED) {
                plugin.compat().playSound(player, "sounds.success");
                Home renamed = plugin.homeManager().getHome(player.getUniqueId(), result.home.getName(), result.home.getWorldName());
                if (renamed != null) {
                    plugin.guiManager().openEdit(player, renamed);
                }
            } else {
                plugin.compat().playSound(player, "sounds.error");
            }
        }
    }

    private enum PromptType {
        CREATE,
        RENAME
    }

    private static class Prompt {
        private final PromptType type;
        private final String homeName;
        private final String worldName;

        private Prompt(PromptType type, String homeName, String worldName) {
            this.type = type;
            this.homeName = homeName;
            this.worldName = worldName;
        }
    }
}
