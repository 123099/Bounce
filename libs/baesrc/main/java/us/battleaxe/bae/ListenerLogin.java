package us.battleaxe.bae;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ListenerLogin implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerLoginEvent e) {
		if(!BAEssentials.isAccessible()) {
			e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
			e.setKickMessage("§cThis server is currently booting or experiencing technical problems! "
					+ "§cPlease try again later and contact an administrator if this problems still persists! "
					+ "§cWe are sorry for the inconvenience :/");
		}
	}
}