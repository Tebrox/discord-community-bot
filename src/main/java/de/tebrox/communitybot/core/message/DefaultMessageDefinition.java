package de.tebrox.communitybot.core.message;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DefaultMessageDefinition {
    private boolean enabled;
    private String content;

    private boolean embedEnabled;
    private String embedTitle;
    private String embedDescription;
    private String embedFooter;
    private String embedColor;
    private String thumbnailUrl;
    private String imageUrl;

    private boolean allowContent;
    private boolean allowEmbed;
    private List<String> placeholders;
}
