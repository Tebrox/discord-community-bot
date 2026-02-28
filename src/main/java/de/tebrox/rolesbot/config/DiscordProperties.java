package de.tebrox.rolesbot.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discord")
public record DiscordProperties(
        @NotBlank(message = "Discord token missing: set DISCORD_TOKEN env var or discord.token in application.yml")
        String token
) {}
