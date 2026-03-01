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

    // -----------------------------
    // Discord-like autocomplete for <# (channels) and <@& / @ (roles)
    // -----------------------------

    function getCaretClientRect(el) {
        const isTextArea = el.nodeName === "TEXTAREA";
        const value = el.value ?? "";
        const pos = el.selectionStart ?? value.length;

        const style = window.getComputedStyle(el);
        const elRect = el.getBoundingClientRect();

        const mirror = document.createElement("div");
        mirror.className = "mention-ac-mirror";

        // Mirror must sit exactly where the element sits in the viewport
        mirror.style.position = "fixed";
        mirror.style.left = elRect.left + "px";
        mirror.style.top = elRect.top + "px";
        mirror.style.width = elRect.width + "px";
        mirror.style.height = elRect.height + "px";

        mirror.style.visibility = "hidden";
        mirror.style.whiteSpace = isTextArea ? "pre-wrap" : "pre";
        mirror.style.wordWrap = "break-word";
        mirror.style.overflow = "hidden"; // important: don't create its own scrollbars

        // Copy typography + box
        mirror.style.fontFamily = style.fontFamily;
        mirror.style.fontSize = style.fontSize;
        mirror.style.fontWeight = style.fontWeight;
        mirror.style.letterSpacing = style.letterSpacing;
        mirror.style.lineHeight = style.lineHeight;
        mirror.style.padding = style.padding;
        mirror.style.border = style.border;
        mirror.style.boxSizing = style.boxSizing;

        // Also copy alignment-related styles that affect caret x-position
        mirror.style.textAlign = style.textAlign;
        mirror.style.direction = style.direction;

        const before = value.substring(0, pos);
        const after = value.substring(pos);

        mirror.textContent = before;

        const marker = document.createElement("span");
        marker.textContent = "\u200b";
        mirror.appendChild(marker);

        const tail = document.createElement("span");
        tail.textContent = after.length ? after.substring(0, 1) : ".";
        tail.style.opacity = "0";
        mirror.appendChild(tail);

        document.body.appendChild(mirror);

        // Sync scroll position for textarea/input (matters for long content)
        mirror.scrollTop = el.scrollTop;
        mirror.scrollLeft = el.scrollLeft;

        const markerRect = marker.getBoundingClientRect();
        document.body.removeChild(mirror);

        return {
            left: markerRect.left,
            top: markerRect.top,
            bottom: markerRect.bottom
        };
    }

    function findTrigger(textBeforeCaret) {
        // Find the most recent trigger among:
        // 1) "<#query"
        // 2) "<@&query"
        // 3) "@query" (role autocomplete, Discord-like)
        const candidates = [];

        const mCh = textBeforeCaret.match(/<#[^\s>]*$/);
        if (mCh) candidates.push({ type: "channel", start: textBeforeCaret.length - mCh[0].length, query: mCh[0].slice(2) });

        const mRole = textBeforeCaret.match(/<@&[^\s>]*$/);
        if (mRole) candidates.push({ type: "role", start: textBeforeCaret.length - mRole[0].length, query: mRole[0].slice(3) });

        // Plain "@": avoid emails by requiring boundary before @, and stop at whitespace
        const mAt = textBeforeCaret.match(/(^|[\s(>])@([^\s@]{0,32})$/);
        if (mAt) {
            const prefixLen = mAt[1].length;
            const wholeLen = mAt[0].length;
            candidates.push({
                type: "roleAt",
                start: textBeforeCaret.length - wholeLen + prefixLen,
                query: mAt[2] || ""
            });
        }

        if (!candidates.length) return null;
        // Use the latest starting trigger
        candidates.sort((a, b) => b.start - a.start);
        return candidates[0];
    }

    function normalizeQuery(q) {
        return (q ?? "").toLowerCase().replace(/[^a-z0-9 _-]/g, "");
    }

    function scoreItem(name, query) {
        // Simple but effective scoring: prefix match > contains
        const n = name.toLowerCase();
        if (!query) return 1;
        if (n.startsWith(query)) return 100;
        const idx = n.indexOf(query);
        if (idx >= 0) return 50 - idx;
        return 0;
    }

    function setupMentionAutocomplete(el) {
        if (!el) return;

        const overlay = document.createElement("div");
        overlay.className = "mention-ac";
        overlay.style.display = "none";

        overlay.style.position = "fixed";
        overlay.style.zIndex = "99999";
        overlay.style.background = "#0f1115";
        overlay.style.border = "1px solid rgba(255,255,255,0.12)";
        overlay.style.borderRadius = "10px";
        overlay.style.boxShadow = "0 10px 30px rgba(0,0,0,0.45)";
        overlay.style.minWidth = "260px";
        overlay.style.maxHeight = "260px";
        overlay.style.overflowY = "auto";
        overlay.style.color = "#fff";

        overlay.setAttribute("role", "listbox");
        document.body.appendChild(overlay);

        let openFor = null; // { type, start, query }
        let items = [];
        let active = 0;

        function close() {
            overlay.style.display = "none";
            overlay.innerHTML = "";
            openFor = null;
            items = [];
            active = 0;
        }

        function render() {
            if (!openFor) return close();

            overlay.innerHTML = "";

            items.forEach((it, idx) => {
                const row = document.createElement("div");
                row.className = "mention-ac-item" + (idx === active ? " is-active" : "");
                row.setAttribute("role", "option");
                row.dataset.index = String(idx);

                const icon = document.createElement("div");
                icon.className = "mention-ac-icon";
                icon.textContent = it.kind === "channel" ? "#" : "@";

                const label = document.createElement("div");
                label.className = "mention-ac-label";
                label.textContent = it.label;

                const meta = document.createElement("div");
                meta.className = "mention-ac-meta";
                meta.textContent = it.kind === "channel" ? "Channel" : "Rolle";

                row.appendChild(icon);
                row.appendChild(label);
                row.appendChild(meta);

                row.addEventListener("mouseenter", () => {
                    active = idx;
                    updateActive();
                });

                row.addEventListener("mousedown", (e) => {
                    // prevent input losing focus
                    e.preventDefault();
                });

                row.addEventListener("click", () => {
                    applySelection(idx);
                });

                overlay.appendChild(row);
            });

            const caret = getCaretClientRect(el);

            // kurz sichtbar machen, damit height gemessen werden kann
            overlay.style.display = "block";
            overlay.style.visibility = "hidden";

            const overlayHeight = overlay.offsetHeight;
            const overlayWidth = overlay.offsetWidth;

            let left = caret.left;
            let top;

            // Prüfen ob genug Platz unterhalb ist
            const spaceBelow = window.innerHeight - caret.bottom;
            const spaceAbove = caret.top;

            if (spaceBelow < overlayHeight + 10 && spaceAbove > overlayHeight + 10) {
                // ➜ oberhalb anzeigen
                top = caret.top - overlayHeight - 6;
            } else {
                // ➜ unterhalb anzeigen (Standard)
                top = caret.bottom + 6;
            }

            // horizontal begrenzen
            left = Math.min(left, window.innerWidth - overlayWidth - 10);

            overlay.style.left = left + "px";
            overlay.style.top = top + "px";
            overlay.style.visibility = "visible";
        }

        function updateActive() {
            const nodes = overlay.querySelectorAll(".mention-ac-item");
            nodes.forEach((n, idx) => {
                if (idx === active) n.classList.add("is-active");
                else n.classList.remove("is-active");
            });

            const current = overlay.querySelector('.mention-ac-item.is-active');
            if (current) {
                current.scrollIntoView({ block: "nearest" });
            }
        }

        function computeItems() {
            const value = el.value ?? "";
            const pos = el.selectionStart ?? value.length;
            const before = value.substring(0, pos);

            const trg = findTrigger(before);
            if (!trg) return close();

            const query = normalizeQuery(trg.query);

            openFor = trg;

            if (trg.type === "channel") {
                const list = (channelsArr || []).map(ch => ({
                    kind: "channel",
                    id: String(ch.id),
                    label: String(ch.name)
                }));

                items = list
                    .map(it => ({ ...it, _score: scoreItem(it.label, query) }))
                    .filter(it => it._score > 0)
                    .sort((a, b) => b._score - a._score)

            } else {
                const list = (rolesArr || []).map(r => ({
                    kind: "role",
                    id: String(r.id),
                    label: String(r.name)
                }));

                items = list
                    .map(it => ({ ...it, _score: scoreItem(it.label, query) }))
                    .filter(it => it._score > 0)
                    .sort((a, b) => b._score - a._score)
            }

            active = 0;
            if (!items.length) return close();
            render();
        }

        function applySelection(index) {
            if (!openFor) return;
            const it = items[index];
            if (!it) return;

            const value = el.value ?? "";
            const pos = el.selectionStart ?? value.length;

            const start = openFor.start;
            const beforeStart = value.substring(0, start);
            const after = value.substring(pos);

            let insert;
            if (it.kind === "channel") {
                insert = `<#${it.id}> `;
            } else {
                // roles: always insert as <@&id>
                insert = `<@&${it.id}> `;
            }

            el.value = beforeStart + insert + after;

            const newPos = (beforeStart + insert).length;
            el.setSelectionRange(newPos, newPos);

            // trigger input so preview updates
            el.dispatchEvent(new Event("input", { bubbles: true }));

            close();
            el.focus();
        }

        el.addEventListener("input", computeItems);

        el.addEventListener("click", () => {
            // reposition + recompute on caret move
            if (overlay.style.display !== "none") computeItems();
        });

        el.addEventListener("scroll", () => {
            if (overlay.style.display !== "none") render();
        });

        el.addEventListener("keydown", (e) => {
            if (overlay.style.display === "none") return;

            if (e.key === "Escape") {
                e.preventDefault();
                close();
                return;
            }

            if (e.key === "ArrowDown") {
                e.preventDefault();
                active = Math.min(active + 1, Math.min(items.length) - 1);
                updateActive();
                return;
            }

            if (e.key === "ArrowUp") {
                e.preventDefault();
                active = Math.max(active - 1, 0);
                updateActive();
                return;
            }

            if (e.key === "Enter" || e.key === "Tab") {
                e.preventDefault();
                applySelection(active);
                return;
            }
        });

        document.addEventListener("mousedown", (e) => {
            if (overlay.style.display === "none") return;
            if (e.target === overlay || overlay.contains(e.target) || e.target === el) return;
            close();
        });
    }

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

        setupMentionAutocomplete(document.querySelector('input[name="embedTitle"]'));
        setupMentionAutocomplete(document.querySelector('textarea[name="embedDescription"]'));
        setupMentionAutocomplete(document.querySelector('input[name="embedFooter"]'));
        setupMentionAutocomplete(document.querySelector('input[name="embedThumbnail"]'));
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