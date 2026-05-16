package net.onehype.VitTheo.BetterHomes.util;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public final class PermissionUtil {
    private PermissionUtil() {
    }

    public static boolean has(Player player, String permission) {
        return player.hasPermission(permission) || player.hasPermission(PermissionNodes.ADMIN);
    }

    public static int highestNumericPermission(Player player, String prefix, int fallback) {
        int highest = fallback;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) {
                continue;
            }
            String permission = info.getPermission().toLowerCase();
            if (!permission.startsWith(prefix.toLowerCase())) {
                continue;
            }
            String raw = permission.substring(prefix.length());
            try {
                highest = Math.max(highest, Integer.parseInt(raw));
            } catch (NumberFormatException ignored) {
            }
        }
        return highest;
    }
}
