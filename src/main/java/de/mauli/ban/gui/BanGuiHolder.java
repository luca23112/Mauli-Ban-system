package de.mauli.ban.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public record BanGuiHolder(Type type, UUID targetUuid, String targetName, String reasonId) implements InventoryHolder {
    public enum Type { REASON, DURATION }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
