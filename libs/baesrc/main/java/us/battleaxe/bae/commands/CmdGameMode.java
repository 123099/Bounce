package us.battleaxe.bae.commands;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import us.battleaxe.bae.api.API;

/** Special hardcoded case for the various gamemode commands. */
public final class CmdGameMode implements CommandExecutor {
	
	private final JavaPlugin instance;
	private final API api;
	
	public CmdGameMode(final JavaPlugin instance, final API api) {
		this.instance = instance;
		this.api = api;
		this.instance.getCommand("gamemode").setExecutor(this);
	}
	
	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, String[] args) {
		if(!sender.hasPermission("cmd.gamemode")) {
			sender.sendMessage(api.getMessage("general.no-permission"));
			return false;
		}
		String fixed = label.toLowerCase();
		if(fixed.startsWith("baessentials:")) {
			fixed = fixed.substring(13);
		}
		if(args.length > 2 || (args.length == 0 && (fixed.equalsIgnoreCase("gm") || fixed.equalsIgnoreCase("gamemode")))) {
			sender.sendMessage(this.api.getMessage("general.disallowed-usage", this.api.getMessage("command.gamemode.usage")));
			return false;
		}
		args = modifyArgs(fixed, args);
		GameMode mode;
		if(args.length == 1) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(this.api.getMessage("general.disallowed-console-command"));
				return false;
			}
			mode = setGameMode(sender, (Player) sender, args[0]);
			if(mode != null) {
				sender.sendMessage(this.api.getMessage("command.gamemode.success-self", WordUtils.capitalizeFully(mode.toString())));
				return true;
			}
		} else {
			if(!sender.hasPermission("cmd.gamemode.others")) {
				sender.sendMessage(api.getMessage("general.no-permission"));
				return false;
			}
			Player target = Bukkit.getPlayerExact(args[1]);
			if(target == null) {
				sender.sendMessage(this.api.getMessage("general.player-not-online", args[0]));
				return false;
			}
			mode = setGameMode(sender, target, args[0]);
			if(mode != null) {
				String gmName = WordUtils.capitalizeFully(mode.toString());
				sender.sendMessage(this.api.getMessage("command.gamemode.sender-success-other", target.getName(), gmName));
				target.sendMessage(this.api.getMessage("command.gamemode.target-success-other", gmName, sender instanceof Player ? ((Player) sender).getName() : "Console"));
				return true;
			}	
		}
		sender.sendMessage(this.api.getMessage("command.gamemode.invalid-gamemode"));
		return false;
	}
	
	private final String[] modifyArgs(final String label, final String[] args) {
		if(label.startsWith("gm") && !label.equals("gm")) {
			return args.length == 0 ? new String[] { label.substring(2, label.length()) } : new String[] { label.substring(2, label.length()), args[0] };
		}
		return args;
	}
	
	private final GameMode setGameMode(CommandSender sender, Player target, String gm) {
		GameMode mode;
		switch(gm.toLowerCase()) {
			case "0":
			case "s":
			case "survival":
				mode = GameMode.SURVIVAL;
				break;
			case "1":
			case "c":
			case "creative":
				mode = GameMode.CREATIVE;
				break;
			case "2":
			case "a":
			case "adventure":
				mode = GameMode.ADVENTURE;
				break;
			case "3":
			case "sp":
			case "spectator":
				mode = GameMode.SPECTATOR;
				break;
			default:
				return null;
		}
		target.setGameMode(mode);
		return mode;
	}
}