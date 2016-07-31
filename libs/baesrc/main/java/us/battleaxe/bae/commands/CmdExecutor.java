package us.battleaxe.bae.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.TextComponent;
import us.battleaxe.bae.api.API;

public abstract class CmdExecutor implements CommandExecutor {
	
	protected static JavaPlugin plugin;
	protected static API api;
	protected final String cmdName;
	protected final String permission;
	protected final boolean canConsoleUse;
	
	public CmdExecutor(final String cmdName, final String permission, final boolean canConsoleUse) {
		this.cmdName = cmdName;
		this.permission = permission;
		this.canConsoleUse = canConsoleUse;
		plugin.getCommand(cmdName).setExecutor(this);
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if(cmd.getName().equalsIgnoreCase(cmdName)) {
			if(!(sender instanceof Player) && !canConsoleUse) {
				sender.sendMessage(api.getMessage("general.disallowed-console-command"));
				return true;
			}
			if(!hasPermission(sender)) return true;
			return execute(sender, args);
		}
		return true;
	}
	
	protected abstract boolean execute(CommandSender sender, String[] args);
	
	protected final boolean isPlayer(final CommandSender sender) {
		return isPlayer(sender, false);
	}
	
	protected final boolean isPlayer(final CommandSender sender, final boolean disallowedUsage) {
		if(!(sender instanceof Player)) {
			if(disallowedUsage) {
				sender.sendMessage(api.getMessage("general.disallowed-console-usage", getCommandMessage("usage")));
			} else {
				sender.sendMessage(api.getMessage("general.disallowed-console-command"));
			}
			return false;
		}
		return true;
	}
	
	protected final boolean isConsole(final CommandSender sender) {
		return isConsole(sender, false);
	}
	
	protected final boolean isConsole(final CommandSender sender, final boolean disallowedUsage) {
		if(!(sender instanceof ConsoleCommandSender)) {
			if(disallowedUsage) {
				sender.sendMessage(api.getMessage("general.disallowed-player-usage"));
			} else {
				sender.sendMessage(api.getMessage("general.disallowed-player-command"));
			}
			return false;
		}
		return true;
	}
	
	protected final boolean hasPermission(final CommandSender sender, final String permission) {
		if(permission == null) return true;
		if(sender instanceof ConsoleCommandSender) return true;
		if(!sender.hasPermission(permission)) {
			sender.sendMessage(api.getMessage("general.no-permission"));
			return false;
		}
		return true;
	}
	
	protected final boolean hasPermission(final CommandSender sender) {
		return hasPermission(sender, this.permission);
	}
	
	protected final boolean noPlayer(final CommandSender sender, final String name) {
		sender.sendMessage(api.getMessage("general.player-not-online", name));
		return false;
	}
	
	protected final String getCommandMessage(String path, boolean ignoreColor, String... replacements) {
		return api.getMessage("command." + this.cmdName + "." + path, ignoreColor, replacements);
	}
	
	protected final String getCommandMessage(String path) {
		return api.getMessage("command." + this.cmdName + "." + path);
	}
	
	protected final String getCommandMessage(String path, String... replacements) {
		return api.getMessage("command." + this.cmdName + "." + path, replacements);
	}
	
	protected final TextComponent getComponentCommandMessage(String path) {
		return api.getComponentMessage("command." + this.cmdName + "." + path);
	}
	
	protected final TextComponent getComponentCommandMessage(String path, String... replacements) {
		return api.getComponentMessage("command." + this.cmdName + "." + path, replacements);
	}
	
	protected final boolean isNumber(final CommandSender sender, final String arg) {
		try {
			Integer.parseInt(arg);
			return true;
		} catch (NumberFormatException e) {
			sender.sendMessage(api.getMessage("general.not-a-number", arg));
			return false;
		}
	}
	
	protected final boolean isPositiveNumber(final CommandSender sender, final String arg) {
		try {
			int parsed = Integer.parseInt(arg);
			if(parsed > 0) {
				return true;
			}
			sender.sendMessage(api.getMessage("general.not-a-positive-number", arg));
			return false;
		} catch (NumberFormatException e) {
			sender.sendMessage(api.getMessage("general.not-a-number", arg));
			return false;
		}
	}
	
	protected final boolean isNotNegativeNumber(final CommandSender sender, final String arg) {
		try {
			int parsed = Integer.parseInt(arg);
			if(parsed >= 0) {
				return true;
			}
			sender.sendMessage(api.getMessage("general.not-positive-or-zero", arg));
			return false;
		} catch (NumberFormatException e) {
			sender.sendMessage(api.getMessage("general.not-a-number", arg));
			return false;
		}
	}
	
	protected final String getName(final CommandSender sender) {
		return sender instanceof Player ? ((Player) sender).getName() : "Console";
	}
	
	protected final boolean checkArgsLength(final CommandSender sender, final int length, final int maxArgs) {
		return checkArgsLength(sender, length, maxArgs, 0);
	}
	
	protected final boolean checkArgsLength(final CommandSender sender, final int length, final int maxArgs, final int minArgs) {
		if(length < minArgs || (maxArgs == -1 ? false : (length > maxArgs))) {
			sendUsageMessage(sender);
			return false;
		}
		return true;
	}
	
	protected final void sendUsageMessage(final CommandSender sender) {
		sender.sendMessage(api.getMessage("general.disallowed-usage", getCommandMessage("usage")));
	}
	
	public final static void registerAllCommands(final JavaPlugin plugin, final API api) {
		CmdExecutor.plugin = plugin;
		CmdExecutor.api = api;
		new CmdBack();
		new CmdBAP();
		new CmdDay();
		new CmdDeleteWarp();
		new CmdDeop();
		new CmdFeed();
		new CmdGameMode(plugin, api);
		new CmdGold();
		new CmdHeal();
		new CmdHub();
		new CmdHurt();
		new CmdInvSee();
		new CmdItem();
		new CmdKill();
		new CmdLightning();
		new CmdMe();
		new CmdMessage();
		new CmdNight();
		new CmdOp();
		new CmdPing();
		new CmdPlayerdata();
		new CmdRegenerate();
		new CmdReply();
		new CmdSay();
		new CmdSetWarp();
		new CmdSpeed();
		new CmdStarve();
		new CmdSudo();
		new CmdTeleport();
		new CmdTeleportHere();
		new CmdTop();
		new CmdTpa();
		new CmdTpAccept();
		new CmdTpPos();
		new CmdWarp();
		new CmdWorld();
		new CmdHead();
	}
}