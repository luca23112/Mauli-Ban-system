package de.mauli.ban.service;

import de.mauli.ban.MauliBanSystemPlugin;
import de.mauli.ban.config.PluginConfig;
import de.mauli.ban.discord.DiscordWebhookClient;
import de.mauli.ban.model.BanRecord;
import de.mauli.ban.storage.BanStorage;
import de.mauli.ban.util.MessageUtil;
import de.mauli.ban.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BanService {
    private final MauliBanSystemPlugin plugin;
    private final PluginConfig pluginConfig;
    private final BanStorage storage;
    private final DiscordWebhookClient webhookClient;

    public BanService(MauliBanSystemPlugin plugin, PluginConfig pluginConfig, BanStorage storage, DiscordWebhookClient webhookClient) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.storage = storage;
        this.webhookClient = webhookClient;
    }

    public void startExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::expireBans, 20L * 60L, 20L * 60L);
    }

    public void shutdown() {
        storage.close();
    }

    public void checkLogin(AsyncPlayerPreLoginEvent event) {
        Optional<BanRecord> activeBan = storage.getActiveBan(event.getUniqueId());
        if (activeBan.isEmpty()) {
            return;
        }
        BanRecord banRecord = activeBan.get();
        if (banRecord.isExpired()) {
            banRecord.expireNow();
            storage.updateBan(banRecord);
            return;
        }
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, String.join("\n", buildBanScreen(banRecord)));
    }

    public void issueBan(CommandSender sender, UUID targetUuid, String targetName, String reason, Duration duration, boolean permanent) {
        Player onlinePlayer = Bukkit.getPlayer(targetUuid);
        String ip = pluginConfig.isIpStorageEnabled() && onlinePlayer != null && onlinePlayer.getAddress() != null
                ? onlinePlayer.getAddress().getAddress().getHostAddress()
                : null;
        Instant expiresAt = permanent || duration == null ? null : Instant.now().plus(duration);
        UUID adminUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String adminName = sender.getName();
        BanRecord record = new BanRecord(UUID.randomUUID().toString(), targetUuid, targetName, ip, adminUuid, adminName, reason, Instant.now(), expiresAt, true);
        storage.saveBan(record);

        Map<String, String> placeholders = placeholders(record);
        String broadcast = MessageUtil.replace(pluginConfig.message("staff-broadcast"), placeholders);
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("mauli.notify")).forEach(player -> player.sendMessage(broadcast));
        sender.sendMessage(MessageUtil.replace(pluginConfig.message("ban-created"), placeholders));
        webhookClient.send("🔨 " + targetName + " wurde von " + adminName + " für " + reason + " (" + placeholders.get("duration") + ") gebannt.");

        if (onlinePlayer != null) {
            onlinePlayer.kickPlayer(String.join("\n", buildBanScreen(record)));
        }
    }

    public boolean unban(CommandSender sender, OfflinePlayer target) {
        Optional<BanRecord> active = storage.getActiveBan(target.getUniqueId());
        if (active.isEmpty()) {
            sender.sendMessage(MessageUtil.replace(pluginConfig.message("not-banned"), basePlaceholders(target.getName() == null ? target.getUniqueId().toString() : target.getName())));
            return false;
        }
        BanRecord record = active.get();
        record.revoke(sender.getName());
        storage.updateBan(record);
        sender.sendMessage(MessageUtil.replace(pluginConfig.message("ban-removed"), basePlaceholders(record.getTargetName())));
        webhookClient.send("✅ " + record.getTargetName() + " wurde von " + sender.getName() + " entbannt.");
        return true;
    }

    public Optional<BanRecord> getActiveBan(UUID uuid) {
        Optional<BanRecord> record = storage.getActiveBan(uuid);
        record.ifPresent(ban -> {
            if (ban.isExpired()) {
                ban.expireNow();
                storage.updateBan(ban);
            }
        });
        return storage.getActiveBan(uuid);
    }

    public List<BanRecord> getHistory(UUID uuid) {
        return storage.getBanHistory(uuid);
    }

    public void expireBans() {
        for (BanRecord record : storage.getAllActiveBans()) {
            if (record.isExpired()) {
                record.expireNow();
                storage.updateBan(record);
            }
        }
    }

    public List<String> buildBanScreen(BanRecord record) {
        return MessageUtil.replace(pluginConfig.messageList("ban-screen"), placeholders(record));
    }

    public Map<String, String> basePlaceholders(String targetName) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("prefix", pluginConfig.message("prefix"));
        placeholders.put("target", targetName);
        return placeholders;
    }

    public Map<String, String> placeholders(BanRecord record) {
        Map<String, String> placeholders = basePlaceholders(record.getTargetName());
        placeholders.put("reason", record.getReason());
        placeholders.put("admin", record.getAdminName());
        placeholders.put("duration", record.isPermanent() ? "Permanent" : TimeUtil.formatRemaining(record.getExpiresAt()));
        placeholders.put("timeout", record.isPermanent() ? "Permanent" : TimeUtil.formatRemaining(record.getExpiresAt()));
        placeholders.put("created_at", TimeUtil.formatInstant(record.getCreatedAt()));
        placeholders.put("expires_at", record.getExpiresAt() == null ? "Permanent" : TimeUtil.formatInstant(record.getExpiresAt()));
        placeholders.put("active", record.isActive() ? "Ja" : "Nein");
        return placeholders;
    }
}
