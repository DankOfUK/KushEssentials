package com.kushsmp.essentials.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles color code translation, including hex colors of the form &#RRGGBB.
 */
public final class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private TextUtil() {
    }

    /**
     * Translate legacy '&' codes and &#RRGGBB hex codes into a colored string.
     */
    public static String color(String input) {
        if (input == null) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(ChatColor.of("#" + hex).toString()));
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Strip all color codes from a string.
     */
    public static String stripColor(String input) {
        return input == null ? "" : ChatColor.stripColor(color(input));
    }

    /**
     * Convert a duration string like "10m", "2h", "1d", "30s", "1w" into milliseconds.
     * Returns -1 if the format is invalid.
     */
    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }
        long total = 0;
        StringBuilder number = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                if (number.length() == 0) {
                    return -1;
                }
                long value = Long.parseLong(number.toString());
                switch (Character.toLowerCase(c)) {
                    case 's': total += value * 1000L; break;
                    case 'm': total += value * 60_000L; break;
                    case 'h': total += value * 3_600_000L; break;
                    case 'd': total += value * 86_400_000L; break;
                    case 'w': total += value * 604_800_000L; break;
                    default: return -1;
                }
                number.setLength(0);
            }
        }
        // Trailing bare number is invalid (no unit).
        if (number.length() > 0) {
            return -1;
        }
        return total == 0 ? -1 : total;
    }

    /**
     * Format a millisecond duration into a compact human string (e.g. "1d 2h 3m").
     */
    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "0s";
        }
        long seconds = millis / 1000;
        long days = seconds / 86_400;
        seconds %= 86_400;
        long hours = seconds / 3_600;
        seconds %= 3_600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
