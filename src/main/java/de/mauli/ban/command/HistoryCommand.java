package de.mauli.ban.command;

import de.mauli.ban.model.BanRecord;
import de.mauli.ban.service.BanService;
import de.mauli.ban.util.MessageUtil;
import de.mauli.ban.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryCommand implements CommandExecutor {
    private final BanService banService;

    public HistoryCommand(BanService banService) {
        this.banService = banService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("/history <spieler>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        List<BanRecord> history = banService.getHistory(target.getUniqueId());
        if (history.isEmpty()) {
            sender.sendMessage(MessageUtil.colorize("&7Keine Bann-Historie vorhanden."));
            return true;
        }
        sender.sendMessage(MessageUtil.colorize("&8&m---------------- &cHistorie von " + target.getName() + " &8&m----------------"));
        int index = 1;
        for (BanRecord record : history) {
            Map<String, String> values = new HashMap<>(banService.placeholders(record));
            values.put("index", String.valueOf(index++));
            values.put("duration", record.isPermanent() ? "Permanent" : TimeUtil.formatRemaining(record.getExpiresAt()));
            sender.sendMessage(MessageUtil.replace("&8#%index% &7%created_at% &8» &f%reason% &8(&c%admin%&8, &7%duration%&8)", values));
        }
        return true;
    }
}
