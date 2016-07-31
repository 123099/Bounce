package us.battleaxe.bae.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.battleaxe.bae.MySQLClient;
import us.battleaxe.bae.ThrowingConsumer;
import us.battleaxe.bae.Util;
import us.battleaxe.bae.api.OfflinePlayerAccount;
import us.battleaxe.bae.api.PermissionManager;
import us.battleaxe.bae.api.UUIDFetcher;
import us.battleaxe.bae.permissions.ImplementedPermissionManager.UpdateAction;
import us.battleaxe.bae.permissions.PlayerGroup;

public final class CmdBAP extends CmdExecutor {
	
	private final MySQLClient client;
	private final PermissionManager permManager;
	
	private final Pattern groupNamePattern;
	
	public CmdBAP() {
		super("bap", "cmd.bap", true);
		
		this.client = api.getMySQLClient();
		this.permManager = api.getPermissionManager();
		
		this.groupNamePattern = Pattern.compile("[\\p{Alpha}_]+");
	}
	
	/* 
	 *  /BAP help                           DONE
	 *  
	 *  /BAP player
	 *    > NAME                            DONE
	 *    > NAME  add    PERM               DONE
	 *    > NAME  add    PERM SCOPE         DONE
	 *    > NAME  remove PERM               DONE
	 *    > NAME  group  GROUP              DONE
	 *  
	 *  /BAP group
	 *    > GROUP                           DONE
	 *    > GROUP add    PERM               DONE
	 *    > GROUP add    PERM SCOPE         DONE
	 *    > GROUP remove PERM               DONE
	 *    > GROUP delete                    DONE
	 *    > GROUP create                    DONE
	 *    > GROUP create PARENT             DONE
	 *    > GROUP prefix PREFIX             DONE
	 *    > GROUP suffix SUFFIX             DONE
	 *    > GROUP parent PARENT             DONE
	 */
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 5, 1)) return false;
		if(args[0].equalsIgnoreCase("help")) {
			if(args.length == 2) {
				if(args[1].equalsIgnoreCase("player")) {
					sender.sendMessage(getCommandMessage("help.player"));
				} else if(args[1].equalsIgnoreCase("group")) {
					sender.sendMessage(getCommandMessage("help.group"));
				} else {
					sender.sendMessage(getCommandMessage("help.general"));
				}
			} else {
				sender.sendMessage(getCommandMessage("help.general"));
			}
			return true;
		}
		
		if(args[0].equalsIgnoreCase("player")) {
			if(args.length == 1 || args.length == 3) {
				sender.sendMessage(getCommandMessage("player.usage"));
				return false;
			}
			
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new PlayerSubcommandRunnable(sender, args));
			return true;
		}
		
		if(args[0].equalsIgnoreCase("group")) {
			if(args.length == 1) {
				StringBuilder groupBuilder = new StringBuilder();
				for(PlayerGroup group : this.permManager.getGroups()) {
					groupBuilder.append(getCommandMessage("group.all-groups-group",
							group.getName(),
							Util.formatInt(group.getId()),
							group.getParent() != null ? group.getParent().getName() : "-",
							group.getParent() != null ? Util.formatInt(group.getParent().getId()) : "0"));
				}
				sender.sendMessage(getCommandMessage("group.all-groups-message", groupBuilder.toString()));
				return true;
			}
			
			final PlayerGroup group = this.permManager.parseGroup(args[1].toLowerCase());
			if(args.length == 2) {
				if(group == null) {
					sender.sendMessage(getCommandMessage("group.group-not-found"));
					return false;
				}
				
				StringBuilder permBuilder = new StringBuilder();
				for(Entry<String, Boolean> entry : group.getPermissions().entrySet()) {
					permBuilder.append(", ").append(entry.getKey()).append("[").append(entry.getValue() ? 1 : 0).append("]");
				}
				
				sender.sendMessage(getCommandMessage("group.info",
						group.getName(), Util.formatInt(group.getId()),
						String.valueOf(group.hasWildcard()),
						group.getParent() != null ? group.getParent().getName() : "default", Util.formatInt(group.getParent() != null ? group.getParent().getId() : 1),
						permBuilder.length() == 0 ? "-" : permBuilder.toString().substring(2),
						group.getPrefix(),
						group.getSuffix()));
				return true;
			}
			
			if(args[2].equalsIgnoreCase("add")) {
				if(group == null) {
					sender.sendMessage(getCommandMessage("group.group-not-found"));
					return false;
				}
				
				String permission = args[3].toLowerCase();
				boolean value = !permission.startsWith("-");
				String finalPermission = !value ? permission.substring(1) : permission; //We have to fool Java's final-in-anonymous-class-politic...
				String scope = (args.length == 5 ? args[4] : "*");
				
				if(finalPermission.length() > 48) {
					sender.sendMessage(getCommandMessage("player.permission-too-long"));
					return false;
				}
				if(scope.length() > 64) {
					sender.sendMessage(getCommandMessage("player.scope-too-long"));
					return false;
				}
				
				try {
					PreparedStatement statement = this.client.prepareStatement("SELECT `action`, `permission` FROM `permissions` WHERE `holder` = ? AND `isplayer` = b'0'");
					statement.setInt(1, group.getId());

					this.client.executeQueryAsynchronously(statement, new ThrowingConsumer<ResultSet>() {
						@Override
						public void accept(ResultSet rs) {
							try {
								while (rs.next()) {
									if(rs.getString(2).equals(finalPermission)) {
										sender.sendMessage(getCommandMessage("group.entry-already-exists", rs.getString(2), rs.getBoolean(1) ? "1" : "0"));
										return;
									}
								}
							} catch (SQLException exc) {
								sender.sendMessage(getCommandMessage("group.remote-permission-add-check-process-failure", exc.getMessage()));
								plugin.getLogger().log(Level.SEVERE, "Failed to check for problematic permission-entries for group " + group.getName() + "/" + group.getId() + "] issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
								return;
							}

							if(scope.equals("*") || api.getBungeename().matches(scope)) {
								group.addAndApply(finalPermission, value);
								sender.sendMessage(getCommandMessage("group.local-permission-added", finalPermission, Util.formatInt(value ? 1 : 0), group.getName(), scope));
							} else {
								sender.sendMessage(getCommandMessage("group.local-permission-added-not-in-scope", scope, finalPermission, Util.formatInt(value ? 1 : 0), group.getName()));
							}

							try {
								group.addRemotePermission(finalPermission, value, scope, new ThrowingConsumer<Integer>() {
									@Override
									public void accept(Integer arg0) {
										sender.sendMessage(getCommandMessage("group.remote-permission-added"));
										insertNotifyStatement(group.getId(), sender, UpdateAction.GROUP_PERMISSION_UPDATE);
									}

									@Override
									public void throwException(Exception exc) {
										sender.sendMessage(getCommandMessage("group.remote-permission-add-failure", exc.getMessage()));
										plugin.getLogger().log(Level.SEVERE, "Failed to synchronize permission-add for group " + group.getName() + "/" + group.getId() + " issued by " + sender.getName(), exc);	
									}
								});
							} catch (SQLException exc) {
								sender.sendMessage(getCommandMessage("group.remote-permission-add-prepare-failure", exc.getMessage()));
								plugin.getLogger().log(Level.SEVERE, "Failed to prepare permission-add for group " + group.getName() + "/" + group.getId() + " issued by " + sender.getName(), exc);
							}
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-permission-add-check-prepare-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Failed to execute check for problematic permission-entries for group " + group.getName() + "/" + group.getId() + " issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
						}
					});

				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("group.remote-permission-add-check-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare  check for problematic permission-entries for group " + group.getName() + "/" + group.getId() + " issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
				}
			} else if(args[2].equalsIgnoreCase("remove")) {
				if(args.length == 5) {
					sender.sendMessage(getCommandMessage("group.usage"));
					return false;
				}
				
				if(group == null) {
					sender.sendMessage(getCommandMessage("group.group-not-found"));
					return false;
				}
				
				String permission = args[3].toLowerCase();
				boolean value = !permission.startsWith("-");
				String finalPermission = !value ? permission.substring(1) : permission;
				
				try {
					PreparedStatement statement = this.client.prepareStatement("SELECT `action`, `permission` FROM `permissions` WHERE `holder` = ? AND `isplayer` = b'0'");
					statement.setInt(1, group.getId());

					this.client.executeQueryAsynchronously(statement, new ThrowingConsumer<ResultSet>() {
						@Override
						public void accept(ResultSet rs) {
							boolean exists = false;
							try {
								while (rs.next()) {
									if(rs.getString(2).equals(finalPermission)) {
										exists = true;
										break;
									}
								}
							} catch (SQLException exc) {
								sender.sendMessage(getCommandMessage("group.remote-permission-remove-process-check-failure", exc.getMessage()));
								plugin.getLogger().log(Level.SEVERE, "Failed to check for matching permission-entries for group " + group.getName() + "/" + group.getId() + " issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
								return;
							}
							
							if(!exists) {
								sender.sendMessage(getCommandMessage("group.entry-doesnt-exist"));
								return;
							}

							if(group.isPermissionSet(finalPermission)) {
								group.removeAndApply(finalPermission);
								sender.sendMessage(getCommandMessage("group.local-permission-removed", finalPermission, Util.formatInt(value ? 1 : 0), group.getName()));
							} else {
								sender.sendMessage(getCommandMessage("group.local-permission-removed-not-in-scope", finalPermission, Util.formatInt(value ? 1 : 0), group.getName()));
							}

							try {
								group.removeRemotePermission(finalPermission, new ThrowingConsumer<Integer>() {
									@Override
									public void accept(Integer arg0) {
										sender.sendMessage(getCommandMessage("group.remote-permission-removed"));
										insertNotifyStatement(group.getId(), sender, UpdateAction.GROUP_PERMISSION_UPDATE);
									}

									@Override
									public void throwException(Exception exc) {
										sender.sendMessage(getCommandMessage("group.remote-permission-remove-failure", exc.getMessage()));
										plugin.getLogger().log(Level.SEVERE, "Failed to synchronize permission-removal for group " + group.getName() + "/" + group.getId() + " issued by " + sender.getName(), exc);	
									}
								});
							} catch (SQLException exc) {
								sender.sendMessage(getCommandMessage("group.remote-permission-remove-prepare-failure", exc.getMessage()));
								plugin.getLogger().log(Level.SEVERE, "Failed to prepare permission-removal for group " + group.getName() + "/" + group.getId() + " issued by " + sender.getName(), exc);	
							}						
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-permission-remove-check-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Failed to execute check for matching permission-entries for " + group.getName() + "/" + group.getId() + " issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);		
						}
					});

				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("group.remote-permission-remove-check-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare check for matching permission-entries for " + group.getName() + "/" + group.getId() + " issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);		
				}
			} else if(args[2].equalsIgnoreCase("delete")) {
				if(args.length == 5) {
					sender.sendMessage(getCommandMessage("group.usage"));
					return false;
				}
				
				if(group == null) {
					sender.sendMessage(getCommandMessage("group.group-not-found"));
					return false;
				}
				
				if(group.getId() == 1) {
					sender.sendMessage(getCommandMessage("group.cant-delete-default"));
					return false;
				}
				this.permManager.unregisterGroup(group);
				sender.sendMessage(getCommandMessage("group.local-group-deleted", group.getName(), Util.formatInt(group.getId())));
				
				try {
					PreparedStatement playerUpdateStatement = this.client.prepareStatement("UPDATE `players` SET `group` = 1 WHERE `group` = ?;");
					playerUpdateStatement.setInt(1, group.getId());
					PreparedStatement deleteStatement = this.client.prepareStatement("DELETE FROM `groups` WHERE `id` = ?;");
					deleteStatement.setInt(1, group.getId());
					PreparedStatement groupUpdateStatement = this.client.prepareStatement("UPDATE `groups` SET `parent` = ? WHERE `parent` = ?;");
					groupUpdateStatement.setInt(1, group.getParent().getId());
					groupUpdateStatement.setInt(2, group.getId());
					
					this.client.executeUpdateAsynchronously(playerUpdateStatement, new ThrowingConsumer<Integer>() {
						@Override
						public void accept(Integer arg0) {
							sender.sendMessage(getCommandMessage("group.remote-deletion-players-updated"));
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-deletion-player-updates-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Error whilst deleting group " + group.getName() + " for " + sender.getName() + " on the remote (Player-Updates)", exc);
						}
					});
					
					this.client.executeUpdateAsynchronously(deleteStatement, new ThrowingConsumer<Integer>() {
						@Override
						public void accept(Integer arg0) {
							sender.sendMessage(getCommandMessage("group.remote-deletion-group-deleted"));
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-deletion-group-deletion-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Error whilst deleting group " + group.getName() + " for " + sender.getName() + " on the remote (Group-Deletion)", exc);
						}
					});
					
					this.client.executeUpdateAsynchronously(groupUpdateStatement, new ThrowingConsumer<Integer>() {
						@Override
						public void accept(Integer arg0) {
							sender.sendMessage(getCommandMessage("group.remote-deletion-dependencies-fixed"));
							insertNotifyStatement(group.getId(), sender, UpdateAction.GROUP_DELETION);
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-deletion-dependency-fixes-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Error whilst deleting group " + group.getName() + " for " + sender.getName() + " on the remote (Dependencies-Fix)", exc);
						}
					});
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("group.remote-deletion-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare deletion of group " + group.getName() + "/" + group.getId() + " for " + sender.getName() + " on the remote!", exc);
				}
			} else if(args[2].equalsIgnoreCase("create")) {
				if(args.length == 5) {
					sender.sendMessage(getCommandMessage("group.usage"));
					return false;
				}
				
				if(group != null) {
					sender.sendMessage(getCommandMessage("group.group-already-exists"));
					return false;
				}
				
				if(args[1].length() > 16) {
					sender.sendMessage(getCommandMessage("group.name-too-long"));
					return false;
				}
				
				if(!this.groupNamePattern.matcher(args[1]).matches()) {
					sender.sendMessage(getCommandMessage("group.illegal-name"));
					return false;
				}
				
				PlayerGroup parent = null;
				if(args.length == 4) {
					parent = this.permManager.parseGroup(args[3]);
					if(parent == null) {
						sender.sendMessage(getCommandMessage("group.parent-not-found"));
						return false;
					}
				} else {
					parent = this.permManager.getGroup(1);
				}
				sender.sendMessage(getCommandMessage("group.creating", args[1].toLowerCase()));
				PlayerGroup finalParent = parent; //We have to fool Java's final-in-anonymous-class-politic...
				
				try {
					PreparedStatement putStatement = this.client.prepareStatement("INSERT INTO `groups` (`name`, `parent`, `prefix`, `suffix`) VALUES (?, ?, ?, ?);", true);
					putStatement.setString(1, args[1].toLowerCase());
					putStatement.setInt(2, parent != null ? parent.getId() : 1);
					putStatement.setString(3, "");
					putStatement.setString(4, "");
					
					this.client.executeUpdateAsynchronously(putStatement, new ThrowingConsumer<Integer>() {
						@Override
						public void accept(Integer arg0) {
							try {
								ResultSet id = putStatement.getGeneratedKeys();
								id.next();
								PlayerGroup created = new PlayerGroup(api, id.getInt(1), args[1].toLowerCase(), finalParent, "", "");
								permManager.registerGroup(created);
								sender.sendMessage(getCommandMessage("group.created", created.getName(), Util.formatInt(created.getId()),
										created.getParent() != null ? created.getParent().getName() : "-", Util.formatInt(created.getParent() != null ? created.getParent().getId() : 0)));
								insertNotifyStatement(created.getId(), sender, UpdateAction.GROUP_CREATION);
							} catch (SQLException exc) {
								sender.sendMessage(getCommandMessage("group.local-creation-failure", exc.getMessage()));
								plugin.getLogger().log(Level.SEVERE, "Failed to create group " + args[1].toLowerCase() + " for " + sender.getName() + " on this server", exc);
							}						
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-creation-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Failed to create group " + args[1].toLowerCase() + " for " + sender.getName() + " on the remote", exc);
						}
					});
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("group.creation-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare creation of group " + args[1].toLowerCase() + " for " + sender.getName(), exc);
				}
			} else if(args[2].equalsIgnoreCase("prefix")) {
				if(args.length != 4) {
					sender.sendMessage(getCommandMessage("group.usage"));
					return false;
				}
				
				if(group == null) {
					sender.sendMessage(getCommandMessage("group.group-not-found"));
					return false;
				}

				String prefix = "";
				//If args[3] equals two quotes ("")
				if(args[3].equals("\"\"")) {
					sender.sendMessage(getCommandMessage("group.local-prefix-reset", group.getName(), Util.formatInt(group.getId())));
				} else {
					if(args[3].length() > 15) {
						sender.sendMessage(getCommandMessage("group.prefix-too-long"));
						return false;
					}
					prefix = args[3].replace("_", " ");
					sender.sendMessage(getCommandMessage("group.local-prefix-set", group.getName(), Util.formatInt(group.getId()), ChatColor.translateAlternateColorCodes('&', prefix)));			
				}
				group.setPrefix(prefix);
				
				try {
					PreparedStatement statement = this.client.prepareStatement("UPDATE `groups` SET `prefix` = ? WHERE `id` = ?;");
					statement.setString(1, prefix);
					statement.setInt(2, group.getId());
					
					this.client.executeUpdateAsynchronously(statement, new ThrowingConsumer<Integer>() {
						@Override
						public void accept(Integer arg0) {
							sender.sendMessage(getCommandMessage("group.remote-prefix-set"));
							insertNotifyStatement(group.getId(), sender, UpdateAction.GROUP_PREFIX_SUFFIX_UPDATE);
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-prefix-update-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Failed to update the prefix of the group" + group.getName() + "/" + group.getId() + " for " + sender.getName() + " on the remote", exc);
						}
					});
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("group.remote-prefix-update-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare the update of the prefix of the group" + group.getName() + "/" + group.getId() + " for " + sender.getName(), exc);
				}
			} else if(args[2].equalsIgnoreCase("suffix")) {
				if(args.length != 4) {
					sender.sendMessage(getCommandMessage("group.usage"));
					return false;
				}
				
				if(group == null) {
					sender.sendMessage(getCommandMessage("group.group-not-found"));
					return false;
				}
				
				String suffix = "";
				//If args[3] equals two quotes ("")
				if(args[3].equals("\"\"")) {
					sender.sendMessage(getCommandMessage("group.local-suffix-reset", group.getName(), Util.formatInt(group.getId())));
				} else {
					if(args[3].length() > 15) {
						sender.sendMessage(getCommandMessage("group.suffix-too-long"));
						return false;
					}
					suffix = args[3].replace("_", " ");
					sender.sendMessage(getCommandMessage("group.local-suffix-set", group.getName(), Util.formatInt(group.getId()), ChatColor.translateAlternateColorCodes('&', suffix)));			
				}
				group.setSuffix(suffix);
				
				try {
					PreparedStatement statement = this.client.prepareStatement("UPDATE `groups` SET `suffix` = ? WHERE `id` = ?;");
					statement.setString(1, suffix);
					statement.setInt(2, group.getId());
					
					this.client.executeUpdateAsynchronously(statement, new ThrowingConsumer<Integer>() {
						@Override
						public void accept(Integer arg0) {
							sender.sendMessage(getCommandMessage("group.remote-suffix-set"));
							insertNotifyStatement(group.getId(), sender, UpdateAction.GROUP_PREFIX_SUFFIX_UPDATE);
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-suffix-update-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Failed to update the suffix of the group" + group.getName() + "/" + group.getId() + " for " + sender.getName() + " on the remote", exc);
						}
					});
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("group.remote-suffix-update-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare the update of the prefix of the group" + group.getName() + "/" + group.getId() + " for " + sender.getName(), exc);
				}
			} else if(args[2].equalsIgnoreCase("parent")) {
				if(args.length != 4) {
					sender.sendMessage(getCommandMessage("group.usage"));
					return false;
				}
				
				if(group == null) {
					sender.sendMessage(getCommandMessage("group.group-not-found"));
					return false;
				}
				
				if(group.equals(this.permManager.getGroup(1))) {
					sender.sendMessage(getCommandMessage("group.cant-change-parent-of-default"));
					return false;
				}
				
				PlayerGroup parent = this.permManager.parseGroup(args[3]);
				if(parent == null) {
					sender.sendMessage(getCommandMessage("group.parent-not-found"));
					return false;
				}
				
				if(group.equals(parent)) {
					sender.sendMessage(getCommandMessage("group.group-equals-parent"));
					return false;
				}
				
				if(parent.getInheritanceChain().contains(group)) {
					sender.sendMessage(getCommandMessage("group.circular-dependency"));
					return false;
				}
				group.setParent(parent);
				sender.sendMessage(getCommandMessage("group.local-parent-set", group.getName(), Util.formatInt(group.getId()), parent.getName(), String.valueOf(parent.getId())));
				PlayerGroup finalParent = parent; //We have to fool Java's final-in-anonymous-class-politic...
				
				try {
					PreparedStatement updateStatement = this.client.prepareStatement("UPDATE `groups` SET `parent` = ? WHERE `id` = ?;");
					updateStatement.setInt(1, parent.getId());
					updateStatement.setInt(2, group.getId());
					
					this.client.executeUpdateAsynchronously(updateStatement, new ThrowingConsumer<Integer>() {
						@Override
						public void accept(Integer arg0) {
							sender.sendMessage(getCommandMessage("group.remote-group-set"));
							insertNotifyStatement(group.getId(), sender, UpdateAction.GROUP_PARENT_UPDATE);
						}

						@Override
						public void throwException(Exception exc) {
							sender.sendMessage(getCommandMessage("group.remote-parent-set-failure", exc.getMessage()));
							plugin.getLogger().log(Level.SEVERE, "Failed to set the parent of group " + group.getName() + "/" + group.getId() + " to " + finalParent.getName() + "/" + finalParent.getId() + " for " + sender.getName() + " on the remote", exc);
						}
					});
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("group.remote-parent-set-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare update of parent of group " + group.getName() + "/" + group.getId() + " to " + finalParent.getName() + "/" + finalParent.getId() + " for " + sender.getName() + " on the remote", exc);
				}			
			} else {
				sender.sendMessage(getCommandMessage("group.usage"));
			}
			return false;
		}
		sendUsageMessage(sender);
		return false;
	}
	
	private void insertNotifyStatement(int updated, CommandSender sender, UpdateAction action) {
		PreparedStatement statement = null;
		try {
			statement = this.client.prepareStatement("INSERT INTO `permission_updates` (`updated`, `action`, `issued`, `issuer`, `source`) VALUES (?, ?, ?, ?, ?);");
			statement.setInt(1, updated);
			statement.setByte(2, action.getId());
			statement.setLong(3, System.currentTimeMillis());
			statement.setInt(4, sender instanceof Player ? api.getOnlineAccount(((Player) sender).getUniqueId()).getId() : 0);
			statement.setString(5, api.getBungeename());
		} catch (SQLException exc) {
			sender.sendMessage(api.getMessage("general.mysql-notify-prepare-failure", exc.getMessage()));
			plugin.getLogger().log(Level.SEVERE, "Failed to prepare permission-change-notification for " + sender.getName() + " with value " + action.toString(), exc);
			return;
		}
		
		this.client.executeUpdateAsynchronously(statement, new ThrowingConsumer<Integer>() {
			@Override
			public void accept(Integer arg0) { /* Ignore */ }

			@Override
			public void throwException(Exception exc) {
				sender.sendMessage(api.getMessage("general.mysql-notify-execute-failure", exc.getMessage()));
				plugin.getLogger().log(Level.SEVERE, "Failed to execute permission-change-notification for " + sender.getName() + (action.isPlayerAction() ? " for player with id " : " for group with id ") + updated, exc);
			}
		});
	}
	
	//The subcommands "add", "remove" and "group" don't use OfflineAccount#addRemotePermission(), #removeRemotePermission() and #setRemoteGroup because those methods are designed to be used in a synchronous context. 
	private class PlayerSubcommandRunnable implements Runnable {
		
		private final CommandSender sender;
		private final String[] args;
		
		public PlayerSubcommandRunnable(CommandSender sender, String[] args) {
			this.sender = sender;
			this.args = args;
		}
		
		@Override
		public void run() {
			OfflinePlayerAccount account;
			if(Bukkit.getPlayer(args[1]) != null) {
				account = api.getOfflineAccount(Bukkit.getPlayer(args[1]).getUniqueId());
			} else {
				try {
					sender.sendMessage(getCommandMessage("player.loading", args[1]));
					UUID uuid = new UUIDFetcher(Arrays.asList(args[1])).call().get(args[1]);
					if(uuid == null) {
						sender.sendMessage(getCommandMessage("player.never-joined-before"));
						return;
					}
					account = api.getOfflineAccount(uuid);
					if(account == null) {
						account = api.loadAccount(uuid);
					}
					if(account == null) {
						sender.sendMessage(getCommandMessage("player.never-joined-before"));
						return;
					}
				} catch (Exception exc) {
					sender.sendMessage(getCommandMessage("player.player-fetch-failure", args[1], exc.getMessage()));
					exc.printStackTrace();
					return;
				}
			}
			
			if(args.length == 2) {
				StringBuilder permBuilder = new StringBuilder();
				for(Entry<String, Boolean> entry : account.getPermissions().entrySet()) {
					permBuilder.append(", ").append(entry.getKey()).append("[").append(entry.getValue() ? 1 : 0).append("]");
				}
				
				sender.sendMessage(getCommandMessage("player.info", args[1], Util.formatInt(account.getId()), String.valueOf(account.hasWildcardPermission()),
						account.getGroup().getName(), Util.formatInt(account.getGroup().getId()), permBuilder.length() == 0 ? "-" : permBuilder.toString().substring(2)));
				return;
			}
			
			if(args[2].equalsIgnoreCase("add")) {
				String permission = args[3].toLowerCase();
				boolean value = !permission.startsWith("-");
				String finalPermission = !value ? permission.substring(1) : permission;
				String scope = (args.length == 5 ? args[4] : "*");
				
				if(finalPermission.length() > 48) {
					sender.sendMessage(getCommandMessage("group.permission-too-long"));
					return;
				}
				if(scope.length() > 64) {
					sender.sendMessage(getCommandMessage("group.scope-too-long"));
					return;
				}
				
				PreparedStatement checkStatement;
				try {
					checkStatement = client.prepareStatement("SELECT `action`, `permission` FROM `permissions` WHERE `holder` = ? AND `isplayer` = b'1'");
					checkStatement.setInt(1, account.getId());
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-add-check-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare check for problematic permission-entries for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
					return;
				}
				
				ResultSet rs;
				try {
					rs = client.executeQuery(checkStatement);
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-add-check-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to exceute check for problematic permission-entries for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
					return;
				}
				
				try {
					while (rs.next()) {
						if(rs.getString(2).equals(finalPermission)) {
							sender.sendMessage(getCommandMessage("player.entry-already-exists", rs.getString(2), rs.getBoolean(1) ? "1" : "0"));
							return;
						}
					}
								
					if(scope.equals("*") || api.getBungeename().matches(scope)) {
						if(account.isLoggedIn()) {
							account.getOnlineAccount().addAndApply(finalPermission, value);
						} else {
							account.addPermission(finalPermission, value);
						}
						sender.sendMessage(getCommandMessage("player.local-permission-added", finalPermission, Util.formatInt(value ? 1 : 0), args[1], scope));
					} else {
						sender.sendMessage(getCommandMessage("player.local-permission-added-not-in-scope", scope, finalPermission, Util.formatInt(value ? 1 : 0), args[1]));
					}
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-add-check-process-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to check for problematic permission-entries for " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
					return;
				}

				PreparedStatement insertStatement;
				try {
					insertStatement = client.prepareStatement("INSERT INTO `permissions` (`holder`, `isplayer`, `action`, `permission`, `scope`) VALUES (?, b'1', ?, ?, ?);");
					insertStatement.setInt(1, account.getId());
					insertStatement.setBoolean(2, value);
					insertStatement.setString(3, finalPermission);
					insertStatement.setString(4, scope);
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-add-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare permission-add for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);
					return;
				}
				
				try {
					client.executeUpdate(insertStatement);
					sender.sendMessage(getCommandMessage("player.remote-permission-added"));
					insertNotifyStatement(account.getId(), sender, UpdateAction.PLAYER_PERMISSION_UPDATE);
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-add-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to synchronize permission-add for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);	
				}
			} else if(args[2].equalsIgnoreCase("remove")) {
				if(args.length == 5) {
					sender.sendMessage(getCommandMessage("player.usage"));
					return;
				}
				
				String permission = args[3].toLowerCase();
				boolean value = !permission.startsWith("-");
				String finalPermission = !value ? permission.substring(1) : permission;
				
				PreparedStatement checkStatement;
				try {
					checkStatement = client.prepareStatement("SELECT `action`, `permission` FROM `permissions` WHERE `holder` = ? AND `isplayer` = b'1'");
					checkStatement.setInt(1, account.getId());
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-remove-check-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare check for matching permission-entries for " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);		
					return;
				}
				
				ResultSet rs;
				try {
					rs = client.executeQuery(checkStatement);
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-remove-check-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to execute check for matching permission-entries for " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);		
					return;
				}
				
				boolean exists = false;
				try {
					while (rs.next()) {
						if(rs.getString(2).equals(finalPermission)) {
							exists = true;
							break;
						}
					}
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-remove-process-check-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to check for matching permission-entries for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName() + " (permission: " + finalPermission + ")", exc);
					return;
				}
				
				if(!exists) {
					sender.sendMessage(getCommandMessage("player.entry-doesnt-exist"));
					return;
				}

				if(account.isPermissionSet(finalPermission)) {
					if(account.isLoggedIn()) {
						account.getOnlineAccount().removeAndApply(finalPermission);
					} else {
						account.removePermission(finalPermission);
					}
					sender.sendMessage(getCommandMessage("player.local-permission-removed", finalPermission, Util.formatInt(value ? 1 : 0), args[1]));
				} else {
					sender.sendMessage(getCommandMessage("player.local-permission-removed-not-in-scope", finalPermission, Util.formatInt(value ? 1 : 0), args[1]));
				}

				PreparedStatement removeStatement;
				try {
					removeStatement = client.prepareStatement("DELETE FROM `permissions` WHERE `permission` = ? AND `holder` = ? AND `isplayer` = b'1';");
					removeStatement.setString(1, finalPermission);
					removeStatement.setInt(2, account.getId());
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-remove-prepare-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to prepare permission-removal for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);	
					return;
				}
				
				try {
					client.executeUpdate(removeStatement);
					sender.sendMessage(getCommandMessage("player.remote-permission-removed"));
					insertNotifyStatement(account.getId(), sender, UpdateAction.PLAYER_PERMISSION_UPDATE);
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-permission-remove-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to synchronize permission-removal for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);	
				}
			} else if(args[2].equalsIgnoreCase("group")) {
				if(args.length == 5) {
					sender.sendMessage(getCommandMessage("player.usage"));
					return;
				}
				
				PlayerGroup group = permManager.parseGroup(args[3]);
				if(group == null) {
					sender.sendMessage(getCommandMessage("player.unknown-group"));
					return;
				}
				
				if(account.isLoggedIn()) {
					account.getOnlineAccount().setLocalGroupAndApply(group);
				} else {
					account.setLocalGroup(group);
				}
				sender.sendMessage(getCommandMessage("player.local-group-updated", args[1], group.getName(), Util.formatInt(group.getId())));
				
				PreparedStatement updateStatement;
				try {
					updateStatement = client.prepareStatement("UPDATE `players` SET `group` = ? WHERE `id` = ?;");
					updateStatement.setInt(1, group.getId());
					updateStatement.setInt(2, account.getId());
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-group-update-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to synchronize group-update for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);	
					return;
				}
				
				try {
					client.executeUpdate(updateStatement);
					sender.sendMessage(getCommandMessage("player.remote-group-updated"));
					insertNotifyStatement(account.getId(), sender, UpdateAction.PLAYER_GROUP_UPDATE);
				} catch (SQLException exc) {
					sender.sendMessage(getCommandMessage("player.remote-group-update-failure", exc.getMessage()));
					plugin.getLogger().log(Level.SEVERE, "Failed to synchronize group-update for player " + args[1] + " [" + account.getUUID().toString() + "/" + account.getId() + "] issued by " + sender.getName(), exc);
				}	
			} else {
				sender.sendMessage(getCommandMessage("player.usage"));
			}				
		}
	}
}