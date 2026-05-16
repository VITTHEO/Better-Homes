package net.onehype.VitTheo.BetterHomes.gui;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.home.Home;
import net.onehype.VitTheo.BetterHomes.home.HomeManager;
import net.onehype.VitTheo.BetterHomes.util.HomeMessages;
import net.onehype.VitTheo.BetterHomes.util.ItemBuilder;
import net.onehype.VitTheo.BetterHomes.util.MessageService;
import net.onehype.VitTheo.BetterHomes.util.PermissionNodes;
import net.onehype.VitTheo.BetterHomes.util.PermissionUtil;
import net.onehype.VitTheo.BetterHomes.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeGuiManager {
    private final BetterHomes plugin;
    private final ItemBuilder itemBuilder;

    public HomeGuiManager(BetterHomes plugin) {
        this.plugin = plugin;
        this.itemBuilder = new ItemBuilder(plugin);
    }

    public void openMain(Player player, int requestedPage) {
        if (!plugin.settings().guiEnabled) {
            plugin.messages().send(player, "gui.disabled");
            return;
        }
        if (!PermissionUtil.has(player, PermissionNodes.GUI)) {
            plugin.messages().send(player, "general.no-permission");
            return;
        }

        List<Home> homes = plugin.homeManager().getHomes(player.getUniqueId());
        List<Integer> slots = plugin.getConfig().getIntegerList("gui.home-slots");
        int perPage = Math.max(1, slots.size());
        int totalPages = Math.max(1, (int) Math.ceil(homes.size() / (double) perPage));
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        HomeGuiHolder holder = new HomeGuiHolder(GuiType.MAIN, page, null, null);
        Inventory inventory = Bukkit.createInventory(holder, plugin.settings().guiRows * 9,
                plugin.compat().inventoryTitle(resolveTitle("gui.title-main",
                        MessageService.placeholders("page", String.valueOf(page), "pages", String.valueOf(totalPages)))));
        holder.setInventory(inventory);
        fill(inventory);

        int start = (page - 1) * perPage;
        for (int i = 0; i < perPage && start + i < homes.size(); i++) {
            int slot = slots.get(i);
            if (slot < 0 || slot >= inventory.getSize()) {
                continue;
            }
            Home home = homes.get(start + i);
            inventory.setItem(slot, itemBuilder.fromConfig("gui.buttons.home", placeholders(home)));
            holder.getHomes().put(slot, home);
        }

        if (page > 1) {
            setButton(holder, inventory, "gui.buttons.previous", "previous");
        }
        if (page < totalPages) {
            setButton(holder, inventory, "gui.buttons.next", "next");
        }
        if (totalPages > 1) {
            setButton(holder, inventory, "gui.buttons.pages", "pages");
        }
        setButton(holder, inventory, "gui.buttons.create", "create");
        setButton(holder, inventory, "gui.buttons.close", "close");
        player.openInventory(inventory);
        plugin.compat().playSound(player, "sounds.open");
    }

    public void openPages(Player player, int currentPage) {
        List<Home> homes = plugin.homeManager().getHomes(player.getUniqueId());
        int perPage = Math.max(1, plugin.getConfig().getIntegerList("gui.home-slots").size());
        int totalPages = Math.max(1, (int) Math.ceil(homes.size() / (double) perPage));

        HomeGuiHolder holder = new HomeGuiHolder(GuiType.PAGES, currentPage, null, null);
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.compat().inventoryTitle(resolveTitle("gui.title-pages", new HashMap<String, String>())));
        holder.setInventory(inventory);
        fill(inventory);

        int[] pageSlots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        for (int i = 0; i < totalPages && i < pageSlots.length; i++) {
            Map<String, String> placeholders = MessageService.placeholders("page", String.valueOf(i + 1));
            inventory.setItem(pageSlots[i], itemBuilder.fromConfig("gui.buttons.page", placeholders));
            holder.getActions().put(pageSlots[i], "page:" + (i + 1));
        }
        setButton(holder, inventory, "gui.buttons.back", "back-main");
        player.openInventory(inventory);
        plugin.compat().playSound(player, "sounds.open");
    }

    public void openEdit(Player player, Home home) {
        if (home == null) {
            return;
        }
        HomeGuiHolder holder = new HomeGuiHolder(GuiType.EDIT, 1, home.getName(), home.getWorldName());
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.compat().inventoryTitle(resolveTitle("gui.title-edit", placeholders(home))));
        holder.setInventory(inventory);
        fill(inventory);
        setButton(holder, inventory, "gui.buttons.teleport", "teleport");
        setButton(holder, inventory, "gui.buttons.move", "move");
        setButton(holder, inventory, "gui.buttons.rename", "rename");
        setButton(holder, inventory, "gui.buttons.public", "public", placeholders(home));
        setButton(holder, inventory, "gui.buttons.delete", "delete");
        setButton(holder, inventory, "gui.buttons.back", "back-main");
        player.openInventory(inventory);
        plugin.compat().playSound(player, "sounds.open");
    }

    public void openConfirmDelete(Player player, Home home) {
        if (home == null) {
            return;
        }
        HomeGuiHolder holder = new HomeGuiHolder(GuiType.CONFIRM_DELETE, 1, home.getName(), home.getWorldName());
        Inventory inventory = Bukkit.createInventory(holder, 27,
                plugin.compat().inventoryTitle(resolveTitle("gui.title-confirm-delete", placeholders(home))));
        holder.setInventory(inventory);
        fill(inventory);
        setButton(holder, inventory, "gui.buttons.confirm", "confirm-delete");
        setButton(holder, inventory, "gui.buttons.cancel", "cancel-delete");
        player.openInventory(inventory);
        plugin.compat().playSound(player, "sounds.open");
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof HomeGuiHolder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getInventory().getSize()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        HomeGuiHolder holder = (HomeGuiHolder) event.getInventory().getHolder();
        plugin.compat().playSound(player, "sounds.click");

        Home clickedHome = holder.getHomes().get(event.getRawSlot());
        if (clickedHome != null) {
            if (event.isShiftClick() && event.isRightClick()) {
                openConfirmDelete(player, clickedHome);
            } else if (event.isRightClick()) {
                openEdit(player, clickedHome);
            } else {
                player.closeInventory();
                plugin.teleportManager().requestTeleport(player, clickedHome);
            }
            return;
        }

        String action = holder.getActions().get(event.getRawSlot());
        if (action == null) {
            return;
        }
        handleAction(player, holder, action);
    }

    private void handleAction(Player player, HomeGuiHolder holder, String action) {
        if ("previous".equals(action)) {
            openMain(player, holder.getPage() - 1);
        } else if ("next".equals(action)) {
            openMain(player, holder.getPage() + 1);
        } else if ("pages".equals(action)) {
            openPages(player, holder.getPage());
        } else if ("create".equals(action)) {
            plugin.promptManager().beginCreate(player);
        } else if ("close".equals(action)) {
            player.closeInventory();
        } else if ("back-main".equals(action)) {
            openMain(player, holder.getPage());
        } else if (action.startsWith("page:")) {
            openMain(player, Integer.parseInt(action.substring("page:".length())));
        } else if ("teleport".equals(action)) {
            Home home = plugin.homeManager().getHome(player.getUniqueId(), holder.getHomeName(), holder.getWorldName());
            player.closeInventory();
            if (home == null) {
                HomeMessages.sendHomeNotFound(plugin, player, holder.getHomeName());
                return;
            }
            plugin.teleportManager().requestTeleport(player, home);
        } else if ("move".equals(action)) {
            moveHome(player, holder);
        } else if ("rename".equals(action)) {
            renameHome(player, holder);
        } else if ("public".equals(action)) {
            togglePublic(player, holder);
        } else if ("delete".equals(action)) {
            Home home = plugin.homeManager().getHome(player.getUniqueId(), holder.getHomeName(), holder.getWorldName());
            if (home == null) {
                HomeMessages.sendHomeNotFound(plugin, player, holder.getHomeName());
                return;
            }
            openConfirmDelete(player, home);
        } else if ("confirm-delete".equals(action)) {
            deleteHome(player, holder);
        } else if ("cancel-delete".equals(action)) {
            Home home = plugin.homeManager().getHome(player.getUniqueId(), holder.getHomeName(), holder.getWorldName());
            if (home == null) {
                openMain(player, 1);
            } else {
                openEdit(player, home);
            }
        }
    }

    private void moveHome(Player player, HomeGuiHolder holder) {
        HomeManager.WorldCheck worldCheck = plugin.homeManager().checkWorld(player, player.getWorld().getName());
        if (worldCheck == HomeManager.WorldCheck.BLOCKED) {
            plugin.messages().send(player, "teleport.world-blocked",
                    MessageService.placeholders("world", player.getWorld().getName()));
            return;
        }
        if (worldCheck == HomeManager.WorldCheck.PERMISSION) {
            plugin.messages().send(player, "teleport.world-permission",
                    MessageService.placeholders("world", player.getWorld().getName()));
            return;
        }
        boolean moved = plugin.homeManager()
                .moveHome(player.getUniqueId(), holder.getHomeName(), holder.getWorldName(), player.getLocation());
        if (moved) {
            plugin.messages().send(player, "homes.moved", MessageService.placeholders("home", holder.getHomeName()));
            plugin.compat().playSound(player, "sounds.success");
            Home home = plugin.homeManager().getHome(player.getUniqueId(), holder.getHomeName(), player.getWorld().getName());
            openEdit(player, home);
        } else {
            HomeMessages.sendHomeNotFound(plugin, player, holder.getHomeName());
            plugin.compat().playSound(player, "sounds.error");
        }
    }

    private void renameHome(Player player, HomeGuiHolder holder) {
        if (!PermissionUtil.has(player, PermissionNodes.RENAME)) {
            plugin.messages().send(player, "general.no-permission");
            return;
        }
        Home home = plugin.homeManager().getHome(player.getUniqueId(), holder.getHomeName(), holder.getWorldName());
        if (home == null) {
            HomeMessages.sendHomeNotFound(plugin, player, holder.getHomeName());
            return;
        }
        plugin.promptManager().beginRename(player, home);
    }

    private void togglePublic(Player player, HomeGuiHolder holder) {
        if (!plugin.settings().publicHomesEnabled || !plugin.settings().allowOwnerPublicToggle) {
            plugin.messages().send(player, "general.no-permission");
            return;
        }
        Home home = plugin.homeManager().getHome(player.getUniqueId(), holder.getHomeName(), holder.getWorldName());
        if (home == null) {
            HomeMessages.sendHomeNotFound(plugin, player, holder.getHomeName());
            return;
        }
        boolean newState = !home.isPublicHome();
        plugin.homeManager().setPublic(player.getUniqueId(), home.getName(), home.getWorldName(), newState);
        plugin.messages().send(player, newState ? "homes.public-enabled" : "homes.public-disabled",
                MessageService.placeholders("home", home.getName()));
        plugin.compat().playSound(player, "sounds.success");
        Home updated = plugin.homeManager().getHome(player.getUniqueId(), home.getName(), home.getWorldName());
        openEdit(player, updated);
    }

    private void deleteHome(Player player, HomeGuiHolder holder) {
        boolean deleted = plugin.homeManager()
                .deleteHome(player.getUniqueId(), holder.getHomeName(), holder.getWorldName());
        if (deleted) {
            plugin.messages().send(player, "homes.deleted", MessageService.placeholders("home", holder.getHomeName()));
            plugin.compat().playSound(player, "sounds.success");
            openMain(player, 1);
        } else {
            HomeMessages.sendHomeNotFound(plugin, player, holder.getHomeName());
            plugin.compat().playSound(player, "sounds.error");
        }
    }

    private void fill(Inventory inventory) {
        if (!plugin.settings().guiFillEmptySlots) {
            return;
        }
        ItemStack filler = itemBuilder.fromConfig("gui.filler");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private void setButton(HomeGuiHolder holder, Inventory inventory, String path, String action) {
        setButton(holder, inventory, path, action, new HashMap<String, String>());
    }

    private void setButton(HomeGuiHolder holder, Inventory inventory, String path, String action,
                           Map<String, String> placeholders) {
        int slot = plugin.getConfig().getInt(path + ".slot", -1);
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        inventory.setItem(slot, itemBuilder.fromConfig(path, placeholders));
        holder.getActions().put(slot, action);
    }

    private Map<String, String> placeholders(Home home) {
        return MessageService.placeholders(
                "home", home.getName(),
                "world", home.getWorldName(),
                "x", String.valueOf((int) Math.floor(home.getX())),
                "y", String.valueOf((int) Math.floor(home.getY())),
                "z", String.valueOf((int) Math.floor(home.getZ())),
                "state", home.isPublicHome() ? "ON" : "OFF");
    }

    private String resolveTitle(String path, Map<String, String> placeholders) {
        return Text.replace(plugin.getConfig().getString(path, ""), placeholders);
    }
}
