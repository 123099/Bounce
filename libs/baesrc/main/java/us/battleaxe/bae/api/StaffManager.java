package us.battleaxe.bae.api;

import io.github.dommi2212.corelib.AdvancedItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import us.battleaxe.bae.BAEssentials;
import us.battleaxe.bae.PlayerState;
import us.battleaxe.bae.api.visibility.GroupVisibilityPolicy;
import us.battleaxe.bae.api.visibility.StaffVisibilityPolicy;
import us.battleaxe.bae.events.PlayerDisableStaffModeEvent;
import us.battleaxe.bae.events.PlayerEnableStaffModeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class StaffManager {

	private final API api;
	private final HashMap<UUID, PlayerState> states;
	private final List<UUID> staffChat;
	private final FileConfiguration config;

	private final AdvancedItem compass;
	private final AdvancedItem wart;
	private final AdvancedItem cookie;
	private final AdvancedItem appleOff;
	private final AdvancedItem appleOn;
	private final AdvancedItem book;

	public StaffManager(API api, FileConfiguration config) {
		this.api = api;
		this.states = new HashMap<UUID, PlayerState>();
		this.staffChat = new ArrayList<UUID>();
		this.config = config;

		this.compass = new AdvancedItem(Material.COMPASS, "§7Player Selector", 1, 0);
		this.wart = new AdvancedItem(Material.NETHER_STALK, "§7Random Player Teleporter", 1, 0);
		this.cookie = new AdvancedItem(Material.COOKIE, "§7Turn Off Moderator Mode", 1, 0);
		this.appleOff = new AdvancedItem(Material.GOLDEN_APPLE, "§7Toggle Staff Chat §c[OFF]", 1, 0);
		this.appleOn = new AdvancedItem(Material.GOLDEN_APPLE, "§7Toggle Staff Chat §a[ON]", 1, 0);

		List<String> pages = this.config.getStringList("pages");
		for(int i = 0; i < pages.size(); i++) {
			pages.set(i, ChatColor.translateAlternateColorCodes('&', pages.get(i)));
		}
		this.book = new AdvancedItem(Material.WRITTEN_BOOK, ChatColor.translateAlternateColorCodes('&', this.config.getString("title")), 1, 0)
				.setBookTitle(ChatColor.translateAlternateColorCodes('&', this.config.getString("title")))
				.setBookAuthor(this.config.getString("author"))
				.setBookPages(pages);

		api.getVisibilityManager().registerPolicy((int) 1e6, new StaffVisibilityPolicy());
	}

	public void clearAllPlayers(){
		for(UUID uuid : this.states.keySet()) {
			clearPlayer(Bukkit.getPlayer(uuid));
		}
	}

	public void clearPlayer(Player player) {
		if(!this.states.containsKey(player.getUniqueId())) return;
		if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
			Location location = getBottomLocation(player);
			if(location == null) {
				player.sendMessage(this.api.getMessage("staffmode.cant-disable-over-air"));
				return;
			}
			player.teleport(location);
		}

		PlayerState state = this.states.get(player.getUniqueId());
		PlayerDisableStaffModeEvent event = new PlayerDisableStaffModeEvent(player, player.getWorld().getSpawnLocation());
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		state.setLocation(event.getSpawn());

		this.staffChat.remove(player.getUniqueId());
		this.states.remove(player.getUniqueId());
		state.reset(player);
	}

	public List<UUID> getStaffMembers() {
		return new ArrayList<UUID>(this.states.keySet());
	}

	public List<UUID> getStaffChatMembers() {
		return new ArrayList<UUID>(this.staffChat);
	}

	public boolean isInStaffChat(Player player) {
		return this.staffChat.contains(player.getUniqueId());
	}

	public boolean isInStaffMode(Player player){
		return this.states.containsKey(player.getUniqueId());
	}

	public void setupPlayer(Player player){
		if(!player.hasPermission("bae.staff")) return;

		PlayerEnableStaffModeEvent evt = new PlayerEnableStaffModeEvent(player);
		Bukkit.getPluginManager().callEvent(evt);
		if(evt.isCancelled()) return;

		this.states.put(player.getUniqueId(), new PlayerState(player));

		PlayerInventory inv = player.getInventory();
		PlayerState.BLANK.reset(player);
		player.setGameMode(GameMode.CREATIVE);

		this.compass.addToInventory(inv, 0);
		this.wart.addToInventory(inv, 2);
		this.cookie.addToInventory(inv, 4);
		this.appleOff.addToInventory(inv, 6);
		this.book.addToInventory(inv, 8);
	}

	public void toggleStaffChat(Player player) {
		UUID uuid = player.getUniqueId();
		if(this.staffChat.contains(uuid)) {
			this.staffChat.remove(uuid);
			this.appleOff.addToInventory(player.getInventory(), 6);
		} else {
			this.staffChat.add(uuid);
			this.appleOn.addToInventory(player.getInventory(), 6);
		}
	}

	private Location getBottomLocation(Player player) {
		Location loc = player.getLocation();
		for(double y = loc.getY(); y > -1; y--) {
			loc.setY(y);
			Block block = player.getWorld().getBlockAt(loc);
			if(block.getType().isSolid())
				return loc.add(0, 1, 0);
		}
		return null;
	}
}