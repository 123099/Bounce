package us.battleaxe.bounce.packets;

import org.bukkit.Location;

import net.minecraft.server.v1_8_R3.EnumParticle;

public class PacketParticleData extends PacketData{

	private EnumParticle particleType;
	
	private float locationX;
	private float locationY;
	private float locationZ;
	
	private float offsetX;
	private float offsetY;
	private float offsetZ;
	
	private int speed;
	private int count;
	
	public PacketParticleData(EnumParticle particleType, Location location, int speed, int count) {
		this(particleType, location, 0, 0, 0, speed, count);
	}
	
	public PacketParticleData(EnumParticle particleType, Location location, float offsetX, float offsetY, float offsetZ, int speed, int count) {
		this.particleType = particleType;
		
		locationX = (float) location.getX();
		locationY = (float) location.getY();
		locationZ = (float) location.getZ();
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		
		this.speed = speed;
		this.count = count;
	}
	
	public EnumParticle getParticleType() {
		return particleType;
	}

	public float getLocationX() {
		return locationX;
	}

	public float getLocationY() {
		return locationY;
	}

	public float getLocationZ() {
		return locationZ;
	}

	public float getOffsetX() {
		return offsetX;
	}

	public float getOffsetY() {
		return offsetY;
	}

	public float getOffsetZ() {
		return offsetZ;
	}

	public int getSpeed() {
		return speed;
	}

	public int getCount() {
		return count;
	}

}
