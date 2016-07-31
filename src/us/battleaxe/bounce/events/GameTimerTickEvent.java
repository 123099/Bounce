package us.battleaxe.bounce.events;

import us.battleaxe.bounce.managers.GameManager;

public class GameTimerTickEvent extends GameTimerEvent {

	private double currentTime;
	
	public GameTimerTickEvent(int taskID, GameManager gameManager, double currentTime) {
		super(taskID, gameManager);
		this.currentTime = currentTime;
	}
	
	public double getCurrentTime() {
		return currentTime;
	}
	
	public int getMinutes() {
		return (int)currentTime / 60;
	}
	
	public int getSeconds() {
		return (int)currentTime % 60;
	}
	
	@Override
	public String toString() {
		return String.format("%02d", getMinutes()) + ":" + String.format("%02d", getSeconds());
	}
}
