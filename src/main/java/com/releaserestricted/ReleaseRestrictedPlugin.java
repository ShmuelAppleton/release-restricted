package com.releaserestricted;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.releaserestricted.Util.ChatNotifications;
import com.releaserestricted.Util.Http;
import com.releaserestricted.Util.ReleaseDate;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Inject
	private ChatMessageManager chatMessageManager;

	private Map<Integer, String> itemNames;
	private Map<Integer, ReleaseDate> itemReleaseDates = new HashMap<>();
	private Map<Integer, ReleaseDate> npcReleaseDates = new HashMap<>();

	private List<Integer> checkedNpcs = new ArrayList<>();
	private List<Integer> approvedNpcs = new ArrayList<>();
	private List<Integer> checkedItems = new ArrayList<>();
	private List<Integer> approvedItems = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		itemNames = Http.getItemNames();
		itemNames.put(995, "Coins");
		ChatNotifications.configNotification(config, chatMessageManager);
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event){
		int itemId = event.getItem().getId();
		String itemName = itemNames.get(itemId);
		boolean isChecked = false;
		boolean isApproved = false;
		if (!checkedItems.isEmpty()){
			for (int id: checkedItems){
				if (id == itemId){
					isChecked = true;
					break;
				}
			}
		}
		if (!isChecked){
			checkedItems.add(itemId);
			if (itemName != null) {
				Http.getReleaseDateByName(itemName, itemId).whenCompleteAsync((date, ex) -> {
					if (date.getYear() <= Integer.parseInt(config.year().toString())) {
						approvedItems.add(itemId);
					}
					itemReleaseDates.put(itemId, date);
				});
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event){
		int npcId = event.getNpc().getId();
		String npcName = event.getNpc().getName();

		boolean isChecked = false;
		boolean isApproved = false;
		if (!checkedNpcs.isEmpty()){
			for (int id: checkedNpcs){
				if (id == npcId){
					isChecked = true;
					break;
				}
			}
		}
		if (!isChecked){
			checkedNpcs.add(npcId);
			Http.getReleaseDateByName(npcName, npcId).whenCompleteAsync(( date, ex) -> {
				if (date.getYear() <= Integer.parseInt(config.year().toString())) {
					approvedNpcs.add(npcId);
				}
				npcReleaseDates.put(npcId, date);
			});
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event){
		if (
				(event.getMenuAction().equals(MenuAction.EXAMINE_NPC) ||
						event.getMenuAction().equals(MenuAction.NPC_FIRST_OPTION) ||
						event.getMenuAction().equals(MenuAction.NPC_SECOND_OPTION) ||
						event.getMenuAction().equals(MenuAction.NPC_THIRD_OPTION) ||
						event.getMenuAction().equals(MenuAction.NPC_FOURTH_OPTION) ||
						event.getMenuAction().equals(MenuAction.NPC_FIFTH_OPTION))
		) {
			final NPC[] cachedNPCs = client.getCachedNPCs();
			int id = event.getId();
			if (id < cachedNPCs.length) {
				NPC target = cachedNPCs[id];
				if (!approvedNpcs.contains(target.getId())) {
					event.consume();
					String releaseDate = "";
					if(npcReleaseDates.get(target.getId()) != null) {
						releaseDate = npcReleaseDates.get(target.getId()).getDate();
					}
					ChatNotifications.sendChatNotification("This npc has not yet been released! Release Date: " + releaseDate, chatMessageManager);
				}
			}
		}
		if (event.getMenuAction().equals(MenuAction.GROUND_ITEM_FIRST_OPTION) ||
				event.getMenuAction().equals(MenuAction.GROUND_ITEM_SECOND_OPTION) ||
				event.getMenuAction().equals(MenuAction.GROUND_ITEM_THIRD_OPTION) ||
				event.getMenuAction().equals(MenuAction.GROUND_ITEM_FOURTH_OPTION) ||
				event.getMenuAction().equals(MenuAction.GROUND_ITEM_FIFTH_OPTION)
		){
			if (!approvedItems.contains(event.getId())) {
				event.consume();
				String releaseDate = "";
				if(itemReleaseDates.get(event.getId()).getDate() != null) {
					releaseDate = itemReleaseDates.get(event.getId()).getDate();
				}
				ChatNotifications.sendChatNotification("This item has not yet been released! Release Date: " + releaseDate, chatMessageManager);
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals("Release-Restricted") && event.getKey().equals("yearSelector")) {
			refresh();
			ChatNotifications.configNotification(config, chatMessageManager);
		}
	}

	private void refresh(){
		approvedNpcs.clear();
		approvedItems.clear();
		if (!checkedNpcs.isEmpty()) {
			for (int id : checkedNpcs) {
				ReleaseDate releaseDate = npcReleaseDates.get(id);
				if (releaseDate != null) {
					if (releaseDate.getYear() <= Integer.parseInt(config.year().toString())) {
						approvedNpcs.add(id);
					}
				}
			}
		}
		if (!checkedItems.isEmpty()) {
			for (int id : checkedItems) {
				ReleaseDate releaseDate = itemReleaseDates.get(id);
				if (releaseDate != null) {
					if (releaseDate.getYear() <= Integer.parseInt(config.year().toString())) {
						approvedItems.add(id);
					}
				}
			}
		}


	}

	@Override
	protected void shutDown() throws Exception
	{
		ChatNotifications.sendChatNotification("Content is now unrestricted", chatMessageManager);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			//ChatNotifications.configNotification(config, chatMessageManager);
		}
	}

	@Provides
	ReleaseRestrictedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ReleaseRestrictedConfig.class);
	}
}
