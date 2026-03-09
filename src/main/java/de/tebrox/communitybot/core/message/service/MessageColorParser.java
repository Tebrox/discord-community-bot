package de.tebrox.communitybot.core.message.service;

import java.awt.Color;

public final class MessageColorParser {

    private MessageColorParser() {}

    public static Color parseOrNull(String hex) {
        if (hex == null || hex.isBlank()) {
            return null;
        }

        String value = hex.trim();
        if (!value.startsWith("#")) {
            value = "#" + value;
        }

        if (!value.matches("^#[0-9a-fA-F]{6}$")) {
            return null;
        }

        return Color.decode(value);
    }
}
