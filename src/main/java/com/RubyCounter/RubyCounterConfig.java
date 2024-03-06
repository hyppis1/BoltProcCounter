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
            keyName = "ExpectedProcs",
            name = "Expected proc rate",
            description = "Shows expected ruby bolt proc rate"
    )
    default boolean ExpectedProcs()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "EnableSinceLastRuby",
            name = "Since last ruby",
            description = "Shows how many attacks since last ruby proc"
    )
    default boolean EnableSinceLastRuby()
    {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "RubyDryChance",
            name = "Ruby dry chance",
            description = "Shows chance of getting ruby bolt proc in last x attacks"
    )
    default boolean RubyDryChance()
    {
        return false;
    }
    @ConfigItem(
            position = 5,
            keyName = "LongestDryStreak",
            name = "Longest dry streak",
            description = "Shows longest streak of no ruby bolt procs"
    )
    default boolean LongestDryStreak()
    {
        return false;
    }


    @ConfigItem(
            position = 6,
            keyName = "KandarinHardDiary",
            name = "Kandarin hard diary",
            description = "Check if Kandarin hard diary is completed (10% increased bolt proc chance)"
    )
    default boolean KandarinHardDiary()
    {
        return true;
    }

    @ConfigItem(
            position = 7,
            keyName = "AcbTracking",
            name = "Separate Acb specs",
            description = "Separate Armadyl crossbow special attacks from normal attacks"
    )
    default boolean AcbTracking()
    {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "AcbOverlay",
            name = "Show separated Acb",
            description = "Shows separated Armadyl crossbow special attacks"
    )
    default boolean AcbOverlay()
    {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "ZcbTracking",
            name = "Separate Zcb specs",
            description = "Separate Zaryte crossbow special attacks"
    )
    default boolean ZcbTracking()
    {
        return true;
    }
    @ConfigItem(
            position = 10,
            keyName = "ZcbOverlay",
            name = "Show separated Zcb",
            description = "Shows separated Zaryte crossbow special attacks"
    )
    default boolean ZcbOverlay()
    {
        return true;
    }


    @ConfigItem(
            position = 11,
            keyName = "resetCounters",
            name = "Toggle to reset Counters",
            description = "Toggle to reset the counters"

    )
    default boolean resetCounters()
    {
        return false;
    }

}
