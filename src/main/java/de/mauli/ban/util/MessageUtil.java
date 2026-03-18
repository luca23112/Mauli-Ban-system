package de.mauli.ban.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MessageUtil {
    private MessageUtil() {}

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public static List<String> colorize(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(colorize(line));
        }
        return result;
    }

    public static String replace(String message, Map<String, String> placeholders) {
        String replaced = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            replaced = replaced.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return colorize(replaced);
    }

    public static List<String> replace(List<String> messages, Map<String, String> placeholders) {
        List<String> lines = new ArrayList<>();
        for (String message : messages) {
            lines.add(replace(message, placeholders));
        }
        return lines;
    }
}
