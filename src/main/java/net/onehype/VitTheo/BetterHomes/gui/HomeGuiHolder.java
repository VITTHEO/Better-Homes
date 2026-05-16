package net.onehype.VitTheo.BetterHomes.gui;

import net.onehype.VitTheo.BetterHomes.home.Home;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class HomeGuiHolder implements InventoryHolder {
    private final GuiType type;
    private final int page;
    private final String homeName;
    private final String worldName;
    private final Map<Integer, String> actions = new HashMap<Integer, String>();
    private final Map<Integer, Home> homes = new HashMap<Integer, Home>();
    private Inventory inventory;

    public HomeGuiHolder(GuiType type, int page, String homeName, String worldName) {
        this.type = type;
        this.page = page;
        this.homeName = homeName;
        this.worldName = worldName;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public GuiType getType() {
        return type;
    }

    public int getPage() {
        return page;
    }

    public String getHomeName() {
        return homeName;
    }

    public String getWorldName() {
        return worldName;
    }

    public Map<Integer, String> getActions() {
        return actions;
    }

    public Map<Integer, Home> getHomes() {
        return homes;
    }
}
