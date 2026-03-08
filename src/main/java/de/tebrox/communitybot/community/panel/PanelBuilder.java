package de.tebrox.communitybot.community.panel;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PanelBuilder {

    private PanelBuilder() {}

    public static MessageEmbed buildEmbed(CommunityGuildConfig cfg) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(cfg.getPanel().getTitle());
        eb.setColor(new Color(0x5865F2));
        eb.setDescription("Wähle aus, für welche Themen du erwähnt werden möchtest.\n" +
                "Du erhältst nur die Benachrichtigungen, die du hier aktivierst.");
        return eb.build();
    }

    public static List<ActionRow> buildActionRows(CommunityGuildConfig cfg) {
        Map<String, CommunityGuildConfig.ButtonConfig> byId = cfg.buttonById();
        List<ActionRow> rows = new ArrayList<>();

        for (List<String> row : cfg.effectiveLayoutRows()) {
            List<Button> buttons = new ArrayList<>();
            for (String id : row) {
                if (id.equals(cfg.getStatusButton().getId())) {
                    buttons.add(ButtonFactory.statusButton(cfg.getStatusButton()));
                } else if (byId.containsKey(id)) {
                    buttons.add(ButtonFactory.fromConfig(byId.get(id)));
                }
            }
            if (!buttons.isEmpty()) rows.add(ActionRow.of(buttons));
        }
        return rows;
    }
}
