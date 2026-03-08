# CommunityBot – Architekturüberblick

Stand: aktueller Codezustand

Dieses Dokument beschreibt die aktuelle Architektur des Projekts auf Modul-Ebene und zeigt, wie die Bereiche **core**, **dashboard** und **community** zusammenarbeiten. Außerdem dient es als Grundlage für die spätere Integration des Ticket-Bereichs.

---

# 1. Ziel der Struktur

Das Projekt ist in drei Hauptbereiche gegliedert:

* **core** – gemeinsame technische Infrastruktur
* **dashboard** – Weboberfläche, HTTP-Controller und Dashboard-nahe Services
* **community** – fachliche Logik des CommunityBots (Panel, Welcome, Guild-Konfiguration)

Diese Trennung ist sinnvoll, weil technische Basisfunktionen nicht mit fachlicher Discord-Logik vermischt werden und das Dashboard als eigener Bereich sauber abgegrenzt bleibt.

---

# 2. Modulübersicht

## `core`

Der `core`-Bereich enthält modulübergreifende Infrastruktur.

### Verantwortlichkeiten

* Security und Login
* Rechteprüfung
* App-/Spring-Konfiguration
* globale Persistence für Dashboard-User
* Logging-Infrastruktur
* technische Utilities
* globale Web-Hilfsklassen

### Enthaltene Teilbereiche

* `core.access`
* `core.config`
* `core.config.appconfig`
* `core.logging`
* `core.persistence`
* `core.security`
* `core.util`
* `core.web`

---

## `dashboard`

Der `dashboard`-Bereich enthält die Weboberfläche und dashboard-spezifische Anwendungslogik.

### Verantwortlichkeiten

* HTTP-Controller
* Dashboard-Seiten und Aktionen
* DTOs für Web-Requests/-Responses
* Demo-/Entwicklungsdaten
* Panel-Refresh aus dem Dashboard heraus
* Discord-Zugriff für die Weboberfläche

### Enthaltene Teilbereiche

* `dashboard.controller`
* `dashboard.dto`
* `dashboard.demo`
* `dashboard.panel`
* `dashboard.service`
* `dashboard.web`

---

## `community`

Der `community`-Bereich enthält die eigentliche Fachlogik des Bots.

### Verantwortlichkeiten

* Guild-Konfiguration
* Rollenpanel
* Welcome-Funktionalität
* Discord-Listener
* persistente Community-Daten
* Community-Services

### Enthaltene Teilbereiche

* `community.config`
* `community.discord.core`
* `community.discord.listener`
* `community.panel`
* `community.persistence.entity`
* `community.persistence.repository`
* `community.service`

---

# 3. Datenfluss im Projekt

## Dashboard → Service → Community-Konfiguration → Discord

Typischer Ablauf bei einer Änderung im Dashboard:

1. Ein Benutzer öffnet eine Dashboard-Seite.
2. Ein Controller in `dashboard.controller` verarbeitet den Request.
3. Dashboard-Services und Access-Prüfungen werden verwendet.
4. Die Guild-Konfiguration wird über Community-Services geladen oder gespeichert.
5. Falls nötig, wird über `dashboard.panel` ein Panel-Refresh ausgelöst.
6. Die Community-Discord-Logik aktualisiert Nachrichten, Buttons oder Welcome-Inhalte in Discord.

---

## Security- und Rechtefluss

1. Login und Security laufen über `core.security`.
2. Benutzer- und Guild-Rechte werden über `core.persistence` geladen.
3. `DashboardAccessService` prüft, welche Bereiche sichtbar oder nutzbar sind.
4. Controller und Views verwenden diese Berechtigungen, um Seiten, Tabs und Aktionen abzusichern.

---

# 4. Verantwortlichkeiten der wichtigsten Klassenbereiche

## `core.security`

Hier liegt die zentrale Sicherheitslogik.

### Beispiele

* `SecurityConfiguration`
* `AuthService`
* `DashboardSecurityService`
* `DashboardOAuth2LoginSuccessHandler`
* `DashboardPermission`
* `PermissionBits`

### Aufgabe

* Login absichern
* Rechte definieren
* Zugriff auf Dashboard-Bereiche steuern

---

## `core.access`

### Beispiele

* `DashboardAccessService`
* `PermissionGuard`

### Aufgabe

* Guild-bezogene Rechte prüfen
* Controller und Views bei Berechtigungsfragen unterstützen

---

## `community.config`

### Beispiel

* `CommunityGuildConfig`

### Aufgabe

* fachliches Modell der Guild-Konfiguration
* enthält Community-spezifische Einstellungen wie Panel, Buttons, Welcome usw.

Wichtig: Das ist kein JPA-Entity-Typ, sondern das Domänenmodell.

---

## `community.persistence`

### Beispiele

* `CommunityGuildConfigEntity`
* `PanelState`
* `WelcomedUser`
* `GuildConfigRepository`
* `PanelStateRepository`
* `WelcomedUserRepository`

