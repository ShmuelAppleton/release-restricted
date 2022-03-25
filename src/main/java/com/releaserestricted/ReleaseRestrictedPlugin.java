package com.releaserestricted;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Release Restricted"
)
public class ReleaseRestrictedPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ReleaseRestrictedConfig config;

	@Override
	protected void startUp() throws Exception
	{
		//log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		//log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Content is restricted beyond year " + config.year(), null);
		}
	}

	@Provides
	ReleaseRestrictedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ReleaseRestrictedConfig.class);
	}
}
