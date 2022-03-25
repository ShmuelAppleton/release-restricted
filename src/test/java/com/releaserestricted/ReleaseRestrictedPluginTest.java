package com.releaserestricted;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ReleaseRestrictedPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ReleaseRestrictedPlugin.class);
		RuneLite.main(args);
	}
}