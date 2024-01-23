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

@Dependencies({
        @Dependency(value = "com.saicone.settings:settings-yaml:14d88b0f12", relocate = {
                "com.saicone.settings", "{package}.libs.settings",
                "org.yaml.snakeyaml", "{package}.libs.snakeyaml"
        }),
        @Dependency(value = "at.favre.lib:bcrypt:0.9.0", relocate = {
                "at.favre.lib.crypto.bcrypt", "{package}.libs.bcrypt",
                "at.favre.lib.bytes", "{package}.libs.bytes"
        }),
        @Dependency(value = "com.github.zafarkhaja:java-semver:0.9.0", relocate = {
                "com.github.zafarkhaja.semver", "{package}.libs.semver"
        })
})
public final class OpGuardPlugin extends JavaPlugin implements Listener {
    // https://bstats.org/plugin/bukkit/OpGuard/540
    public static final int BSTATS = 540;

    public OpGuardPlugin() {
        new EzlibLoader().logger((level, msg) -> {
            switch (level) {
                case 1:
                    if (msg.contains("insecure protocol")) {
                        break;
                    }
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

        OpGuard opguard = new OpGuard(this);

        new UpdateCheckTask(opguard);

        if (opguard.config().metricsAreEnabled()) {
            new Metrics(this, BSTATS);
        }
    }
}
