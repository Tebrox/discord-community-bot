package de.tebrox.rolesbot.config;

import java.util.*;

/**
 * Per-guild Discord bot configuration loaded from config.yml.
 * This is a plain POJO, NOT a JPA entity. Persistence of Discord panel state is in the DB.
 */
public class GuildConfig {

    private PanelConfig panel = new PanelConfig();
    private List<ButtonConfig> buttons = new ArrayList<>();
    private StatusButtonConfig statusButton = new StatusButtonConfig();
    private LayoutConfig layout = new LayoutConfig();
    private WelcomeConfig welcome = new WelcomeConfig();

    public PanelConfig getPanel() { return panel; }
    public void setPanel(PanelConfig p) { this.panel = p; }
    public List<ButtonConfig> getButtons() { return buttons; }
    public void setButtons(List<ButtonConfig> b) { this.buttons = b; }
    public StatusButtonConfig getStatusButton() { return statusButton; }
    public void setStatusButton(StatusButtonConfig s) { this.statusButton = s; }
    public LayoutConfig getLayout() { return layout; }
    public void setLayout(LayoutConfig l) { this.layout = l; }
    public WelcomeConfig getWelcome() { return welcome; }
    public void setWelcome(WelcomeConfig w) { this.welcome = w; }

    public Map<String, ButtonConfig> buttonById() {
        Map<String, ButtonConfig> map = new LinkedHashMap<>();
        for (ButtonConfig b : buttons) {
            if (map.containsKey(b.getId()))
                throw new IllegalArgumentException("Doppelte Button-ID: \"" + b.getId() + "\"");
            map.put(b.getId(), b);
        }
        return map;
    }

    public List<List<String>> effectiveLayoutRows() {
        List<List<String>> configured = (layout != null) ? layout.getRows() : null;
        if (configured != null && !configured.isEmpty()) return configured;

        List<List<String>> auto = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (ButtonConfig btn : buttons) {
            if (current.size() == 5) { auto.add(new ArrayList<>(current)); current.clear(); }
            current.add(btn.getId());
        }
        if (!current.isEmpty()) auto.add(new ArrayList<>(current));

        if (statusButton != null && statusButton.getId() != null) {
            boolean found = auto.stream().anyMatch(r -> r.contains(statusButton.getId()));
            if (!found) {
                if (auto.isEmpty() || auto.get(auto.size() - 1).size() == 5)
                    auto.add(new ArrayList<>(List.of(statusButton.getId())));
                else
                    auto.get(auto.size() - 1).add(statusButton.getId());
            }
        }
        return auto;
    }

    public List<String> validateLayout() {
        List<String> errors = new ArrayList<>();
        Map<String, ButtonConfig> byId;
        try { byId = buttonById(); }
        catch (IllegalArgumentException e) { errors.add(e.getMessage()); return errors; }

        Set<String> known = new HashSet<>(byId.keySet());
        if (statusButton != null && statusButton.getId() != null) known.add(statusButton.getId());

        List<List<String>> rows = effectiveLayoutRows();
        int rowIdx = 0;
        for (List<String> row : rows) {
            rowIdx++;
            if (row.size() > 5) errors.add("Row " + rowIdx + " hat mehr als 5 Buttons (" + row.size() + ")");
            for (String id : row)
                if (!known.contains(id)) errors.add("Layout referenziert unbekannte Button-ID: " + id);
        }
        if (rows.size() > 5) errors.add("Mehr als 5 Rows konfiguriert (" + rows.size() + ")");
        return errors;
    }

    // ------------------------------------------------------------------ nested POJOs

    public static class PanelConfig {
        private String title = "Benachrichtigungs-Rollen";
        private String requirePermission = "MANAGE_SERVER";
        private String allowedChannelId = "";
        private boolean enforceChannelLock = false;

        public String getTitle() { return title; }
        public void setTitle(String t) { this.title = t; }
        public String getRequirePermission() { return requirePermission; }
        public void setRequirePermission(String p) { this.requirePermission = p; }
        public String getAllowedChannelId() { return allowedChannelId; }
        public void setAllowedChannelId(String id) { this.allowedChannelId = id; }
        public boolean isEnforceChannelLock() { return enforceChannelLock; }
        public void setEnforceChannelLock(boolean b) { this.enforceChannelLock = b; }
    }

    public static class ButtonConfig {
        private String id = "";
        private String label = "";
        private String roleId = "";
        private String style = "PRIMARY";

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String l) { this.label = l; }
        public String getRoleId() { return roleId; }
        public void setRoleId(String r) { this.roleId = r; }
        public String getStyle() { return style; }
        public void setStyle(String s) { this.style = s; }
    }

    public static class StatusButtonConfig {
        private String id = "status_meine_rollen";
        private String label = "\uD83D\uDCCB Meine Rollen";
        private String style = "SECONDARY";

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String l) { this.label = l; }
        public String getStyle() { return style; }
        public void setStyle(String s) { this.style = s; }
    }

    public static class LayoutConfig {
        private List<List<String>> rows = new ArrayList<>();
        public List<List<String>> getRows() { return rows; }
        public void setRows(List<List<String>> rows) { this.rows = rows; }
    }

    public static class WelcomeConfig {
        private boolean enabled = false;
        private String channelId = "CHANNEL_ID_WELCOME";
        private boolean sendDm = false;
        private int deleteAfterSeconds = 0;
        private boolean onlyFirstJoin = true;
        private String message = "";
        private EmbedConfig embed = new EmbedConfig();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean e) { this.enabled = e; }
        public String getChannelId() { return channelId; }
        public void setChannelId(String c) { this.channelId = c; }
        public boolean isSendDm() { return sendDm; }
        public void setSendDm(boolean s) { this.sendDm = s; }
        public int getDeleteAfterSeconds() { return deleteAfterSeconds; }
        public void setDeleteAfterSeconds(int d) { this.deleteAfterSeconds = d; }
        public boolean isOnlyFirstJoin() { return onlyFirstJoin; }
        public void setOnlyFirstJoin(boolean o) { this.onlyFirstJoin = o; }
        public String getMessage() { return message; }
        public void setMessage(String m) { this.message = m; }
        public EmbedConfig getEmbed() { return embed; }
        public void setEmbed(EmbedConfig e) { this.embed = e; }
    }

    public static class EmbedConfig {
        private boolean enabled = true;
        private String color = "#5865F2";
        private String title = "Willkommen, {mention}!";
        private String description = "**{user}** ist dem Server **{server}** beigetreten.\nMitglieder: **{memberCount}**";
        private String footer = "User ID: {id}";
        private String thumbnail = "{avatarUrl}";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean e) { this.enabled = e; }
        public String getColor() { return color; }
        public void setColor(String c) { this.color = c; }
        public String getTitle() { return title; }
        public void setTitle(String t) { this.title = t; }
        public String getDescription() { return description; }
        public void setDescription(String d) { this.description = d; }
        public String getFooter() { return footer; }
        public void setFooter(String f) { this.footer = f; }
        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String t) { this.thumbnail = t; }
    }
}
