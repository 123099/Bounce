package us.battleaxe.bae;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ListenerCommand implements Listener {
	
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
		String command = e.getMessage();
		if(command.contains("minecraft") || command.contains("bukkit")) {
			e.setMessage(command.replace("minecraft", "").replace("bukkit", "").replace(":", ""));
		}
	}
}