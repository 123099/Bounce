package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import us.battleaxe.bounce.GameStatus;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.Constants;
import us.battleaxe.bounce.utils.PlayerExtension;
import us.battleaxe.bounce.utils.VectorExtension;

public class BlockBounceEventListener implements Listener{

	@EventHandler
	public void onFallDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player)) 
			return;

		if(GameManager.isPlayerInGame((Player)event.getEntity()))
			if(event.getCause() == DamageCause.FALL)
				event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBounce(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		GameManager gameManager = GameManager.getInstanceForPlayer(player);

		if(GameManager.isPlayerInGame(player) && gameManager.getGameStatus() == GameStatus.InProgress) //If player is in game, the game manager attribute is never null
		{
			Location locationUnder = event.getTo().clone().add(0, -1, 0);
			
			if(isLocationSolid(locationUnder))
			{
				Vector currentVelocity = player.getVelocity();
				
				if(currentVelocity.getY() < -0.1)
				{
					Vector reflectedVelocity = null;
					if(currentVelocity.getY() < -0.2)
						reflectedVelocity = VectorExtension.reflectAlong(currentVelocity, new Vector(0,1,0), 1.6f);
					else
						reflectedVelocity = VectorExtension.reflectAlong(currentVelocity, new Vector(0,1,0), 3f);
					
					if(reflectedVelocity.length() > Constants.MaxSpeed)
						reflectedVelocity.normalize().multiply(Constants.MaxSpeed);
					player.setVelocity(reflectedVelocity);
				}
			}
		}
	}
	
	private boolean isLocationSolid(Location centerLocation) {
		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 3; ++j)
			{
				Location loc = centerLocation.clone().add(i - 1, 0, j - 1);
				if(loc.getBlock().getType() != Material.AIR)
					return true;
			}
		
		return false;
	}
}
