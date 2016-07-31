package us.battleaxe.bounce.utils;

import org.bukkit.scheduler.BukkitRunnable;

import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.events.GameTimerCancelledEvent;
import us.battleaxe.bounce.events.GameTimerTickEvent;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.events.EventRaiser;

public class CountDownTimer extends BukkitRunnable{

	public final EventRaiser eventRaiser = new EventRaiser();
	
	private GameManager gameManager;
	
	private double currentTime;
	private int step; //every how many ticks does the timer display a message
	
	public CountDownTimer(GameManager gameManager, double startingTime, int startingStep) {
		this.gameManager = gameManager;
		currentTime = startingTime;
		step = 20 * startingStep;
	}
	
	public void start() {
		runTaskTimer(Bounce.getPlugin(Bounce.class), 0, 20);
	}
	
	@Override
	public void run() {
		if(currentTime == 0)
		{
			cancel();
			eventRaiser.raiseEvent(this, new GameTimerCancelledEvent(getTaskId(), gameManager));
		}
		
		if((currentTime * 20) % step == 0)
			eventRaiser.raiseEvent(this, new GameTimerTickEvent(getTaskId(), gameManager, currentTime));
		
		--currentTime;
		
		if(currentTime <= 5)
			step = 20;
	}

}
