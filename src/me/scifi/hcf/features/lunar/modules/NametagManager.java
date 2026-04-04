package me.scifi.hcf.features.lunar.modules;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.nametag.Nametag;
import com.lunarclient.apollo.module.nametag.NametagModule;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class NametagManager {

	/**
	 * The LunarClient Apollo module responsible for nametag functionality.
	 */
	private final NametagModule nametagModule = Apollo.getModuleManager().getModule(NametagModule.class);

	public void overrideNametags(Player target, Player viewer, List<String> tag) {
		Apollo.getPlayerManager().getPlayer(viewer.getUniqueId())
				.ifPresent(apolloViewer -> nametagModule.overrideNametag(apolloViewer, target.getUniqueId(), Nametag
						.builder().lines(tag.stream().map(Component::text).collect(Collectors.toList())).build()));
	}
}