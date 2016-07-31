package us.battleaxe.bae.commands;

import java.util.Arrays;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.dommi2212.BungeeBridge.packets.PacketGetServerByPlayer;
import me.dommi2212.BungeeBridge.packets.PacketIsPlayerOnline;
import me.dommi2212.BungeeBridge.util.IsOnlineResult;
import us.battleaxe.bae.Util;
import us.battleaxe.bae.api.OfflinePlayerAccount;
import us.battleaxe.bae.api.UUIDFetcher;

public final class CmdPlayerdata extends CmdExecutor {
	
	public CmdPlayerdata() {
		super("playerdata", "cmd.playerdata", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 1, 1)) return false;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new CommandRunnable(sender, args));
		return true;
	}	
	
	private class CommandRunnable implements Runnable {
		
		private final CommandSender sender;
		private final String[] args;
		
		public CommandRunnable(CommandSender sender, String[] args) {
			this.sender = sender;
			this.args = args;
		}
		
		@Override
		public void run() {
			OfflinePlayerAccount account;
			if(Bukkit.getPlayer(args[0]) != null) {
				account = api.getOfflineAccount(Bukkit.getPlayer(args[0]).getUniqueId());
			} else {
				try {
					sender.sendMessage(getCommandMessage("player-loading", args[0]));
					UUID uuid = new UUIDFetcher(Arrays.asList(args[0])).call().get(args[0]);
					if(uuid == null) {
						sender.sendMessage(getCommandMessage("player-never-joined-before"));
						return;
					}
					account = api.loadAccount(uuid);
					if(account == null) {
						sender.sendMessage(getCommandMessage("player-never-joined-before"));
						return;
					}
				} catch (Exception exc) {
					sender.sendMessage(getCommandMessage("player-fetch-failure", args[0], exc.getMessage()));
					exc.printStackTrace();
					return;
				}
			}
			
			String server = null;
			PacketIsPlayerOnline isOnline = new PacketIsPlayerOnline(account.getUUID());
			switch ((IsOnlineResult) isOnline.send()) {
			case OFFLINE:
				server = "-";
				break;
			case ONLINE:
				PacketGetServerByPlayer getServer = new PacketGetServerByPlayer(account.getUUID());
				server = (String) getServer.send();
				break;
			case UNKNOWN:
			case UUID_AND_NAME_NULL:
				server = "§cError";
				break;
			default:
				break;
			}
			
			sender.sendMessage(getCommandMessage("success", args[0], Util.formatInt(account.getId()),
					DatatypeConverter.printHexBinary(Util.toByteArray(account.getUUID())),
					account.getGroup().getName(), Util.formatInt(account.getGroup().getId()),
					Util.formatInt(account.getGold()),
					server));
		}
	}
}