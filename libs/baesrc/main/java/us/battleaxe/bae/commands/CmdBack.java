package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.api.OnlinePlayerAccount;

public final class CmdBack extends CmdExecutor {

	public CmdBack() {
		super("back", "cmd.back", true);
	}

	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1)) return false;
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			OnlinePlayerAccount account = api.getOnlineAccount(player);
			Location last = account.getLastLocation();
			
			if(last == null) {
				player.sendMessage(getCommandMessage("no-last-location"));
				return false;
			}
			
			account.updateLastLocation();
			player.teleport(last);
			sender.sendMessage(getCommandMessage("success-self"));
			return true;
		}
		
		if(args.length == 1) {
			if(!hasPermission(sender, "cmd.back.others")) return false;
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			
			OnlinePlayerAccount account = api.getOnlineAccount(target);
			Location last = account.getLastLocation();
			
			if(last == null) {
				sender.sendMessage(getCommandMessage("no-last-location"));
				return false;
			}
			
			account.updateLastLocation();
			target.teleport(last);
			target.sendMessage(getCommandMessage("teleported", getName(sender)));
			sender.sendMessage(getCommandMessage("success-other", target.getName()));
			return true;
		}
		return false;
	}	
}