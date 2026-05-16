package net.onehype.VitTheo.BetterHomes.util;

import net.onehype.VitTheo.BetterHomes.config.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageService {
    private final ConfigManager configManager;

    public MessageService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void send(CommandSender sender, String path) {
        send(sender, path, new HashMap<String, String>());
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        String message = get(path, placeholders);
        if (message.length() > 0) {
            sender.sendMessage(message);
        }
    }

    public void sendList(CommandSender sender, String path, Map<String, String> placeholders) {
        FileConfiguration messages = configManager.messages();
        List<String> list = messages.getStringList(path);
        if (list == null || list.isEmpty()) {
            String single = get(path, placeholders);
            if (single.length() > 0) {
                sender.sendMessage(single);
            }
            return;
        }

        for (String line : list) {
            sender.sendMessage(format(line, placeholders));
        }
    }

    public String get(String path, Map<String, String> placeholders) {
        String raw = configManager.messages().getString(path, "");
        return format(raw, placeholders);
    }

    public String format(String raw, Map<String, String> placeholders) {
        Map<String, String> merged = new HashMap<String, String>();
        if (placeholders != null) {
            merged.putAll(placeholders);
        }
        merged.put("prefix", configManager.messages().getString("prefix", ""));
        return Text.color(Text.replace(raw, merged));
    }

    public static Map<String, String> placeholders(String... values) {
        Map<String, String> map = new HashMap<String, String>();
        if (values == null) {
            return map;
        }
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }
}
