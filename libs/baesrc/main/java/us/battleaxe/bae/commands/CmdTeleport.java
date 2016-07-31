package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public final class CmdTeleport extends CmdExecutor {
	
	public CmdTeleport() {
		super("teleport", "cmd.teleport", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 4, 1)) return false;
		if(args.length == 1) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			
			api.getOnlineAccount(player).updateLastLocation();
			player.teleport(target);
			sender.sendMessage(getCommandMessage("sender-teleported-to-target", target.getName()));
			target.sendMessage(getCommandMessage("target-teleported-to-target", getName(sender)));
			return true;
		}
		if(args.length == 2) {
			Player player = Bukkit.getPlayerExact(args[0]);
			if(player == null) return noPlayer(sender, args[0]);
			Player target = Bukkit.getPlayerExact(args[1]);
			if(target == null) return noPlayer(sender, args[1]);
			
			api.getOnlineAccount(player).updateLastLocation();
			player.teleport(target);
			sender.sendMessage(getCommandMessage("sender-teleported-player-to-target", player.getName(), target.getName()));
			target.sendMessage(getCommandMessage("player-teleported-player-to-target", getName(sender), player.getName()));
			player.sendMessage(getCommandMessage("target-teleported-player-to-target", getName(sender), target.getName()));
			return true;
		}
		if(args.length == 3) {
			if(!isPlayer(sender, true)) return false;
			try {
				double x = Double.parseDouble(args[0]);
				double y = Double.parseDouble(args[1]);
				double z = Double.parseDouble(args[2]);				
				Player player = (Player) sender;
				
				api.getOnlineAccount(player).updateLastLocation();
				player.teleport(new Location(player.getWorld(), x, y, z));
				sender.sendMessage(getCommandMessage("teleported-self-to-location", args[0], args[1], args[2]));
				return true;
			} catch (NumberFormatException exc) {
				sender.sendMessage(getCommandMessage("invalid-cords"));
				return false;
			}
		}
		if(args.length == 4) {
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			try {
				double x = Double.parseDouble(args[1]);
				double y = Double.parseDouble(args[2]);
				double z = Double.parseDouble(args[3]);
				
				api.getOnlineAccount(target).updateLastLocation();
				target.teleport(new Location(target.getWorld(), x, y, z));
				sender.sendMessage(getCommandMessage("sender-teleported-other-to-location", target.getName(), args[1], args[2], args[3]));
				target.sendMessage(getCommandMessage("target-teleported-other-to-location", getName(sender), args[1], args[2], args[3]));
				return true;
			} catch (NumberFormatException exc) {
				sender.sendMessage(getCommandMessage("invalid-cords"));
				return false;
			}
		}
		return false;
	}
}