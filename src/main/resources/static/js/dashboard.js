(function () {
  'use strict';

  async function fetchJson(url) {
    const res = await fetch(url, { credentials: 'same-origin' });
    if (res.status === 401 || res.status === 403) {
      window.location.href = '/auth/login';
      return null;
    }
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return res.json();
  }

  function renderGuilds(guilds) {
    const grid = document.getElementById('guilds-grid');
    grid.innerHTML = '';

    if (!guilds || guilds.length === 0) {
      grid.innerHTML = '<div class="muted">Keine Server gefunden.</div>';
      return;
    }

    for (const g of guilds) {
      const a = document.createElement('a');
      a.className = 'guild-card';
      a.href = '/dashboard/' + g.id;

      const icon = document.createElement('div');
      icon.className = 'guild-icon';
      if (g.iconUrl) {
        const img = document.createElement('img');
        img.src = g.iconUrl;
        img.alt = g.name;
        icon.appendChild(img);
      } else {
        icon.textContent = (g.name || '?').charAt(0).toUpperCase();
      }

      const name = document.createElement('div');
      name.className = 'guild-name';
      name.textContent = g.name;

      const id = document.createElement('div');
      id.className = 'guild-id';
      id.textContent = g.id;

      a.appendChild(icon);
      a.appendChild(name);
      a.appendChild(id);
      grid.appendChild(a);
    }
  }

  fetchJson('/api/bot/guilds')
    .then(renderGuilds)
    .catch(err => {
      console.error(err);
      document.getElementById('guilds-grid').innerHTML =
        '<div class="alert alert-error">Fehler beim Laden der Server.</div>';
    });
})();