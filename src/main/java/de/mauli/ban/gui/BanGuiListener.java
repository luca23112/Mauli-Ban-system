package de.mauli.ban.gui;

import de.mauli.ban.config.PluginConfig;
import de.mauli.ban.model.BanDurationOption;
import de.mauli.ban.model.BanReason;
import de.mauli.ban.service.BanService;
import de.mauli.ban.util.ItemBuilder;
import de.mauli.ban.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class BanGuiListener implements Listener {
    private final BanService banService;
    private final PluginConfig pluginConfig;

    public BanGuiListener(BanService banService, PluginConfig pluginConfig) {
        this.banService = banService;
        this.pluginConfig = pluginConfig;
    }

    public void openReasonGui(Player moderator, Player target) {
        Inventory inventory = Bukkit.createInventory(new BanGuiHolder(BanGuiHolder.Type.REASON, target.getUniqueId(), target.getName(), null), 27, MessageUtil.colorize("&8Banngründe für &c" + target.getName()));
        fillBackground(inventory);
        int slot = 10;
        for (BanReason reason : pluginConfig.getReasons().values()) {
            inventory.setItem(slot++, new ItemBuilder(reason.material() == null ? Material.PAPER : reason.material())
                    .name(reason.displayName())
                    .lore(reason.description())
                    .build());
        }
        moderator.openInventory(inventory);
    }

    private void openDurationGui(Player moderator, BanGuiHolder holder, BanReason reason) {
        Inventory inventory = Bukkit.createInventory(new BanGuiHolder(BanGuiHolder.Type.DURATION, holder.targetUuid(), holder.targetName(), reason.id()), 27, MessageUtil.colorize("&8Dauer für &c" + holder.targetName()));
        fillBackground(inventory);
        int slot = 10;
        for (BanDurationOption option : pluginConfig.getDurations()) {
            List<String> lore = new ArrayList<>();
            lore.add("&7Grund: " + reason.displayName());
            lore.add("&7Dauer: " + option.display());
            inventory.setItem(slot++, new ItemBuilder(option.material()).name(option.display()).lore(lore).build());
        }
        moderator.openInventory(inventory);
    }

    private void fillBackground(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7").build());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof BanGuiHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player moderator) || event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }
        if (holder.type() == BanGuiHolder.Type.REASON) {
            BanReason selected = pluginConfig.getReasons().values().stream()
                    .filter(reason -> MessageUtil.colorize(reason.displayName()).equals(event.getCurrentItem().getItemMeta().getDisplayName()))
                    .findFirst().orElse(null);
            if (selected != null) {
                openDurationGui(moderator, holder, selected);
            }
            return;
        }
        BanReason reason = pluginConfig.getReasons().get(holder.reasonId());
        BanDurationOption option = pluginConfig.getDurations().stream()
                .filter(entry -> MessageUtil.colorize(entry.display()).equals(event.getCurrentItem().getItemMeta().getDisplayName()))
                .findFirst().orElse(null);
        if (reason == null || option == null) {
            return;
        }
        banService.issueBan(moderator, holder.targetUuid(), holder.targetName(), reason.defaultReason(), option.duration(), option.permanent());
        moderator.closeInventory();
    }
}
