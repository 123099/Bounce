package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.api.OnlinePlayerAccount;

public final class CmdTpAccept extends CmdExecutor {
	
	public CmdTpAccept() {
		super("tpaccept", "cmd.tpaccept", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		Player player = (Player) sender;
		Player issuer = Bukkit.getPlayerExact(args[0]);
		if(issuer == null) return noPlayer(sender, args[0]);
		OnlinePlayerAccount playerAccount = api.getOnlineAccount(player);
		OnlinePlayerAccount issuerAccount = api.getOnlineAccount(player);
		
		if(!issuerAccount.hasTeleportationRequest(issuer)) {
			player.sendMessage(getCommandMessage("no-request"));
			return false;
		}
		
		playerAccount.updateLastLocation();
		player.teleport(issuer);
		player.sendMessage(getCommandMessage("success", issuer.getName()));
		issuerAccount.removeTeleportationRequest(issuer);
		issuer.sendMessage(getCommandMessage("target-success", player.getName()));
		return true;
	}
}