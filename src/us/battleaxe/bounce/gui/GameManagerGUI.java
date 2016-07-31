package us.battleaxe.bounce.gui;

import net.minecraft.server.v1_8_R3.Packet;
import us.battleaxe.bounce.events.GameEndEvent;
import us.battleaxe.bounce.events.GameStartEvent;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.MessageFactory;
import us.battleaxe.bounce.utils.events.EventListener;

public class GameManagerGUI implements EventListener{

	public void onGameStartEvent(Object sender, GameStartEvent event) {
		Packet[] message = MessageFactory.createPluginMessage("Game Started");
		((GameManager) sender).broadcastPackets(message);
	}
	
	public void onGameEndEvent(Object sender, GameEndEvent event) {
		Packet[] message = MessageFactory.createPluginMessage("Winner: " + (event.getWinner() == null ? "no one" : event.getWinner().getName()));
		((GameManager) sender).broadcastPackets(message);
	}
}
