package us.battleaxe.bae.api.visibility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.battleaxe.bae.events.PlayerDisableStaffModeEvent;
import us.battleaxe.bae.events.PlayerEnableStaffModeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class StaffVisibilityPolicy extends VisibilityPolicyBase implements Listener {
	private final Set<UUID> staff = new HashSet<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onStaffEnable(PlayerEnableStaffModeEvent e) {
		staff.add(e.getPlayer().getUniqueId());
		Bukkit.getOnlinePlayers().stream()
				.map(Entity::getUniqueId)
				.forEach(id -> {
					update(id, e.getPlayer().getUniqueId());
					update(e.getPlayer().getUniqueId(), id);
				});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onStaffDisable(PlayerDisableStaffModeEvent e) {
		staff.remove(e.getPlayer().getUniqueId());
		Bukkit.getOnlinePlayers().stream()
				.map(Entity::getUniqueId)
				.forEach(id -> {
					update(id, e.getPlayer().getUniqueId());
					update(e.getPlayer().getUniqueId(), id);
				});
	}

	@Override
	protected Map<UUID, Set<UUID>> getPairings() {
		Map<UUID, Set<UUID>> pairings = new HashMap<>();

		Set<UUID> online = Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).collect(Collectors.toSet());
		online.forEach(id -> pairings.put(id, staff));
		staff.forEach(id -> pairings.put(id, online));

		return pairings;
	}

	@Override
	public Optional<Boolean> canSee(UUID subject, UUID object) {
		if (staff.contains(subject)) {
			return Optional.of(true);
		} else if (staff.contains(object)) {
			return Optional.of(false);
		} else {
			return Optional.empty();
		}
	}
}
