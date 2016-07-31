package us.battleaxe.bounce.gameeventlisteners;

import org.bukkit.ChatColor;

import us.battleaxe.bounce.events.GameTimerCancelledEvent;
import us.battleaxe.bounce.events.GameTimerTickEvent;
import us.battleaxe.bounce.utils.CountDownTimer;
import us.battleaxe.bounce.utils.events.EventListener;

public class EndGameTimerListener implements EventListener{

	public void onGameTimerTickEvent(Object sender, GameTimerTickEvent event) {
		event.getGameManager().broadcastMessage(ChatColor.GREEN + event.toString());
	}
	
	public void onGameTimerCancelledEvent(Object sender, GameTimerCancelledEvent event) {
		((CountDownTimer)sender).eventRaiser.unregisterListener(this);
		event.getGameManager().endGame();
	}
}
