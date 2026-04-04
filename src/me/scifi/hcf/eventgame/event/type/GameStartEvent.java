package me.scifi.hcf.eventgame.event.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.scifi.hcf.eventgame.event.GameEvent;
import me.scifi.hcf.eventgame.faction.EventFaction;

@RequiredArgsConstructor
@Getter
public class GameStartEvent extends GameEvent {

	private final EventFaction faction;

}
