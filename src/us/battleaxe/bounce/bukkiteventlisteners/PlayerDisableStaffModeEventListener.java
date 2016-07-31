package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import us.battleaxe.bae.events.PlayerDisableStaffModeEvent;
import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.managers.GameManager;

public class PlayerDisableStaffModeEventListener implements Listener{

	@EventHandler
	public void onPlayerDisableStaffMode(PlayerDisableStaffModeEvent event) {
		Bounce bounce = Bounce.getPlugin(Bounce.class);
		GameManager gameManager = bounce.getGameManager(event.getPlayer().getWorld());
		if(gameManager != null)
			event.setSpawn(gameManager.getLobbySpawnPoint());
	}
}
