package de.tebrox.communitybot.community.discord.buttons;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public final class ButtonFactory {

    private ButtonFactory() {}

    public static Button fromConfig(CommunityGuildConfig.ButtonConfig cfg) {
        return Button.of(parseStyle(cfg.getStyle()), cfg.getId(), cfg.getLabel());
    }

    public static Button statusButton(CommunityGuildConfig.StatusButtonConfig cfg) {
        return Button.of(parseStyle(cfg.getStyle()), cfg.getId(), cfg.getLabel());
    }

    public static ButtonStyle parseStyle(String s) {
        if (s == null) return ButtonStyle.PRIMARY;
        return switch (s.toUpperCase()) {
            case "SECONDARY" -> ButtonStyle.SECONDARY;
            case "SUCCESS"   -> ButtonStyle.SUCCESS;
            case "DANGER"    -> ButtonStyle.DANGER;
            default          -> ButtonStyle.PRIMARY;
        };
    }
}
