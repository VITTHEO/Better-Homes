package net.onehype.VitTheo.BetterHomes.listener;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PromptListener implements Listener {
    private final BetterHomes plugin;

    public PromptListener(BetterHomes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (!plugin.promptManager().hasPrompt(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        final String message = event.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            public void run() {
                plugin.promptManager().handleInput(player, message);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.promptManager().clear(event.getPlayer().getUniqueId());
    }
}
