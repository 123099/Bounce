package us.battleaxe.bae;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.OfflinePlayerAccount;

public class ListenerAsyncLogin implements Listener {
	
	private final BAEssentials instance;
	private final API api;
	
	public ListenerAsyncLogin(BAEssentials instance, API api) throws SQLException {
		this.instance = instance;
		this.api = api;
	}
	 
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(AsyncPlayerPreLoginEvent e) {
		UUID uuid = e.getUniqueId();
		if(uuid == null) {
			e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			e.setKickMessage(this.api.getMessage("general.synchronization-failure"));
			return;
		}
		
		try {
			OfflinePlayerAccount account = this.api.loadAccount(uuid);
			if(account != null) {
				if(!account.loadPermissions()) {
					e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
					e.setKickMessage(this.api.getMessage("general.synchronization-failure"));
					return;
				}
			} else {
				try {
					this.api.createAccount(uuid);
				} catch (Exception exc) {
					e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
					e.setKickMessage(this.api.getMessage("general.synchronization-failure"));
					this.instance.getLogger().log(Level.SEVERE, "Failed to create Player-Account [" + uuid.toString() + "]", exc);
				}
			}
		} catch (Exception exc) {
			e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			e.setKickMessage(this.api.getMessage("general.synchronization-failure"));
			this.instance.getLogger().log(Level.SEVERE, "Failed to load Player-Account " + uuid.toString(), exc);
			return;
		}
	}
}