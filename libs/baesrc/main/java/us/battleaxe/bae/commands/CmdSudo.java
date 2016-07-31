package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;

public final class CmdSudo extends CmdExecutor {
	
	public CmdSudo() {
		super("sudo", "cmd.sudo", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, -1, 2)) return false;
		Player target = Bukkit.getPlayerExact(args[0]);
		if(target == null) return noPlayer(sender, args[0]);
		String msg = Util.compile(args, 1);
		if(msg.startsWith("c:")) {
			target.chat(msg.substring(2));
			sender.sendMessage(getCommandMessage("success-chat", true, target.getName(), msg.substring(2)));
			return true;
		}
		if(!msg.startsWith("/")) {
			sender.sendMessage(getCommandMessage("slash-needed"));
			return false;
		}
		target.chat(msg);
		sender.sendMessage(getCommandMessage("success-command", true, target.getName(), msg));
		return true;
	}
}