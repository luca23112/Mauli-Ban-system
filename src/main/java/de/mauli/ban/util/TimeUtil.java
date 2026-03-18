package de.mauli.ban.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            .withLocale(Locale.GERMANY)
            .withZone(ZoneId.systemDefault());

    private TimeUtil() {}

    public static Duration parseDuration(String input) {
        if (input == null || input.isBlank() || input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) {
            return null;
        }
        long number = Long.parseLong(input.substring(0, input.length() - 1));
        char unit = Character.toLowerCase(input.charAt(input.length() - 1));
        return switch (unit) {
            case 'm' -> Duration.ofMinutes(number);
            case 'h' -> Duration.ofHours(number);
            case 'd' -> Duration.ofDays(number);
            case 'w' -> Duration.ofDays(number * 7);
            default -> throw new IllegalArgumentException("Unbekannte Zeitangabe: " + input);
        };
    }

    public static String formatInstant(Instant instant) {
        return instant == null ? "Permanent" : FORMATTER.format(instant);
    }

    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "Permanent";
        }
        long days = duration.toDays();
        if (days > 0) return days + " Tag(e)";
        long hours = duration.toHours();
        if (hours > 0) return hours + " Stunde(n)";
        return duration.toMinutes() + " Minute(n)";
    }

    public static String formatRemaining(Instant expiresAt) {
        if (expiresAt == null) {
            return "Permanent";
        }
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (remaining.isNegative()) {
            return "Abgelaufen";
        }
        return formatDuration(remaining);
    }
}