### Aufgabe

* Datenbankrepräsentation und Zugriff auf Community-Daten

---

## `community.service`

### Beispiele

* `CommunityGuildConfigService`
* `PanelService`
* `WelcomeTrackingService`

### Aufgabe

* Fachlogik zwischen Controller/Dashboard und Persistence/Discord
* Laden, Speichern, Validieren und Anwenden von Community-Daten

---

## `community.discord.listener`

### Beispiele

* `GuildLifecycleListener`
* `PanelAdminListener`
* `ReloadListener`
* `RoleButtonsListener`
* `WelcomeListener`

### Aufgabe

* Reaktion auf Discord-Events
* Verknüpfung von Discord-Aktionen mit Community-Fachlogik

---

## `dashboard.panel`

### Beispiele

* `PanelRefresher`
* `LivePanelRefresher`
* `NoopPanelRefresher`

### Aufgabe

* Dashboard-seitiger Trigger für Panel-Aktualisierung
* Entkopplung zwischen Weboberfläche und echter Discord-Aktualisierung

---

# 5. Warum die aktuelle Struktur sinnvoll ist

Die aktuelle Aufteilung ist architektonisch gut, weil sie drei Ebenen trennt:

* **technische Basis** (`core`)
* **Web-/Dashboard-Schicht** (`dashboard`)
* **fachliche Bot-Logik** (`community`)

Dadurch wird vermieden, dass:

* Security und Fachlogik vermischt werden,
* Dashboard-Code direkt in Discord-Listenern landet,
* Community-spezifische Daten versehentlich in globale Infrastruktur rutschen.

---

# 6. Vorbereitung für TicketBot-Integration

Die aktuelle Struktur ist bereits geeignet, um später ein weiteres Modul aufzunehmen:

* `ticket`

Geplante Zielidee:

* `core` bleibt gemeinsam genutzt
* `dashboard` bleibt gemeinsame Verwaltungsoberfläche
* `community` bleibt für Rollen/Welcome/Community-Funktionen
* `ticket` ergänzt Ticket-spezifische Discord-, Service- und Persistence-Logik

## Vorteil

So kann später ein gemeinsames Projekt entstehen mit:

* gemeinsamer Login-/Security-Struktur
* gemeinsamem Dashboard
* getrennten Fachbereichen
* sauberer Erweiterbarkeit

---

# 7. Empfohlene Grundregel für neue Klassen

Beim Hinzufügen neuer Klassen sollte diese einfache Regel gelten:

## Nach `core`

Nur wenn die Klasse:

* von mehreren Modulen genutzt wird,
* technische Infrastruktur ist,
* keine Community- oder Ticket-Fachlogik enthält.

## Nach `dashboard`

Wenn die Klasse:

* Web-/Controller-/DTO-/Dashboard-spezifisch ist,
* vom Dashboard aus Aktionen steuert,
* keine eigenständige Bot-Domäne darstellt.

## Nach `community`

Wenn die Klasse:

* Rollenpanel, Welcome, Guild-Konfiguration oder Discord-Communitylogik betrifft,
* fachlich zum CommunityBot gehört.

## Später nach `ticket`

Wenn die Klasse:

* nur Tickets, Support, Ticket-Buttons, Ticket-Konfiguration oder Ticket-Persistence betrifft.

---

# 8. Aktuelle Schwachstellen / mögliche Feinschliffe

## Paketname `community.discord.listener`

Üblicher wäre `listeners` statt `listener`.

Das ist kein Funktionsproblem, aber eine kleine Namensinkonsistenz.

## Repository-Name `GuildConfigRepository`

Da die Entity bereits `CommunityGuildConfigEntity` heißt, wäre langfristig auch ein konsistenterer Name sinnvoll, z. B. `CommunityGuildConfigRepository`.

Das ist optional, aber für spätere Ticket-Integration klarer.

---

# 9. Zusammenfassung

Die aktuelle Architektur ist bereits deutlich strukturierter als eine flache Paketverteilung.

## Positiv

* klare Trennung von Technik, Web und Fachlogik
* gute Grundlage für Rechte- und Dashboard-Steuerung
* sinnvoll für späteren Merge mit Ticket-Funktionalität
* Community-Daten und Discord-Logik sind fachlich gebündelt

## Wichtig für die Zukunft

* `core` nicht zum Sammelordner werden lassen
* neue Ticket-Funktionalität direkt in ein eigenes `ticket`-Modul legen
* Dashboard weiterhin als gemeinsame Oberfläche behandeln
* fachliche Community-Logik nicht zurück in globale Packages verschieben

---

# 10. Langfristiges Zielbild

```text
de.tebrox.communitybot
├─ core
├─ dashboard
├─ community
└─ ticket
```

Diese Struktur ist die passende Basis für ein gemeinsames Bot-Projekt mit mehreren Fachmodulen unter einer gemeinsamen technischen Plattform.
