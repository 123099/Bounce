package us.battleaxe.bae;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

public class PingRetriever {
	
	private final String version;
	private final Class<?> classCraftPlayer;
	private final Class<?> classEntityPlayer;
	private final Method methodGetHandle;
	private final Field fieldPing;
	
	public PingRetriever() throws Exception {
		this.version = Util.retrieveBukkitVersion();
		this.classCraftPlayer = Class.forName("org.bukkit.craftbukkit." + this.version + ".entity.CraftPlayer");
		this.classEntityPlayer = Class.forName("net.minecraft.server." + this.version + ".EntityPlayer");
		this.methodGetHandle = this.classCraftPlayer.getMethod("getHandle");
		this.fieldPing = this.classEntityPlayer.getDeclaredField("ping");
	}

	public int getPing(Player player) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object craftPlayer = this.classCraftPlayer.cast(player);
		Object entityPlayer = this.methodGetHandle.invoke(craftPlayer);
		return this.fieldPing.getInt(entityPlayer);
	}
}