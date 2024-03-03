package com.RubyCounter;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.config.ConfigManager;

import java.awt.*;
import java.awt.Color;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class RubyCounterOverlay extends OverlayPanel
{

    @Inject
    private RubyCounterConfig config;
    private final Client client;
    private final RubyCounterPlugin plugin;


    @Inject
    private RubyCounterOverlay(RubyCounterPlugin plugin, Client client, RubyCounterConfig config)
    {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ruby Counter overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.EnableOverLay())
        {
            if (!plugin.soundMuted)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Attacks: ")
                        .right(String.valueOf(plugin.attackCounter))
                        .build());

                if (plugin.rubyCounter >= plugin.expectedProcs)
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Procs: ")
                            .right(String.valueOf(plugin.rubyCounter))
                            .rightColor(Color.GREEN)
                            .build());
                }
                else
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Procs: ")
                            .right(String.valueOf(plugin.rubyCounter))
                            .rightColor(Color.RED)
                            .build());
                }


                if (config.ExpectedProcs())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Expected: ")
                            .right(String.valueOf(plugin.expectedProcs))
                            .build());
                }

                if (config.EnableSinceLastRuby())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Since last proc: ")
                            .right(String.valueOf(plugin.attacksSinceLastRuby))
                            .build());
                }

                if (config.RubyDryChance())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Dry chance: ")
                            .right(String.valueOf(plugin.rubyDryRate + "%"))
                            .build());
                }

                if (config.LongestDryStreak())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Longest dry: ")
                            .right(String.valueOf(plugin.longestDryStreak))
                            .build());
                }

                if (config.KandarinHardDiary())
                {
                    if ((double) plugin.rubyCounter / plugin.attackCounter * 100 >= 6.6)
                    {
                        panelComponent.getChildren().add(LineComponent.builder()
                                .left("Rate: ")
                                .right(plugin.rate + "%")
                                .rightColor(Color.GREEN)
                                .build());
                    }
                    else
                    {
                        panelComponent.getChildren().add(LineComponent.builder()
                                .left("Rate: ")
                                .right(plugin.rate + "%")
                                .rightColor(Color.RED)
                                .build());
                    }
                }
                else
                {
                    if ((double) plugin.rubyCounter / plugin.attackCounter * 100 >= 6)
                    {
                        panelComponent.getChildren().add(LineComponent.builder()
                                .left("Rate: ")
                                .right(plugin.rate + "%")
                                .rightColor(Color.GREEN)
                                .build());
                    }
                    else
                    {
                        panelComponent.getChildren().add(LineComponent.builder()
                                .left("Rate: ")
                                .right(plugin.rate + "%")
                                .rightColor(Color.RED)
                                .build());
                    }
                }





                if (config.AcbOverlay())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("----------")
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Acb specs: ")
                            .right(String.valueOf(plugin.acbSpecsUsed))
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Acb procs: ")
                            .right(String.valueOf(plugin.acbSpecsProcs))
                            .build());
                }

                if (config.ZcbOverlay())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("----------")
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Zcb specs: ")
                            .right(String.valueOf(plugin.zcbSpecsUsed))
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Zcb procs: ")
                            .right(String.valueOf(plugin.zcbSpecsProcs))
                            .build());
                }
            }
            else
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Sound effects must be enabled to track ruby bolt procs. Can be at 1%")
                        .build());
            }
        }

        return super.render(graphics);

    }

}
