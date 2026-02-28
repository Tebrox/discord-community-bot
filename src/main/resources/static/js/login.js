(function () {

  function getMeta(name) {
    const el = document.querySelector(`meta[name="${name}"]`);
    return el ? el.getAttribute("content") : "";
  }

  const btn = document.getElementById("login-btn");
  if (btn) btn.addEventListener("click", doLogin);

  function doLogin() {
    const passwordInput = document.getElementById("password");
    const errorDiv = document.getElementById("error-msg");

    if (!passwordInput || !errorDiv) return;

    const password = passwordInput.value;
    errorDiv.style.display = "none";

    const csrfToken = getMeta("_csrf");
    const csrfHeader = getMeta("_csrf_header") || "X-CSRF-TOKEN";

    fetch("/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken
      },
      body: JSON.stringify({ password })
    })
      .then(async response => {
        const data = await response.json().catch(() => ({}));

        if (response.ok && data.success) {
          window.location.href = data.redirect || "/";
        } else {
          errorDiv.textContent = data.message || "Fehler";
          errorDiv.style.display = "block";
        }
      })
      .catch(() => {
        errorDiv.textContent = "Netzwerkfehler.";
        errorDiv.style.display = "block";
      });
  }

  document.addEventListener("DOMContentLoaded", () => {
    const passwordInput = document.getElementById("password");

    if (passwordInput) {
      passwordInput.addEventListener("keydown", e => {
        if (e.key === "Enter") {
          doLogin();
        }
      });
    }

    // Falls dein Button onclick="doLogin()" nutzt:
    window.doLogin = doLogin;
  });

})();