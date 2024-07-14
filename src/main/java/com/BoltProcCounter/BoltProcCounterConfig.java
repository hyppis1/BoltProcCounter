package com.BoltProcCounter;

import net.runelite.client.config.*;

@ConfigGroup("BoltProcCounter")
public interface BoltProcCounterConfig extends Config
{

    @ConfigItem(
            position = 1,
            keyName = "EnableOverlay",
            name = "Show overlay",
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
                    "<br/>Data is saved when client is closed or player logs out." +
                    "<br/>Data is loaded when logging in, or when this is turned on." +
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
            keyName = "AccuracyPassRule",
            name = "Accuracy pass rule",
            description = "Bolt effects that needs to pass accuracy check, will use only hits that dealt dmg." +
                    "<br/>Bolt effects that bypass accuracy will have no impact."
    )
    default boolean AccuracyPassRule()
    {
        return false;
    }

    @ConfigItem(
            position = 6,
            keyName = "AcbTracking",
            name = "Separate Acb specs",
            description = "Separate Armadyl crossbow special attacks from normal attacks"
    )
    default boolean AcbTracking()
    {
        return true;
    }

    @ConfigItem(
            position = 7,
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
            position = 8,
            closedByDefault = true
    )
    String ShownStatsSection = "ShownStats";

    @ConfigItem(
            section = ShownStatsSection,
            position = 1,
            keyName = "ShowBoltName",
            name = "Show equipped bolts info",
            description = "Shows equipped bolts info in overlay"
    )
    default boolean ShowBoltName()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 2,
            keyName = "ShowAttacks",
            name = "Show amount of attacks done",
            description = "Shows amount of attacks in overlay"
    )
    default boolean ShowAttacks()
    {
        return true;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 3,
            keyName = "AttackDealtDmg",
            name = "Show attacks that hit",
            description = "Shows amount of attacks that hit/passed accuracy in overlay." +
                    "<br/>Bolt effects that bypass accuracy check will cause small deviation."
    )
    default boolean AttackDealtDmg()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 4,
            keyName = "OverallAccuracy",
            name = "Show overall accuracy",
            description = "Shows overall accuracy with attacks in overlay" +
                    "<br/>Bolt effects that bypass accuracy check will cause small deviation."
    )
    default boolean OverallAccuracy()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 5,
            keyName = "ShowProcs",
            name = "Show amount of bolt procs",
            description = "Shows amount of bolt procs in overlay"
    )
    default boolean ShowProcs()
    {
        return true;
    }
    @ConfigItem(
            section = ShownStatsSection,
            position = 6,
            keyName = "ExpectedProcs",
            name = "Show expected proc rate",
            description = "Shows expected bolt proc rate in overlay"
    )
    default boolean ExpectedProcs()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 7,
            keyName = "EnableSinceLastProc",
            name = "Show since last proc",
            description = "Shows how many attacks since last bolt proc in overlay"
    )
    default boolean EnableSinceLastProc()
    {
        return true;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 8,
            keyName = "ProcDryChance",
            name = "Show proc dry chance",
            description = "Shows chance of getting bolt proc in last x attacks in overlay"
    )
    default boolean ProcDryChance()
    {
        return false;
    }
    @ConfigItem(
            section = ShownStatsSection,
            position = 9,
            keyName = "LongestDryStreak",
            name = "Show longest dry streak",
            description = "Shows longest dry streak of no bolt procs in overlay"
    )
    default boolean LongestDryStreak()
    {
        return false;
    }

    @ConfigItem(
            section = ShownStatsSection,
            position = 10,
            keyName = "ShowProcRate",
            name = "Show bolt proc rate",
            description = "Shows bolt proc rate in overlay"
    )
    default boolean ShowProcRate()
    {
        return true;
    }
    @ConfigItem(
            section = ShownStatsSection,
            position = 11,
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
            position = 12,
            keyName = "ZcbOverlay",
            name = "Show separated Zcb",
            description = "Shows separated Zaryte crossbow special attacks"
    )
    default boolean ZcbOverlay()
    {
        return false;
    }



    @ConfigSection(
            name = "Data sample tracking",
            description = "Configure your data sample tracking",
            position = 9,
            closedByDefault = true
    )
    String dataTrackingSection = "DataTracking";

    @ConfigItem(
            section = dataTrackingSection,
            position = 1,
            keyName = "dataSampleSaving",
            name = "Enable sample saving",
            description = "Makes additional .txt file that appends with sample data." +
                    "<br/>Saves data when attack counter is divisible by sample size." +
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
            keyName = "saveSinceLast",
            name = "Save since last",
            description = "Save since last proc in sample data"
    )
    default boolean saveSinceLast() {return true;}

    @ConfigItem(
            section = dataTrackingSection,
            position = 5,
            keyName = "saveLongestDry",
            name = "Save longest dry",
            description = "Save longest dry in sample data"
    )
    default boolean saveLongestDry() {return true;}
    @ConfigItem(
            section = dataTrackingSection,
            position = 6,
            keyName = "saveProcs",
            name = "Save procs",
            description = "Save amount of procs in sample data"
    )
    default boolean saveProcs() {return true;}
    
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
            section = dataTrackingSection,
            position = 9,
            keyName = "saveAttackDealtDmg",
            name = "Save attacks that hit",
            description = "Save amount of attacks that hit in sample data"
    )
    default boolean saveAttackDealtDmg()
    {
        return false;
    }



    @ConfigItem(
            position = 10,
            keyName = "resetCounters",
            name = "Toggle to reset Counters",
            description = "Toggle to reset the counters for this bolt type. Resets also saved data on client closure"

    )
    default boolean resetCounters()
    {
        return false;
    }

}
