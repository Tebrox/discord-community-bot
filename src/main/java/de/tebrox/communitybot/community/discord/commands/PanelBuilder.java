package de.tebrox.communitybot.community.discord.commands;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.discord.buttons.ButtonFactory;
import de.tebrox.communitybot.core.message.service.ResolvedMessage;
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

    public static MessageEmbed buildEmbed(CommunityGuildConfig cfg, ResolvedMessage message) {
        return buildEmbed(message).build();
    }

    public static EmbedBuilder buildEmbed(ResolvedMessage message) {
        EmbedBuilder eb = new EmbedBuilder();
        if(message.getEmbedTitle() != null && !message.getEmbedTitle().isBlank()) eb.setTitle(message.getEmbedTitle());
        if(message.getEmbedDescription() != null && !message.getEmbedDescription().isBlank()) eb.setDescription(message.getEmbedDescription());
        if(message.getEmbedFooter() != null && !message.getEmbedFooter().isBlank()) eb.setFooter(message.getEmbedFooter());
        if(message.getThumbnailUrl() != null && !message.getThumbnailUrl().isBlank() && message.getThumbnailUrl().startsWith("http")) eb.setThumbnail(message.getThumbnailUrl());
        if(message.getImageUrl() != null && !message.getImageUrl().isBlank() && message.getImageUrl().startsWith("http")) eb.setImage(message.getImageUrl());

        try {
            eb.setColor(Color.decode(message.getEmbedColor() != null ? message.getEmbedColor() : "#5865F2"));
        }catch (Exception ignored) {
            eb.setColor(new Color(0x5865F2));
        }

        return eb;
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
