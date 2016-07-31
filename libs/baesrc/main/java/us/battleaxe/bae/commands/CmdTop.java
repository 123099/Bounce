package us.battleaxe.bae.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdTop extends CmdExecutor {
	
	public CmdTop() {
		super("top", "cmd.top", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 0)) return false;
		Player player = (Player) sender;
		
		Location location = player.getLocation();
		double y = (double) player.getWorld().getHighestBlockYAt(location);
		if(y <= location.getY()) {
			player.sendMessage(getCommandMessage("no-block-above"));
			return false;
		}
		location.setY(y);
		api.getOnlineAccount(player).updateLastLocation();
		player.teleport(location);
		player.sendMessage(getCommandMessage("success"));
		return true;
	}
}