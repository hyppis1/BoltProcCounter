package com.BoltProcCounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("BoltProcCounter")
public interface BoltProcCounterConfig extends Config
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
            keyName = "KandarinHardDiary",
            name = "Kandarin hard diary",
            description = "Check if Kandarin hard diary is completed (10% increased bolt proc chance)"
    )
    default boolean KandarinHardDiary()
    {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "pvpRates",
            name = "Use pvp proc rates",
            description = "Enable if you are tracking bolt proc rates in pvp (different rates for some bolts procs compared to pvm)"
    )
    default boolean pvpRates()
    {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "AcbTracking",
            name = "Separate Acb specs",
            description = "Separate Armadyl crossbow special attacks from normal attacks"
    )
    default boolean AcbTracking()
    {
        return true;
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

    @ConfigSection(
            name = "Display data in overlay",
            description = "Configure what data you want to see on the overlay",
            position = 6,
            closedByDefault = true
    )
    String ShownStatsSection = "ShownStats";

    @ConfigItem(
            section = ShownStatsSection,
            position = 1,
            keyName = "ShowBoltName",
            name = "Equipped bolts info",
            description = "Shows equipped bolts info in overlay"
    )
    default boolean ShowBoltName()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 2,
            keyName = "ExpectedProcs",
            name = "Expected proc rate",
            description = "Shows expected bolt proc rate"
    )
    default boolean ExpectedProcs()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 3,
            keyName = "EnableSinceLastProc",
            name = "Since last proc",
            description = "Shows how many attacks since last bolt proc"
    )
    default boolean EnableSinceLastProc()
    {
        return true;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 4,
            keyName = "ProcDryChance",
            name = "Proc dry chance",
            description = "Shows chance of getting bolt proc in last x attacks"
    )
    default boolean ProcDryChance()
    {
        return false;
    }
    @ConfigItem(
            section = ShownStatsSection,
            position = 5,
            keyName = "LongestDryStreak",
            name = "Longest dry streak",
            description = "Shows longest streak of no bolt procs"
    )
    default boolean LongestDryStreak()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 6,
            keyName = "AcbOverlay",
            name = "Show separated Acb",
            description = "Shows separated Armadyl crossbow special attacks"
    )
    default boolean AcbOverlay()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 7,
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
            description = "Toggle to reset the counters for this bolt type"

    )
    default boolean resetCounters()
    {
        return false;
    }

}
