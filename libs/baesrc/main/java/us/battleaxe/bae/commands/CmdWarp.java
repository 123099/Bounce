package us.battleaxe.bae.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.battleaxe.bae.api.Warp;

import java.util.List;

public final class CmdWarp extends CmdExecutor {
	
	public CmdWarp() {
		super("warp", "cmd.warp", true);
	}

	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1)) return false;
		if(args.length == 0) {
			if(!hasPermission(sender, "cmd.warp.list")) return false;
			
			StringBuilder warpBuilder = new StringBuilder();
			List<Warp> warps = api.getWarpManager().getWarps();
			warps.sort(null);
			for(Warp warp : warps) {
				warpBuilder.append(", ").append(warp.getName());
			}
			
			sender.sendMessage(getCommandMessage("list", warpBuilder.length() == 0 ? "-" : warpBuilder.toString().substring(2)));		
			return true;
		} else {
			if(!isPlayer(sender)) return false;
		
			Warp warp = api.getWarpManager().getWarp(args[0]);
			if(warp == null) {
				sender.sendMessage(getCommandMessage("unknown-warp"));
				return false;
			}
			
			if(!warp.isWorldValid()) {
				sender.sendMessage(getCommandMessage("invalid-world"));
				return false;
			}
			
			if(!warp.teleport((Player) sender)) {
				sender.sendMessage(getCommandMessage("no-permission"));
				return false;
			}
			
			sender.sendMessage(getCommandMessage("success", warp.getName()));
			return true;
		}
	}	
}