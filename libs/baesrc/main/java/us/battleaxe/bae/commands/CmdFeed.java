package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdFeed extends CmdExecutor {

	public CmdFeed() {
		super("feed", "cmd.feed", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1)) return false;
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			player.setFoodLevel(20);
			player.sendMessage(getCommandMessage("success-self"));
			return true;
		} else {
			if(!hasPermission(sender, "cmd.feed.others")) return false;			
			if(args[0].equals("*")) {
				for(Player online : Bukkit.getOnlinePlayers()) {
					online.setHealth(online.getMaxHealth());
					online.setFoodLevel(20);
					online.sendMessage(getCommandMessage("target-success", getName(sender)));
				}
				sender.sendMessage(getCommandMessage("success-all"));
				return true;
			}
			
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			target.setFoodLevel(20);
			target.sendMessage(getCommandMessage("target-success", getName(sender)));
			sender.sendMessage(getCommandMessage("success-other", target.getName()));
			return true;
		}
	}
}