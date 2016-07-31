package us.battleaxe.bounce.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.GameStatus;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.packets.EnumChatAction;
import us.battleaxe.bounce.packets.PacketChatData;
import us.battleaxe.bounce.packets.PacketCreator;
import us.battleaxe.bounce.packets.PacketSender;
import us.battleaxe.bounce.utils.Constants;

public class PlayerCommandQuit extends PlayerCommandExecutor{

	@Override
	public boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
		Bounce bounce = Bounce.getPlugin(Bounce.class);
		GameManager gameManager = bounce.getGameManager(player.getWorld());
		if(gameManager != null)
		{
			if(gameManager.getGameStatus() == GameStatus.Starting)
			{
				PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "You cannot quit now. Game about to start.", EnumChatAction.ActionBar);
				PacketSender.SendPacket(player, PacketCreator.createChatPacket(chatData));
				return false;
			}
			
			PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "Quitting game...", EnumChatAction.ActionBar);
			PacketSender.SendPacket(player, PacketCreator.createChatPacket(chatData));
			gameManager.removePlayer(player);
			return true;
		}
		
		return false;
	}
}
