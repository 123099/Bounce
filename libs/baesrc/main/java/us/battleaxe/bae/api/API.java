package us.battleaxe.bae.api;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.battleaxe.bae.AutoRestarter;
import us.battleaxe.bae.GlobalChatFormatter;
import us.battleaxe.bae.MySQLClient;
import us.battleaxe.bae.ScoreboardHandler;
import us.battleaxe.bae.AutoBroadcaster;
import us.battleaxe.bae.api.visibility.VisibilityManager;

import java.util.List;
import java.util.UUID;

public interface API {
	
	MySQLClient getMySQLClient();
	WorldSeparator getWorldSeparator();
	GlobalChatFormatter getChatFormatter();
	PermissionManager getPermissionManager();
	WarpManager getWarpManager();
	AutoRestarter getAutoRestarter();
	AutoBroadcaster getAutoBroadcaster();
	ScoreboardHandler getScoreboardHandler();
	StaffManager getStaffManager();
	VisibilityManager getVisibilityManager();
	String getMessage(String path);
	String getMessage(String path, String... replacements);
	String getMessage(String path, boolean ignoreColor, String[] replacements);
	TextComponent getComponentMessage(String path);
	TextComponent getComponentMessage(String path, String[] replacements);
	long getTeleportationRequestTimeout();
	OnlinePlayerAccount getOnlineAccount(UUID uuid);
	OnlinePlayerAccount getOnlineAccount(String name);
	OnlinePlayerAccount getOnlineAccount(int id);
	OnlinePlayerAccount getOnlineAccount(Player player);
	OfflinePlayerAccount getOfflineAccount(UUID uuid);
	OfflinePlayerAccount getOfflineAccount(int id);
	OfflinePlayerAccount loadAccount(UUID uuid) throws Exception;
	void createAccount(UUID uuid) throws Exception;
	String getDefaultTabHeader();
	String getDefaultTabFooter();
	void setDefaultTabHeader(String header);
	void setDefaultTabFooter(String footer);
	String getBungeename();
	void sendKeepAlive();
	public Hologram createHologram(JavaPlugin owner, Location location, String text);
	public List<Hologram> getHolograms(JavaPlugin owner);
	public List<Hologram> getHolograms(World world);
	
}