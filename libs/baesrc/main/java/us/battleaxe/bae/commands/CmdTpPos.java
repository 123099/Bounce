package us.battleaxe.bae.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdTpPos extends CmdExecutor {
	
	public CmdTpPos() {
		super("tppos", "cmd.tppos", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(args.length != 3 && args.length != 5) {
			sendUsageMessage(sender);
			return false;
		}
		try {
			double x = Double.parseDouble(args[0]);
			double y = Double.parseDouble(args[1]);
			double z = Double.parseDouble(args[2]);
			float yaw = args.length == 5 ? Float.parseFloat(args[3]) : 0f;
			float pitch = args.length == 5 ? Float.parseFloat(args[4]) : 0f;
			Player player = (Player) sender;
			
			api.getOnlineAccount(player).updateLastLocation();
			player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
			if(args.length == 5) {
				player.sendMessage(getCommandMessage("success-xyz", args[0], args[1], args[2], args[3], args[4]));
			} else {
				player.sendMessage(getCommandMessage("success-all", args[0], args[1], args[2], "0", "0"));
			}
			return true;
		} catch (NumberFormatException exc) {
			sender.sendMessage(getCommandMessage("invalid-cords"));
			return false;
		}
	}
}