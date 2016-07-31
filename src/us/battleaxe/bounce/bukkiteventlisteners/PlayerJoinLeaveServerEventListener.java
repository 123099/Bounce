package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.PlayerExtension;

public class PlayerJoinLeaveServerEventListener implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Bounce bounce = Bounce.getPlugin(Bounce.class);
		GameManager gameManager = bounce.getGameManager(event.getPlayer().getWorld());
		if(gameManager != null)
			gameManager.teleportPlayerOutside(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if(GameManager.isPlayerInGame(event.getPlayer()))
		{
			GameManager gameManager = GameManager.getInstanceForPlayer(event.getPlayer());
			gameManager.removePlayer(event.getPlayer());
		}
	}
}
