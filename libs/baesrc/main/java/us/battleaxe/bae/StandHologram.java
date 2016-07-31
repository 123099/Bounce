package us.battleaxe.bae;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import us.battleaxe.bae.api.Hologram;

public class StandHologram implements Hologram {

	private final static double PROVIDER_OFFSET = -2.245;
	private final static double BOTTOM_OFFSET = 0.5;

	private final ImplementedAPI api;
	private final JavaPlugin owner;
	private Location location;
	private ArmorStand entity;
	private String text;

	public StandHologram(ImplementedAPI api, JavaPlugin owner, Location location, String text) {
		this.api = api;
		this.owner = owner;
		this.location = location.clone();
		this.text = text;
	}

	@Override
	public JavaPlugin getOwner() {
		return this.owner;
	}

	@Override
	public Location getLocation() {
		return this.location.clone();
	}

	@Override
	public Location getProviderLocation() {
		return this.location.clone().add(0, PROVIDER_OFFSET, 0);
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public boolean isVisible() {
		return this.entity != null;
	}

	@Override
	public void setVisible(boolean visible) {
		if(visible && isVisible() || !visible && !isVisible()) return;
		if(visible) {
			this.entity = (ArmorStand) this.location.getWorld().spawnEntity(getProviderLocation(), EntityType.ARMOR_STAND);
			this.entity.setCustomNameVisible(true);
			this.entity.setCustomName(this.text);
			this.entity.setVisible(false);
			this.entity.setGravity(false);
		} else {
			this.entity.remove();
			this.entity = null;
		}
	}

	@Override
	public void moveTo(Location location) {
		setLocation(location);
	}

	@Override
	public void setLocation(Location location) {
		if(this.location.getWorld().equals(location.getWorld())) {
			this.api.setHologramWorld(this, this.location.getWorld());
		}
		this.location = location;
		if(!isVisible()) return;
		this.entity.teleport(this.getProviderLocation());
	}

	@Override
	public void setText(String text) {
		this.text = text;
		if(!isVisible()) return;
		this.entity.setCustomName(this.text);
	}

	public static double getProviderOffset() {
		return PROVIDER_OFFSET;
	}

	public static double getBottomOffset() {
		return BOTTOM_OFFSET;
	}
}