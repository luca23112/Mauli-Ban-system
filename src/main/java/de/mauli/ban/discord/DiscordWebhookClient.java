package de.mauli.ban.discord;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookClient {
    private final JavaPlugin plugin;
    private final String webhookUrl;

    public DiscordWebhookClient(JavaPlugin plugin, String webhookUrl) {
        this.plugin = plugin;
        this.webhookUrl = webhookUrl;
    }

    public void send(String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(webhookUrl).toURL().openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String payload = "{\"content\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                connection.getInputStream().close();
            } catch (Exception exception) {
                plugin.getLogger().warning("Discord Webhook fehlgeschlagen: " + exception.getMessage());
            }
        });
    }
}
