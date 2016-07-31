package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;

public final class CmdPing extends CmdExecutor {
	
	public CmdPing() {
		super("ping", "cmd.ping", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1)) return false;
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			
			int ping = api.getOnlineAccount(player).getPing();
			if(ping != -1) {
				player.sendMessage(getCommandMessage("success-self", Util.formatInt(ping)));
			} else {
				player.sendMessage(getCommandMessage("failure-self"));
			}
			return true;
		} else {
			if(!hasPermission(sender, "cmd.ping.others")) return false;
			Player player = Bukkit.getPlayerExact(args[0]);
			if(player == null) return noPlayer(sender, args[0]);
			
			int ping = api.getOnlineAccount(player).getPing();
			if(ping != -1) {
				sender.sendMessage(getCommandMessage("success-other", player.getName(), Util.formatInt(ping)));
			} else {
				sender.sendMessage(getCommandMessage("failure-other", player.getName()));
			}
			return true;
		}
	}	
}