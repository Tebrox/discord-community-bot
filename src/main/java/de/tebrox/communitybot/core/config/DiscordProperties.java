package de.tebrox.communitybot.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {

        @Setter
        @Getter
        private BotProperties community = new BotProperties();

        @Setter
        @Getter
        private BotProperties ticket = new BotProperties();


    @Setter
    @Getter
    public static class BotProperties {
            private boolean enabled = true;
            private String token;

            public boolean isConfigured() {
                return token != null && !token.isBlank();
            }
    }
}