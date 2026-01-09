/*
 * OpGuard - Password protected op.
 * Copyright © 2016-2022 OpGuard Contributors (https://github.com/GuardedOperators/OpGuard)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.guardedoperators.opguard;

import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.ezlib.EzlibLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Dependencies(value = {
        @Dependency("com.saicone.settings:settings:1.0.5"),
        @Dependency("com.saicone.settings:settings-yaml:1.0.5"),
        @Dependency("com.saicone.delivery4j:delivery4j:1.1.5"),
        @Dependency("com.saicone.delivery4j:broker-sql-hikari:1.1.5"),
        @Dependency("com.saicone.delivery4j:broker-redis:1.1.5"),
        @Dependency("org.slf4j:slf4j-nop:1.7.36"),
        @Dependency("at.favre.lib:bcrypt:0.9.0"),
        @Dependency("com.github.zafarkhaja:java-semver:0.9.0")
}, relocations = {
        "com.saicone.settings", "{package}.libs.settings",
        "com.saicone.types", "{package}.libs.types",
        "org.yaml.snakeyaml", "{package}.libs.snakeyaml",
        "com.saicone.delivery4j", "{package}.libs.delivery4j",
        "com.zaxxer.hikari", "{package}.libs.hikari",
        "redis.clients.jedis", "{package}.libs.jedis",
        "com.google.gson", "{package}.libs.gson",
        "org.apache.commons.pool2", "{package}.libs.commons.pool2",
        "org.json", "{package}.libs.json",
        "org.slf4j", "{package}.libs.slf4j",
        "at.favre.lib.crypto.bcrypt", "{package}.libs.bcrypt",
        "at.favre.lib.bytes", "{package}.libs.bytes",
        "com.github.zafarkhaja.semver", "{package}.libs.semver",
})
public final class OpGuardPlugin extends JavaPlugin implements Listener {
    // https://bstats.org/plugin/bukkit/OpGuard/540
    public static final int BSTATS = 540;

    private OpGuard opguard;

    public OpGuardPlugin() {
        new EzlibLoader().logger((level, msg) -> {
            switch (level) {
                case 1:
                    getLogger().severe(msg);
                    break;
                case 2:
                    getLogger().warning(msg);
                    break;
                case 3:
                    getLogger().info(msg);
                    break;
                default:
                    break;
            }
        }).replace("{package}", "com.github.guardedoperators.opguard").load();
    }

    @Override
    public void onEnable() {
        Path dir = getDataFolder().toPath();

        if (!Files.isDirectory(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        opguard = new OpGuard(this);

        new UpdateCheckTask(opguard);

        if (opguard.config().metricsAreEnabled()) {
            new Metrics(this, BSTATS);
        }
    }

    @Override
    public void onDisable() {
        opguard.delivery().unload();
    }
}
