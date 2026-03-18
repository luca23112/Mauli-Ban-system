package de.mauli.ban.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemBuilder {
    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(MessageUtil.colorize(name));
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(MessageUtil.colorize(lore));
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder glow() {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}
