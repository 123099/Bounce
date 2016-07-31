package us.battleaxe.bae.commands;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdLightning extends CmdExecutor {
	
	private final int MAX_DISTANCE = 120;
	
	public CmdLightning() {
		super("lightning", "cmd.lightning", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, 1, 1)) return false;
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			Block block = player.getTargetBlock((Set<Material>) null, this.MAX_DISTANCE);
			if(block.getLocation().distanceSquared(player.getLocation()) > this.MAX_DISTANCE * this.MAX_DISTANCE) {
				sender.sendMessage(getCommandMessage("too-far-away"));
				return false;
			}
			Chunk chunk = block.getChunk();
			if(!chunk.isLoaded() && !chunk.load()) {
				plugin.getLogger().severe("An error occurred whilst trying to load chunk X: " + chunk.getX() + " Z: " + chunk.getZ() + " for CmdLightning!");
				return false;
			}
			block.getWorld().strikeLightning(block.getLocation());
			return true;
		} else {
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			target.getWorld().strikeLightning(target.getLocation());
			target.sendMessage(getCommandMessage("target-success", getName(sender)));
			sender.sendMessage(getCommandMessage("success-other", target.getName()));
			return true;
		}
	}
}