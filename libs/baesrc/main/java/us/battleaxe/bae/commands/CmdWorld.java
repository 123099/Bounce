package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdWorld extends CmdExecutor {

	public CmdWorld() {
		super("world", "cmd.world", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		Player player = (Player) sender;
		
		if(args[0].equalsIgnoreCase("list")) {
			StringBuilder builder = new StringBuilder();
			for(World world : Bukkit.getWorlds()) {
				builder.append(", ").append(world.getName());
			}
			player.sendMessage(getCommandMessage("list", builder.toString().substring(2)));
		} else {
			World world = Bukkit.getWorld(args[0]);
			if(world == null) {
				player.sendMessage(getCommandMessage("unknown-world"));
				return false;
			}
			player.teleport(world.getSpawnLocation());
			player.sendMessage(getCommandMessage("success", world.getName()));
		}
		return true;
	}
}