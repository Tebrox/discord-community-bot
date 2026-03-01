(function () {
    function $(id) { return document.getElementById(id); }

    function safeText(el, value) {
        if (!el) return;
        el.textContent = value == null ? "" : String(value);
    }

    function normalizeHexColor(input) {
        if (!input) return null;
        let c = String(input).trim();

        if (c.length === 0) return null;
        if (!c.startsWith("#")) c = "#" + c;

        // Accept #RGB or #RRGGBB
        const m = c.match(/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/);
        if (!m) return null;

        if (m[1].length === 3) {
            const r = m[1][0], g = m[1][1], b = m[1][2];
            return "#" + r + r + g + g + b + b;
        }
        return c.toUpperCase();
    }

    function setEmbedColorValue(hex) {
        const input = document.querySelector('input[name="embedColor"]');
        if (!input) return;
        input.value = hex;

        // Trigger input event so existing listeners update preview if open
        input.dispatchEvent(new Event("input", { bubbles: true }));
    }

    function getEmbedColorValueOrDefault() {
        const v = document.querySelector('input[name="embedColor"]')?.value ?? "";
        const norm = normalizeHexColor(v);
        return norm || "#5865F2";
    }

    const PREVIEW_AVATAR = "https://cdn.discordapp.com/embed/avatars/4.png";

    function readJsonFromDom(id) {
        const el = document.getElementById(id);
        if (!el) return [];
        try { return JSON.parse(el.textContent || "[]"); }
        catch { return []; }
    }

    function readTextFromDom(id, fallback) {
        const el = document.getElementById(id);
        if (!el) return fallback;
        const t = (el.textContent || "").trim();
        return t.length ? t : fallback;
    }

    const channelsArr = readJsonFromDom("welcomeChannelsJson");
    const rolesArr = readJsonFromDom("welcomeRolesJson");
    const PREVIEW_SERVER = readTextFromDom("welcomeServerName", "Preview Server")

    const channelById = new Map(channelsArr.map(c => [String(c.id), String(c.name)]));
    const roleById = new Map(rolesArr.map(r => [String(r.id), String(r.name)]));

    // For “#channelname” chips (plain)
    const channelNameSet = new Set(channelsArr.map(c => String(c.name).toLowerCase()));

    function escapeHtml(s) {
        return String(s)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    /**
     * Converts text to safe HTML and applies Discord-like preview formatting:
     * - placeholders replaced with sample values
     * - channel/user/role mentions shown as "chips"
     * - links clickable
     * - inline code styled
     */
    function previewFormatToHtml(text) {
        // 1) escape first to prevent HTML injection
        let s = escapeHtml(text ?? "");

        // 2) sample placeholder values for preview
        const sample = {
            "{mention}": "@User",
            "{user}": "User",
            "{tag}": "User#1234",
            "{id}": "123456789012345678",
            "{server}": PREVIEW_SERVER,
            "{memberCount}": "1337",
            "{avatarUrl}": PREVIEW_AVATAR,
            "{avatar}": PREVIEW_AVATAR // user requested placeholder
        };

        // replace placeholders (case-insensitive for convenience)
        for (const [k, v] of Object.entries(sample)) {
            const re = new RegExp(escapeHtml(k).replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), "gi");
            s = s.replace(re, escapeHtml(v));
        }

        // 3) Discord channel mention <#id> -> #name
        s = s.replace(/&lt;#([0-9]{5,25})&gt;/g, (_, id) => {
            const name = channelById.get(String(id));
            const shown = name ? `#${escapeHtml(name)}` : `#unknown-${id}`;
            return `<span class="mention">${shown}</span>`;
        });

        // 4) Discord role mention <@&id> -> @role-id
        s = s.replace(/&lt;@&amp;([0-9]{5,25})&gt;/g, (_, id) => {
            const name = roleById.get(String(id));
            const shown = name ? `@${escapeHtml(name)}` : `@role-${id}`;
            return `<span class="mention">${shown}</span>`;
        });

        // 5) Discord user mention <@id> / <@!id> -> @User
        s = s.replace(/&lt;@!?([0-9]{5,25})&gt;/g, (_, id) => {
            return `<span class="mention">@user-${id}</span>`;
        });

        // Also style plain "@name" as mention chip (best-effort).
        // Avoid emails like test@example.com by requiring start/space/punctuation prefix.
        s = s.replace(/(^|[\s(>])@([^\s@]{2,32})/g, (full, prefix, name) => {
            // skip if looks like email domain part after @ (contains '.')
            if (name.includes(".")) return full;
            return `${prefix}<span class="mention user">@${escapeHtml(name)}</span>`;
        });

        // Also render plain "#channelname" as a chip (best effort).
        // Avoid turning hex colors like #5865F2 into chips.
        s = s.replace(/(^|[\s(>])#([a-zA-Z0-9_-]{1,100})/g, (full, prefix, name) => {
            const lower = String(name).toLowerCase();

            // don't treat hex colors as channels
            const isHex = /^[0-9a-fA-F]{3}$/.test(name) || /^[0-9a-fA-F]{6}$/.test(name);
            if (isHex) return full;

            if (!channelNameSet.has(lower)) return full;

            return `${prefix}<span class="mention channel">#${escapeHtml(name)}</span>`;
        });

        // 6) Links (safe: we already escaped)
        s = s.replace(/(https?:\/\/[^\s<]+)/g, (url) => {
            return `<a class="preview-link" href="${url}" target="_blank" rel="noopener noreferrer">${url}</a>`;
        });

        // 7) Inline code: `code`
        s = s.replace(/`([^`]+)`/g, (_, code) => {
            return `<code class="preview-code">${escapeHtml(code)}</code>`;
        });

        // 8) line breaks
        s = s.replaceAll("\n", "<br>");

        return s;
    }

    function renderPreview() {
        const embedEnabled = document.querySelector('input[name="embedEnabled"]')?.checked ?? false;

        const title = document.querySelector('input[name="embedTitle"]')?.value ?? "";
        const desc = document.querySelector('textarea[name="embedDescription"]')?.value ?? "";
        const footer = document.querySelector('input[name="embedFooter"]')?.value ?? "";
        const color = document.querySelector('input[name="embedColor"]')?.value ?? "";

        const previewTitle = $("previewEmbedTitle");
        const previewDesc = $("previewEmbedDescription");
        const previewFooter = $("previewEmbedFooter");
        const previewColor = $("previewEmbedColor");

        if (!embedEnabled) {
            safeText(previewTitle, "Embed ist deaktiviert");
            safeText(previewDesc, "Aktiviere „Embed aktiviert“, um eine Vorschau zu sehen.");
            safeText(previewFooter, "");
            if (previewColor) previewColor.style.background = "var(--border)";
            return;
        }

        previewTitle.innerHTML = previewFormatToHtml(title.trim() || "Kein Titel");
        previewDesc.innerHTML = previewFormatToHtml(desc.trim() || "Keine Beschreibung");
        previewFooter.innerHTML = previewFormatToHtml(footer.trim());

        const thumb = document.querySelector('input[name="embedThumbnail"]')?.value ?? "";
        const previewThumb = $("previewEmbedThumbnail");
        if (previewThumb) {
            // For src we need a raw URL (no HTML). Keep it simple & predictable.
            const raw = (thumb.trim() || "")
                .replace(/{avatar}/gi, PREVIEW_AVATAR)
                .replace(/{avatarUrl}/gi, PREVIEW_AVATAR);
            const url = raw.startsWith("http") ? raw : "";
            if (url) {
                previewThumb.src = url;
                previewThumb.style.display = "block";
            } else {
                previewThumb.style.display = "none";
            }
        }

        const norm = normalizeHexColor(color);
        if (previewColor) previewColor.style.background = norm || "var(--primary)";
    }

    function openModal() {
        const modal = $("welcomePreviewModal");
        if (!modal) return;
        renderPreview();
        modal.classList.add("is-open");
        modal.setAttribute("aria-hidden", "false");
        document.body.classList.add("modal-open");
    }

    function closeModal() {
        const modal = $("welcomePreviewModal");
        if (!modal) return;
        modal.classList.remove("is-open");
        modal.setAttribute("aria-hidden", "true");
        document.body.classList.remove("modal-open");
    }

    document.addEventListener("DOMContentLoaded", function () {
        const btn = $("welcomePreviewBtn");
        const closeBtn = $("welcomePreviewCloseBtn");
        const okBtn = $("welcomePreviewOkBtn");
        const backdrop = $("welcomePreviewModal");

        btn?.addEventListener("click", openModal);
        closeBtn?.addEventListener("click", closeModal);
        okBtn?.addEventListener("click", closeModal);

        // click on backdrop closes
        backdrop?.addEventListener("click", function (e) {
            if (e.target === backdrop) closeModal();
        });

        // ESC closes
        document.addEventListener("keydown", function (e) {
            if (e.key !== "Escape") return;
            closeModal();
            closeColorModal();
        });

        // live update while modal open OR just keep it always in sync
        const watchedSelectors = [
            'input[name="embedEnabled"]',
            'input[name="embedTitle"]',
            'textarea[name="embedDescription"]',
            'input[name="embedFooter"]',
            'input[name="embedColor"]',
            'input[name="embedThumbnail"]'
        ];

        watchedSelectors.forEach(sel => {
            document.querySelector(sel)?.addEventListener("input", function () {
                const modal = $("welcomePreviewModal");
                if (modal?.classList.contains("is-open")) renderPreview();
            });
            document.querySelector(sel)?.addEventListener("change", function () {
                const modal = $("welcomePreviewModal");
                if (modal?.classList.contains("is-open")) renderPreview();
            });
        });
    });

    // ---- Color Picker Modal
    const colorBtn = $("embedColorPickerBtn");
    const colorModal = $("embedColorModal");
    const colorClose = $("embedColorCloseBtn");
    const colorCancel = $("embedColorCancelBtn");
    const colorApply = $("embedColorApplyBtn");
    const nativePicker = $("embedColorNativePicker");
    const hexField = $("embedColorHex");

    let pendingColor = null;

    function openColorModal() {
        if (!colorModal) return;

        const current = getEmbedColorValueOrDefault();
        pendingColor = current;

        if (nativePicker) nativePicker.value = current;
        if (hexField) hexField.value = current;

        colorModal.classList.add("is-open");
        colorModal.setAttribute("aria-hidden", "false");
        document.body.classList.add("modal-open");
    }

    function closeColorModal() {
        if (!colorModal) return;
        colorModal.classList.remove("is-open");
        colorModal.setAttribute("aria-hidden", "true");
        document.body.classList.remove("modal-open");
    }

    function updatePendingFromHex(raw) {
        const norm = normalizeHexColor(raw);
        if (!norm) return false;
        pendingColor = norm;
        if (nativePicker) nativePicker.value = norm;
        if (hexField) hexField.value = norm;
        return true;
    }

    colorBtn?.addEventListener("click", openColorModal);
    colorClose?.addEventListener("click", closeColorModal);
    colorCancel?.addEventListener("click", closeColorModal);

    // click on backdrop closes
    colorModal?.addEventListener("click", function (e) {
        if (e.target === colorModal) closeColorModal();
    });

    // native picker updates hex
    nativePicker?.addEventListener("input", function () {
        pendingColor = nativePicker.value.toUpperCase();
        if (hexField) hexField.value = pendingColor;
    });

    // hex field updates native picker on valid hex
    hexField?.addEventListener("input", function () {
        updatePendingFromHex(hexField.value);
    });

    // presets
    document.querySelectorAll("#embedColorModal .colorpreset").forEach(btn => {
        btn.addEventListener("click", function () {
            const c = btn.getAttribute("data-color");
            if (c) updatePendingFromHex(c);
        });
    });

    colorApply?.addEventListener("click", function () {
        const chosen = normalizeHexColor(pendingColor) || getEmbedColorValueOrDefault();
        setEmbedColorValue(chosen);
        closeColorModal();
    });
})();