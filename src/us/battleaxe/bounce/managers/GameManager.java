package us.battleaxe.bounce.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_8_R3.Packet;
import us.battleaxe.bae.BAEssentials;
import us.battleaxe.bae.ThrowingConsumer;
import us.battleaxe.bae.api.PlayerAccount;
import us.battleaxe.bounce.Bounce;
import us.battleaxe.bounce.GameStatus;
import us.battleaxe.bounce.events.GameEndEvent;
import us.battleaxe.bounce.events.GameStartEvent;
import us.battleaxe.bounce.events.PlayerCountChangeEvent;
import us.battleaxe.bounce.gameeventlisteners.EndGameTimerListener;
import us.battleaxe.bounce.gameeventlisteners.PlayerCountChangeEventListener;
import us.battleaxe.bounce.gui.GameManagerGUI;
import us.battleaxe.bounce.packets.EnumChatAction;
import us.battleaxe.bounce.packets.PacketChatData;
import us.battleaxe.bounce.packets.PacketCreator;
import us.battleaxe.bounce.packets.PacketSender;
import us.battleaxe.bounce.skills.DashSkill;
import us.battleaxe.bounce.utils.Constants;
import us.battleaxe.bounce.utils.CountDownTimer;
import us.battleaxe.bounce.utils.PlayerExtension;
import us.battleaxe.bounce.utils.events.EventRaiser;

//TODO: Decompose me please :(
public class GameManager{

	public final EventRaiser eventRaiser = new EventRaiser();
	
	private World gameWorld;
	private SignManager signManager;
	private ScoreManager scoreManager;
	
	private GameStatus gameStatus;
	private List<Player> inGamePlayers;
	
	private HashMap<Player, List<DashSkill>> skills;
	
	private Location spawnLocation;
	
	private CountDownTimer timer;
	
	public GameManager(World world, ConfigManager configManager) {
		gameWorld = world;
		skills = new HashMap<>();
		inGamePlayers = new ArrayList<Player>();
		
		gameStatus = GameStatus.InLobby;
				
		spawnLocation = configManager.getGameWorldSpawn(gameWorld.getName());
		if(spawnLocation == null)
		{
			spawnLocation = gameWorld.getSpawnLocation().clone().add(0,300,0);
			configManager.setGameWorldSpawn(gameWorld.getName(), spawnLocation);
		}
		
		signManager = new SignManager(this, configManager.getGameWorldSign(gameWorld.getName()));
		signManager.updateSignStatus();
		
		scoreManager = new ScoreManager();
		
		eventRaiser.registerListener(new PlayerCountChangeEventListener());
		eventRaiser.registerListener(new GameManagerGUI());
	}
	
	public Location getSpawnLocation() {
		return spawnLocation;
	}
	
	public SignManager getSignManager() {
		return signManager;
	}
	
	public void setGameStatus(GameStatus status) {
		gameStatus = status;
		signManager.updateSignStatus();
	}
	
	public GameStatus getGameStatus() {
		return gameStatus;
	}
	
	public World getGameWorld() {
		return gameWorld;
	}
	
	public int getInGamePlayerCount() {
		return inGamePlayers.size();
	}
	
	public List<Player> getInGamePlayers(){
		return new ArrayList<Player>(inGamePlayers);
	}
	
	public int getMaxPlayers() {
		return Constants.MaxPlayers;
	}
	
	public int getMinPlayers() {
		return Constants.MinPlayers;
	}
	
	public boolean hasEnoughPlayers() {
		return getInGamePlayerCount() >= getMinPlayers();
	}
	
	public ScoreManager getScoreManager() {
		return scoreManager;
	}
	
	public void destroy() {
		eventRaiser.removeAllListeners();
	}
	
