package de.mauli.ban.model;

import java.time.Instant;
import java.util.UUID;

public class BanRecord {
    private final String id;
    private final UUID targetUuid;
    private final String targetName;
    private final String targetIp;
    private final UUID adminUuid;
    private final String adminName;
    private final String reason;
    private final Instant createdAt;
    private Instant expiresAt;
    private boolean active;
    private Instant revokedAt;
    private String revokedBy;

    public BanRecord(String id, UUID targetUuid, String targetName, String targetIp, UUID adminUuid, String adminName, String reason, Instant createdAt, Instant expiresAt, boolean active) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.targetIp = targetIp;
        this.adminUuid = adminUuid;
        this.adminName = adminName;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public String getId() { return id; }
    public UUID getTargetUuid() { return targetUuid; }
    public String getTargetName() { return targetName; }
    public String getTargetIp() { return targetIp; }
    public UUID getAdminUuid() { return adminUuid; }
    public String getAdminName() { return adminName; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getRevokedBy() { return revokedBy; }

    public boolean isPermanent() { return expiresAt == null; }
    public boolean isExpired() { return expiresAt != null && Instant.now().isAfter(expiresAt); }

    public void revoke(String revokedBy) {
        this.active = false;
        this.revokedAt = Instant.now();
        this.revokedBy = revokedBy;
    }

    public void expireNow() {
        this.active = false;
        this.expiresAt = Instant.now();
    }
}
