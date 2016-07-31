package us.battleaxe.bounce.bukkiteventlisteners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.GameStatus;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.packets.EnumChatAction;
import us.battleaxe.bounce.packets.PacketChatData;
import us.battleaxe.bounce.packets.PacketCreator;
import us.battleaxe.bounce.packets.PacketSender;
import us.battleaxe.bounce.utils.Constants;
import us.battleaxe.bounce.utils.MessageFactory;
import us.battleaxe.bounce.utils.PlayerExtension;

public class PlayerPvPEventListener implements Listener {
	
	//TODO: Awful code! Rewrite!
	@EventHandler
	public void onPlayerKill(PlayerDeathEvent event) {
		event.getDrops().clear();
		
		if(GameManager.isPlayerInGame(event.getEntity()) && PlayerExtension.GetAttribute(GameManager.class, event.getEntity(), "GameManager").getGameStatus() != GameStatus.InProgress)
		{
			World entityWorld = event.getEntity().getWorld();
			event.getEntity().spigot().respawn();
			event.getEntity().teleport(entityWorld.getSpawnLocation());
			String deathMessage = event.getDeathMessage();
			event.setDeathMessage("");
			List<Player> players = GameManager.getInstanceForPlayer(event.getEntity()).getInGamePlayers();
			for(Player player : players)
				player.sendMessage(deathMessage);
			return;
		}
		
		Player killer = event.getEntity().getKiller();
		if(killer == null || killer == event.getEntity())
		{
			if(GameManager.isPlayerInGame(event.getEntity()))
			{
				HashMap<String, String> replacementValues = new HashMap<>();
				replacementValues.put(MessageFactory.DeathMessageKillerKey, "the void");
				replacementValues.put(MessageFactory.DeathMessageVictimKey, event.getEntity().getName());
				replacementValues.put(MessageFactory.DeathMessageReasonKey, formatDamageCause(event.getEntity().getLastDamageCause().getCause()));
				event.setDeathMessage(MessageFactory.formatDeathMessage(Constants.DeathMessage, replacementValues));
				event.getEntity().spigot().respawn();
				
				String deathMessage = event.getDeathMessage();
				event.setDeathMessage("");
				List<Player> players = GameManager.getInstanceForPlayer(event.getEntity()).getInGamePlayers();
				for(Player player : players)
					player.sendMessage(deathMessage);
			}
			return;
		}
		
		if(GameManager.isPlayerInGame(killer) && GameManager.isPlayerInGame(event.getEntity()))
		{
			GameManager gameManager = PlayerExtension.GetAttribute(GameManager.class, killer, "GameManager");
			gameManager.getScoreManager().AddScore(killer, Constants.PointsPerKill);
			
			HashMap<String, String> replacementValues = new HashMap<>();
			replacementValues.put(MessageFactory.DeathMessageKillerKey, killer.getName());
			replacementValues.put(MessageFactory.DeathMessageVictimKey, event.getEntity().getName());
			replacementValues.put(MessageFactory.DeathMessageReasonKey, formatDamageCause(event.getEntity().getLastDamageCause().getCause()));
			event.setDeathMessage(MessageFactory.formatDeathMessage(Constants.DeathMessage, replacementValues));
			event.getEntity().spigot().respawn();
			
			String deathMessage = event.getDeathMessage();
			event.setDeathMessage("");
			List<Player> players = GameManager.getInstanceForPlayer(event.getEntity()).getInGamePlayers();
			for(Player player : players)
				player.sendMessage(deathMessage);
		}
	}
	
	private String formatDamageCause(DamageCause damageCause) {
		switch(damageCause) {
		case PROJECTILE:
			return "fast moving target";
		case VOID:
			return "darkness";
		case ENTITY_ATTACK:
			return "masterful slashing skills";
		case FIRE_TICK:
		case FIRE:
			return "playing with fire";
		default:
			return damageCause.toString();
		}
	}
	
	@EventHandler
	public void onPlayerHitPlayer(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player))
			return;
		
		if(GameManager.isPlayerInGame((Player)event.getEntity()))
		{
			if(event.getDamager() == event.getEntity())
			{
				event.setCancelled(true);
				return;
			}
			
			GameManager gameManager = GameManager.getInstanceForPlayer((Player)event.getEntity());
			if(gameManager.getGameStatus() != GameStatus.InProgress)
			{
				if(event.getDamager() instanceof Player)
				{
					PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "You cannot PvP yet. Relax for now.", EnumChatAction.ActionBar);
					PacketSender.SendPacket((Player) event.getDamager(), PacketCreator.createChatPacket(chatData));
				}
				event.setCancelled(true);
			}
			else if(event.getDamager() instanceof Arrow)
				event.setDamage(9999);
			else if(event.getDamager() instanceof Player)
			{
				Player damager = (Player) event.getDamager();
				if(damager.getItemInHand().getType() == Material.IRON_SWORD)
					event.setDamage(Constants.SwordDamage);
			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(GameManager.isPlayerInGame(event.getPlayer()))
		{
			GameManager gameManager = GameManager.getInstanceForPlayer(event.getPlayer());
			if(gameManager.getGameStatus() == GameStatus.InProgress)
			{
				Bukkit.getScheduler().scheduleSyncDelayedTask(Bounce.getPlugin(Bounce.class), () -> gameManager.equipPlayer(event.getPlayer()), 1);
				event.setRespawnLocation(gameManager.getSpawnLocation());
			}
		}
	}
	
	@EventHandler
	public void onPlayerHealthRegen(EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(GameManager.isPlayerInGame(player))
				event.setCancelled(true);
		}
	}
	
	/*
	@EventHandler
	public void onPlayerShootArrow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player)	
		{
			Player player = (Player) event.getEntity();
			if(GameManager.isPlayerInGame(player))
			{
				if(event.getProjectile() instanceof Arrow)
				{
					Arrow arrow = (Arrow) event.getProjectile();
					Vector velocity = arrow.getVelocity().clone();
					Vector sidePlane = velocity.clone().crossProduct(new Vector(0,1,0));
					Vector arrowUp = sidePlane.clone().crossProduct(velocity).normalize();
					
					Arrow topArrow = player.launchProjectile(Arrow.class, velocity.multiply(0.8));
					Arrow bottomArrow = player.launchProjectile(Arrow.class, velocity.multiply(1.2));
					
					topArrow.teleport(arrow.getLocation().clone().add(arrowUp.clone().multiply(0.5)));
					bottomArrow.teleport(arrow.getLocation().clone().add(arrowUp.clone().multiply(-0.5)));
				}
			}
		}
	}
	*/
}
