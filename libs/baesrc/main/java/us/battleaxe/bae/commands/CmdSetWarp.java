package us.battleaxe.bae.commands;

import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;
import us.battleaxe.bae.api.Warp;

public final class CmdSetWarp extends CmdExecutor {

	private final Pattern warpNamePattern;
	
	public CmdSetWarp() {
		super("setwarp", "cmd.setwarp", false);
		
		this.warpNamePattern = Pattern.compile("[\\p{Alnum}_]+");
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		//Blacklisting "list" in the regex is unnecessary complicated.
		if(!this.warpNamePattern.matcher(args[0]).matches() || args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(getCommandMessage("illegal-name"));
			return false;
		}
		
		if(api.getWarpManager().exists(args[0])) {
			Warp warp = api.getWarpManager().getWarp(args[0]);
			warp.setLocation(((Player) sender).getLocation());
			if(warp.writeToFile()) {
				sender.sendMessage(getCommandMessage("reset-success", warp.getName(), warp.getWorld(), Util.formatDouble(warp.getX()), Util.formatDouble(warp.getY()),
						Util.formatDouble(warp.getZ()), Util.formatDouble(warp.getYaw()), Util.formatDouble(warp.getPitch())));
			} else {
				sender.sendMessage(getCommandMessage("reset-failure", warp.getName()));
			}
		} else {
			Warp warp = api.getWarpManager().registerWarp(args[0], ((Player) sender).getLocation());
			if(warp.writeToFile()) {
				sender.sendMessage(getCommandMessage("set-success", warp.getName(), warp.getWorld(), Util.formatDouble(warp.getX()), Util.formatDouble(warp.getY()),
						Util.formatDouble(warp.getZ()), Util.formatDouble(warp.getYaw()), Util.formatDouble(warp.getPitch())));
			} else {
				sender.sendMessage(getCommandMessage("set-failure", warp.getName()));
			}
		}
		return true;
	}
}