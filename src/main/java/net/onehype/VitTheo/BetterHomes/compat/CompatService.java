package net.onehype.VitTheo.BetterHomes.compat;

import net.onehype.VitTheo.BetterHomes.BetterHomes;
import net.onehype.VitTheo.BetterHomes.util.Text;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Locale;

public class CompatService {
    private final BetterHomes plugin;

    public CompatService(BetterHomes plugin) {
        this.plugin = plugin;
    }

    public ItemStack item(String materialCandidates, int amount) {
        MaterialChoice choice = material(materialCandidates);
        return new ItemStack(choice.material, amount, choice.data);
    }

    public MaterialChoice material(String materialCandidates) {
        if (materialCandidates == null || materialCandidates.trim().length() == 0) {
            return new MaterialChoice(Material.STONE, (short) 0);
        }

        String[] candidates = materialCandidates.split(",");
        for (String rawCandidate : candidates) {
            MaterialChoice choice = parseMaterial(rawCandidate.trim());
            if (choice != null) {
                return choice;
            }
        }
        return new MaterialChoice(Material.STONE, (short) 0);
    }

    private MaterialChoice parseMaterial(String candidate) {
        if (candidate.length() == 0) {
            return null;
        }

        short data = 0;
        String name = candidate;
        if (candidate.contains(":")) {
            String[] parts = candidate.split(":", 2);
            name = parts[0];
            try {
                data = Short.parseShort(parts[1]);
            } catch (NumberFormatException ignored) {
                data = 0;
            }
        }

        Alias alias = alias(name);
        if (alias != null) {
            name = alias.name;
            if (!candidate.contains(":")) {
                data = alias.data;
            }
        }

        Material material = Material.matchMaterial(name);
        if (material == null) {
            material = Material.getMaterial(name.toUpperCase(Locale.ENGLISH));
        }
        if (material == null) {
            return null;
        }
        return new MaterialChoice(material, data);
    }

    private Alias alias(String name) {
        String key = name.toUpperCase(Locale.ENGLISH);
        if ("GRAY_STAINED_GLASS_PANE".equals(key) || "GREY_STAINED_GLASS_PANE".equals(key)) {
            return new Alias("STAINED_GLASS_PANE", (short) 7);
        }
        if ("LIME_WOOL".equals(key)) {
            return new Alias("WOOL", (short) 5);
        }
        if ("RED_WOOL".equals(key)) {
            return new Alias("WOOL", (short) 14);
        }
        if ("PLAYER_HEAD".equals(key)) {
            return new Alias("SKULL_ITEM", (short) 3);
        }
        if ("OAK_SIGN".equals(key)) {
            return new Alias("SIGN", (short) 0);
        }
        return null;
    }

    public void playSound(Player player, String configPath) {
        if (!plugin.settings().soundsEnabled) {
            return;
        }
        String value = plugin.getConfig().getString(configPath, "");
        if (value.length() == 0) {
            return;
        }
        String[] candidates = value.split(",");
        for (String candidate : candidates) {
            try {
                Sound sound = Sound.valueOf(candidate.trim().toUpperCase(Locale.ENGLISH));
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                return;
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void playEffect(Location location, String configPath) {
        if (!plugin.settings().effectsEnabled || location == null || location.getWorld() == null) {
            return;
        }
        String value = plugin.getConfig().getString(configPath, "");
        if (value.length() == 0) {
            return;
        }
        String[] candidates = value.split(",");
        for (String candidate : candidates) {
            try {
                Effect effect = Effect.valueOf(candidate.trim().toUpperCase(Locale.ENGLISH));
                location.getWorld().playEffect(location, effect, 0);
                return;
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        String coloredTitle = Text.color(title);
        String coloredSubtitle = Text.color(subtitle);
        try {
            Method method = player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            method.invoke(player, coloredTitle, coloredSubtitle, fadeIn, stay, fadeOut);
            return;
        } catch (Exception ignored) {
        }
        try {
            Method method = player.getClass().getMethod("sendTitle", String.class, String.class);
            method.invoke(player, coloredTitle, coloredSubtitle);
            return;
        } catch (Exception ignored) {
        }
        if (coloredSubtitle != null && coloredSubtitle.length() > 0) {
            player.sendMessage(coloredSubtitle);
        }
    }

    public String inventoryTitle(String title) {
        return Text.truncate(Text.color(title), 32);
    }

    public static class MaterialChoice {
        public final Material material;
        public final short data;

        public MaterialChoice(Material material, short data) {
            this.material = material;
            this.data = data;
        }
    }

    private static class Alias {
        private final String name;
        private final short data;

        private Alias(String name, short data) {
            this.name = name;
            this.data = data;
        }
    }
}
