package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CmdInvSee extends CmdExecutor {

	public CmdInvSee() {
		super("invsee", "cmd.invsee", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		Player player = (Player) sender;
		Player target = Bukkit.getPlayerExact(args[0]);
		if(target == null) return noPlayer(sender, args[0]);
		
		player.openInventory(target.getInventory());
		return true;
	}
}