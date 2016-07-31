package us.battleaxe.bounce.utils;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

import net.minecraft.server.v1_8_R3.ChatModifier;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import us.battleaxe.bounce.packets.EnumChatAction;
import us.battleaxe.bounce.packets.PacketChatData;
import us.battleaxe.bounce.packets.PacketCreator;
import us.battleaxe.bounce.packets.PacketTitleData;

public final class MessageFactory {

	public static final String DeathMessageKeyIdentifier = "%";
	
	public static final String DeathMessageKillerKey = "killer";
	public static final String DeathMessageVictimKey = "victim";
	public static final String DeathMessageReasonKey = "reason";
	
	public static Packet[] createPluginMessage(String message) {
		ChatModifier titleChatModifier = new ChatModifier();
		titleChatModifier.setColor(EnumChatFormat.GREEN);
		PacketTitleData titleData = new PacketTitleData(Constants.PluginName, titleChatModifier);
		
		ChatModifier subTitlechatModifier = new ChatModifier();
		subTitlechatModifier.setColor(EnumChatFormat.BLUE);
		PacketTitleData subtitleData = new PacketTitleData(message, subTitlechatModifier);
		
		Packet titlePacket = PacketCreator.createTitlePacket(titleData, EnumTitleAction.TITLE, 0, 40, 0);
		Packet subtitlePacket = PacketCreator.createTitlePacket(subtitleData, EnumTitleAction.SUBTITLE, 0, 40, 0);
		
		return new Packet[] {titlePacket, subtitlePacket};
	}
	
	public static Packet createActionBarMessage(String message) {
		PacketChatData chatData = new PacketChatData(ChatColor.GRAY + message, EnumChatAction.ActionBar);
		return PacketCreator.createChatPacket(chatData);
	}
	
	public static String formatDeathMessage(String deathMessage, HashMap<String, String> replacementValues) {
		String newDeathMessage = deathMessage + "";
		for(String key : replacementValues.keySet())
			newDeathMessage = newDeathMessage.replace(DeathMessageKeyIdentifier + key, replacementValues.get(key));
		
		return newDeathMessage;
	}
}
