package us.battleaxe.bae;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoBroadcaster {

    private final BAEssentials instance;
    private final FileConfiguration config;
	private final AtomicInteger position;

	private boolean enabled;
    private String prefix;
    private long interval;
    private List<String> messages;
	private boolean inOrder;
	private BukkitTask task;

    public AutoBroadcaster(BAEssentials instance, FileConfiguration config) {
        this.instance = instance;
		this.config = config;
		this.position = new AtomicInteger(0);
    }

    public void enable() {
		this.enabled = true;
		this.prefix = config.getString("prefix").length() != 0 ? Util.color(config.getString("prefix")) + " " : "";
		this.interval = Long.parseLong(this.config.getString("interval"));
		this.inOrder = config.getBoolean("in-order");
		this.messages = new ArrayList<String>();
		for(String message : this.config.getStringList("messages")) {
			this.messages.add(Util.color(message));
		}
		if(this.messages.size() == 0) return;
		if(!this.inOrder) {
			Collections.shuffle(this.messages);
		}

		this.task = Bukkit.getScheduler().runTaskTimer(this.instance, new Runnable() {
			@Override
			public void run() {
				int pos = position.getAndIncrement();
				for(Player online : Bukkit.getOnlinePlayers()) {
					online.sendMessage(prefix != null ? prefix + messages.get(pos) : messages.get(pos));
				}

				if(position.get() >= messages.size()) {
					position.set(0);
					if(!inOrder) {
						Collections.shuffle(messages);
					}
				}
			}
		}, this.interval, this.interval);
    }

    public void disable() {
		if(this.task != null) this.task.cancel();
		this.enabled = false;
		this.position.set(0);
	}

	public boolean isEnabled() {
		return this.enabled;
	}
}