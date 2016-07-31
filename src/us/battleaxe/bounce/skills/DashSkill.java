package us.battleaxe.bounce.skills;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.gameeventlisteners.ProgressBarChangeEvent;
import us.battleaxe.bounce.gui.ProgressBar;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.Constants;
import us.battleaxe.bounce.utils.events.BukkitRunnableCancelledEvent;
import us.battleaxe.bounce.utils.events.EventListener;

public class DashSkill implements Listener, EventListener{

	private Bounce bounce;
	
	private Player player;
	
	private Material activatorItem;
	private ItemStack lastClickedItemStack;
	private String itemStackOriginalName;
	
	private double travelTime;
	private double speed;
	private double cooldown;
	
	private double hitRadius;
	
	private boolean offCooldown;
	private boolean dashing;
	
	public DashSkill(Bounce bounce, Player player, Material activatorItem, double distance, double travelTime, double cooldown, double hitRadius) {
		this.bounce = bounce;
		this.player = player;
		this.activatorItem = activatorItem;
		this.travelTime = travelTime;
		this.speed = distance / travelTime;
		this.cooldown = cooldown;
		this.hitRadius = hitRadius;
		
		offCooldown = true;
		dashing = false;
	}
	
	@EventHandler
	public void onActivatorRightClick(PlayerInteractEvent event) {
		if(event.getPlayer() != player)
			return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(event.getItem() == null || event.getItem().getType() != activatorItem)
			return;
		
		if(GameManager.isPlayerInGame(player) && offCooldown)
		{
			lastClickedItemStack = event.getItem();
			if(itemStackOriginalName == null)
				itemStackOriginalName = lastClickedItemStack.getItemMeta().getDisplayName();
			
			Vector direction = player.getEyeLocation().getDirection();
			player.setVelocity(direction.clone().multiply(speed));
			
			offCooldown = false;
			Bukkit.getScheduler().scheduleSyncDelayedTask(bounce, () -> offCooldown = true, (long)(cooldown * 20 + 10));
			
			dashing = true;
			Bukkit.getScheduler().scheduleSyncDelayedTask(bounce, () -> dashing = false, (long)(travelTime * 20));
			
			ProgressBar progressBar = new ProgressBar(ChatColor.GREEN, ChatColor.RED, (int)(cooldown * 20), 5, 50);
			progressBar.eventRaiser.registerListener(this);
			progressBar.start();
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.getPlayer() != player)
			return;
		if(GameManager.isPlayerInGame(player) && dashing)
		{
			List<Entity> nearbyEntities = player.getNearbyEntities(hitRadius, hitRadius, hitRadius);
			for(Entity entity : nearbyEntities)
				if(entity instanceof Player)
				{
					Player victim = (Player) entity;
					victim.damage(Constants.DashDamage, player);
				}
		}
	}
	
	public void onProgressBarUpdate(Object sender, ProgressBarChangeEvent event) {
		ItemMeta meta = lastClickedItemStack.getItemMeta();
		meta.setDisplayName(itemStackOriginalName + " " + event.getProgressBar());
		lastClickedItemStack.setItemMeta(meta);
		player.updateInventory();
	}
	
	public void onProgressBarCancelled(Object sender, BukkitRunnableCancelledEvent event) {
		ItemMeta meta = lastClickedItemStack.getItemMeta();
		meta.setDisplayName(itemStackOriginalName);
		lastClickedItemStack.setItemMeta(meta);
		player.updateInventory();
		
		itemStackOriginalName = null;
	}
}
