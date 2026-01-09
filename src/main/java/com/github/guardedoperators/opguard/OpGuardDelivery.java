package com.github.guardedoperators.opguard;

import com.saicone.delivery4j.AbstractMessenger;
import com.saicone.delivery4j.Broker;
import com.saicone.delivery4j.broker.HikariBroker;
import com.saicone.delivery4j.broker.RedisBroker;
import com.saicone.delivery4j.util.LogFilter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.RedisClient;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

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
            subscribe(LASTIP_CHANNEL).consume((channel, lines) -> {
                opguard.log(4, "Received last ip update: " + Arrays.toString(lines));
                opguard.verifier().updateLastIp(UUID.fromString(lines[0]), lines[1]);
            });
            final Broker broker = loadBroker();
            broker.setLogger(LogFilter.valueOf(opguard.logger(), () -> opguard.config().getLogLevel()));
            start(broker);
        }
    }

    public void unload() {
        close();
        clear();
    }

    @Override
    protected @NotNull Broker loadBroker() {
        switch (opguard.config().getMessengerType().toUpperCase()) {
            case "REDIS":
                return new RedisBroker(RedisClient.create(opguard.config().getRedisUrl()));
            case "SQL":
                final HikariBroker broker = HikariBroker.of(
                        opguard.config().getSqlUrl(),
                        opguard.config().getSqlUsername(),
                        opguard.config().getSqlPassword()
                );
                broker.setTablePrefix(opguard.config().getSqlTablePrefix());
                return broker;
            default:
                throw new IllegalStateException("The messenger type '" + opguard.config().getMessengerType() + "' doesn't exists");
        }
    }
}
