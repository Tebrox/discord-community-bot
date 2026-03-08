package de.tebrox.communitybot.ticket.web.controller;

import de.tebrox.communitybot.core.access.DashboardAccessService;
import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.core.util.SnowflakeValidator;
import de.tebrox.communitybot.ticket.persistence.entity.TicketGuildConfig;
import de.tebrox.communitybot.ticket.service.TicketGuildConfigService;
import de.tebrox.communitybot.ticket.service.TicketService;
import de.tebrox.communitybot.ticket.web.dto.TicketGuildConfigUpdateRequest;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TicketApiController {

    private final DashboardAccessService accessService;
    private final TicketGuildConfigService ticketGuildConfigService;
    private final TicketService ticketService;
    private final JDA ticketJda;

    public TicketApiController(
            DashboardAccessService accessService,
            TicketGuildConfigService ticketGuildConfigService,
            TicketService ticketService,
            @Qualifier("ticketJda") JDA ticketJda
    ) {
        this.accessService = accessService;
        this.ticketGuildConfigService = ticketGuildConfigService;
        this.ticketService = ticketService;
        this.ticketJda = ticketJda;
    }

    @PostMapping("/guild/{guildId}/tickets/save")
    public String saveTicketSettings(
            @PathVariable String guildId,
            @RequestParam(required = false) String ticketChannelId,
            @RequestParam(required = false) String logChannelId,
            @RequestParam(required = false) String embedTitle,
            @RequestParam(required = false) String embedDescription,
            @RequestParam(required = false) TicketGuildConfig.ThreadType threadType,
            @RequestParam(required = false, name = "supportRoleIds") List<String> supportRoleIds,
            @RequestParam(required = false, name = "categoryLabels") List<String> categoryLabels,
            @RequestParam(required = false, name = "categoryEmojis") List<String> categoryEmojis,
            @RequestParam(required = false, name = "categoryDescriptions") List<String> categoryDescriptions
    ) {
        SnowflakeValidator.validate(guildId, "guildId");

        if (!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS)) {
            return "redirect:/?forbidden";
        }

        List<TicketGuildConfigUpdateRequest.CategoryDto> categories = new ArrayList<>();
        if (categoryLabels != null) {
            for (int i = 0; i < categoryLabels.size(); i++) {
                String label = safeGet(categoryLabels, i);
                String emoji = safeGet(categoryEmojis, i);
                String description = safeGet(categoryDescriptions, i);

                if (label != null && !label.isBlank()) {
                    categories.add(new TicketGuildConfigUpdateRequest.CategoryDto(label.trim(), emptyToNull(emoji), emptyToNull(description)));
                }
            }
        }

        TicketGuildConfigUpdateRequest request = new TicketGuildConfigUpdateRequest(
                emptyToNull(ticketChannelId),
                emptyToNull(logChannelId),
                emptyToNull(embedTitle),
                emptyToNull(embedDescription),
                threadType,
                supportRoleIds == null ? List.of() : supportRoleIds,
                categories
        );

        try {
            ticketGuildConfigService.save(guildId, request);
            return "redirect:/guild/" + guildId + "/tickets?saved";
        } catch (IllegalArgumentException ex) {
            return "redirect:/guild/" + guildId + "/tickets?error";
        }
    }

    @PostMapping("/guild/{guildId}/tickets/repost")
    public String repostTicketPanel(
            @PathVariable String guildId,
            @RequestParam(required = false) String channelId
    ) {
        SnowflakeValidator.validate(guildId, "guildId");

        if (!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS)) {
            return "redirect:/?forbidden";
        }

        Guild guild = ticketJda.getGuildById(guildId);
        if (guild == null) {
            return "redirect:/guild/" + guildId + "/tickets?botMissing";
        }

        TextChannel targetChannel = null;
        if (channelId != null && !channelId.isBlank()) {
            targetChannel = guild.getTextChannelById(channelId);
        } else {
            var cfgOpt = ticketGuildConfigService.findByGuildId(guildId);
            if (cfgOpt.isPresent() && cfgOpt.get().getTicketChannelId() != null) {
                targetChannel = guild.getTextChannelById(cfgOpt.get().getTicketChannelId());
            }
        }

        if (targetChannel == null) {
            return "redirect:/guild/" + guildId + "/tickets?missingChannel";
        }

        ticketService.postTicketPanel(guild, targetChannel, true);
        return "redirect:/guild/" + guildId + "/tickets?reposted";
    }

    @PostMapping("/guild/{guildId}/tickets/test")
    public String testTicketPanel(
            @PathVariable String guildId,
            @RequestParam String channelId
    ) {
        SnowflakeValidator.validate(guildId, "guildId");
        SnowflakeValidator.validate(channelId, "channelId");

        if (!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS)) {
            return "redirect:/?forbidden";
        }

        Guild guild = ticketJda.getGuildById(guildId);
        if (guild == null) {
            return "redirect:/guild/" + guildId + "/tickets?botMissing";
        }

        TextChannel targetChannel = guild.getTextChannelById(channelId);
        if (targetChannel == null) {
            return "redirect:/guild/" + guildId + "/tickets?missingChannel";
        }

        ticketService.postTicketPanel(guild, targetChannel, false);
        return "redirect:/guild/" + guildId + "/tickets?tested";
    }

    private static String safeGet(List<String> list, int index) {
        return list != null && index < list.size() ? list.get(index) : null;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}