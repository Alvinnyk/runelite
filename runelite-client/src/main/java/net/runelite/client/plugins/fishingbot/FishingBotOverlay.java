package net.runelite.client.plugins.fishingbot;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class FishingBotOverlay extends Overlay {

    private static final String FISHING_SPOT = "Fishing spot";

    private final Client client;
    private final FishingBotPlugin plugin;
    private final FishingBotConfig config;

    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public FishingBotOverlay(Client client, FishingBotPlugin plugin, FishingBotConfig config) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }


    @Override
    public Dimension render(Graphics2D graphics) {

        if(!config.botDetails()){
            return null;
        }

        panelComponent.setPreferredSize(new Dimension(ComponentConstants.STANDARD_WIDTH * 2, 0));
        panelComponent.getChildren().clear();

        if (client.getLocalPlayer().getInteracting() != null && client.getLocalPlayer().getInteracting().getName()
                .contains(FISHING_SPOT))
        {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Fishing")
                    .color(Color.GREEN)
                    .build());
        }
        else
        {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("NOT fishing")
                    .color(Color.RED)
                    .build());
        }

        if(plugin.getIsStartUp()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("STARTING UP..")
                    .color(Color.GREEN)
                    .build());
        } else {
            if (plugin.getIsStable())
            {
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text("STABLE")
                        .color(Color.GREEN)
                        .build());
            }
            else
            {
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text("BROKEN")
                        .color(Color.RED)
                        .build());
            }

        }

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Bot tick:")
                .right(Integer.toString(plugin.getBotTick()))
                .build()
        );


        if(plugin.getIsMouseMoving()){
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("isMouseMoving:")
                    .right("MOVING")
                    .rightColor(Color.RED)
                    .build()
            );
        } else {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("isMouseMoving:")
                    .right("NOT MOVING")
                    .rightColor(Color.GREEN)
                    .build()
            );
        }

        int inventorySize = plugin.getInventorySize();
        if(inventorySize >= 28) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Inventory: ")
                    .right("FULL")
                    .rightColor(Color.RED)
                    .build()
            );
        } else {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Inventory: ")
                    .right(Integer.toString(inventorySize))
                    .rightColor(Color.GREEN)
                    .build()
            );
        }

        return panelComponent.render(graphics);
    }
}
