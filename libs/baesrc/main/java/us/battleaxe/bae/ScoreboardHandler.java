package us.battleaxe.bae;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import us.battleaxe.bae.api.OnlinePlayerAccount;

public class ScoreboardHandler {
	
	private final Scoreboard board;
	private final Map<TeamKey, Team> teams;
	
	private boolean enabled;
	private boolean addPrefixSpacer;
	private boolean addSuffixSpacer;
	
	public ScoreboardHandler() {
		this.board = Bukkit.getScoreboardManager().getMainScoreboard();
		this.teams = new HashMap<TeamKey, Team>();
	}
	
	public void enable() {
		enable(true, true);
	}
	
	public void enable(boolean addPrefixSpacer, boolean addSuffixSpacer) {
		this.enabled = true;
		this.addPrefixSpacer = addPrefixSpacer;
		this.addSuffixSpacer = addSuffixSpacer;
		clear();
	}
	
	public void disable() {
		this.enabled = false;
		clear();
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public boolean addsPrefixSpacer() {
		return this.addPrefixSpacer;
	}
	
	public boolean addsSuffixSpacer() {
		return this.addSuffixSpacer;
	}
	
	public void updateSpacers(boolean addPrefixSpacer, boolean addSuffixSpacer) {
		for(Team team : this.board.getTeams()) {
			if(!team.getName().startsWith("BAE_")) continue;
			if(this.addPrefixSpacer != addPrefixSpacer) {
				if(addPrefixSpacer) {
					team.setPrefix(team.getPrefix().substring(0, team.getPrefix().length() - 1));
				} else {
					team.setPrefix(team.getPrefix().concat(" "));
				}
			}
			if(this.addSuffixSpacer != addSuffixSpacer) {
				if(addSuffixSpacer) {
					team.setSuffix(team.getSuffix().substring(0, team.getSuffix().length() - 1));
				} else {
					team.setSuffix(" ".concat(team.getSuffix()));
				}
			}
		}
		this.addPrefixSpacer = addPrefixSpacer;
		this.addSuffixSpacer = addSuffixSpacer;
	}

	public void update(OnlinePlayerAccount account) {
		TeamKey key = new TeamKey(account.getTabPrefix(), account.getTabSuffix());
		Team team;
		if(this.teams.containsKey(key)) {
			team = this.teams.get(key);
		} else {
			String teamName = "BAE_" + key.hashCode();
			if(this.board.getTeam(teamName) != null) {
				//Something went wrong whilst registering the team the first time.
				team = this.board.getTeam(teamName);
				this.teams.put(key, team);
			} else {
				team = this.board.registerNewTeam(teamName);
				team.setPrefix(ChatColor.translateAlternateColorCodes('&', account.getTabPrefix()).concat(this.addPrefixSpacer ? " " : ""));
				team.setSuffix(ChatColor.translateAlternateColorCodes('&', (this.addSuffixSpacer ? " " : "").concat(account.getTabSuffix())));
				this.teams.put(key, team);
			}
		}
		team.addEntry(account.getPlayer().getName());
	}
	
	public void clear() {
		for(Team team : this.board.getTeams()) {
			if(team.getName().startsWith("BAE_")) {
				team.unregister();
			}
		}
	}
	
	private static final class TeamKey {
		
		private final String prefix;
		private final String suffix;
		
		public TeamKey(String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
			result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TeamKey other = (TeamKey) obj;
			if (prefix == null) {
				if (other.prefix != null)
					return false;
			} else if (!prefix.equals(other.prefix))
				return false;
			if (suffix == null) {
				if (other.suffix != null)
					return false;
			} else if (!suffix.equals(other.suffix))
				return false;
			return true;
		}
	}
}