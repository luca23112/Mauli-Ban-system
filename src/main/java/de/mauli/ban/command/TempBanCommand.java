package de.mauli.ban.command;

import de.mauli.ban.service.BanService;
import de.mauli.ban.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TempBanCommand implements CommandExecutor, TabCompleter {
    private final BanService banService;
    private final boolean permanentOnly;

    public TempBanCommand(BanService banService, boolean permanentOnly) {
        this.banService = banService;
        this.permanentOnly = permanentOnly;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (permanentOnly) {
            if (args.length < 2) {
                sender.sendMessage("/pban <spieler> <grund>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            banService.issueBan(sender, target.getUniqueId(), target.getName() == null ? args[0] : target.getName(), reason, null, true);
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("/tempban <spieler> <zeit> <grund>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        Duration duration;
        try {
            duration = TimeUtil.parseDuration(args[1]);
        } catch (Exception exception) {
            sender.sendMessage("Ungültige Zeit. Beispiele: 1h, 12h, 3d, 1w");
            return true;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        banService.issueBan(sender, target.getUniqueId(), target.getName() == null ? args[0] : target.getName(), reason, duration, false);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(player -> player.getName()).toList();
        }
        if (!permanentOnly && args.length == 2) {
            return List.of("1h", "12h", "1d", "3d", "7d", "perm");
        }
        return List.of();
    }
}
