package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;

public final class CmdMe extends CmdExecutor {
	
	public CmdMe() {
		super("me", "cmd.me", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, -1, 1)) return false;
		Player player = (Player) sender;
		Bukkit.broadcastMessage(getCommandMessage("format",
				api.getOnlineAccount(player).getGroup().getPrefix(),
				sender.getName(),
				api.getOnlineAccount(player).getGroup().getSuffix(),
				ChatColor.translateAlternateColorCodes('&', Util.compile(args))));
		return true;
	}
}