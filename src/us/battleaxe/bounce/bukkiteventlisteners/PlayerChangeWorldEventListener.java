package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.ChatColor;
import us.battleaxe.bae.BAEssentials;
import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.GameStatus;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.packets.EnumChatAction;
import us.battleaxe.bounce.packets.PacketChatData;
import us.battleaxe.bounce.packets.PacketCreator;
import us.battleaxe.bounce.packets.PacketSender;
import us.battleaxe.bounce.utils.Constants;

public class PlayerChangeWorldEventListener implements Listener{

	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		
		if(player.getWorld().getName().equalsIgnoreCase("world"))
		{
			player.getInventory().clear();
			
			ItemStack instructions = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta bookMeta = (BookMeta) instructions.getItemMeta();
			
			bookMeta.setTitle(ChatColor.GREEN + "Instruction Manual");
			bookMeta.setAuthor(ChatColor.GREEN + Constants.PluginName);
			
			String page0 = 
					ChatColor.GREEN + "" + ChatColor.BOLD + Constants.PluginName + "\n" +
					ChatColor.DARK_GREEN + "Bounce your way to victory!\n\n" +
					ChatColor.DARK_AQUA + "Every kill awards you with 10 points.\n" +
					"The bow and arrow instantly kill a player.\n" +
					"The sword deals half a player's health in damage.\n" +
					"Right click the sword to call forth it's true power!";
			bookMeta.setPages(page0);
			
			instructions.setItemMeta(bookMeta);
			player.getInventory().addItem(instructions);
			player.getInventory().setHeldItemSlot(0);
			
			player.updateInventory();
		}
		
		if(!GameManager.isPlayerInGame(player))
		{
			if(BAEssentials.getAPI().getStaffManager().isInStaffMode(player))
			{
				PacketChatData chatData = new PacketChatData(ChatColor.RED + "Disable staff mode to join.", EnumChatAction.ActionBar);
				PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
				return;
			}
			
			Bounce bounce = Bounce.getPlugin(Bounce.class);
			GameManager gameManager = bounce.getGameManager(player.getWorld());
			
			if(gameManager != null)
			{
				int playerCount = gameManager.getInGamePlayerCount();
				if(playerCount < Constants.MaxPlayers && gameManager.getGameStatus() != GameStatus.InProgress)
					gameManager.addPlayer(event.getPlayer());
			}
		}
		else
		{
			GameManager gameManager = GameManager.getInstanceForPlayer(player);
			gameManager.removePlayer(player);
			
			PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "Leaving game...", EnumChatAction.ActionBar);
			PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
		}
	}
}