	public void addPlayer(Player player) {
		inGamePlayers.add(player);
		signManager.updateSignStatus();
		
		player.getInventory().clear();
		
		ItemStack instructions = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta bookMeta = (BookMeta) instructions.getItemMeta();
		
		bookMeta.setTitle(ChatColor.GREEN + "Instruction Manual");
		bookMeta.setAuthor(ChatColor.GREEN + Constants.PluginName);
		
		String page0 = 
				ChatColor.GREEN + "" + ChatColor.BOLD + Constants.PluginName + "\n" +
				ChatColor.DARK_GREEN + "Bounce your way to victory!\n\n" +
				ChatColor.DARK_AQUA + "Every kill awards you with 10 points.\n" +
				"The bow and arrow instantly kill a player.\n" +
				"The sword deals half a player's health in damage.\n" +
				"Right click the sword to call forth it's true power!";
		bookMeta.setPages(page0);
		
		instructions.setItemMeta(bookMeta);
		player.getInventory().addItem(instructions);
		player.getInventory().setHeldItemSlot(0);
		
		PlayerExtension.AddAttribute(player, "GameManager", this);
		
		eventRaiser.raiseEvent(this, new PlayerCountChangeEvent(this, getInGamePlayerCount()));
	}
	
	public void broadcastMessage(String message) {
		PacketChatData chatData = new PacketChatData(ChatColor.GRAY + message, EnumChatAction.ActionBar);
		broadcastPackets(PacketCreator.createChatPacket(chatData));
	}
	
	public void broadcastPackets(Packet... packets) {
		for(Player player : inGamePlayers)
			for(Packet packet : packets)
				PacketSender.SendPacket(player, packet);
	}
	
	public void startGame() {
		if(gameStatus == GameStatus.InProgress)
			return;
		
		if(!hasEnoughPlayers())
		{
			endGame();
			return;
		}
		
		for(Player player : inGamePlayers)
		{			
			player.teleport(spawnLocation);
			
			player.setFireTicks(0);
	        player.setHealth(player.getMaxHealth());
	        player.setFoodLevel(999);
	        player.setGameMode(GameMode.ADVENTURE);
			
			equipPlayer(player);
			setupPlayerSkills(player);
		}
		
		scoreManager.AddPlayers(inGamePlayers);
		
		eventRaiser.raiseEvent(this, new GameStartEvent());
		
		timer = new CountDownTimer(this, Constants.TimeForGame, 1);
		timer.eventRaiser.registerListener(new EndGameTimerListener());
		timer.start();
		
		setGameStatus(GameStatus.InProgress);
	}
	
	public void endGame() {
		if(timer != null)
		{
			timer.eventRaiser.removeAllListeners();
			timer.cancel();
		}
		
		Player winningPlayer = null;
		
		for(Player player : inGamePlayers)
		{
			int score = scoreManager.GetScore(player);

			if(winningPlayer == null || score > scoreManager.GetScore(winningPlayer))
				winningPlayer = player;
		}
		
		for(Player player : inGamePlayers)
		{			
			int gold = scoreManager.GetScore(player);
			if(player == winningPlayer)
				gold *= 2;
			else
				gold /= 2;
			
			PlayerAccount playerAccount = BAEssentials.getAPI().getOnlineAccount(player);
			playerAccount.giveGold(gold);
			try 
			{
				playerAccount.updateRemoteGold(
						new ThrowingConsumer<Integer>() {
							@Override
							public void accept(Integer t) {	}
							@Override
							public void throwException(Exception exception) {
								Bounce.getPlugin(Bounce.class).getLogger().log(Level.WARNING, "Failed to update remote gold for player " + player.getName() + ".\nException thrown.", exception);
							}
						}
				);
			} 
			catch (SQLException e1) { Bounce.getPlugin(Bounce.class).getLogger().log(Level.WARNING, "Failed to update remote gold for player " + player.getName() + ".\nSQL Exception thrown.", e1); }
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bounce.getPlugin(Bounce.class), () -> { clearPlayer(player); teleportPlayerOutside(player); }, 60);
		}
		
		eventRaiser.raiseEvent(this, new GameEndEvent(winningPlayer));
		
		scoreManager.RemovePlayers(inGamePlayers);
		inGamePlayers.clear();
		
		for(Entity e : gameWorld.getEntitiesByClass(Arrow.class))
			e.remove();
		
