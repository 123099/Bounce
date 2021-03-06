package us.battleaxe.bae.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BAEChatEvent extends Event implements Cancellable {

    private static HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Player player;

    public BAEChatEvent(Player player) {
        this.player = player;
    }
	
    public Player getPlayer() {
		return this.player;
	}

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}