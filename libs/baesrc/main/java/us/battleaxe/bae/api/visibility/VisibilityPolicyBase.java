package us.battleaxe.bae.api.visibility;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class VisibilityPolicyBase implements VisibilityPolicy {
	VisibilityManager manager;

	public final Optional<Boolean> canSee(Player subject, Player object) {
		Validate.notNull(subject, "Subject player cannot be null");
		Validate.notNull(object, "Object player cannot be null");
		return canSee(subject.getUniqueId(), object.getUniqueId());
	}

	protected abstract Map<UUID, Set<UUID>> getPairings();

	protected final void update(UUID subject, UUID object) {
		Player pSubject = Bukkit.getPlayer(subject);
		Player pObject = Bukkit.getPlayer(object);
		if (pSubject == null || pObject == null) {
			return;
		}
		manager.updateVisibility(pSubject, pObject);
	}
}
