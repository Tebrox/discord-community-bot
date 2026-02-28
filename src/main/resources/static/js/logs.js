(function () {
  let currentFilter = "ALL";

  const container = document.getElementById("log-container");
  if (!container) return;

  const liveIndicator = document.getElementById("live-indicator");

  let offset = parseInt(container.getAttribute("data-offset") || "0", 10);
  if (Number.isNaN(offset)) offset = 0;

  function applyFilter() {
    container.querySelectorAll(".log-entry").forEach(el => {
      if (currentFilter === "ALL") {
        el.style.display = "";
      } else {
        const cls = "log-" + currentFilter.toLowerCase();
        el.style.display = el.classList.contains(cls) ? "" : "none";
      }
    });
  }

  function setActive(btn) {
    document.querySelectorAll(".filter-btn").forEach(b => b.classList.remove("active"));
    if (btn) btn.classList.add("active");
  }

  // Buttons binden
  document.querySelectorAll(".filter-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      currentFilter = btn.getAttribute("data-level") || "ALL";
      setActive(btn);
      applyFilter();
    });
  });

  // Clear display button
  const clearBtn = document.getElementById("clear-display-btn");
  if (clearBtn) {
    clearBtn.addEventListener("click", () => {
      container.innerHTML = "";
      // Offset neu setzen, sonst stimmt "since" nicht mehr zur Anzeige.
      // Da dein SSE nur neue Einträge anhängt, ist es am sinnvollsten,
      // den Offset auf 0 zu setzen, damit wieder sauber gezählt wird.
      offset = 0;
      applyFilter();
    });
  }

  function scrollToBottom() {
    container.scrollTop = container.scrollHeight;
  }

  // SSE
  const evtSource = new EventSource("/api/logs/stream?since=" + encodeURIComponent(offset));
  evtSource.onmessage = (e) => {
    const html = e.data || "";
    if (!html.trim()) return;

    const wasAtBottom = (container.scrollHeight - container.scrollTop) <= (container.clientHeight + 50);
    container.insertAdjacentHTML("beforeend", html);

    offset = container.querySelectorAll(".log-entry").length;

    applyFilter();
    if (wasAtBottom) scrollToBottom();
  };

  evtSource.onerror = () => {
    if (liveIndicator) liveIndicator.style.background = "#ed4245";
  };

  window.addEventListener("beforeunload", () => {
    try { evtSource.close(); } catch (e) {}
  });

  // initial
  applyFilter();
  scrollToBottom();
})();