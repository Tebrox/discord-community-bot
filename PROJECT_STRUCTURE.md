# CommunityBot – Klassenübersicht

Stand: aktueller Codezustand

Diese Datei listet **alle Java-Klassen nach Package** auf und trennt zwischen:

* **Produktionscode (`src/main/java`)**
* **Tests (`src/test/java`)**

---

# Produktionscode

## Root

### `de.tebrox.communitybot`

* `CommunityBotApplication`

---

# Community Modul

## `de.tebrox.communitybot.community.config`

* `CommunityGuildConfig`

---

## `de.tebrox.communitybot.community.discord.core`

* `DiscordListenerRegistrar`

---

## `de.tebrox.communitybot.community.discord.listener`

* `GuildLifecycleListener`
* `PanelAdminListener`
* `ReloadListener`
* `RoleButtonsListener`
* `WelcomeListener`

---

## `de.tebrox.communitybot.community.panel`

* `ButtonFactory`
* `PanelBuilder`

---

## `de.tebrox.communitybot.community.persistence.entity`

* `CommunityGuildConfigEntity`
* `PanelState`
* `WelcomedUser`

---

## `de.tebrox.communitybot.community.persistence.repository`

* `GuildConfigRepository`
* `PanelStateRepository`
* `WelcomedUserRepository`

---

## `de.tebrox.communitybot.community.service`

* `CommunityGuildConfigService`
* `PanelService`
* `WelcomeTrackingService`

---

# Core Modul

## `de.tebrox.communitybot.core.access`

* `DashboardAccessService`
* `PermissionGuard`

---

## `de.tebrox.communitybot.core.config`

* `BuildInfoConfig`
* `DashboardProperties`
* `DiscordProperties`
* `JdaConfiguration`
* `SchedulingConfig`

---

## `de.tebrox.communitybot.core.config.appconfig`

* `AppConfig`
* `AppConfigHolder`
* `ConfigLoader`

---

## `de.tebrox.communitybot.core.logging`

* `LogBuffer`
* `LogBufferAppender`

---

## `de.tebrox.communitybot.core.persistence.entity`

* `DashboardUserEntity`
* `DashboardUserGuildPermissionEntity`
* `LoginAttempt`

---

## `de.tebrox.communitybot.core.persistence.repository`

* `DashboardUserGuildPermissionRepository`
* `DashboardUserRepository`
* `LoginAttemptRepository`

---

## `de.tebrox.communitybot.core.security`

* `AuthService`
* `DashboardOAuth2LoginSuccessHandler`
* `DashboardPermission`
* `DashboardSecurityService`
* `PasswordHasher`
* `PermissionBits`
* `SecurityConfiguration`

---

## `de.tebrox.communitybot.core.util`

* `ChannelGuard`
* `RolesSafetyChecker`
* `SnowflakeValidator`

---

## `de.tebrox.communitybot.core.web`

* `GlobalExceptionHandler`

---

# Dashboard Modul

## `de.tebrox.communitybot.dashboard.controller`

* `AdminController`
* `AuthController`
* `BotApiController`
* `DashboardController`
* `DashboardViewController`
* `LogController`

---

## `de.tebrox.communitybot.dashboard.demo`

* `DemoGuildConfigSeeder`

---

## `de.tebrox.communitybot.dashboard.dto`

* `GuildDto`
* `LoginRequest`

---

## `de.tebrox.communitybot.dashboard.panel`

* `LivePanelRefresher`
* `NoopPanelRefresher`
* `PanelRefresher`

---

## `de.tebrox.communitybot.dashboard.service`

* `DashboardDiscordService`
* `DemoDashboardDiscordService`
* `LiveDashboardDiscordService`

---

## `de.tebrox.communitybot.dashboard.web`

* `DashboardModelAttributes`

---

# Testcode

Diese Klassen befinden sich unter **`src/test/java`** und gehören **nicht zum Produktionscode**.

## `de.tebrox.communitybot.auth`

* `AuthServiceTest`

---

## `de.tebrox.communitybot.core.logging`

* `LogBufferTest`

---
