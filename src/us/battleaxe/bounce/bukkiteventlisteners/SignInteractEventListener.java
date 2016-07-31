package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import net.md_5.bungee.api.ChatColor;
import us.battleaxe.bae.BAEssentials;
import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.GameStatus;
import us.battleaxe.bounce.managers.ConfigManager;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.packets.EnumChatAction;
import us.battleaxe.bounce.packets.PacketChatData;
import us.battleaxe.bounce.packets.PacketCreator;
import us.battleaxe.bounce.packets.PacketSender;
import us.battleaxe.bounce.permissions.Permissions;
import us.battleaxe.bounce.utils.PlayerExtension;

public class SignInteractEventListener implements Listener {

	private Bounce bounce;
	private ConfigManager configManager;
	
	public SignInteractEventListener(Bounce bounce) {
		this.bounce = bounce;
		configManager = bounce.getConfigManager();
	}
	
	@EventHandler
	public void onSignInteractEvent(PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		
		if(clickedBlock == null)
			return;
		
		if(clickedBlock.getState() instanceof Sign)
		{
			Sign sign = (Sign) clickedBlock.getState();
			
			Object playerCreatingSignAttribute = PlayerExtension.GetAttribute(event.getPlayer(), "CreatingSign");
			
			if(playerCreatingSignAttribute != null)
				handleSignCreation(sign, event, (String) playerCreatingSignAttribute);
			else
				handleMinigamePlayer(sign, event);
		}
	}
	
	@EventHandler
	public void onSignBreakEvent(BlockBreakEvent event) {
		Block block = event.getBlock();
		
		if(block == null)
			return;
		
		if(block.getState() instanceof Sign)
		{
			Sign sign = (Sign) block.getState();
			String firstLine = sign.getLine(0);
			firstLine = ChatColor.stripColor(firstLine);
			
			Bounce bounce = Bounce.getPlugin(Bounce.class);
			GameManager gameManager = bounce.getGameManager(Bukkit.getWorld(firstLine));
			
			if(gameManager != null)
				if(event.getPlayer().hasPermission(Permissions.Destroy))
				{
					bounce.removeGameManager(gameManager);
					PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "Successfully removed world " + firstLine, EnumChatAction.ActionBar);
					PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
				}
				else
				{
					PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "Can't destroy sign. It's quite important for the game.", EnumChatAction.ActionBar);
					PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
					event.setCancelled(true);
				}
		}
	}
	
	private void handleSignCreation(Sign sign, PlayerInteractEvent event, String worldName) {
		configManager.setGameWorldSign(worldName, sign);
		bounce.reloadGames();
		
		PlayerExtension.RemoveAttribute(event.getPlayer(), "CreatingSign");
		PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "Successfully created status sign for world " + worldName + "!", EnumChatAction.ActionBar);
		PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
		event.setCancelled(true);
	}
	
	private void handleMinigamePlayer(Sign sign, PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		if(BAEssentials.getAPI().getStaffManager().isInStaffMode(player))
		{
			PacketChatData chatData = new PacketChatData(ChatColor.RED + "Disable staff mode to join.", EnumChatAction.ActionBar);
			PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
			return;
		}
		
		String firstLine = sign.getLine(0);
		firstLine = ChatColor.stripColor(firstLine).trim();
		
		World world = Bukkit.getServer().getWorld(firstLine);
		if(world != null)
		{
			GameManager gameManager = Bounce.getPlugin(Bounce.class).getGameManager(world);
			if(gameManager != null)
			{
				if(gameManager.getInGamePlayerCount() >= gameManager.getMaxPlayers())
				{
					PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "The game is full.", EnumChatAction.ActionBar);
					PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
				}
				else if(gameManager.getGameStatus() == GameStatus.InProgress)
				{
					PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "The game is already in progress. Try again later.", EnumChatAction.ActionBar);
					PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
				}
				else
				{
					PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "You are being teleported to the game world...", EnumChatAction.ActionBar);
					PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
					player.teleport(world.getSpawnLocation());
				}
			}
		}
	}
}
