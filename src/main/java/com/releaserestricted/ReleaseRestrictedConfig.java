package com.releaserestricted;

import com.releaserestricted.Util.YearOptions;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.time.Year;

@ConfigGroup("Release-Restricted")
public interface ReleaseRestrictedConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "yearSelector",
			name = "Year",
			description = "The final year of approved content"
	)
	default YearOptions year() {
		return YearOptions._2005;
	}
}
