package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdTeleportHere extends CmdExecutor {
	
	public CmdTeleportHere() {
		super("teleporthere", "cmd.tphere", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		Player player = (Player) sender;
		Player target = Bukkit.getPlayerExact(args[0]);
		if(target == null) return noPlayer(sender, args[0]);
		
		api.getOnlineAccount(target).updateLastLocation();
		target.teleport(player);
		player.sendMessage(getCommandMessage("sender-teleported", target.getName()));
		target.sendMessage(getCommandMessage("target-teleported", player.getName()));
		return true;
	}
}