package us.battleaxe.bae;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import us.battleaxe.bae.api.WorldSeparator;

public class ListenerWorldSeparator implements Listener {
	
	private final WorldSeparator separator;
	
	public ListenerWorldSeparator(WorldSeparator separator) {
		this.separator = separator;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent e) {
		this.separator.updateWorld(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChangedWorld(PlayerChangedWorldEvent e) {
		this.separator.updateWorld(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		if(this.separator.broadcastDeath(e.getEntity(), e.getDeathMessage())) {
			e.setDeathMessage(null);
		}
	}
	
}