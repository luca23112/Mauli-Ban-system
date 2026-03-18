package de.mauli.ban;

import de.mauli.ban.command.BanCommand;
import de.mauli.ban.command.CheckCommand;
import de.mauli.ban.command.HistoryCommand;
import de.mauli.ban.command.TempBanCommand;
import de.mauli.ban.command.UnbanCommand;
import de.mauli.ban.config.PluginConfig;
import de.mauli.ban.discord.DiscordWebhookClient;
import de.mauli.ban.gui.BanGuiListener;
import de.mauli.ban.listener.PlayerConnectionListener;
import de.mauli.ban.service.BanService;
import de.mauli.ban.storage.BanStorage;
import de.mauli.ban.storage.FileBanStorage;
import de.mauli.ban.storage.MySqlBanStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class MauliBanSystemPlugin extends JavaPlugin {
    private PluginConfig pluginConfig;
    private BanService banService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.pluginConfig = new PluginConfig(this);
        this.pluginConfig.load();

        BanStorage storage = createStorage();
        DiscordWebhookClient webhookClient = new DiscordWebhookClient(this, pluginConfig.getDiscordWebhookUrl());
        this.banService = new BanService(this, pluginConfig, storage, webhookClient);
        this.banService.startExpirationTask();

        BanGuiListener banGuiListener = new BanGuiListener(banService, pluginConfig);
        registerCommands(banGuiListener);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(banService), this);
        getServer().getPluginManager().registerEvents(banGuiListener, this);
        getLogger().info("MauliBanSystem wurde aktiviert.");
    }

    @Override
    public void onDisable() {
        if (banService != null) {
            banService.shutdown();
        }
    }

    private BanStorage createStorage() {
        try {
            if (pluginConfig.isMysqlEnabled()) {
                return new MySqlBanStorage(this, pluginConfig);
            }
        } catch (Exception exception) {
            getLogger().warning("MySQL konnte nicht initialisiert werden, nutze Datei-Speicher: " + exception.getMessage());
        }
        return new FileBanStorage(this, pluginConfig);
    }

    private void registerCommands(BanGuiListener banGuiListener) {
        register("ban", new BanCommand(pluginConfig, banGuiListener));
        register("pban", new TempBanCommand(banService, true));
        register("tempban", new TempBanCommand(banService, false));
        register("unban", new UnbanCommand(banService));
        register("check", new CheckCommand(banService));
        register("history", new HistoryCommand(banService));
    }

    private void register(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command fehlt in plugin.yml: " + name);
            return;
        }
        if (executor instanceof org.bukkit.command.CommandExecutor commandExecutor) {
            command.setExecutor(commandExecutor);
        }
        if (executor instanceof org.bukkit.command.TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }
}