		setGameStatus(GameStatus.InLobby);
	}
	
	public void equipPlayer(Player player) {
		player.getInventory().clear();
		player.updateInventory();
		
		ItemStack bow = new ItemStack(Material.BOW, 1);
		bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1); //Power I
		ItemMeta bowMeta = bow.getItemMeta();
		bowMeta.setDisplayName(ChatColor.GREEN + Constants.BowName);
		bow.setItemMeta(bowMeta);
		
		ItemStack arrows = new ItemStack(Material.ARROW, 32);
		ItemMeta arrowsMeta = arrows.getItemMeta();
		arrowsMeta.setDisplayName(ChatColor.GREEN + Constants.ArrowName);
		arrows.setItemMeta(arrowsMeta);
		
		ItemStack ironsword = new ItemStack(Material.IRON_SWORD, 1);
		ironsword.addEnchantment(Enchantment.DAMAGE_ALL, 1); //Sharpness I
		ItemMeta swordMeta = ironsword.getItemMeta();
		swordMeta.setDisplayName(ChatColor.GREEN + Constants.SwordName);
		ironsword.setItemMeta(swordMeta);
		
		ItemStack poisonPotion = new ItemStack(Material.POTION, 10, (short) 16420);
		ItemMeta poisonPotionMeta = poisonPotion.getItemMeta();
		poisonPotionMeta.setDisplayName(ChatColor.GREEN + Constants.PoisonPotionName);
		poisonPotion.setItemMeta(poisonPotionMeta);
		
		player.getInventory().addItem(ironsword, bow, poisonPotion);
		player.getInventory().setItem(8, arrows);
		
		player.getInventory().setHeldItemSlot(0);
		
		player.updateInventory();
	}
	
	private void setupPlayerSkills(Player player) {
		Bounce bounce = Bounce.getPlugin(Bounce.class);
		PluginManager pm = Bukkit.getPluginManager();
		
		DashSkill dash = new DashSkill(bounce, player, Material.IRON_SWORD, Constants.DashDistance, Constants.DashTravelTime, Constants.DashCooldown, Constants.DashRadius);
		pm.registerEvents(dash, bounce);
		
		List<DashSkill> playerSkills = skills.get(player);
		if(playerSkills == null)
			playerSkills = new ArrayList<>();
		
		playerSkills.add(dash);
		skills.put(player, playerSkills);
	}
	
	public void removePlayer(Player player) {
		inGamePlayers.remove(player);
		signManager.updateSignStatus();
		
		scoreManager.RemovePlayer(player);
		
		teleportPlayerOutside(player);
		
		clearPlayer(player);
		
		if(gameStatus == GameStatus.InProgress && !hasEnoughPlayers())
		{
			broadcastMessage("Not enough players remaining. Game will now end..");
			endGame();
		}
	}
	
	private void clearPlayer(Player player) {
		player.getInventory().clear();
		player.updateInventory();
		
		player.setFireTicks(0);
        player.setHealth(player.getMaxHealth());
		
		player.setVelocity(new Vector());
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30, 50));
		
		List<DashSkill> playerSkills = skills.get(player);
		if(playerSkills != null)
			for(DashSkill skill : playerSkills)
				HandlerList.unregisterAll(skill);
		
		PlayerExtension.RemoveAttribute(player, "GameManager");

		ItemStack instructions = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta bookMeta = (BookMeta) instructions.getItemMeta();
		
		bookMeta.setTitle(ChatColor.GREEN + "Instruction Manual");
		bookMeta.setAuthor(ChatColor.GREEN + Constants.PluginName);
		
		String page0 = 
				ChatColor.GREEN + "" + ChatColor.BOLD + Constants.PluginName + "\n" +
				ChatColor.DARK_GREEN + "Bounce your way to victory!\n\n" +
				ChatColor.DARK_AQUA + "Every kill awards you with 10 points.\n" +
				"The bow and arrow instantly kill a player.\n" +
				"The sword deals half a player's health in damage.\n" +
				"Right click the sword to call forth it's true power!";
		bookMeta.setPages(page0);
		
		instructions.setItemMeta(bookMeta);
		player.getInventory().addItem(instructions);
		player.getInventory().setHeldItemSlot(0);
		
		player.updateInventory();
	}
	
	public void teleportPlayerOutside(Player player) {		
		player.teleport(getLobbySpawnPoint());
	}
	
	public Location getLobbySpawnPoint() {
		World mainWorld = Bukkit.getWorld("world");
		return mainWorld.getSpawnLocation();
	}
	
	public static boolean isPlayerInGame(Player player) {
		return PlayerExtension.ContainsAttribute(player, "GameManager");
	}
	
	public static GameManager getInstanceForPlayer(Player player) {
		return PlayerExtension.GetAttribute(GameManager.class, player, "GameManager");
	}
}
