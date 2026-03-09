(function () {
    function $(id) {
        return document.getElementById(id);
    }

    function getSelectedMessageKeyFromDom() {
        const stateEl = $("messagePageState");
        return stateEl?.dataset.selectedMessageKey || "";
    }

    function getMessageCards() {
        return Array.from(document.querySelectorAll(".message-editor-card"));
    }

    function findCardByKey(key) {
        return getMessageCards().find(card => card.dataset.messageKey === key) || null;
    }

    function hideAllCards() {
        getMessageCards().forEach(card => {
            card.style.display = "none";
        });
    }

    function updateEmbedVisibility(card) {
        if (!card) return;

        const embedToggle = card.querySelector(".embed-enabled-toggle");
        const embedBlock = card.querySelector(".embed-config-block");

        if (!embedToggle || !embedBlock) return;

        embedBlock.style.display = embedToggle.checked ? "block" : "none";
    }

    function showCardByKey(key) {
        hideAllCards();

        if (!key) return;

        const card = findCardByKey(key);
        if (!card) return;

        card.style.display = "block";
        updateEmbedVisibility(card);
    }

    function bindEmbedToggles() {
        getMessageCards().forEach(card => {
            const embedToggle = card.querySelector(".embed-enabled-toggle");
            if (!embedToggle) return;

            embedToggle.addEventListener("change", function () {
                updateEmbedVisibility(card);
            });

            updateEmbedVisibility(card);
        });
    }

    function bindMessageSelect() {
        const select = $("message-select");
        if (!select) return;

        select.addEventListener("change", function () {
            const selectedKey = select.value || "";
            showCardByKey(selectedKey);
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        const select = $("message-select");
        const selectedMessageKey = getSelectedMessageKeyFromDom();

        bindEmbedToggles();
        bindMessageSelect();

        if (select && selectedMessageKey) {
            select.value = selectedMessageKey;
        }

        showCardByKey(select?.value || selectedMessageKey || "");
    });
})();