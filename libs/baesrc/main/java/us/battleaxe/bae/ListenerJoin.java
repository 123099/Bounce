package us.battleaxe.bae;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.OfflinePlayerAccount;

public class ListenerJoin implements Listener {

	private final BAEssentials instance;
	private final API api;
	
	public ListenerJoin(BAEssentials instance, API api) {
		this.instance = instance;
		this.api = api;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerLoginEvent e) {
		OfflinePlayerAccount account = this.api.getOfflineAccount(e.getPlayer().getUniqueId());
		try {
			account.login(e.getPlayer());
		} catch (Exception exc) {
			e.setResult(Result.KICK_OTHER);
			e.setKickMessage(this.api.getMessage("general.synchronization-failure"));
			this.instance.getLogger().log(Level.SEVERE, "Failed to process Player-Login for player " + e.getPlayer().getUniqueId().toString(), exc);
			return;
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);
	}

}