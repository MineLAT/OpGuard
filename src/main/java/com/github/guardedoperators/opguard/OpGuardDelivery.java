package com.github.guardedoperators.opguard;

import com.saicone.delivery4j.AbstractMessenger;
import com.saicone.delivery4j.DeliveryClient;
import com.saicone.delivery4j.client.HikariDelivery;
import com.saicone.delivery4j.client.RedisDelivery;
import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Dependencies(value = {
        @Dependency("com.saicone.delivery4j:delivery4j:1.0"),
        @Dependency(value = "com.saicone.delivery4j:delivery4j-hikari:1.0",
                relocate = {"com.zaxxer.hikari", "{package}.libs.hikari"}
        ),
        @Dependency(value = "com.saicone.delivery4j:delivery4j-redis:1.0",
                relocate = {
                        "redis.clients.jedis", "{package}.libs.jedis",
                        "com.google.gson", "{package}.libs.gson",
                        "org.apache.commons.pool2", "{package}.libs.commons.pool2",
                        "org.json", "{package}.libs.json"
                }
        ),
        @Dependency("org.slf4j:slf4j-nop:1.7.36")
}, relocations = {
        "com.saicone.delivery4j", "{package}.libs.delivery4j",
        "org.slf4j", "{package}.libs.slf4j"
})
public class OpGuardDelivery extends AbstractMessenger {

    private static final String LASTIP_CHANNEL = "opguard:lastip";

    private final OpGuard opguard;

    OpGuardDelivery(OpGuard opguard) {
        this.opguard = Objects.requireNonNull(opguard, "opguard");
        load();
    }

    public void sendLastIp(@NotNull Player player) {
        send(LASTIP_CHANNEL, player.getUniqueId(), player.getAddress().getAddress().getHostAddress());
    }

    public void load() {
        close();
        if (opguard.config().isMessengerEnabled()) {
            subscribe(LASTIP_CHANNEL, (lines) -> {
                log(4, "Received last ip update: " + Arrays.toString(lines));
                opguard.verifier().updateLastIp(UUID.fromString(lines[0]), lines[1]);
            });
            start();
        }
    }

    public void unload() {
        close();
        clear();
    }

    @Override
    protected @NotNull DeliveryClient loadDeliveryClient() {
        switch (opguard.config().getMessengerType().toUpperCase()) {
            case "REDIS":
                return RedisDelivery.of(opguard.config().getRedisUrl());
            case "SQL":
                return HikariDelivery.of(
                        opguard.config().getSqlUrl(),
                        opguard.config().getSqlUsername(),
                        opguard.config().getSqlPassword(),
                        opguard.config().getSqlTablePrefix()
                );
            default:
                throw new IllegalStateException("The messenger type '" + opguard.config().getMessengerType() + "' doesn't exists");
        }
    }

    @Override
    public void log(int level, @NotNull Throwable t) {
        opguard.log(level, t);
    }

    @Override
    public void log(int level, @NotNull String msg) {
        opguard.log(level, msg);
    }

    @Override
    public @NotNull Runnable async(@NotNull Runnable runnable) {
        final BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(opguard.plugin(), runnable);
        return task::cancel;
    }

    @Override
    public @NotNull Runnable asyncRepeating(@NotNull Runnable runnable, long time, @NotNull TimeUnit unit) {
        final long ticks = unit.toMillis(time) / 50;
        final BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(opguard.plugin(), runnable, ticks, ticks);
        return task::cancel;
    }
}
