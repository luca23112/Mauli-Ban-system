package de.mauli.ban.command;

import de.mauli.ban.model.BanRecord;
import de.mauli.ban.service.BanService;
import de.mauli.ban.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CheckCommand implements CommandExecutor {
    private final BanService banService;

    public CheckCommand(BanService banService) {
        this.banService = banService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("/check <spieler>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        Optional<BanRecord> active = banService.getActiveBan(target.getUniqueId());
        if (active.isEmpty()) {
            sender.sendMessage(MessageUtil.colorize("&7Dieser Spieler ist aktuell nicht gebannt."));
            return true;
        }
        BanRecord record = active.get();
        Map<String, String> placeholders = new HashMap<>(banService.placeholders(record));
        placeholders.put("uuid", target.getUniqueId().toString());
        sender.sendMessage(MessageUtil.colorize("&8&m------------------------------------------------"));
        sender.sendMessage(MessageUtil.replace("&cBann-Informationen für &f%target%", placeholders));
        sender.sendMessage(MessageUtil.replace("&7UUID: &f%uuid%", placeholders));
        sender.sendMessage(MessageUtil.replace("&7Grund: &f%reason%", placeholders));
        sender.sendMessage(MessageUtil.replace("&7Gebannt von: &f%admin%", placeholders));
        sender.sendMessage(MessageUtil.replace("&7Erstellt am: &f%created_at%", placeholders));
        sender.sendMessage(MessageUtil.replace("&7Läuft ab: &f%expires_at%", placeholders));
        sender.sendMessage(MessageUtil.replace("&7Aktiv: &f%active%", placeholders));
        sender.sendMessage(MessageUtil.colorize("&8&m------------------------------------------------"));
        return true;
    }
}
