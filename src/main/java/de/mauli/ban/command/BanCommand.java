package de.mauli.ban.command;

import de.mauli.ban.config.PluginConfig;
import de.mauli.ban.gui.BanGuiListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class BanCommand implements CommandExecutor, TabCompleter {
    private final PluginConfig pluginConfig;
    private final BanGuiListener banGuiListener;

    public BanCommand(PluginConfig pluginConfig, BanGuiListener banGuiListener) {
        this.pluginConfig = pluginConfig;
        this.banGuiListener = banGuiListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können dieses GUI verwenden.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage("/ban <spieler>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(pluginConfig.message("player-not-found"));
            return true;
        }
        banGuiListener.openReasonGui(player, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList() : List.of();
    }
}
