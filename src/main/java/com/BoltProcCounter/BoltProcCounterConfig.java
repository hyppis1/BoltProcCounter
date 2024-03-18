package com.BoltProcCounter;

import net.runelite.client.config.*;

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
        return false;
    }

    @ConfigItem(
            position = 2,
            keyName = "dataSaving",
            name = "Load/Save data",
            description = "Will overwrite current data with newly loaded data." +
                    "<br/>Save location: %USERPROFILE%/.runelite/bolt-proc-counter/%INGAMENAME%/"
    )
    default boolean dataSaving() {return true;}

    @ConfigItem(
            position = 3,
            keyName = "KandarinHardDiary",
            name = "Kandarin hard diary",
            description = "Check if Kandarin hard diary is completed (10% increased bolt proc chance)"
    )
    default boolean KandarinHardDiary()
    {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "pvpRates",
            name = "Use pvp proc rates",
            description = "Enable if you are tracking bolt proc rates in pvp (different rates for some bolts procs compared to pvm)"
    )
    default boolean pvpRates()
    {
        return false;
    }


    @ConfigItem(
            position = 5,
            keyName = "AcbTracking",
            name = "Separate Acb specs",
            description = "Separate Armadyl crossbow special attacks from normal attacks"
    )
    default boolean AcbTracking()
    {
        return true;
    }

    @ConfigItem(
            position = 6,
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
            position = 7,
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
            description = "Shows expected bolt proc rate (Assumes 100% accuracy)"
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
            description = "Shows chance of getting bolt proc in last x attacks (Assumes 100% accuracy)"
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
            description = "Shows longest dry streak of no bolt procs"
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

    @ConfigSection(
            name = "Data sample tracking",
            description = "Configure your data sample tracking",
            position = 8,
            closedByDefault = true
    )
    String dataTrackingSection = "DataTracking";

    @ConfigItem(
            section = dataTrackingSection,
            position = 1,
            keyName = "dataSampleSaving",
            name = "Enable sample saving",
            description = "Makes additional .txt file that appends with sample data." +
                    "<br/>Load/Save data needs to be enabled to save sample data." +
                    "<br/>Save location: %USERPROFILE%/.runelite/bolt-proc-counter/%INGAMENAME%/"
    )
    default boolean dataSampleSaving() {return false;}

    @ConfigItem(
            section = dataTrackingSection,
            position = 2,
            keyName = "sampleSize",
            name = "Sample size",
            description = "Save sample data every X attacks"
    )
    @Range(min = 1)
    default int sampleSize() { return 100; }

    @ConfigItem(
            section = dataTrackingSection,
            position = 3,
            keyName = "saveAttacks",
            name = "Save attacks",
            description = "Save amount of attacks in sample data"
    )
    default boolean saveAttacks() {return true;}
    @ConfigItem(
            section = dataTrackingSection,
            position = 4,
            keyName = "saveProcs",
            name = "Save procs",
            description = "Save amount of procs in sample data"
    )
    default boolean saveProcs() {return true;}
    @ConfigItem(
            section = dataTrackingSection,
            position = 5,
            keyName = "saveSinceLast",
            name = "Save since last",
            description = "Save since last proc in sample data"
    )
    default boolean saveSinceLast() {return true;}

    @ConfigItem(
            section = dataTrackingSection,
            position = 6,
            keyName = "saveLongestDry",
            name = "Save longest dry",
            description = "Save longest dry in sample data"
    )
    default boolean saveLongestDry() {return true;}

    @ConfigItem(
            section = dataTrackingSection,
            position = 7,
            keyName = "saveAcbData",
            name = "Save Acb data",
            description = "Save Acb specs/procs in sample data"
    )
    default boolean saveAcbData() {return true;}
    @ConfigItem(
            section = dataTrackingSection,
            position = 8,
            keyName = "saveZcbData",
            name = "Save Zcb data",
            description = "Save Zcb specs/procs in sample data"
    )
    default boolean saveZcbData() {return true;}


    @ConfigItem(
            position = 9,
            keyName = "resetCounters",
            name = "Toggle to reset Counters",
            description = "Toggle to reset the counters for this bolt type. Resets also saved data on next attack"

    )
    default boolean resetCounters()
    {
        return false;
    }

}
