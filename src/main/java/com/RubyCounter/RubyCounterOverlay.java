package com.RubyCounter;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.config.ConfigManager;

import java.awt.*;

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
                        .left("Attacks:  " + plugin.attackCounter)
                        .build());
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Procs:  " + plugin.rubyCounter)
                        .build());

                if (config.EnableSinceLastRuby())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Since last proc:  " + plugin.attacksSinceLastRuby)
                            .build());
                }

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Rate:  " + plugin.rate + "%")
                        .build());

                if (config.AcbOverlay())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("----------")
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Acb specs:  " + plugin.acbSpecsUsed)
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Acb procs:  " + plugin.acbSpecsProcs)
                            .build());
                }

                if (config.ZcbOverlay())
                {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("----------")
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Zcb specs:  " + plugin.zcbSpecsUsed)
                            .build());
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Zcb procs:  " + plugin.zcbSpecsProcs)
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
