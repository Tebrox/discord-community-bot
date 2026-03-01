(function () {
  let currentFilter = "ALL";
  let autoScrollEnabled = true;

  const MAX_ENTRIES = 2000;
  const MAX_RECONNECT_DELAY = 20000;

  const container = document.getElementById("log-container");
  const liveIndicator = document.getElementById("live-indicator");
  const statusEl = document.getElementById("sse-status");
  const autoScrollBtn = document.getElementById("auto-scroll-btn");

  if (!container) return;

  let offset = container.querySelectorAll(".log-entry").length;
  if (Number.isNaN(offset) || offset < 0) offset = 0;

  let evtSource = null;
  let reconnectAttempt = 0;
  let reconnectTimer = null;
  let reconnectInterval = null;

  function setStatus(text) {
    if (statusEl) statusEl.textContent = text || "";
  }

  function setLive(ok) {
    if (!liveIndicator) return;
    liveIndicator.style.background = ok ? "#3ba55c" : "#ed4245";
  }

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

  function scrollToBottom() {
    container.scrollTop = container.scrollHeight;
  }

  function isAtBottom() {
    return (container.scrollHeight - container.scrollTop)
      <= (container.clientHeight + 50);
  }

  function trimIfNeeded() {
    const entries = container.querySelectorAll(".log-entry");
    const extra = entries.length - MAX_ENTRIES;
    if (extra <= 0) return;

    for (let i = 0; i < extra; i++) {
      entries[i].remove();
    }
  }

  function clearReconnectTimers() {
    if (reconnectTimer) clearTimeout(reconnectTimer);
    if (reconnectInterval) clearInterval(reconnectInterval);
    reconnectTimer = null;
    reconnectInterval = null;
  }

  function scheduleReconnect() {
    reconnectAttempt++;
    const delay = Math.min(
      MAX_RECONNECT_DELAY,
      1000 * Math.pow(2, Math.min(5, reconnectAttempt - 1))
    );

    let seconds = Math.ceil(delay / 1000);
    setStatus(`Reconnect in ${seconds}s…`);

    reconnectInterval = setInterval(() => {
      seconds--;
      if (seconds > 0) {
        setStatus(`Reconnect in ${seconds}s…`);
      }
    }, 1000);

    reconnectTimer = setTimeout(() => {
      clearReconnectTimers();
      connect();
    }, delay);
  }

  function connect() {
    try { if (evtSource) evtSource.close(); } catch (e) {}

    setLive(true);
    setStatus("Connecting…");

    evtSource = new EventSource("/api/logs/stream?since=" + offset);

    evtSource.onopen = () => {
      reconnectAttempt = 0;
      setLive(true);
      setStatus("Live");
    };

    evtSource.addEventListener("log", (e) => {
      const html = e?.data || "";
      if (!html.trim()) return;

      const wasAtBottom = isAtBottom();

      container.insertAdjacentHTML("beforeend", html);

      const added = (html.match(/class=['"]log-entry\b/g) || []).length;
      offset += added;

      trimIfNeeded();
      applyFilter();

      if (autoScrollEnabled && wasAtBottom) {
        scrollToBottom();
      }
    });

    evtSource.addEventListener("ping", () => {});

    evtSource.onerror = () => {
      setLive(false);
      setStatus("Disconnected");
      try { evtSource.close(); } catch (e) {}
      scheduleReconnect();
    };
  }

  // Filter Buttons
  document.querySelectorAll(".filter-btn[data-level]").forEach(btn => {
    btn.addEventListener("click", () => {
      currentFilter = btn.getAttribute("data-level") || "ALL";
      document.querySelectorAll(".filter-btn")
        .forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      applyFilter();
    });
  });

  // Auto-Scroll Toggle
  if (autoScrollBtn) {
    autoScrollBtn.addEventListener("click", () => {
      autoScrollEnabled = !autoScrollEnabled;
      autoScrollBtn.textContent = autoScrollEnabled
        ? "Auto-Scroll: AN"
        : "Auto-Scroll: AUS";
      if (autoScrollEnabled) scrollToBottom();
    });
  }

  // Clear Button
  const clearBtn = document.getElementById("clear-display-btn");
  if (clearBtn) {
    clearBtn.addEventListener("click", () => {
      container.innerHTML = "";

      fetch("/api/logs/count", { cache: "no-store" })
        .then(r => r.ok ? r.text() : "0")
        .then(t => {
          const n = parseInt(t, 10);
          if (!Number.isNaN(n)) offset = n;
        });
    });
  }

  window.addEventListener("beforeunload", () => {
    try { if (evtSource) evtSource.close(); } catch (e) {}
    clearReconnectTimers();
  });

  applyFilter();
  scrollToBottom();
  connect();
})();