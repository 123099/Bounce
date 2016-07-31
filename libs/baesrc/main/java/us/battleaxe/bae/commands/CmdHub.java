package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.dommi2212.BungeeBridge.packets.PacketConnectPlayer;
import me.dommi2212.BungeeBridge.util.ConnectResult;

public final class CmdHub extends CmdExecutor {

	public CmdHub() {
		super("hub", "cmd.hub", false);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 0)) return false;
		Player player = (Player) sender;
	
		String server = plugin.getConfig().getString("hub-server");
		if(api.getBungeename().equals(server)) {
			player.sendMessage(getCommandMessage("already-connected"));
			return false;
		}
		
		PacketConnectPlayer packet = new PacketConnectPlayer(player.getUniqueId(), server);
		api.getOnlineAccount(player).updateLastLocation();
		player.sendMessage(getCommandMessage("connecting"));
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {			
			@Override
			public void run() {
				switch ((ConnectResult) packet.send()) {
				case CONNECT:
					break;
				default:
					player.sendMessage(getCommandMessage("failure"));
					break;
				}
			}
		});
		return true;
	}
}