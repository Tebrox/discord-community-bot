document.addEventListener("DOMContentLoaded", () => {
  // ------------------------------------------------------------
  // Vorhandenes Edit-Verhalten
  // ------------------------------------------------------------
  const params = new URLSearchParams(window.location.search);
  const editId = params.get("editId");

  const jsonEl = document.getElementById("buttons-json");
  if (editId && jsonEl) {
    let buttons = [];
    try {
      buttons = JSON.parse(jsonEl.textContent || "[]");
    } catch (e) {
      console.warn("Failed to parse buttons JSON", e);
      buttons = [];
    }

    const btn = buttons.find(b => b.id === editId);
    if (btn) {
      const setVal = (id, val) => {
        const el = document.getElementById(id);
        if (el) el.value = (val ?? "");
      };

      setVal("btn-id", btn.id);
      setVal("btn-label", btn.label);
      setVal("btn-roleId", btn.roleId);
      setVal("btn-style", btn.style);
    }
  }

  // ------------------------------------------------------------
  // Drag & Drop Sortierung
  // ------------------------------------------------------------
  const list = document.getElementById("roles-button-sort-list");
  const form = document.getElementById("button-order-form");
  const orderInput = document.getElementById("button-order-input");

  if (!list || !form || !orderInput) {
    return;
  }

  let draggedRow = null;

  const getRows = () => Array.from(list.querySelectorAll("tr.sortable-button-row"));

  const updateOrderInput = () => {
    const ids = getRows()
      .map(row => row.getAttribute("data-button-id"))
      .filter(id => id && id.trim() !== "");
    orderInput.value = ids.join(",");
  };

  getRows().forEach(row => {
    row.addEventListener("dragstart", (e) => {
      draggedRow = row;
      row.classList.add("dragging");
      if (e.dataTransfer) {
        e.dataTransfer.effectAllowed = "move";
        e.dataTransfer.setData("text/plain", row.getAttribute("data-button-id") || "");
      }
    });

    row.addEventListener("dragend", () => {
      row.classList.remove("dragging");
      getRows().forEach(r => r.classList.remove("drag-over"));
      updateOrderInput();
      draggedRow = null;
    });

    row.addEventListener("dragover", (e) => {
      e.preventDefault();
      if (!draggedRow || draggedRow === row) return;

      const rect = row.getBoundingClientRect();
      const offset = e.clientY - rect.top;
      const insertBefore = offset < rect.height / 2;

      row.classList.add("drag-over");

      if (insertBefore) {
        list.insertBefore(draggedRow, row);
      } else {
        list.insertBefore(draggedRow, row.nextSibling);
      }
    });

    row.addEventListener("dragleave", () => {
      row.classList.remove("drag-over");
    });

    row.addEventListener("drop", (e) => {
      e.preventDefault();
      row.classList.remove("drag-over");
      updateOrderInput();
    });
  });

  updateOrderInput();

  form.addEventListener("submit", () => {
    updateOrderInput();
  });
});