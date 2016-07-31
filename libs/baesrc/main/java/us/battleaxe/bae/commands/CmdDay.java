package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdDay extends CmdExecutor {
	
	public CmdDay() {
		super("day", "cmd.day", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1)) return false;
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			((Player) sender).getWorld().setTime(0L);
			sender.sendMessage(getCommandMessage("success-own"));
			return true;
		} else {
			final World world = Bukkit.getWorld(args[0]);
			if(world == null) {
				sender.sendMessage(getCommandMessage("invalid-world"));
				return false;
			}
			world.setTime(0L);
			sender.sendMessage(getCommandMessage("success-other", world.getName()));
			return true;
		}
	}
}