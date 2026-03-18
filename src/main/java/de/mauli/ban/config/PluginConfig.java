package de.mauli.ban.config;

import de.mauli.ban.model.BanDurationOption;
import de.mauli.ban.model.BanReason;
import de.mauli.ban.util.MessageUtil;
import de.mauli.ban.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PluginConfig {
    private final JavaPlugin plugin;
    private final Map<String, BanReason> reasons = new HashMap<>();
    private final List<BanDurationOption> durations = new ArrayList<>();
    private FileConfiguration config;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        reasons.clear();
        durations.clear();

        ConfigurationSection reasonSection = config.getConfigurationSection("ban-reasons");
        if (reasonSection != null) {
            for (String key : reasonSection.getKeys(false)) {
                ConfigurationSection entry = reasonSection.getConfigurationSection(key);
                if (entry == null) continue;
                reasons.put(key.toLowerCase(Locale.ROOT), new BanReason(
                        key.toLowerCase(Locale.ROOT),
                        entry.getString("display-name", key),
                        Material.matchMaterial(entry.getString("material", "PAPER")),
                        entry.getStringList("description"),
                        entry.getString("default-reason", key)
                ));
            }
        }

        for (Map<?, ?> raw : config.getMapList("durations")) {
            String id = String.valueOf(raw.get("id"));
            String display = String.valueOf(raw.get("display"));
            String durationInput = String.valueOf(raw.get("duration"));
            Material material = Material.matchMaterial(String.valueOf(raw.containsKey("material") ? raw.get("material") : "CLOCK"));
            Duration duration = TimeUtil.parseDuration(durationInput);
            durations.add(new BanDurationOption(id, display, material == null ? Material.CLOCK : material, durationInput, duration, duration == null));
        }
    }

    public boolean isMysqlEnabled() { return config.getString("storage.type", "FILE").equalsIgnoreCase("MYSQL"); }
    public boolean isIpStorageEnabled() { return config.getBoolean("storage.ip-storage", false); }
    public String getStorageFileName() { return config.getString("storage.file-name", "bans.yml"); }
    public String getMysqlHost() { return config.getString("storage.mysql.host"); }
    public int getMysqlPort() { return config.getInt("storage.mysql.port"); }
    public String getMysqlDatabase() { return config.getString("storage.mysql.database"); }
    public String getMysqlUsername() { return config.getString("storage.mysql.username"); }
    public String getMysqlPassword() { return config.getString("storage.mysql.password"); }
    public boolean useMysqlSsl() { return config.getBoolean("storage.mysql.use-ssl", false); }
    public String getDiscordWebhookUrl() { return config.getString("discord.webhook-url", ""); }
    public Map<String, BanReason> getReasons() { return reasons; }
    public List<BanDurationOption> getDurations() { return durations; }

    public String message(String path) {
        return MessageUtil.colorize(config.getString("messages." + path, ""));
    }

    public List<String> messageList(String path) {
        return MessageUtil.colorize(config.getStringList("messages." + path));
    }
}
