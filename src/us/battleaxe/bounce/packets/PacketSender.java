package us.battleaxe.bounce.packets;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import us.battleaxe.bounce.Bounce;


public final class PacketSender {

	public static void SendPacket(Player player, Packet packet) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		PlayerConnection connection = craftPlayer.getHandle().playerConnection;
		connection.sendPacket(packet);
	}
	
	public static void SendPacketsInSequence(Player player, List<Packet> packets, int ticksBetweenPackets) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		PlayerConnection connection = craftPlayer.getHandle().playerConnection;
		
		Iterator<Packet> packetsIterator = packets.iterator();
		int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bounce.getPlugin(Bounce.class), new Runnable() {

			@Override
			public void run() {
				if(packetsIterator.hasNext())
					connection.sendPacket(packetsIterator.next());
			}
			
		}, 0, ticksBetweenPackets);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(Bounce.getPlugin(Bounce.class), new Runnable() {

			@Override
			public void run() {
				Bukkit.getScheduler().cancelTask(taskID);
			}
			
		}, ticksBetweenPackets * packets.size());
	}
}
