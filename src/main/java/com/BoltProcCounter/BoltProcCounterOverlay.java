package com.BoltProcCounter;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.components.LineComponent;

import java.awt.*;
import java.text.DecimalFormat;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class BoltProcCounterOverlay extends OverlayPanel
{

    @Inject
    private final BoltProcCounterConfig config;
    private final Client client;
    private final BoltProcCounterPlugin plugin;


    @Inject
    private BoltProcCounterOverlay(BoltProcCounterPlugin plugin, Client client, BoltProcCounterConfig config)
    {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ruby Counter overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.EnableOverLay() && plugin.wasAmmoIndex != -1)
        {
            if (!plugin.soundMutedB2B)
            {
                if (config.ShowBoltName())
                {
                    double formatedRate = plugin.expectedRate;
                    formatedRate *= 100.0;
                    formatedRate = Double.parseDouble(new DecimalFormat("#.##").format(formatedRate));

                    panelComponent.getChildren().add(LineComponent.builder()
                            .left(plugin.ammoName)
                            .right(formatedRate + "%")
                            .build());

                }

                if (config.ShowAttacks())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Attacks: ")
                            .right(String.valueOf(plugin.attackCounterArray[plugin.wasAmmoIndex]))
                            .build());
                }

                if (config.AttackDealtDmg())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Attacks hit: ")
                            .right(String.valueOf(plugin.attackDealtDmgArray[plugin.wasAmmoIndex]))
                            .build());
                }

                if (config.OverallAccuracy())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Accuracy: ")
                            .right(plugin.overallAccuracy + "%")
                            .build());
                }

                if (config.ShowProcs())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Procs: ")
                            .right(String.valueOf(plugin.procCounterArray[plugin.wasAmmoIndex]))
                            .build());
                }

                if (config.ExpectedProcs())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Expected: ")
                            .right(String.valueOf(plugin.expectedProcs))
                            .build());
                }

                if (config.EnableSinceLastProc())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Since last proc: ")
                            .right(String.valueOf(plugin.attacksSinceLastProcArray[plugin.wasAmmoIndex]))
                            .build());
                }

                if (config.ProcDryChance())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Dry chance: ")
                            .right(plugin.procDryRate + "%")
                            .build());
                }

                if (config.LongestDryStreak())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Longest dry: ")
                            .right(String.valueOf(plugin.longestDryStreakArray[plugin.wasAmmoIndex]))
                            .build());
                }

                if (config.ShowProcRate())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Proc rate: ")
                            .right(plugin.rate + "%")
                            .build());
                }

                if (config.AcbOverlay())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("----------")
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Acb specs: ")
                            .right(String.valueOf(plugin.acbSpecsUsedArray[plugin.wasAmmoIndex]))
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Acb procs: ")
                            .right(String.valueOf(plugin.acbSpecsProcsArray[plugin.wasAmmoIndex]))
                            .build());
                }

                if (config.ZcbOverlay())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("----------")
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Zcb specs: ")
                            .right(String.valueOf(plugin.zcbSpecsUsedArray[plugin.wasAmmoIndex]))
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Zcb procs: ")
                            .right(String.valueOf(plugin.zcbSpecsProcsArray[plugin.wasAmmoIndex]))
                            .build());
                }
            }
            else
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Sound effects must be enabled to track bolt procs. Can be at 1%")
                        .build());
            }
        }

        return super.render(graphics);

    }

}
