package us.battleaxe.bounce.events;

import org.bukkit.entity.Player;

import us.battleaxe.bounce.utils.events.Event;

public class GameEndEvent extends Event {

	private Player winner;
	
	public GameEndEvent(Player winner) {
		this.winner = winner;
	}
	
	public Player getWinner() {
		return winner;
	}
}
