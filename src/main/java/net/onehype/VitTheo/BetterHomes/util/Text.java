package net.onehype.VitTheo.BetterHomes.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Text {
    private Text() {
    }

    public static String color(String value) {
        if (value == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public static List<String> colorList(List<String> values) {
        List<String> colored = new ArrayList<String>();
        if (values == null) {
            return colored;
        }
        for (String value : values) {
            colored.add(color(value));
        }
        return colored;
    }

    public static String replace(String value, Map<String, String> placeholders) {
        if (value == null) {
            return "";
        }
        String result = value;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace("%" + entry.getKey() + "%", entry.getValue() == null ? "" : entry.getValue());
            }
        }
        return result;
    }

    public static List<String> replaceList(List<String> values, Map<String, String> placeholders) {
        List<String> result = new ArrayList<String>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            result.add(replace(value, placeholders));
        }
        return result;
    }

    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength));
    }

    public static String seconds(long seconds) {
        if (seconds < 0) {
            return "0";
        }
        return String.valueOf(seconds);
    }
}
