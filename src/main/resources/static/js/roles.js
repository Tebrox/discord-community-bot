(function () {
  // Pre-fill form if ?editId= is present
  const params = new URLSearchParams(window.location.search);
  const editId = params.get("editId");
  if (!editId) return;

  const jsonEl = document.getElementById("buttons-json");
  if (!jsonEl) return;

  let buttons = [];
  try {
    buttons = JSON.parse(jsonEl.textContent || "[]");
  } catch (e) {
    return;
  }

  const btn = buttons.find(b => b.id === editId);
  if (!btn) return;

  const setVal = (id, val) => {
    const el = document.getElementById(id);
    if (el) el.value = val ?? "";
  };

  setVal("btn-id", btn.id);
  setVal("btn-label", btn.label);
  setVal("btn-roleId", btn.roleId);
  setVal("btn-style", btn.style);
})();