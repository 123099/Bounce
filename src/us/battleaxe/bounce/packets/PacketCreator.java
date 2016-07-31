package us.battleaxe.bounce.packets;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public final class PacketCreator {

	public static PacketPlayOutTitle createTitlePacket(PacketTitleData titleData, EnumTitleAction titleAction, int fadeInTime, int stayTime, int fadeOutTime) {
		ChatComponentText chatComponentText = new ChatComponentText(titleData.getText());
		chatComponentText.setChatModifier(titleData.getChatModifier());

		PacketPlayOutTitle packet = new PacketPlayOutTitle(titleAction, chatComponentText, fadeInTime, stayTime, fadeOutTime);
		return packet;
	}
	
	public static PacketPlayOutChat createChatPacket(PacketChatData chatData) {
		ChatComponentText chatComponentText = new ChatComponentText(chatData.getText());
		
		PacketPlayOutChat packet = new PacketPlayOutChat(chatComponentText, (byte) chatData.getChatAction().ordinal());
		return packet;
	}
	
	public static PacketPlayOutWorldParticles createParticlePacket(PacketParticleData particleData) {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles
		(
			particleData.getParticleType(),
			true,
			particleData.getLocationX(),
			particleData.getLocationY(),
			particleData.getLocationZ(),
			particleData.getOffsetX(),
			particleData.getOffsetY(),
			particleData.getOffsetZ(),
			particleData.getSpeed(),
			particleData.getCount()
		);
		
		return packet;
	}
}
