package de.tebrox.communitybot.core.message.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageTemplateRenderer {

    public String render(String template, Map<String, String> placeholders) {
        if (template == null || template.isBlank()) {
            return template;
        }

        String result = template;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() == null ? "" : entry.getValue();
                result = result.replace("{" + key + "}", value);
            }
        }

        return result;
    }
}
