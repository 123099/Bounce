package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

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
