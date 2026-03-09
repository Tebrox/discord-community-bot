package de.tebrox.communitybot.core.message.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResolvedMessage {
    private boolean enabled;
    private String content;

    private boolean embedEnabled;
    private String embedTitle;
    private String embedDescription;
    private String embedFooter;
    private String embedColor;
    private String thumbnailUrl;
    private String imageUrl;

    private boolean custom;
}
