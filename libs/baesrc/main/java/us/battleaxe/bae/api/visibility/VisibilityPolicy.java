package us.battleaxe.bae.api.visibility;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface VisibilityPolicy {
	/**
	 * Queries this policy's opinion on whether the subject player should be
	 * able to see the object player, or if the policy has no effect on the
	 * subject.
	 *
	 * @param subject The player viewing the object
	 * @param object  The player being viewed by the subject
	 * @return True if the subject can see the object, false if not, or an empty
	 * Optional if the policy has no effect.
	 */
	Optional<Boolean> canSee(Player subject, Player object);

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
	Optional<Boolean> canSee(UUID subject, UUID object);
}
