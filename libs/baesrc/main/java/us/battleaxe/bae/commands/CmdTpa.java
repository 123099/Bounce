package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;
import us.battleaxe.bae.api.OnlinePlayerAccount;

public final class CmdTpa extends CmdExecutor {
	
	public CmdTpa() {
		super("tpa", "cmd.tpa", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		Player player = (Player) sender;
		Player target = Bukkit.getPlayerExact(args[0]);
		if(target == null) return noPlayer(sender, args[0]);
		OnlinePlayerAccount account = api.getOnlineAccount(target);
		
		if(account.hasTeleportationRequest(player)) {
			player.sendMessage(getCommandMessage("already-sent", target.getName()));
			return false;
		}
		
		account.issueTeleportationRequest(player);
		target.spigot().sendMessage(getComponentCommandMessage("received", sender.getName(), Util.formatLong(api.getTeleportationRequestTimeout())));
		player.sendMessage(getCommandMessage("sent", target.getName(), Util.formatLong(api.getTeleportationRequestTimeout())));
		return true;
	}
}