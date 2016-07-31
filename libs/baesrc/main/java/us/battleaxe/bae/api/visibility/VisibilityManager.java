package us.battleaxe.bae.api.visibility;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Manages visibility policies that allow multiple plugins to hide and show
 * players without conflict.
 */
public class VisibilityManager implements Listener {
	private final Plugin plugin;

	private final Map<Integer, VisibilityPolicy> policyMap = new TreeMap<>();

	public VisibilityManager(Plugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		Bukkit.getOnlinePlayers().forEach(player -> {
			updateVisibility(event.getPlayer(), player);
			updateVisibility(player, event.getPlayer());
		});
	}

	/**
	 * Registers a VisibilityPolicy at a specified priority. Policies at a
	 * higher priority override policies at a lower priority. The priority must
	 * be unique; i.e. no two policies may have the same priority.
	 *
	 * @param priority The priority, an integer greater than zero
	 * @param policy   The policy to register
	 * @throws IllegalArgumentException if a policy already exists with the same
	 *                                  priority
	 */
	public void registerPolicy(int priority, VisibilityPolicyBase policy) {
		Validate.isTrue(priority > 0, "Priority must be a positive integer");
		Validate.notNull(policy, "Policy cannot be null");

		if (policyMap.containsKey(priority)) {
			throw new IllegalArgumentException("Policy with priority " + priority + " already registered");
		}

		policyMap.put(priority, policy);
		policy.manager = this;

		if (policy instanceof Listener) {
			plugin.getServer().getPluginManager().registerEvents((Listener) policy, plugin);
		}

		updatePolicyPairings(policy);
	}

	/**
	 * Unregisters a VisibilityPolicy. This will free the policy's priority for
	 * usage by another policy.
	 *
	 * @param policy The policy to unregister
	 */
	public void unregisterPolicy(VisibilityPolicyBase policy) {
		Validate.notNull(policy, "Policy cannot be null");

		if (!policyMap.values().remove(policy)) {
			return;
		}
		policy.manager = null;

		if (policy instanceof Listener) {
			HandlerList.unregisterAll((Listener) policy);
		}

		updatePolicyPairings(policy);
	}

	/**
	 * Updates the visibility of the object to the subject. This will recompute
	 * whether the subject should be able to see the object, then apply a change
	 * ingame if necessary.
	 *
	 * @param subject The player viewing the object
	 * @param object  The player being viewed by the subject
	 */
	void updateVisibility(Player subject, Player object) {
		Validate.notNull(subject, "Subject cannot be null");
		Validate.notNull(object, "Object cannot be null");

		if (subject.equals(object)) {
			return;
		}

		boolean visible = true;
		for (VisibilityPolicy policy : policyMap.values()) {
			Optional<Boolean> opinion = policy.canSee(subject, object);
			if (opinion.isPresent()) {
				visible = opinion.get();
			}
		}

		if (visible) {
			subject.showPlayer(object);
		} else {
			subject.hidePlayer(object);
		}
	}

	private void updatePolicyPairings(VisibilityPolicyBase policy) {
		for (Map.Entry<UUID, Set<UUID>> pair : policy.getPairings().entrySet()) {
			Player subject = Bukkit.getPlayer(pair.getKey());
			if (subject != null) {
				for (UUID objectId : pair.getValue()) {
					if (objectId.equals(pair.getKey())) {
						continue;
					}
					Player object = Bukkit.getPlayer(objectId);
					updateVisibility(subject, object);
				}
			}
		}
	}
}
