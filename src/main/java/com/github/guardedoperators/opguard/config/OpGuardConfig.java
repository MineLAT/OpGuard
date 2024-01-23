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
package com.github.guardedoperators.opguard.config;

import com.github.guardedoperators.opguard.OpGuard;
import com.saicone.settings.Settings;
import com.saicone.settings.SettingsData;
import com.saicone.settings.SettingsLoader;
import com.saicone.settings.data.DataType;
import com.saicone.settings.source.YamlSettingsSource;

import java.util.List;

public final class OpGuardConfig {

    private final SettingsData<Settings> data = SettingsData.of("config.yml").or(DataType.INPUT_STREAM, "config.yml");
    private List<String> toggleExecution = List.of();

    public OpGuardConfig(OpGuard plugin) {
        data.parentFolder(plugin.plugin().getDataFolder()).source(new YamlSettingsSource());
        SettingsLoader.simple().load(data);
        toggleExecution = yaml().get("toggle-execution").asStringList();
    }

    private Settings yaml() {
        return data.getLoaded();
    }

    public void reload() {
        SettingsLoader.simple().load(data);
    }

    public boolean isLocked() {
        return yaml().get("lock").asBoolean(false);
    }

    public boolean isOfflineMode() {
        return yaml().get("offline-mode").asBoolean(false);
    }

    public boolean canOnlyOpIfOnline() {
        return yaml().get("only-op-if-online").asBoolean(true);
    }

    public boolean canOnlyDeopIfOnline() {
        return yaml().get("only-deop-if-online").asBoolean(false);
    }

    public boolean canManagePasswordInGame() {
        return yaml().get("manage-password-in-game").asBoolean(true);
    }

    public boolean isManagementPermissionEnabled() {
        return yaml().get("use-opguard-management-permission-node").asBoolean(false);
    }

    public boolean isToggleCommand() {
        return yaml().get("use-toggle-op-command").asBoolean(false);
    }

    public List<String> toggleExecution() {
        return toggleExecution;
    }

    public List<String> toggleCommandsOp() {
        return yaml().get("toggle-commands", "op").asStringList();
    }

    public List<String> toggleCommandsDeop() {
        return yaml().get("toggle-commands", "deop").asStringList();
    }

    public List<String> toggleCommandsFail() {
        return yaml().get("toggle-commands", "fail").asStringList();
    }

    public List<String> verifyCommandsOp() {
        return yaml().get("verify-commands", "op").asStringList();
    }

    public List<String> verifyCommandsDeop() {
        return yaml().get("verify-commands", "deop").asStringList();
    }

    public List<String> verifyCommandsFail() {
        return yaml().get("verify-commands", "fail").asStringList();
    }

    public boolean canShutDownOnDisable() {
        return yaml().get("shutdown-on-disable").asBoolean(false);
    }

    public boolean canExemptSelfFromPlugMan() {
        return yaml().get("exempt-opguard-from-plugman").asBoolean(true);
    }

    // Inspections

    public boolean canCheckPermissions() {
        return yaml().get("check-permissions").asBoolean(true);
    }

    public boolean canDisableOtherPlugins() {
        return yaml().get("disable-malicious-plugins-when-caught").asBoolean(true);
    }

    public boolean canRenameOtherPlugins() {
        return canDisableOtherPlugins() && yaml().get("rename-malicious-plugins-when-caught").asBoolean(true);
    }

    // Plugin Exemptions

    public boolean shouldExemptPlugins() {
        return yaml().get("enable-exempt-plugins").asBoolean(true);
    }

    public List<String> getExemptPlugins() {
        return yaml().get("exempt-plugins").asStringList();
    }

    // Logging

    public boolean loggingIsEnabled() {
        return yaml().get("enable-logging").asBoolean(true);
    }

    public boolean canLogPluginAttempts() {
        return yaml().get("log-plugin-attempts").asBoolean(true);
    }

    public boolean canLogConsoleAttempts() {
        return yaml().get("log-console-attempts").asBoolean(true);
    }

    public boolean canLogPlayerAttempts() {
        return yaml().get("log-player-attempts").asBoolean(true);
    }

    // Messages

    public String getWarningPrefix() {
        return yaml().get("warn-prefix").asString("&cOpGuard (&4&lWarning&c)&7:");
    }

    public String getWarningEmphasisColor() {
        return yaml().get("warn-emphasis-color").asString("&c");
    }

    public boolean canSendPluginAttemptWarnings() {
        return yaml().get("warn-plugin-attempts").asBoolean(true);
    }

    public boolean canSendConsoleOpAttemptWarnings() {
        return yaml().get("warn-console-op-attempts").asBoolean(true);
    }

    public boolean canSendConsoleOpGuardAttemptWarnings() {
        return yaml().get("warn-console-opguard-attempts").asBoolean(true);
    }

    public boolean canSendPlayerOpAttemptWarnings() {
        return yaml().get("warn-player-op-attempts").asBoolean(true);
    }

    public boolean canSendPlayerOpGuardAttemptWarnings() {
        return yaml().get("warn-player-opguard-attempts").asBoolean(true);
    }

    public String getSecurityPrefix() {
        return yaml().get("security-prefix").asString("&eOpGuard (&6&lSecurity&e)&7:");
    }

    public boolean canSendSecurityWarnings() {
        return yaml().get("enable-security-warnings").asBoolean(true);
    }

    public String getOkayPrefix() {
        return yaml().get("okay-prefix").asString("&aOpGuard (&2&lOkay&a)&7:");
    }

    // Punishments

    public boolean canPunishPluginAttempts() {
        return yaml().get("punish-plugin-attempts").asBoolean(true);
    }

    public boolean canPunishConsoleOpAttempts() {
        return yaml().get("punish-console-op-attempts").asBoolean(true);
    }

    public boolean canPunishConsoleOpGuardAttempts() {
        return yaml().get("punish-console-opguard-attempts").asBoolean(false);
    }

    public List<String> getPunishmentCommands() {
        return yaml().get("punishment-commands").asStringList();
    }

    // Update Checks

    public boolean canCheckForUpdates() {
        return yaml().get("check-for-updates").asBoolean(false);
    }

    public long getUpdateCheckInterval() {
        return yaml().get("update-interval").asLong(12L);
    }

    // Metrics

    public boolean metricsAreEnabled() {
        return yaml().get("metrics").asBoolean(true);
    }

    public String getVersion() {
        return yaml().get("version").asString();
    }
}
