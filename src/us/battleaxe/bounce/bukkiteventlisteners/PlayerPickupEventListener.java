package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import us.battleaxe.bounce.managers.GameManager;

public class PlayerPickupEventListener implements Listener{

	@EventHandler
	public void onPlayerPickupArrow(PlayerPickupItemEvent event) {
		if(GameManager.isPlayerInGame(event.getPlayer()))
			if(event.getItem().getItemStack().getType() == Material.ARROW)
				event.setCancelled(true);
	}
}
