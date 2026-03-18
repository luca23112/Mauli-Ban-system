package de.mauli.ban.storage;

import de.mauli.ban.model.BanRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BanStorage {
    void saveBan(BanRecord record);
    void updateBan(BanRecord record);
    Optional<BanRecord> getActiveBan(UUID uuid);
    List<BanRecord> getBanHistory(UUID uuid);
    List<BanRecord> getAllActiveBans();
    void close();
}
