package net.onehype.VitTheo.BetterHomes.listener;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GuiListener implements Listener {
    private final BetterHomes plugin;

    public GuiListener(BetterHomes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.guiManager().handleClick(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof net.onehype.VitTheo.BetterHomes.gui.HomeGuiHolder) {
            event.setCancelled(true);
        }
    }
}
