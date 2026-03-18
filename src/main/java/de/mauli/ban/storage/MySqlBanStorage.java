package de.mauli.ban.storage;

import de.mauli.ban.config.PluginConfig;
import de.mauli.ban.model.BanRecord;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MySqlBanStorage implements BanStorage {
    private final Connection connection;

    public MySqlBanStorage(JavaPlugin plugin, PluginConfig config) throws Exception {
        String url = "jdbc:mysql://" + config.getMysqlHost() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase() +
                "?useSSL=" + config.useMysqlSsl() + "&characterEncoding=utf8";
        this.connection = DriverManager.getConnection(url, config.getMysqlUsername(), config.getMysqlPassword());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS mauli_bans (id VARCHAR(64) PRIMARY KEY, target_uuid VARCHAR(36), target_name VARCHAR(32), target_ip VARCHAR(64), admin_uuid VARCHAR(36), admin_name VARCHAR(32), reason TEXT, created_at VARCHAR(40), expires_at VARCHAR(40), active BOOLEAN, revoked_at VARCHAR(40), revoked_by VARCHAR(32))");
        }
        plugin.getLogger().info("MySQL-Bannspeicher verbunden.");
    }

    @Override
    public void saveBan(BanRecord record) {
        String sql = "INSERT INTO mauli_bans (id,target_uuid,target_name,target_ip,admin_uuid,admin_name,reason,created_at,expires_at,active,revoked_at,revoked_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            writeRecord(ps, record);
            ps.executeUpdate();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void updateBan(BanRecord record) {
        String sql = "UPDATE mauli_bans SET target_uuid=?,target_name=?,target_ip=?,admin_uuid=?,admin_name=?,reason=?,created_at=?,expires_at=?,active=?,revoked_at=?,revoked_by=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, record.getTargetUuid().toString());
            ps.setString(2, record.getTargetName());
            ps.setString(3, record.getTargetIp());
            ps.setString(4, record.getAdminUuid() == null ? null : record.getAdminUuid().toString());
            ps.setString(5, record.getAdminName());
            ps.setString(6, record.getReason());
            ps.setString(7, record.getCreatedAt().toString());
            ps.setString(8, record.getExpiresAt() == null ? null : record.getExpiresAt().toString());
            ps.setBoolean(9, record.isActive());
            ps.setString(10, record.getRevokedAt() == null ? null : record.getRevokedAt().toString());
            ps.setString(11, record.getRevokedBy());
            ps.setString(12, record.getId());
            ps.executeUpdate();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void writeRecord(PreparedStatement ps, BanRecord record) throws Exception {
        ps.setString(1, record.getId());
        ps.setString(2, record.getTargetUuid().toString());
        ps.setString(3, record.getTargetName());
        ps.setString(4, record.getTargetIp());
        ps.setString(5, record.getAdminUuid() == null ? null : record.getAdminUuid().toString());
        ps.setString(6, record.getAdminName());
        ps.setString(7, record.getReason());
        ps.setString(8, record.getCreatedAt().toString());
        ps.setString(9, record.getExpiresAt() == null ? null : record.getExpiresAt().toString());
        ps.setBoolean(10, record.isActive());
        ps.setString(11, record.getRevokedAt() == null ? null : record.getRevokedAt().toString());
        ps.setString(12, record.getRevokedBy());
    }

    @Override
    public Optional<BanRecord> getActiveBan(UUID uuid) {
        return querySingle("SELECT * FROM mauli_bans WHERE target_uuid=? AND active=TRUE ORDER BY created_at DESC LIMIT 1", uuid.toString());
    }

    @Override
    public List<BanRecord> getBanHistory(UUID uuid) {
        return queryList("SELECT * FROM mauli_bans WHERE target_uuid=? ORDER BY created_at DESC", uuid.toString());
    }

    @Override
    public List<BanRecord> getAllActiveBans() {
        return queryList("SELECT * FROM mauli_bans WHERE active=TRUE", null);
    }

    private Optional<BanRecord> querySingle(String sql, String parameter) {
        List<BanRecord> list = queryList(sql, parameter);
        return list.stream().findFirst();
    }

    private List<BanRecord> queryList(String sql, String parameter) {
        List<BanRecord> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (parameter != null) {
                ps.setString(1, parameter);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new BanRecord(
                        rs.getString("id"),
                        UUID.fromString(rs.getString("target_uuid")),
                        rs.getString("target_name"),
                        rs.getString("target_ip"),
                        rs.getString("admin_uuid") == null ? null : UUID.fromString(rs.getString("admin_uuid")),
                        rs.getString("admin_name"),
                        rs.getString("reason"),
                        Instant.parse(rs.getString("created_at")),
                        rs.getString("expires_at") == null ? null : Instant.parse(rs.getString("expires_at")),
                        rs.getBoolean("active")
                ));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return result;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (Exception ignored) {
        }
    }
}
