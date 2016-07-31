package us.battleaxe.bounce.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.gameeventlisteners.ProgressBarChangeEvent;
import us.battleaxe.bounce.utils.events.BukkitRunnableCancelledEvent;
import us.battleaxe.bounce.utils.events.EventRaiser;

public class ProgressBar extends BukkitRunnable{

	public final EventRaiser eventRaiser = new EventRaiser();
	
	private ChatColor foreground;
	private ChatColor background;
	
	private int totalTime;
	private int tick;
	
	private int bars;
	
	private int timePassed;
	
	private boolean running;
	
	public ProgressBar(ChatColor foreground, ChatColor background, int totalTime, int tick, int bars) {
		this.foreground = foreground;
		this.background = background;
		
		this.totalTime = totalTime;
		this.tick = tick;
		
		this.bars = bars;
		
		timePassed = 0;
		running = false;
	}
	
	public void start() {
		if(isRunning())
			return;
		
		timePassed = 0;
		running = true;
		
		Bounce bounce = Bounce.getPlugin(Bounce.class);
		runTaskTimer(bounce, 0, tick);
	}
	
	public boolean isRunning() {
		return running;
	}
	
	@Override
	public void run() {
		if(timePassed > totalTime)
		{
			running = false;
			cancel();
			eventRaiser.raiseEvent(this, new BukkitRunnableCancelledEvent(getTaskId()));
			
			return;
		}
		
		float percentageComplete = (float) timePassed / totalTime;
		
		String bar = foreground + "";
		
		for(int i = 0; i < bars; ++i)
		{
			if(i > (int) (percentageComplete * bars))
				bar += background;
			
			bar += "|";
		}
		
		eventRaiser.raiseEvent(this, new ProgressBarChangeEvent(bar));
		
		timePassed += tick;
	}	
}
