package us.battleaxe.bounce.utils;

import org.bukkit.ChatColor;

public final class Constants {

	public static final String PluginName = "Bounce";
	public static final String PluginTag = ChatColor.BLUE + "[" + ChatColor.GREEN + PluginName + ChatColor.BLUE + "]" + ChatColor.GRAY + " ";
	
	public static final String scoreboardName = ChatColor.GREEN + "" + ChatColor.BOLD + "BOUNCE";
	
	public static int MinPlayers = 2;
	public static int MaxPlayers = 10;
	
	public static int PointsPerKill = 10;
	
	public static double TimeToStartGame = 15;
	public static double TimeForGame = 300;
	
	public static double MaxSpeed = 15;
	
	public static double SwordDamage = 10;
	
	public static double DashDamage = 10;
	public static double DashDistance = 15;
	public static double DashTravelTime = 2;
	public static double DashCooldown = 3;
	public static double DashRadius = 2;
	
	public static String BowName = "QuickShooter";
	public static String ArrowName = "One Shot Wonder";
	public static String SwordName = "The Half-Killer";
	public static String PoisonPotionName = "Silent Predator";
	
	public static String DeathMessage = PluginTag + "%Victim was killed by %Killer {%Reason}";
}
