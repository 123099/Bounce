package us.battleaxe.bae.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;
import us.battleaxe.bae.api.OnlinePlayerAccount;

public final class CmdReply extends CmdExecutor {
	
	public CmdReply() {
		super("reply", "cmd.reply", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, -1, 1)) return false;
		Player player = (Player) sender;
		OnlinePlayerAccount playerAccount = api.getOnlineAccount(player);
		OnlinePlayerAccount targetAccount = playerAccount.getLastMessageRecipient();
		if(targetAccount == null) {
			player.sendMessage(getCommandMessage("recipient-not-online"));
			return false;
		}
		String msg = Util.compile(args);
		Player target = targetAccount.getPlayer();
		
		player.sendMessage(api.getMessage("command.message.sent", target.getName(), msg));
		targetAccount.setLastMessageRecipient(playerAccount);
		target.sendMessage(api.getMessage("command.message.received", player.getName(), msg));
		return true;
	}
}