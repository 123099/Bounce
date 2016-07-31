package us.battleaxe.bae.api;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public interface Hologram {

	JavaPlugin getOwner();
	Location getLocation();
	Location getProviderLocation();
	String getText();
	boolean isVisible();
	void setVisible(boolean visible);
	void moveTo(Location location);
	void setLocation(Location location);
	void setText(String text);

}