package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;

public final class CmdHurt extends CmdExecutor {

	public CmdHurt() {
		super("hurt", "cmd.hurt", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 2, 1)) return false;
		if(args.length == 1) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			if(!isPositiveNumber(sender, args[0])) return false;
			int damage = Integer.parseInt(args[0]);
			player.damage(damage);
			player.sendMessage(getCommandMessage("success-self", Util.formatInt(damage)));
			return true;
		} else {
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			if(!isPositiveNumber(sender, args[1])) return true;
			int damage = Integer.parseInt(args[1]);
			target.damage(damage);
			sender.sendMessage(getCommandMessage("success-other", target.getName(), Util.formatInt(damage)));
			return true;
		}
	}
}