package de.mauli.ban.storage;

import de.mauli.ban.config.PluginConfig;
import de.mauli.ban.model.BanRecord;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FileBanStorage implements BanStorage {
    private final File file;
    private final YamlConfiguration yaml;
    private final ConcurrentMap<String, BanRecord> cache = new ConcurrentHashMap<>();

    public FileBanStorage(JavaPlugin plugin, PluginConfig pluginConfig) {
        this.file = new File(plugin.getDataFolder(), pluginConfig.getStorageFileName());
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        for (String key : yaml.getKeys(false)) {
            String base = key + ".";
            BanRecord record = new BanRecord(
                    key,
                    UUID.fromString(yaml.getString(base + "targetUuid")),
                    yaml.getString(base + "targetName", "unknown"),
                    yaml.getString(base + "targetIp"),
                    parseUuid(yaml.getString(base + "adminUuid")),
                    yaml.getString(base + "adminName", "Console"),
                    yaml.getString(base + "reason", "Unbekannt"),
                    Instant.parse(yaml.getString(base + "createdAt")),
                    yaml.contains(base + "expiresAt") ? Instant.parse(yaml.getString(base + "expiresAt")) : null,
                    yaml.getBoolean(base + "active", true)
            );
            cache.put(key, record);
        }
    }

    private UUID parseUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private synchronized void flush() {
        for (String key : yaml.getKeys(false)) {
            yaml.set(key, null);
        }
        for (BanRecord record : cache.values()) {
            String base = record.getId() + ".";
            yaml.set(base + "targetUuid", record.getTargetUuid().toString());
            yaml.set(base + "targetName", record.getTargetName());
            yaml.set(base + "targetIp", record.getTargetIp());
            yaml.set(base + "adminUuid", record.getAdminUuid() == null ? null : record.getAdminUuid().toString());
            yaml.set(base + "adminName", record.getAdminName());
            yaml.set(base + "reason", record.getReason());
            yaml.set(base + "createdAt", record.getCreatedAt().toString());
            yaml.set(base + "expiresAt", record.getExpiresAt() == null ? null : record.getExpiresAt().toString());
            yaml.set(base + "active", record.isActive());
            yaml.set(base + "revokedAt", record.getRevokedAt() == null ? null : record.getRevokedAt().toString());
            yaml.set(base + "revokedBy", record.getRevokedBy());
        }
        try {
            yaml.save(file);
        } catch (IOException exception) {
            throw new RuntimeException("Konnte Bann-Datei nicht speichern", exception);
        }
    }

    @Override
    public void saveBan(BanRecord record) {
        cache.put(record.getId(), record);
        flush();
    }

    @Override
    public void updateBan(BanRecord record) {
        cache.put(record.getId(), record);
        flush();
    }

    @Override
    public Optional<BanRecord> getActiveBan(UUID uuid) {
        return cache.values().stream().filter(record -> record.getTargetUuid().equals(uuid) && record.isActive()).findFirst();
    }

    @Override
    public List<BanRecord> getBanHistory(UUID uuid) {
        return cache.values().stream()
                .filter(record -> record.getTargetUuid().equals(uuid))
                .sorted(Comparator.comparing(BanRecord::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<BanRecord> getAllActiveBans() {
        return new ArrayList<>(cache.values().stream().filter(BanRecord::isActive).toList());
    }

    @Override
    public void close() {
        flush();
    }
}
