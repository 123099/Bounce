package us.battleaxe.bae.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.dommi2212.BungeeBridge.packets.PacketIsPlayerOnline;
import me.dommi2212.BungeeBridge.util.IsOnlineResult;
import us.battleaxe.bae.MySQLClient;
import us.battleaxe.bae.Util;
import us.battleaxe.bae.api.NameFetcher;
import us.battleaxe.bae.api.OfflinePlayerAccount;
import us.battleaxe.bae.api.UUIDFetcher;

public final class CmdGold extends CmdExecutor {
	
	private final MySQLClient client;
	private final Map<UUID, String> topNames;
	
	public CmdGold() {
		super("gold", "cmd.gold", true);
		
		this.client = api.getMySQLClient();
		this.topNames = Collections.synchronizedMap(new HashMap<UUID, String>());
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(args.length > 3) {
			if(sender.hasPermission("cmd.gold.edit")) {
				sender.sendMessage(api.getMessage("general.disallowed-usage", getCommandMessage("team-usage")));
			} else {
				sendUsageMessage(sender);
			}
			return false;
		}
		if(args.length == 0) {
			if(!isPlayer(sender, true)) return false;
			sender.sendMessage(getCommandMessage("success-self", Util.formatInt(api.getOnlineAccount((Player) sender).getGold())));
			return true;
		}
		if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("usage")) {
			if(sender.hasPermission("cmd.gold.edit")) {
				sender.sendMessage(getCommandMessage("help", getCommandMessage("team-usage")));
			} else {
				sender.sendMessage(getCommandMessage("help", getCommandMessage("usage")));
			}
			return true;
		}
		if(args[0].equalsIgnoreCase("top")) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new TopCommandRunnable(sender));
			return true;
		}
		if(!sender.hasPermission("cmd.gold.edit")) {
			sendUsageMessage(sender);
			return false;
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new SubCommandRunnable(sender, args));
		return true;
	}
	
	private boolean validateArgs(CommandSender sender, OfflinePlayerAccount account, String parse, boolean allowZero) {
		if(allowZero) {
			if(!isNotNegativeNumber(sender, parse)) return false;
		} else {
			if(!isPositiveNumber(sender, parse)) return false;
		}
		if(account.isLoggedIn()) return true;
		
		PacketIsPlayerOnline packet = new PacketIsPlayerOnline(account.getUUID());
		switch ((IsOnlineResult) packet.send()) {
		case OFFLINE:
			return true;
		case ONLINE:
			sender.sendMessage(getCommandMessage("illegal-access"));
			return false;
		case UNKNOWN:
		case UUID_AND_NAME_NULL:
			sender.sendMessage(getCommandMessage("error"));
			return false;
		default:
			return false;
		}
	}
	
	private class TopCommandRunnable implements Runnable {
		
		private final CommandSender sender;
		
		public TopCommandRunnable(CommandSender sender) {
			this.sender = sender;
		}
		
		@Override
		public void run() {
			sender.sendMessage(getCommandMessage("top.loading"));
			PreparedStatement statement;
			try {
				statement = client.prepareStatement("SELECT `uuid`, `gold` FROM `players` ORDER BY `gold` DESC LIMIT 5;");
			} catch (SQLException exc) {
				sender.sendMessage(getCommandMessage("top.gold-fetch-failure"));
				plugin.getLogger().log(Level.SEVERE, "Failed to prepare fetch for the five richest people from the database", exc);	
				return;
			}
			
			Map<UUID, Integer> gold = new LinkedHashMap<UUID, Integer>(5);
			List<UUID> toLoad = new ArrayList<UUID>(5);
			try {
				ResultSet rs = client.executeQuery(statement);
				while(rs.next()) {
					UUID uuid = Util.fromByteArray(rs.getBytes(1));
					gold.put(uuid, rs.getInt(2));
					if(!topNames.containsKey(uuid)) {
						toLoad.add(uuid);
					}
				}
			} catch (SQLException exc) {
				sender.sendMessage(getCommandMessage("top.gold-fetch-failure"));
				plugin.getLogger().log(Level.SEVERE, "Failed to fetch the five richest people from the database", exc);	
				return;
			}
			
			if(!toLoad.isEmpty()) {
				try {
					topNames.putAll(new NameFetcher(toLoad).call());
				} catch(Exception exc) {
					sender.sendMessage(getCommandMessage("top.gold-fetch-failure"));
					plugin.getLogger().log(Level.SEVERE, "Failed to fetch names for the five richest people from Mojang", exc);	
					return;
				}
			}
			
			StringBuilder elements = new StringBuilder();
			int number = 1;
			for(Entry<UUID, Integer> entry : gold.entrySet()) {
				elements.append("\n").append(getCommandMessage("top.element", String.valueOf(number++), topNames.get(entry.getKey()), Util.formatInt(entry.getValue())));
			}
			sender.sendMessage(getCommandMessage("top.header", elements.toString()));
		}
	}
	
	private class SubCommandRunnable implements Runnable {
		
		private final CommandSender sender;
		private final String[] args;
		
		public SubCommandRunnable(CommandSender sender, String[] args) {
			this.sender = sender;
			this.args = args;
		}
		
		@Override
		public void run() {
			OfflinePlayerAccount account;
			if(Bukkit.getPlayer(args[0]) != null) {
				account = api.getOfflineAccount(Bukkit.getPlayer(args[0]).getUniqueId());
			} else {
				try {
					sender.sendMessage(getCommandMessage("player-loading", args[0]));
					UUID uuid = new UUIDFetcher(Arrays.asList(args[0])).call().get(args[0]);
					if(uuid == null) {
						sender.sendMessage(getCommandMessage("player-never-joined-before"));
						return;
					}
					account = api.loadAccount(uuid);
					if(account == null) {
						sender.sendMessage(getCommandMessage("player-never-joined-before"));
						return;
					}
				} catch (Exception exc) {
					sender.sendMessage(getCommandMessage("player-fetch-failure", args[0], exc.getMessage()));
					exc.printStackTrace();
					return;
				}
			}
			
			if(args.length == 1) {
				sender.sendMessage(getCommandMessage("success-other", args[0], Util.formatInt(account.getGold())));
				return;
			}
			
			if(args[1].equalsIgnoreCase("give")) {
				if(!validateArgs(sender, account, args[2], false)) return;
				int given = Integer.parseInt(args[2]);
				account.giveGold(given);
				sender.sendMessage(getCommandMessage("given", args[0], Util.formatInt(given), Util.formatInt(account.getGold())));
				if(account.isLoggedIn()) {
					account.getOnlineAccount().getPlayer().sendMessage(getCommandMessage("target-given", Util.formatInt(given)));
				}
			} else if(args[1].equalsIgnoreCase("take")) {
				if(!validateArgs(sender, account, args[2], false)) return;
				int taken = Integer.parseInt(args[2]);
				account.takeGold(taken);
				sender.sendMessage(getCommandMessage("taken", Util.formatInt(taken), args[0], Util.formatInt(account.getGold())));
				if(account.isLoggedIn()) {
					account.getOnlineAccount().getPlayer().sendMessage(getCommandMessage("target-taken", Util.formatInt(taken)));
				}
			} else if(args[1].equalsIgnoreCase("set")) {
				if(!validateArgs(sender, account, args[2], true)) return;
				int gold = Integer.parseInt(args[2]);
				account.setGold(gold);
				sender.sendMessage(getCommandMessage("set", args[0], Util.formatInt(gold)));
				if(account.isLoggedIn()) {
					account.getOnlineAccount().getPlayer().sendMessage(getCommandMessage("target-set", Util.formatInt(gold)));
				}
			} else {
				sender.sendMessage(api.getMessage("general.disallowed-usage", getCommandMessage("team-usage")));
				return;
			}
			
			PreparedStatement updateStatement;
			try {
				updateStatement = client.prepareStatement("UPDATE `players` SET `gold` = ? WHERE `id` = ?;");
				updateStatement.setInt(1, account.getGold());
				updateStatement.setInt(2, account.getId());
			} catch (SQLException exc) {
				sender.sendMessage(getCommandMessage("remote-update-failure", exc.getMessage()));
				plugin.getLogger().log(Level.SEVERE, "Failed to synchronize gold-update for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);	
				return;
			}
			
			try {
				client.executeUpdate(updateStatement);
			} catch (SQLException exc) {
				sender.sendMessage(getCommandMessage("remote-update-failure", exc.getMessage()));
				plugin.getLogger().log(Level.SEVERE, "Failed to synchronize gold-update for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);
			}
		}
	}
}