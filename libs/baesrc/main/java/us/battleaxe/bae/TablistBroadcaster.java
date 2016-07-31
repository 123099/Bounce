package us.battleaxe.bae;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

public class TablistBroadcaster {
	
	private final String version;
	private final Class<?> classCraftPlayer;
	private final Class<?> classEntityPlayer;
	private final Class<?> classPlayerConnection;
	private final Class<?> classPacket;
	private final Class<?> classPacketHeadFooter;
	private final Class<?> classChatSerializer;
	private final Method methodGetHandle;
	private final Method methodSendPacket;
	private final Method methodA;
	private final Field fieldA;
	private final Field fieldB;
	private final Field fieldPlayerConnection;
	
	public TablistBroadcaster() throws Exception {
		this.version = Util.retrieveBukkitVersion();
		this.classCraftPlayer = Class.forName("org.bukkit.craftbukkit." + this.version + ".entity.CraftPlayer");
		this.classEntityPlayer = Class.forName("net.minecraft.server." + this.version + ".EntityPlayer");
		this.classPlayerConnection = Class.forName("net.minecraft.server." + this.version + ".PlayerConnection");
		this.classPacket = Class.forName("net.minecraft.server." + this.version + ".Packet");
		this.classPacketHeadFooter = Class.forName("net.minecraft.server." + this.version + ".PacketPlayOutPlayerListHeaderFooter");
		this.classChatSerializer = Class.forName("net.minecraft.server." + this.version + ".IChatBaseComponent$ChatSerializer");
		
		this.methodGetHandle = this.classCraftPlayer.getMethod("getHandle");
		this.methodSendPacket = this.classPlayerConnection.getMethod("sendPacket", this.classPacket);
		this.methodA = this.classChatSerializer.getMethod("a", String.class);
		
		this.fieldA = this.classPacketHeadFooter.getDeclaredField("a");
		this.fieldB = this.classPacketHeadFooter.getDeclaredField("b");
		this.fieldPlayerConnection = this.classEntityPlayer.getDeclaredField("playerConnection");
		
		this.fieldA.setAccessible(true);
		this.fieldB.setAccessible(true);
		this.fieldPlayerConnection.setAccessible(true);
	}

	public void send(Player player, String header, String footer) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		String sentHeader; 
		String sentFooter;
		
		if(header == null || header.equals("")) {
			sentHeader = "{\"translate\": \"\"}";
		} else {
			sentHeader = "{\"text\": \"" + header.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
		}
		
		if(footer == null || footer.equals("")) {
			sentFooter = "{\"translate\": \"\"}";
		} else {
			sentFooter = "{\"text\": \"" + footer.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
		}
		
		Object packet = this.classPacketHeadFooter.newInstance();
		this.fieldA.set(packet, this.methodA.invoke(null, sentHeader));
		this.fieldB.set(packet, this.methodA.invoke(null, sentFooter));
		
        Object craftPlayer = this.classCraftPlayer.cast(player);
		Object entityPlayer = this.methodGetHandle.invoke(craftPlayer);
		Object connection = this.fieldPlayerConnection.get(entityPlayer);
		this.methodSendPacket.invoke(connection, packet);
	}
}