package com.salah.mcpplayersservice;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PlayerService {

	Map<String, Player> players = new HashMap<>();

	@Tool(name = "addPlayer", description = "Add a footballer player")
	public Player addPlayer(Player player) {
		players.put(player.name(), player);
		return player;
	}

	@Tool(name = "DeletePlayer", description = "Delete one footballer player")
	public String deletePlayer(String name) {
		players.remove(name);
		return "deleted";
	}

	@Tool(name = "GetPlayer", description = "Get one footballer player")
	public Player getPlayer(String name) {
		return players.get(name);
	}

	public record Player(String name, String team) {
	}

}

// add a footballer player
// delete one
// get theme