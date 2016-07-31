package us.battleaxe.bae;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class Util {

	public final static char COLOR_CHAR = '&';

	private final static String version;
	private final static NumberFormat numberFormat;
	
	public static byte[] toByteArray(UUID uuid) {
		return ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
	}
	
	public static UUID fromByteArray(byte[] array) {
		ByteBuffer buffer = ByteBuffer.wrap(array);
		return new UUID(buffer.getLong(), buffer.getLong());
	}
	
	public static String retrieveBukkitVersion() {
		return version;
	}
	
	public static double removeDigitsAfterPoint(double d, int keptDigits) {
		return Math.floor(d * Math.pow(10, keptDigits)) / Math.pow(10, keptDigits);
	}
	
	public static String compile(String[] args) {
		return compile(args, 0);
	}
	
	public static String compile(String[] args, int offset) {
		final StringBuilder builder = new StringBuilder();
		for(int x = offset; x < args.length; x++) {
			if(x != offset) builder.append(" ");
			builder.append(args[x]);
		}
		return builder.toString();
	}
	
	public static String formatInt(int i) {
		return formatLong((long) i);
	}
	
	public static String formatLong(long l) {
		return numberFormat.format(l);
	}
	
	public static String formatDouble(double d) {
		return numberFormat.format(d);
	}

	public static String color(String string) {
		return ChatColor.translateAlternateColorCodes(COLOR_CHAR, string);
	}
	
	static {
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		version = packageName.substring(packageName.lastIndexOf('.') + 1);
		
		if(!Pattern.compile("v\\d_\\d_R\\d").matcher(version).matches()) {
			throw new IllegalStateException("Invalid Server-Version or -Software!");
		}
		
		numberFormat = NumberFormat.getInstance(Locale.US);
	}
}