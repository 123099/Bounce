package us.battleaxe.bae.commands;

import org.bukkit.command.CommandSender;

public final class CmdDeleteWarp extends CmdExecutor {

	public CmdDeleteWarp() {
		super("deletewarp", "cmd.deletewarp", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		if(!api.getWarpManager().exists(args[0])) {
			sender.sendMessage(getCommandMessage("unknown-warp"));
			return false;
		}
		
		api.getWarpManager().unregisterWarp(args[0]);
		if(api.getWarpManager().save()) {
			sender.sendMessage(getCommandMessage("deletion-success"));
		} else {
			sender.sendMessage(getCommandMessage("deletion-failure"));
		}		
		return true;
	}
}