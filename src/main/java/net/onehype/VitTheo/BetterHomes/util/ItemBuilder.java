package net.onehype.VitTheo.BetterHomes.util;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {
    private final BetterHomes plugin;

    public ItemBuilder(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public ItemStack fromConfig(String path) {
        return fromConfig(path, new HashMap<String, String>());
    }

    public ItemStack fromConfig(String path, Map<String, String> placeholders) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null) {
            return plugin.compat().item("STONE", 1);
        }

        ItemStack item = plugin.compat().item(section.getString("material", "STONE"), 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (section.contains("name")) {
                meta.setDisplayName(Text.color(Text.replace(section.getString("name", ""), placeholders)));
            }
            List<String> lore = section.getStringList("lore");
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(Text.colorList(Text.replaceList(lore, placeholders)));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
