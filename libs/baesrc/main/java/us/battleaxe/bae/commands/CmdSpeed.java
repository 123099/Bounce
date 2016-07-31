package us.battleaxe.bae.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;

public final class CmdSpeed extends CmdExecutor {
	
	public CmdSpeed() {
		super("speed", "cmd.speed", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		Player player = (Player) sender;
		if(!isPositiveNumber(sender, args[0])) return false;
		int input = Math.max(0, Math.min(9, Integer.parseInt(args[0])));
		float speed =  ((float) input) / ((float) 10);
		player.setWalkSpeed(speed + 0.1f);
		player.setFlySpeed(speed);
		player.sendMessage(getCommandMessage("success", Util.formatInt(input)));
		return true;
	}
}