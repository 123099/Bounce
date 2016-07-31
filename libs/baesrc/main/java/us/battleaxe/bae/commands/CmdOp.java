package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdOp extends CmdExecutor {
	
	public CmdOp() {
		super("op", "cmd.op", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		if(!isConsole(sender)) return false;
		
		Player target = Bukkit.getPlayerExact(args[0]);
		if(target == null) return noPlayer(sender, args[0]);
		if(target.isOp()) {
			sender.sendMessage(getCommandMessage("already-op", target.getName()));
			return false;
		}
		target.setOp(true);
		sender.sendMessage(getCommandMessage("success", target.getName()));
		return true;
	}
}