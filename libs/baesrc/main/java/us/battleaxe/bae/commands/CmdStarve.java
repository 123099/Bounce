package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;

public final class CmdStarve extends CmdExecutor {

	public CmdStarve() {
		super("starve", "cmd.starve", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 2, 1)) return false;
		if(args.length == 1) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			if(!isPositiveNumber(player, args[0])) return false;
			int starved = Integer.parseInt(args[0]);
			player.setFoodLevel(Math.max(0, player.getFoodLevel() - starved));
			player.sendMessage(getCommandMessage("success-self", Util.formatInt(starved)));
			return true;
		} else {
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			if(!isPositiveNumber(sender, args[1])) return false;
			int starved = Integer.parseInt(args[1]);
			target.setFoodLevel(Math.max(0, target.getFoodLevel() - starved));
			sender.sendMessage(getCommandMessage("success-other", target.getName(), Util.formatInt(starved)));
			return true;
		}
	}
}