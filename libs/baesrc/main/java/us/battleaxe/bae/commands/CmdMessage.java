package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;
import us.battleaxe.bae.api.OnlinePlayerAccount;

public final class CmdMessage extends CmdExecutor {
	
	public CmdMessage() {
		super("message", "cmd.message", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, -1, 2)) return false;
		Player player = (Player) sender;
		Player target = Bukkit.getPlayerExact(args[0]);
		if(target == null) return noPlayer(sender, args[0]);
		String msg = Util.compile(args, 1);
		OnlinePlayerAccount playerAccount = api.getOnlineAccount(player);
		OnlinePlayerAccount targetAccount = api.getOnlineAccount(target);
		
		playerAccount.setLastMessageRecipient(targetAccount);
		player.sendMessage(getCommandMessage("sent", target.getName(), msg));
		targetAccount.setLastMessageRecipient(playerAccount);
		target.sendMessage(getCommandMessage("received", player.getName(), msg));
		return true;
	}
}