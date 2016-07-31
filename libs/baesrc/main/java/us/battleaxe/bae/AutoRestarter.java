package us.battleaxe.bae;

import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import us.battleaxe.bae.api.API;

public class AutoRestarter {
	
	private final BAEssentials instance;
	private final API api;
	private final HashMap<Long, Boolean> warnings;
	
	private long nextRestart;
	private boolean enabled;
	private BukkitTask task;
	
	public AutoRestarter(BAEssentials instance, API api) {
		this.instance = instance;
		this.api = api;
		this.warnings = new HashMap<Long, Boolean>();
		this.enabled = false;
	}
	
	public void enable() {
		long buffer = Long.MAX_VALUE;
		for(String restart : this.instance.getConfig().getStringList("auto-restart.schedule")) {
			String[] split = restart.split("\\:");
			int hour = Integer.valueOf(split[0]);
			int min = Integer.valueOf(split[1]);
			
			Calendar calendar = Calendar.getInstance();
			if(calendar.get(Calendar.HOUR_OF_DAY) >= hour || (calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) == min)) {
				calendar.add(Calendar.DATE, 1);
			}
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, min);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if(buffer > calendar.getTimeInMillis()) {
				buffer = calendar.getTimeInMillis();
			}
		}
		
		for(String warning : this.instance.getConfig().getStringList("auto-restart.warnings")) {
			String[] split = warning.split("\\:");
			this.warnings.put(Long.valueOf(split[0]), (Boolean) split[1].equalsIgnoreCase("mins"));
		}
		this.nextRestart = buffer;
		this.enabled = true;
		
		this.task = Bukkit.getScheduler().runTaskTimer(this.instance, new Runnable() {
			@Override
			public void run() {
				//Compare time in seconds
				long diff = (nextRestart / 1000) - (System.currentTimeMillis() / 1000);
				if(diff <= 0) {
					Bukkit.broadcastMessage(api.getMessage("auto-restart.now"));
					Bukkit.shutdown();
				} else {
					if(warnings.containsKey((Long) diff)) {
						boolean min = warnings.get((Long) diff);
						String msg = api.getMessage("auto-restart.warning",
								String.valueOf(min ? diff / 60 : diff),
								(min ? "minute" : "second") + (min ? ((diff / 60 == 1) ? "" : "s") : (diff == 1) ? "" : "s"));
						Bukkit.broadcastMessage(msg);
					}
				}
			}
		}, 0L, 20L);
	}
	
	public void disable() {
		if(this.task != null) this.task.cancel();
		this.enabled = false;
		this.nextRestart = 0L;
		this.warnings.clear();
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
}