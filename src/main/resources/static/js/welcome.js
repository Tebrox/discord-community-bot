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

        safeText(previewTitle, title.trim() || "Kein Titel");
        safeText(previewDesc, desc.trim() || "Keine Beschreibung");
        safeText(previewFooter, footer.trim());

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
            'input[name="embedColor"]'
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