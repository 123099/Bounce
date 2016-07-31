package us.battleaxe.bae.api.visibility;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GroupVisibilityPolicy extends VisibilityPolicyBase {
	private final Map<UUID, Map<UUID, Boolean>> table = new HashMap<>();

	/**
	 * Sets this policy's opinion on whether the subject player should be able
	 * to see the object player, or whether the policy should have no effect on
	 * the subject.
	 *
	 * @param subject The player viewing the object
	 * @param object  The player being viewed by the subject
	 * @param visible True if the subject should see the object, false if not,
	 *                or null if the policy should have no effect.
	 */
	public void setVisibility(Player subject, Player object, Boolean visible) {
		Validate.notNull(subject, "Subject player cannot be null");
		Validate.notNull(object, "Object player cannot be null");
		setVisibility(subject.getUniqueId(), object.getUniqueId(), visible);
	}

	/**
	 * Sets this policy's opinion on whether the subject player should be able
	 * to see the object player, or whether the policy should have no effect on
	 * the subject.
	 *
	 * @param subject The UUID of the player viewing the object
	 * @param object  The UUID of the player being viewed by the subject
	 * @param visible True if the subject should see the object, false if not,
	 *                or null if the policy should have no effect.
	 */
	public void setVisibility(UUID subject, UUID object, Boolean visible) {
		Validate.notNull(subject, "Subject UUID cannot be null");
		Validate.notNull(object, "Object UUID cannot be null");

		if (subject.equals(object)) {
			return; // This occurs commonly and is benign, so fail silently
		}

		if (visible == null) {
			// Remove any existing subject/object configuration
			if (table.containsKey(subject)) {
				Map<UUID, Boolean> map = table.get(subject);
				if (map.containsKey(object)) {
					map.remove(object);
					// If this was the only configuration, we can delete the map
					// to save memory.
					if (map.isEmpty()) {
						table.remove(subject);
					}
				}
				// We don't have to delete the map if the object's configuration
				// was not present since there must be configurations for other
				// objects, otherwise the map would already have been deleted
				// by removal of said configurations, or the map would never
				// have been created in the first place.
			}
		} else {
			// Create the map to store configurations if not already existent
			if (!table.containsKey(subject)) {
				table.put(subject, new HashMap<>());
			}
			Map<UUID, Boolean> map = table.get(subject);
			map.put(object, visible);
		}

		if (manager != null) {
			Player subjectPlayer = Bukkit.getPlayer(subject);
			Player objectPlayer = Bukkit.getPlayer(object);
			if (subjectPlayer != null && objectPlayer != null) {
				manager.updateVisibility(subjectPlayer, objectPlayer);
			}
		}
	}

	public void setVisibility(UUID subject, Set<UUID> objects, Boolean visible) {
		Validate.notNull(subject, "Subject UUID cannot be null");
		Validate.notNull(objects, "Object UUID set cannot be null");

		objects.forEach(o -> setVisibility(subject, o, visible));
	}

	public void setVisibility(Set<UUID> subjects, UUID object, Boolean visible) {
		Validate.notNull(subjects, "Subject UUID set cannot be null");
		Validate.notNull(object, "Object UUID cannot be null");

		subjects.forEach(s -> setVisibility(s, object, visible));
	}

	public void setVisibility(Set<UUID> subjects, Set<UUID> objects, Boolean visible) {
		Validate.notNull(subjects, "Subject UUID set cannot be null");
		Validate.notNull(objects, "Object UUID set cannot be null");

		subjects.forEach(s -> objects.forEach(o -> setVisibility(s, o, visible)));
	}

	protected Map<UUID, Set<UUID>> getPairings() {
		Map<UUID, Set<UUID>> pairings = new HashMap<>();
		for (UUID subject : table.keySet()) {
			pairings.put(subject, table.get(subject).keySet());
		}
		return pairings;
	}

	/**
	 * Queries this policy's opinion on whether the subject player should be
	 * able to see the object player, or if the policy has no effect on the
	 * subject.
	 *
	 * @param subject The UUID of the player viewing the object
	 * @param object  The UUID of the player being viewed by the subject
	 * @return True if the subject can see the object, false if not, or an empty
	 * Optional if the policy has no effect.
	 */
	public Optional<Boolean> canSee(UUID subject, UUID object) {
		Validate.notNull(subject, "Subject UUID cannot be null");
		Validate.notNull(object, "Object UUID cannot be null");

		// No opinion if the subject does not have a configuration map
		if (!table.containsKey(subject)) {
			return Optional.empty();
		}

		// No opinion if the configuration map has no entry for the object
		Map<UUID, Boolean> map = table.get(subject);
		if (!map.containsKey(object)) {
			return Optional.empty();
		}

		Boolean visibility = map.get(object);
		if (visibility == null) {
			// Null values should not be allowed, but just in case...
			return Optional.empty();
		} else {
			return Optional.of(visibility);
		}
	}
}
