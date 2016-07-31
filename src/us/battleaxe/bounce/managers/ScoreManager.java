package us.battleaxe.bounce.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import us.battleaxe.bounce.utils.Constants;

public class ScoreManager {

	private Scoreboard scoreboard;
	private Objective objective;
	
	private List<Player> players;
	
	public ScoreManager() {
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective(Constants.scoreboardName, "");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		players = new ArrayList<>();
	}
	
	public void AddPlayer(Player player) {
		players.add(player);
		SetScore(player, 0);
		updateScoreboard();
	}
	
	public void AddPlayers(List<Player> players) {
		for(Player player : players)
			AddPlayer(player);
	}
	
	public void RemovePlayer(Player player) {
		players.remove(player);
		removeScoreboard(player);
	}
	
	public void RemovePlayers(List<Player> players) {
		for(Player player : players)
			RemovePlayer(player);
	}
	
	public void AddScore(Player player, int scoreToAdd) {
		SetScore(player, GetScore(player) + scoreToAdd);
	}
	
	public void SetScore(Player player, int scoreToSet) {
		Score score = objective.getScore(player.getName());
		score.setScore(scoreToSet);
		updateScoreboard();
	}
	
	public int GetScore(Player player) {
		Score score = objective.getScore(player.getName());
		return score.getScore();
	}
	
	private void updateScoreboard() {
		for(Player player : players)
			player.setScoreboard(scoreboard);
	}
	
	private void removeScoreboard(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
}
