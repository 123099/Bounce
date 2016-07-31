package us.battleaxe.bounce.bukkiteventlisteners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import net.md_5.bungee.api.ChatColor;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.packets.EnumChatAction;
import us.battleaxe.bounce.packets.PacketChatData;
import us.battleaxe.bounce.packets.PacketCreator;
import us.battleaxe.bounce.packets.PacketSender;
import us.battleaxe.bounce.permissions.Permissions;

public class BlockBreakEventListener implements Listener{

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		
		if(block == null)
			return;
		
		if(GameManager.isPlayerInGame(event.getPlayer()) && !event.getPlayer().hasPermission(Permissions.Destroy))
		{
			PacketChatData chatData = new PacketChatData(ChatColor.GRAY + "You cannot destroy blocks, only hop on them.", EnumChatAction.ActionBar);
			PacketSender.SendPacket(event.getPlayer(), PacketCreator.createChatPacket(chatData));
			event.setCancelled(true);
		}
	}
}
