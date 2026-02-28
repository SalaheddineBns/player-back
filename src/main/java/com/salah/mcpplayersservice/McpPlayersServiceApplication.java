package com.salah.mcpplayersservice;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.List;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class McpPlayersServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpPlayersServiceApplication.class, args);
	}

	@Bean
	public List<ToolCallback> tools(PlayerService playerService) {
		return List.of(ToolCallbacks.from(playerService));
	}

}
