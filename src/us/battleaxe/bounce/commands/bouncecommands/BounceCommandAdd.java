package us.battleaxe.bounce.commands.bouncecommands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import us.battleaxe.bounce.commands.PlayerCommandExecutor;
import us.battleaxe.bounce.permissions.Permissions;
import us.battleaxe.bounce.utils.Constants;
import us.battleaxe.bounce.utils.PlayerExtension;

public class BounceCommandAdd extends PlayerCommandExecutor {

	@Override
	public boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
		if(!player.hasPermission(Permissions.Add))
		{
			player.sendMessage(Constants.PluginTag + "Sorry, you do not have permission to use this command.");
			return false;
		}
		
		if(args.length != 2)
		{
			player.sendMessage(Constants.PluginTag + "Please provide the name of the game world this sign should be tracking the status of.");
			return false;
		}
		
		PlayerExtension.AddAttribute(player, "CreatingSign", args[1]);
		player.sendMessage(Constants.PluginTag + "Right click on a sign to set it as a status sign of game world " + args[0]);
		
		return true;
	}

}
