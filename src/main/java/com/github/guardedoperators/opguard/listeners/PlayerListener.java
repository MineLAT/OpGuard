package com.github.guardedoperators.opguard.listeners;

import com.github.guardedoperators.opguard.OpGuard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final OpGuard opguard;

    public PlayerListener(OpGuard opguard) {
        this.opguard = Objects.requireNonNull(opguard, "opguard");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        if (opguard.config().isToggleCommand()) {
            event.getPlayer().setOp(false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerQuitEvent event) {
        if (opguard.config().isToggleCommand()) {
            event.getPlayer().setOp(false);
        }
    }
}
