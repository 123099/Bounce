package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdKill extends CmdExecutor {
	
	public CmdKill() {
		super("kill", "cmd.kill", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1)) return false;
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			player.setHealth(0);
			player.sendMessage(getCommandMessage("success-self"));
			return true;
		} else {
			if(!hasPermission(sender, "cmd.kill.others")) return false;
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			target.setHealth(0);
			target.sendMessage(getCommandMessage("target-success", getName(sender)));
			sender.sendMessage(getCommandMessage("success-other", target.getName()));
			return true;
		}
	}
}