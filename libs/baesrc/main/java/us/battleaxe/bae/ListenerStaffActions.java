package us.battleaxe.bae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import io.github.dommi2212.corelib.AdvancedItem;
import net.md_5.bungee.api.ChatColor;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.OnlinePlayerAccount;

public class ListenerStaffActions implements Listener{
	
	private final API api;
	private final HashMap<UUID, Integer> pages;
	private final AdvancedItem nextPageItem;
	private final AdvancedItem previousPageItem;
	private final Random random;

	//TODO Format
	//TODO NPE on Join?
	public ListenerStaffActions(API api){
		this.api = api;
		this.pages = new HashMap<UUID, Integer>();
		this.nextPageItem = new AdvancedItem(Material.ARROW, "§cNext Page", 1, 0)
				.addItemFlags(ItemFlag.HIDE_ENCHANTS)
				.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		this.previousPageItem = new AdvancedItem(Material.FEATHER, "§cPrevious Page", 1, 0)
				.addItemFlags(ItemFlag.HIDE_ENCHANTS)
				.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		this.random = new Random();
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack clicked = e.getCurrentItem();
		if(!this.api.getStaffManager().isInStaffMode(player)) return;
		e.setCancelled(true);
		
		if(clicked == null) return;
		if(this.nextPageItem.equalsItem(clicked)) openPlayerSelector(player, this.pages.get(player.getUniqueId()) + 1);
		if(this.previousPageItem.equalsItem(clicked)) openPlayerSelector(player, this.pages.get(player.getUniqueId()) - 1);
		if(clicked.getType() != Material.SKULL_ITEM) return;
		
		String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
		if(Bukkit.getPlayer(name) == null) {
			player.sendMessage(this.api.getMessage("staffmode.player-logged-off"));
			player.closeInventory();
			return;
		}
		player.teleport(Bukkit.getPlayer(name));
	}
	
	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		if(this.api.getStaffManager().isInStaffMode((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if(player.hasPermission("bae.staff")) {
			this.api.getStaffManager().setupPlayer(player);
			return;
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		this.api.getStaffManager().clearPlayer(e.getPlayer());
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e){
		Player player = e.getPlayer();
		ItemStack clicked = e.getItem();
		if(!this.api.getStaffManager().isInStaffMode(player)) return;
		if(clicked == null) return;
		if(e.getItem().getType() == Material.WRITTEN_BOOK) return;
		e.setCancelled(true);
		
		switch(clicked.getType()) {
		case COMPASS:
			openPlayerSelector(player, 0);
			break;
		case NETHER_STALK:
			teleportRandom(player);
			break;
		case COOKIE:
			this.api.getStaffManager().clearPlayer(player);
			break;
		case GOLDEN_APPLE:
			this.api.getStaffManager().toggleStaffChat(player);
			break;
		default:
			break;
		}
	}
	
	public void teleportRandom(Player player){
		List<Player> players = new ArrayList<Player>();
		for(Player online : Bukkit.getOnlinePlayers()){
			if(this.api.getStaffManager().isInStaffMode(online)) continue;
			players.add(online);
		}
		if(players.size() < 1) return;
		player.teleport(players.get(this.random.nextInt(players.size())));
	}
	
	private ItemStack getHead(String name){
		OnlinePlayerAccount account = api.getOnlineAccount(name);
        String rank = ChatColor.translateAlternateColorCodes('&', account.getGroup().getPrefix());
        AdvancedItem skull = new AdvancedItem(Material.SKULL_ITEM, "§6" + name, 1, 0, "§7Rank: " + rank, "§7Gold: §6" + account.getGold());
        skull.setSkullOwner(name);
        return skull.toItem();
    }
	
	private List<String> getPlayers() {
		List<String> players = new ArrayList<String>();
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(this.api.getStaffManager().isInStaffMode(online)) continue;
			players.add(online.getName());
		}
		players.sort(String.CASE_INSENSITIVE_ORDER);
		return players;
	}
	
	private void openPlayerSelector(Player player, int page) {
		List<String> players = getPlayers();
		Inventory inv = Bukkit.createInventory(null, 54, "§cPlayers");
		
		if(players.size() > 54) {
			if(page * 45 > players.size()) {
				page = players.size() / 45;
				if(players.size() % 45 != 0) {
					page++;
				}
			}
			int i = page * 45;
			while (i < (page + 1) * 45 && i < players.size()) {
				inv.addItem(getHead(players.get(i)));
				i++;
			}
			if(page != 0) {
				this.previousPageItem.addToInventory(inv, 45);
			}
			if(i != players.size()) {
				this.nextPageItem.addToInventory(inv, 53);
			}
		} else {
			page = 0;
			for(String other : players) {
				inv.addItem(getHead(other));
			}
		}
		this.pages.put(player.getUniqueId(), page);
		player.openInventory(inv);
	}
}