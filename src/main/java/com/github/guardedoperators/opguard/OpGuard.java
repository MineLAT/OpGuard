/*
 * OpGuard - Password protected op.
 * Copyright © 2016-2021 OpGuard Contributors (https://github.com/GuardedOperators/OpGuard)
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

import com.github.guardedoperators.opguard.config.DataStorage;
import com.github.guardedoperators.opguard.config.OpGuardConfig;
import com.github.zafarkhaja.semver.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.Objects;

public final class OpGuard
{
	private final OpGuardPlugin plugin;
	private final Version version;
	private final Log log;
	private final OpGuardConfig config;
	private final OpVerifier verifier;
	private final OpGuardCommand command;
	
	OpGuard(OpGuardPlugin plugin)
	{
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.version = Version.valueOf(plugin.getDescription().getVersion());
		this.log = new Log(plugin, "guard");
		this.config = new OpGuardConfig(plugin);
		this.verifier = new OpVerifier(new DataStorage(this));
		this.command = new OpGuardCommand(this);
		
		register(new CommandInterceptor(this));
		register(new PluginDisableHijack(this));
		register(new PermissionChecker(this));
	}
	
	private <L extends Listener> L register(L listener)
	{
		Bukkit.getPluginManager().registerEvents(listener, plugin);
		return listener;
	}
	
	public OpGuardPlugin plugin() { return plugin; }
	
	public Version version() { return version; }
	
	public OpGuardConfig config() { return config; }
	
	public OpVerifier verifier() { return verifier; }
	
	public OpGuard log(Context context)
	{
		if (context.hasMessage() && context.isLoggable())
		{
			log(context.getMessage());
		}
		return this;
	}
	
	public OpGuard log(String message)
	{
		if (config.loggingIsEnabled())
		{
			log.append(message);
		}
		return this;
	}
	
	public OpGuard warn(Context context)
	{
		if (context.hasMessage() && context.isWarnable())
		{
			warn(context.getMessage());
		}
		return this;
	}
	
	public OpGuard warn(CommandSender sender, Context context)
	{
		if (context.hasMessage() && context.isWarnable())
		{
			Messenger.send(sender, context.getMessage());
		}
		return this;
	}
	
	public OpGuard warn(String message)
	{
		Messenger.broadcast(message, "opguard.warn");
		return this;
	}
	
	public void run(CommandSender sender, String[] args)
	{
		command.execute(sender, args);
	}
	
	public void punish(Context context, String username)
	{
		if (!context.isPunishable()) { return; }
		
		Context copy = context.copy().punishment();
		
		for (String command : config.getPunishmentCommands())
		{
			command = command.replaceAll("(%player%)", username);
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
		}
		
		copy.okay("Punished &7" + username + "&f for attempting to gain op");
		warn(copy).log(copy);
	}
}
