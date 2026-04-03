package me.scifi.hcf.lunar.modules;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.nametag.Nametag;
import com.lunarclient.apollo.module.nametag.NametagModule;

import lombok.Getter;
import net.kyori.adventure.text.Component;

/**
 * Manages the integration between the server and the LunarClient Apollo Nametag
 * module.
 * <p>
 * This class allows overriding a player’s nametag as seen by a specific viewer.
 * It uses the {@link NametagModule} provided by the Apollo API to send custom
 * nametag lines (prefixes, suffixes, and other contextual information).
 * </p>
 *
 * <p>
 * <strong>Example use case:</strong>
 * </p>
 * <ul>
 * <li>Display faction names or ranks above player heads.</li>
 * <li>Show dynamic information such as combat tags or health.</li>
 * <li>Integrate color-coded relationships (ally, enemy, etc.).</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> This class only affects LunarClient players connected
 * through the Apollo API. Other clients will not see any change.
 * </p>
 *
 * @author DoctorDark / adapted by Leandro (HCF Fork)
 * @see <a href="https://lunarclient.dev/apollo/developers/modules/nametag"
 *      target="_blank"> LunarClient Apollo Nametag Documentation </a>
 */
@Getter
public class NametagManager {

	/**
	 * The LunarClient Apollo module responsible for nametag functionality.
	 */
	private final NametagModule nametagModule = Apollo.getModuleManager().getModule(NametagModule.class);

	/**
	 * Overrides the nametag of a given {@link Player target} as seen by another
	 * {@link Player viewer}.
	 * <p>
	 * The provided list of strings will replace the default nametag lines above the
	 * target's head. Each entry in the list represents a line in the nametag,
	 * rendered from top to bottom.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>{@code
	 * nametagManager.overrideNametags(target, viewer, Arrays.asList("&c[ENEMY]", "&f" + target.getName()));
	 * }</pre>
	 *
	 * @param target The player whose nametag will be overridden.
	 * @param viewer The player who will see the overridden nametag.
	 * @param tag    A list of strings representing each line in the new nametag.
	 *               The text supports color codes and formatting handled by
	 *               Adventure {@link Component}.
	 */
	public void overrideNametags(Player target, Player viewer, List<String> tag) {
		Apollo.getPlayerManager().getPlayer(viewer.getUniqueId())
				.ifPresent(apolloViewer -> nametagModule.overrideNametag(apolloViewer, target.getUniqueId(), Nametag
						.builder().lines(tag.stream().map(Component::text).collect(Collectors.toList())).build()));
	}
}