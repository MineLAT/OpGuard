package com.github.guardedoperators.opguard.listeners;

import com.github.guardedoperators.opguard.OpGuard;
import com.github.guardedoperators.opguard.OpVerifier;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final OpGuard opguard;
    private final OpVerifier verifier;

    public PlayerListener(OpGuard opguard) {
        this.opguard = Objects.requireNonNull(opguard, "opguard");
        this.verifier = opguard.verifier();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        if (verifier.isVerified(event.getPlayer())) {
            if (opguard.config().isToggleCommand()) {
                if (verifier.isSameIp(event.getPlayer())) {
                    opguard.logger().info("The player " + event.getPlayer().getName() + " joins with the same IP");
                    Bukkit.getScheduler().runTaskLater(opguard.plugin(), () -> event.getPlayer().setOp(true), 80L);
                    return;
                } else {
                    opguard.logger().warning("The player " + event.getPlayer().getName() + " doesn't have the same IP, removing OP state...");
                }
            } else {
                Bukkit.getScheduler().runTaskLater(opguard.plugin(), () -> event.getPlayer().setOp(true), 80L);
                return;
            }
        }
        event.getPlayer().setOp(false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().isOp() && !verifier.isVerified(event.getPlayer())) {
            opguard.logger().warning("Removing OP from " + event.getPlayer().getName() + " due its verified");
            event.getPlayer().setOp(false);
        }
    }
}
