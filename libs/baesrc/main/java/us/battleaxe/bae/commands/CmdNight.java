package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdNight extends CmdExecutor {
	
	public CmdNight() {
		super("night", "cmd.night", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1)) return false;
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			((Player) sender).getWorld().setTime(13000L);
			sender.sendMessage(getCommandMessage("success-own"));
			return true;
		} else {
			final World world = Bukkit.getWorld(args[0]);
			if(world == null) {
				sender.sendMessage(getCommandMessage("invalid-world", args[0]));
				return false;
			}
			world.setTime(13000L);
			sender.sendMessage(getCommandMessage("success-other", world.getName()));
			return true;
		}
	}
}