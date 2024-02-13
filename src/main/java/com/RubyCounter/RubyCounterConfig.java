package com.RubyCounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("RubyCounter")
public interface RubyCounterConfig extends Config
{

    @ConfigItem(
            position = 1,
            keyName = "EnableOverlay",
            name = "Counter overlay",
            description = "Shows stats in overlay"
    )
    default boolean EnableOverLay()
    {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "EnableSinceLastRuby",
            name = "Since last ruby",
            description = "Shows how many attacks since last ruby proc"
    )
    default boolean EnableSinceLastRuby()
    {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "AcbTracking",
            name = "Separate Acb specs",
            description = "Separate Armadyl crossbow special attacks from normal attacks"
    )
    default boolean AcbTracking()
    {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "AcbOverlay",
            name = "Show separated Acb",
            description = "Shows separated Armadyl crossbow special attacks"
    )
    default boolean AcbOverlay()
    {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "ZcbTracking",
            name = "Separate Zcb specs",
            description = "Separate Zaryte crossbow special attacks"
    )
    default boolean ZcbTracking()
    {
        return true;
    }
    @ConfigItem(
            position = 6,
            keyName = "ZcbOverlay",
            name = "Show separated Zcb",
            description = "Shows separated Zaryte crossbow special attacks"
    )
    default boolean ZcbOverlay()
    {
        return true;
    }


    @ConfigItem(
            position = 7,
            keyName = "resetCounters",
            name = "Toggle to reset Counters",
            description = "Toggle to reset the counters"

    )
    default boolean resetCounters()
    {
        return false;
    }

}
