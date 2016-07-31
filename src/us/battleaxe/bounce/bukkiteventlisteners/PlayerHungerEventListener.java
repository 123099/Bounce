package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import us.battleaxe.bounce.managers.GameManager;

public class PlayerHungerEventListener implements Listener{

	@EventHandler
	public void onPlayerHungerChange(FoodLevelChangeEvent event) {
		if(!(event.getEntity() instanceof Player))
			return;
		
		if(GameManager.isPlayerInGame((Player)event.getEntity()))
			event.setCancelled(true);
	}
}
