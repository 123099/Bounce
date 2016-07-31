package us.battleaxe.bae;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import us.battleaxe.bae.api.API;

public class ListenerDeath implements Listener {
	
	private final BAEssentials instance;
	private final API api;
	
	public ListenerDeath(BAEssentials instance, API api) {
		this.instance = instance;
		this.api = api;
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		final Player player = e.getEntity();
		this.api.getOnlineAccount(player).updateLastLocation();
		Bukkit.getScheduler().runTask(this.instance, new Runnable() {
			@Override
			public void run() {
				player.spigot().respawn();
			}
		});
	}
}