package us.battleaxe.bounce.gameeventlisteners;

import us.battleaxe.bounce.GameStatus;
import us.battleaxe.bounce.events.GameTimerCancelledEvent;
import us.battleaxe.bounce.events.GameTimerTickEvent;
import us.battleaxe.bounce.events.PlayerCountChangeEvent;
import us.battleaxe.bounce.utils.Constants;
import us.battleaxe.bounce.utils.CountDownTimer;
import us.battleaxe.bounce.utils.events.EventListener;

public class PlayerCountChangeEventListener implements EventListener{

	public void onPlayerCountChange(Object sender, PlayerCountChangeEvent event) {
		if(event.getGameManager().getGameStatus() == GameStatus.InLobby && event.getGameManager().hasEnoughPlayers())
		{
			CountDownTimer timer = new CountDownTimer(event.getGameManager(), Constants.TimeToStartGame, 1);
			timer.eventRaiser.registerListener(new StartGameTimerListener());
			timer.start();

			event.getGameManager().setGameStatus(GameStatus.Starting);
		}
	}
}
