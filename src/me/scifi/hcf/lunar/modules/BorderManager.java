package me.scifi.hcf.lunar.modules;

import java.awt.Color;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.cuboid.Cuboid2D;
import com.lunarclient.apollo.module.border.Border;
import com.lunarclient.apollo.module.border.BorderModule;
import com.lunarclient.apollo.player.ApolloPlayer;

import me.scifi.hcf.faction.claim.Claim;

/**
 * Manages the creation and removal of region borders (claims, events, etc.) for
 * players using Lunar Client via the Apollo API.
 * <p>
 * This class allows you to send visual borders around specific areas, such as
 * faction claims, KOTH zones, or warzones, which appear directly on the
 * client-side of Lunar Client players.
 * </p>
 *
 * <h2>Example usage:</h2>
 * 
 * <pre>{@code
 * BorderManager manager = new BorderManager();
 *
 * // Send a red border around a faction claim
 * manager.sendBorderPacket(player, claim, Color.RED);
 *
 * // Later, remove that same border
 * manager.sendRemoveBorderPacket(player, claim.getClaimUniqueID());
 * }</pre>
 *
 * @author
 * @version 1.0
 * @see <a href="https://lunarclient.dev/apollo/developers/modules/border"
 *      target="_blank"> Lunar Client Apollo Border Module</a>
 */
public class BorderManager { // TODO: Hook to Combat Timer/VisualiseHandler.

	/**
	 * Displays a colored border around the given {@link Claim} for the specified
	 * player.
	 * <p>
	 * The border is visible only to that player and does not affect other players.
	 * Borders can be used to visually represent protected zones (safezones, faction
	 * claims, KOTH areas, etc.).
	 * </p>
	 *
	 * @param player the player to display the border to
	 * @param claim  the claim defining the area (min/max coordinates and world)
	 * @param color  the color of the border (e.g., {@link Color#RED})
	 */
	public void sendBorderPacket(Player player, Claim claim, Color color) {
		Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
		BorderModule borderModule = Apollo.getModuleManager().getModule(BorderModule.class);

		apolloPlayerOpt.ifPresent(apolloPlayer -> {
			Border border = Border.builder().id(claim.getClaimUniqueID().toString()) // unique border ID
					.world(claim.getWorld().getName()).cancelEntry(true) // prevents entry
					.cancelExit(true) // prevents exit
					.canShrinkOrExpand(false).color(color)
					.bounds(Cuboid2D.builder().minX(claim.getMinimumX()).minZ(claim.getMinimumZ())
							.maxX(claim.getMaximumX()).maxZ(claim.getMaximumZ()).build())
					.durationTicks(100) // border display duration (in ticks)
					.build();

			borderModule.displayBorder(apolloPlayer, border);
		});
	}

	/**
	 * Removes a border from a player's client using its unique ID.
	 * <p>
	 * This should be used to clean up borders after events end, claims are
	 * unclaimed, or when a player no longer needs to see a particular region.
	 * </p>
	 *
	 * @param player the player whose client will have the border removed
	 * @param id     the unique border ID (usually {@link Claim#getClaimUniqueID()})
	 */
	public void sendRemoveBorderPacket(Player player, UUID id) {
		Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
		BorderModule borderModule = Apollo.getModuleManager().getModule(BorderModule.class);

		apolloPlayerOpt.ifPresent(apolloPlayer -> borderModule.removeBorder(apolloPlayer, id.toString()));
	}
}
