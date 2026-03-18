package de.mauli.ban.listener;

import de.mauli.ban.service.BanService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerConnectionListener implements Listener {
    private final BanService banService;

    public PlayerConnectionListener(BanService banService) {
        this.banService = banService;
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        banService.checkLogin(event);
    }
}
