package de.tebrox.rolesbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Loads/saves the multi-guild Discord config (roles, welcome, panel layout) from guilds.yml.
 * Separate from config.yml (application config loaded by ConfigLoader).
 */
public final class GuildConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(GuildConfigLoader.class);
    private static final String GUILDS_FILE = "guilds.yml";

    private GuildConfigLoader() {}

    public static Map<String, GuildConfig> loadOrCreate() {
        File file = new File(GUILDS_FILE);
        if (!file.exists()) {
            log.info("guilds.yml not found – creating empty template.");
            writeEmptyTemplate(file);
        }
        return load(file);
    }

    public static Map<String, GuildConfig> reload() {
        return load(new File(GUILDS_FILE));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, GuildConfig> load(File file) {
        Yaml yaml = new Yaml();
        try (InputStream is = new FileInputStream(file)) {
            Map<String, Object> raw = yaml.load(is);
            if (raw == null) return new LinkedHashMap<>();
            return parseAll(raw);
        } catch (Exception e) {
            log.error("Failed to load guilds.yml: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    public static synchronized void saveAtomic(Map<String, GuildConfig> guilds) throws IOException {
        File target = new File(GUILDS_FILE);
        File tmp = new File(GUILDS_FILE + ".tmp");
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);

        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> guildsMap = new LinkedHashMap<>();
        for (Map.Entry<String, GuildConfig> entry : guilds.entrySet()) {
            guildsMap.put(entry.getKey(), guildToMap(entry.getValue()));
        }
        root.put("guilds", guildsMap);

        try (FileWriter fw = new FileWriter(tmp, StandardCharsets.UTF_8)) {
            new Yaml(opts).dump(root, fw);
        }
        Files.move(tmp.toPath(), target.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        log.debug("guilds.yml saved atomically.");
    }

    // ---- parser ----

    @SuppressWarnings("unchecked")
    private static Map<String, GuildConfig> parseAll(Map<String, Object> raw) {
        Map<String, GuildConfig> result = new LinkedHashMap<>();
        Object guildsObj = raw.get("guilds");
        if (!(guildsObj instanceof Map)) return result;
        Map<String, Object> guildsMap = (Map<String, Object>) guildsObj;
        for (Map.Entry<String, Object> entry : guildsMap.entrySet()) {
            String guildId = entry.getKey();
            Map<String, Object> gRaw = entry.getValue() instanceof Map
                    ? (Map<String, Object>) entry.getValue()
                    : new HashMap<>();
            result.put(guildId, parseGuild(gRaw));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static GuildConfig parseGuild(Map<String, Object> gRaw) {
        GuildConfig gc = new GuildConfig();

        if (gRaw.containsKey("panel")) {
            Map<String, Object> p = (Map<String, Object>) gRaw.get("panel");
            GuildConfig.PanelConfig pc = new GuildConfig.PanelConfig();
            pc.setTitle(str(p, "title", "Benachrichtigungs-Rollen"));
            pc.setRequirePermission(str(p, "requirePermission", "MANAGE_SERVER"));
            pc.setAllowedChannelId(str(p, "allowedChannelId", ""));
            pc.setEnforceChannelLock(bool(p, "enforceChannelLock", false));
            gc.setPanel(pc);
        }

        if (gRaw.containsKey("buttons")) {
            List<Map<String, Object>> bList = (List<Map<String, Object>>) gRaw.get("buttons");
            List<GuildConfig.ButtonConfig> buttons = new ArrayList<>();
            if (bList != null) {
                for (Map<String, Object> b : bList) {
                    GuildConfig.ButtonConfig bc = new GuildConfig.ButtonConfig();
                    bc.setId(str(b, "id", ""));
                    bc.setLabel(str(b, "label", ""));
                    bc.setRoleId(str(b, "roleId", ""));
                    bc.setStyle(str(b, "style", "PRIMARY"));
                    buttons.add(bc);
                }
            }
            gc.setButtons(buttons);
        }

        if (gRaw.containsKey("statusButton")) {
            Map<String, Object> sb = (Map<String, Object>) gRaw.get("statusButton");
            GuildConfig.StatusButtonConfig sbc = new GuildConfig.StatusButtonConfig();
            sbc.setId(str(sb, "id", "status_meine_rollen"));
            sbc.setLabel(str(sb, "label", "\uD83D\uDCCB Meine Rollen"));
            sbc.setStyle(str(sb, "style", "SECONDARY"));
            gc.setStatusButton(sbc);
        }

        if (gRaw.containsKey("layout")) {
            Map<String, Object> layoutMap = (Map<String, Object>) gRaw.get("layout");
            if (layoutMap != null && layoutMap.containsKey("rows")) {
                List<List<String>> rows = new ArrayList<>();
                List<Object> rawRows = (List<Object>) layoutMap.get("rows");
                if (rawRows != null) {
                    for (Object row : rawRows) {
                        List<String> rowList = new ArrayList<>();
                        if (row instanceof List) {
                            for (Object item : (List<?>) row) rowList.add(item.toString());
                        }
                        rows.add(rowList);
                    }
                }
                GuildConfig.LayoutConfig lc = new GuildConfig.LayoutConfig();
                lc.setRows(rows);
                gc.setLayout(lc);
            }
        }

        if (gRaw.containsKey("welcome")) {
            Map<String, Object> w = (Map<String, Object>) gRaw.get("welcome");
            GuildConfig.WelcomeConfig wc = new GuildConfig.WelcomeConfig();
            wc.setEnabled(bool(w, "enabled", false));
            wc.setChannelId(str(w, "channelId", "CHANNEL_ID_WELCOME"));
            wc.setSendDm(bool(w, "sendDm", false));
            wc.setDeleteAfterSeconds(intVal(w, "deleteAfterSeconds", 0));
            wc.setOnlyFirstJoin(bool(w, "onlyFirstJoin", true));
            wc.setMessage(str(w, "message", ""));
            if (w.containsKey("embed")) {
                Map<String, Object> em = (Map<String, Object>) w.get("embed");
                GuildConfig.EmbedConfig ec = new GuildConfig.EmbedConfig();
                ec.setEnabled(bool(em, "enabled", true));
                ec.setColor(str(em, "color", "#5865F2"));
                ec.setTitle(str(em, "title", "Willkommen, {mention}!"));
                ec.setDescription(str(em, "description", "**{user}** ist dem Server beigetreten."));
                ec.setFooter(str(em, "footer", ""));
                ec.setThumbnail(str(em, "thumbnail", "{avatarUrl}"));
                wc.setEmbed(ec);
            }
            gc.setWelcome(wc);
        }

        return gc;
    }

    public static GuildConfig defaultGuildConfig() {
        return parseGuild(new HashMap<>());
    }

    // ---- serializer ----

    public static Map<String, Object> guildToMap(GuildConfig gc) {
        Map<String, Object> gMap = new LinkedHashMap<>();

        Map<String, Object> panel = new LinkedHashMap<>();
        panel.put("title", gc.getPanel().getTitle());
        panel.put("requirePermission", gc.getPanel().getRequirePermission());
        panel.put("allowedChannelId", gc.getPanel().getAllowedChannelId());
        panel.put("enforceChannelLock", gc.getPanel().isEnforceChannelLock());
        gMap.put("panel", panel);

        List<Map<String, Object>> buttons = new ArrayList<>();
        for (GuildConfig.ButtonConfig b : gc.getButtons()) {
            Map<String, Object> bm = new LinkedHashMap<>();
            bm.put("id", b.getId());
            bm.put("label", b.getLabel());
            bm.put("roleId", b.getRoleId());
            bm.put("style", b.getStyle());
            buttons.add(bm);
        }
        gMap.put("buttons", buttons);

        Map<String, Object> sb = new LinkedHashMap<>();
        sb.put("id", gc.getStatusButton().getId());
        sb.put("label", gc.getStatusButton().getLabel());
        sb.put("style", gc.getStatusButton().getStyle());
        gMap.put("statusButton", sb);

        Map<String, Object> layout = new LinkedHashMap<>();
        layout.put("rows", gc.getLayout().getRows());
        gMap.put("layout", layout);

        Map<String, Object> wc = new LinkedHashMap<>();
        wc.put("enabled", gc.getWelcome().isEnabled());
        wc.put("channelId", gc.getWelcome().getChannelId());
        wc.put("sendDm", gc.getWelcome().isSendDm());
        wc.put("deleteAfterSeconds", gc.getWelcome().getDeleteAfterSeconds());
        wc.put("onlyFirstJoin", gc.getWelcome().isOnlyFirstJoin());
        wc.put("message", gc.getWelcome().getMessage());
        Map<String, Object> em = new LinkedHashMap<>();
        em.put("enabled", gc.getWelcome().getEmbed().isEnabled());
        em.put("color", gc.getWelcome().getEmbed().getColor());
        em.put("title", gc.getWelcome().getEmbed().getTitle());
        em.put("description", gc.getWelcome().getEmbed().getDescription());
        em.put("footer", gc.getWelcome().getEmbed().getFooter());
        em.put("thumbnail", gc.getWelcome().getEmbed().getThumbnail());
        wc.put("embed", em);
        gMap.put("welcome", wc);

        return gMap;
    }

    private static void writeEmptyTemplate(File file) {
        String tpl = "# RolesBot v4 – Guild Configuration\n" +
                "# guilds are auto-registered when the bot joins a server.\n" +
                "# You can also add manual entries below.\n\n" +
                "guilds: {}\n";
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(tpl);
        } catch (IOException e) {
            log.error("Could not write guilds.yml: {}", e.getMessage());
        }
    }

    // ---- helpers ----
    private static String str(Map<String, Object> m, String k, String def) {
        Object v = m.get(k); return v != null ? v.toString() : def;
    }
    private static boolean bool(Map<String, Object> m, String k, boolean def) {
        Object v = m.get(k);
        if (v instanceof Boolean b) return b;
        if (v != null) return Boolean.parseBoolean(v.toString());
        return def;
    }
    private static int intVal(Map<String, Object> m, String k, int def) {
        Object v = m.get(k);
        if (v instanceof Number n) return n.intValue();
        if (v != null) { try { return Integer.parseInt(v.toString()); } catch (NumberFormatException ignored) {} }
        return def;
    }
}
