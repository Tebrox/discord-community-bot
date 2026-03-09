package de.tebrox.communitybot.core.message.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuildMessageConfigRequest {
    private boolean enabled;
    private String content;
    private boolean embedEnabled;
    private String embedTitle;
    private String embedDescription;
    private String embedFooter;
    private String embedColor;
    private String thumbnailUrl;
    private String imageUrl;
}
