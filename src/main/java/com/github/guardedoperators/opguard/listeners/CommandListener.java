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
package com.github.guardedoperators.opguard.listeners;

import com.github.guardedoperators.opguard.*;
import com.github.guardedoperators.opguard.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public final class CommandListener implements Listener {
    private final OpGuard api;
    private final OpVerifier verifier;

    public CommandListener(OpGuard api) {
        this.api = api;
        this.verifier = api.verifier();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        int index = command.indexOf(' ');
        if (api.config().isToggleCommand() && api.config().toggleExecution().contains(index < 0 ? command.toLowerCase() : command.substring(0, index).toLowerCase())) {
            if (!verifier.isVerified(player)) {
                return;
            }

            event.setCancelled(true);

            if (index > 0 && index + 1 < command.length()) {
                Placeholders placeholders = new Placeholders();
                placeholders.map("player", "username").to(player::getName);

                String password = command.substring(index + 1);
                if (!verifier.hasPassword(player)) {
                    verifier.updatePassword(player, password);
                } else if (!verifier.isPassword(player, password)) {
                    for (String cmd : api.config().toggleCommandsFail()) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), placeholders.update(cmd));
                    }
                    Messenger.send(player, "&cError: &fInvalid password");
                    return;
                }

                verifier.updateLastIp(player);

                boolean result = !player.isOp();
                player.setOp(result);
                if (result) {
                    for (String cmd : api.config().toggleCommandsOp()) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), placeholders.update(cmd));
                    }
                    Messenger.send(player, "&aSuccess: &fNow you are a verified operator");
                } else {
                    for (String cmd : api.config().toggleCommandsDeop()) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), placeholders.update(cmd));
                    }
                    Messenger.send(player, "&aSuccess: &fNow you are NOT a verified operator");
                }
            } else {
                Messenger.send(player, "&c&oCorrect Usage:&f " + command + " <password>");
            }
            return;
        }

        if (intercept(player, event.getMessage(), event)) {
            event.setMessage(collapse(command));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onConsoleCommand(ServerCommandEvent event) {
        String command = event.getCommand();

        if (intercept(event.getSender(), command, event)) {
            event.setCommand(collapse(command));
        }
    }

    private String collapse(String command) {
        return "opguard:intercepted(" + command.replaceAll("/| .*", "") + ")";
    }

    private boolean intercept(CommandSender sender, String command, Event event) {
        String[] cmd = command.split(" ");
        String base = cmd[0].toLowerCase();

        if (!base.matches("(?i)^/?((minecraft:)?(de)?op|o(g|pguard))$")) {
            return false;
        }

        Context context = new Context(api).attemptFrom(sender);
        PluginStackChecker stack = new PluginStackChecker(api);

        if (stack.hasFoundPlugin()) {
            String name = stack.getPlugin().getName();

            context.pluginAttempt().warning(
                    "The plugin <!>" + name + "&f attempted to make &7" + sender.getName() +
                            "&f execute <!>" + ((!command.startsWith("/")) ? "/" : "") + command
            );
            api.warn(context).log(context);

            stack.disablePlugin(api, context);
            return true;
        } else if (stack.hasAllowedPlugins()) {
            Context allowed = context.copy();
            String name = stack.getTopAllowedPlugin().getName();

            allowed.okay(
                    "The plugin &7" + name + "&f was allowed to execute &7" +
                            ((!base.startsWith("/")) ? "/" : "") + base +
                            "&f on behalf of &7" + sender.getName()
            );
            api.warn(allowed).log(allowed);
        }

        if (base.matches("(?i)^/?(minecraft:)?op$")) {
            context.setOp();

            if (cmd.length > 1) {
                String name = cmd[1];
                context.warning(sender.getName() + " attempted to " + base + " <!>" + name);
                api.warn(context).log(context).punish(context, name);
            }
        } else if (base.matches("(?i)^/?o(g|pguard)$")) {
            boolean isVerifiedOperator = sender.isOp() && api.verifier().isVerified(sender);
            boolean isPermitted = sender.hasPermission("opguard.manage") && api.config().isManagementPermissionEnabled();

            if (isVerifiedOperator || isPermitted) {
                api.run(sender, cmd);

                if (event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                }
            } else {
                context.incorrectlyUsedOpGuard().warning(sender.getName() + " attempted to access OpGuard");
                api.warn(context).log(context);
            }
        }
        return true;
    }
}
