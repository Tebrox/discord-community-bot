# CommunityBot – Full API Reference

This document was generated from the current uploaded source code.
It lists the declared types, constructors and methods found in the project.

Notes:
- Only declarations from the project source are included.
- Production code and test code are separated.
- Nested types are shown with their parent type name, for example `CommunityGuildConfig.PanelConfig`.

# Production Code

## Package: `de.tebrox.communitybot`

### Class: `CommunityBotApplication`

Main Spring Boot entry point for the application.

**Source:** `src/main/java/de/tebrox/communitybot/CommunityBotApplication.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public static void main(String[] args)`
  - Executes `main`.


## Package: `de.tebrox.communitybot.community.config`

### Class: `CommunityGuildConfig`

Configuration class or configuration model used for `communityguild`.

**Source:** `src/main/java/de/tebrox/communitybot/community/config/CommunityGuildConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public PanelConfig getPanel()`
  - Returns the panel.
- `public void setPanel(PanelConfig p)`
  - Updates the panel.
- `public List<ButtonConfig> getButtons()`
  - Returns the buttons.
- `public void setButtons(List<ButtonConfig> b)`
  - Updates the buttons.
- `public StatusButtonConfig getStatusButton()`
  - Returns the status button.
- `public void setStatusButton(StatusButtonConfig s)`
  - Updates the status button.
- `public LayoutConfig getLayout()`
  - Returns the layout.
- `public void setLayout(LayoutConfig l)`
  - Updates the layout.
- `public WelcomeConfig getWelcome()`
  - Returns the welcome.
- `public void setWelcome(WelcomeConfig w)`
  - Updates the welcome.
- `public List<List<String>> effectiveLayoutRows()`
  - Executes `effectiveLayoutRows`.
- `public List<String> validateLayout()`
  - Validates the layout.

### Class: `CommunityGuildConfig.ButtonConfig`

Configuration class or configuration model used for `button`.

**Source:** `src/main/java/de/tebrox/communitybot/community/config/CommunityGuildConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getId()`
  - Returns the id.
- `public void setId(String id)`
  - Updates the id.
- `public String getLabel()`
  - Returns the label.
- `public void setLabel(String l)`
  - Updates the label.
- `public String getRoleId()`
  - Returns the role id.
- `public void setRoleId(String r)`
  - Updates the role id.
- `public String getStyle()`
  - Returns the style.
- `public void setStyle(String s)`
  - Updates the style.

### Class: `CommunityGuildConfig.EmbedConfig`

Configuration class or configuration model used for `embed`.

**Source:** `src/main/java/de/tebrox/communitybot/community/config/CommunityGuildConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public boolean isEnabled()`
  - Checks whether the enabled.
- `public void setEnabled(boolean e)`
  - Updates the enabled.
- `public String getColor()`
  - Returns the color.
- `public void setColor(String c)`
  - Updates the color.
- `public String getTitle()`
  - Returns the title.
- `public void setTitle(String t)`
  - Updates the title.
- `public String getDescription()`
  - Returns the description.
- `public void setDescription(String d)`
  - Updates the description.
- `public String getFooter()`
  - Returns the footer.
- `public void setFooter(String f)`
  - Updates the footer.
- `public String getThumbnail()`
  - Returns the thumbnail.
- `public void setThumbnail(String t)`
  - Updates the thumbnail.

### Class: `CommunityGuildConfig.LayoutConfig`

Configuration class or configuration model used for `layout`.

**Source:** `src/main/java/de/tebrox/communitybot/community/config/CommunityGuildConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public List<List<String>> getRows()`
  - Returns the rows.
- `public void setRows(List<List<String>> rows)`
  - Updates the rows.

### Class: `CommunityGuildConfig.PanelConfig`

Configuration class or configuration model used for `panel`.

**Source:** `src/main/java/de/tebrox/communitybot/community/config/CommunityGuildConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getTitle()`
  - Returns the title.
- `public void setTitle(String t)`
  - Updates the title.
- `public String getRequirePermission()`
  - Returns the require permission.
- `public void setRequirePermission(String p)`
  - Updates the require permission.
- `public String getAllowedChannelId()`
  - Returns the allowed channel id.
- `public void setAllowedChannelId(String id)`
  - Updates the allowed channel id.
- `public boolean isEnforceChannelLock()`
  - Checks whether the enforce channel lock.
- `public void setEnforceChannelLock(boolean b)`
  - Updates the enforce channel lock.

### Class: `CommunityGuildConfig.StatusButtonConfig`

Configuration class or configuration model used for `statusbutton`.

**Source:** `src/main/java/de/tebrox/communitybot/community/config/CommunityGuildConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getId()`
  - Returns the id.
- `public void setId(String id)`
  - Updates the id.
- `public String getLabel()`
  - Returns the label.
- `public void setLabel(String l)`
  - Updates the label.
- `public String getStyle()`
  - Returns the style.
- `public void setStyle(String s)`
  - Updates the style.

### Class: `CommunityGuildConfig.WelcomeConfig`

Configuration class or configuration model used for `welcome`.

**Source:** `src/main/java/de/tebrox/communitybot/community/config/CommunityGuildConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public boolean isEnabled()`
  - Checks whether the enabled.
- `public void setEnabled(boolean e)`
  - Updates the enabled.
- `public String getChannelId()`
  - Returns the channel id.
- `public void setChannelId(String c)`
  - Updates the channel id.
- `public boolean isSendDm()`
  - Checks whether the send dm.
- `public void setSendDm(boolean s)`
  - Updates the send dm.
- `public int getDeleteAfterSeconds()`
  - Returns the delete after seconds.
- `public void setDeleteAfterSeconds(int d)`
  - Updates the delete after seconds.
- `public boolean isOnlyFirstJoin()`
  - Checks whether the only first join.
- `public void setOnlyFirstJoin(boolean o)`
  - Updates the only first join.
- `public String getMessage()`
  - Returns the message.
- `public void setMessage(String m)`
  - Updates the message.
- `public EmbedConfig getEmbed()`
  - Returns the embed.
- `public void setEmbed(EmbedConfig e)`
  - Updates the embed.


## Package: `de.tebrox.communitybot.community.discord.core`

### Class: `DiscordListenerRegistrar`

Discord listener that reacts to events related to `DiscordRegistrar`.

**Source:** `src/main/java/de/tebrox/communitybot/community/discord/core/DiscordListenerRegistrar.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void registerListeners()`
  - Registers the listeners.


## Package: `de.tebrox.communitybot.community.discord.listener`

### Class: `GuildLifecycleListener`

Discord listener that reacts to events related to `GuildLifecycle`.

**Source:** `src/main/java/de/tebrox/communitybot/community/discord/listener/GuildLifecycleListener.java`

**Constructors**

- `public GuildLifecycleListener(CommunityGuildConfigService configManager)`
  - Creates a new `GuildLifecycleListener` instance.

**Methods**

- `public void onGuildJoin(GuildJoinEvent event)`
  - Handles the `onGuildJoin` event callback.
- `public void onGuildLeave(GuildLeaveEvent event)`
  - Handles the `onGuildLeave` event callback.

### Class: `PanelAdminListener`

Discord listener that reacts to events related to `PanelAdmin`.

**Source:** `src/main/java/de/tebrox/communitybot/community/discord/listener/PanelAdminListener.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void onSlashCommandInteraction(SlashCommandInteractionEvent event)`
  - Handles the `onSlashCommandInteraction` event callback.

### Class: `ReloadListener`

Discord listener that reacts to events related to `Reload`.

**Source:** `src/main/java/de/tebrox/communitybot/community/discord/listener/ReloadListener.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void setJda(JDA jda)`
  - Updates the jda.
- `public void onSlashCommandInteraction(SlashCommandInteractionEvent event)`
  - Handles the `onSlashCommandInteraction` event callback.

### Class: `RoleButtonsListener`

Discord listener that reacts to events related to `RoleButtons`.

**Source:** `src/main/java/de/tebrox/communitybot/community/discord/listener/RoleButtonsListener.java`

**Constructors**

- `public RoleButtonsListener(CommunityGuildConfigService configManager)`
  - Creates a new `RoleButtonsListener` instance.

**Methods**

- `public void onButtonInteraction(ButtonInteractionEvent event)`
  - Handles the `onButtonInteraction` event callback.

### Class: `WelcomeListener`

Discord listener that reacts to events related to `Welcome`.

**Source:** `src/main/java/de/tebrox/communitybot/community/discord/listener/WelcomeListener.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void onGuildMemberJoin(GuildMemberJoinEvent event)`
  - Handles the `onGuildMemberJoin` event callback.


## Package: `de.tebrox.communitybot.community.panel`

### Class: `ButtonFactory`

Class responsible for `ButtonFactory` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/community/panel/ButtonFactory.java`

**Constructors**

- `private ButtonFactory()`
  - Creates a new `ButtonFactory` instance.

**Methods**

- `public static Button fromConfig(CommunityGuildConfig.ButtonConfig cfg)`
  - Executes `fromConfig`.
- `public static Button statusButton(CommunityGuildConfig.StatusButtonConfig cfg)`
  - Executes `statusButton`.
- `public static ButtonStyle parseStyle(String s)`
  - Parses the style.

### Class: `PanelBuilder`

Builder class used to assemble `panel` content or components.

**Source:** `src/main/java/de/tebrox/communitybot/community/panel/PanelBuilder.java`

**Constructors**

- `private PanelBuilder()`
  - Creates a new `PanelBuilder` instance.

**Methods**

- `public static MessageEmbed buildEmbed(CommunityGuildConfig cfg)`
  - Builds the embed.
- `public static List<ActionRow> buildActionRows(CommunityGuildConfig cfg)`
  - Builds the action rows.


## Package: `de.tebrox.communitybot.community.persistence.entity`

### Class: `CommunityGuildConfigEntity`

Configuration class or configuration model used for `communityguildentity`.

**Source:** `src/main/java/de/tebrox/communitybot/community/persistence/entity/CommunityGuildConfigEntity.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Class: `PanelState`

Class responsible for `PanelState` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/community/persistence/entity/PanelState.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public boolean hasState()`
  - Checks whether the state.

### Class: `WelcomedUser`

Class responsible for `WelcomedUser` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/community/persistence/entity/WelcomedUser.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.


## Package: `de.tebrox.communitybot.community.persistence.repository`

### Interface: `GuildConfigRepository`

Interface that defines the contract for `GuildConfigRepository`.

**Source:** `src/main/java/de/tebrox/communitybot/community/persistence/repository/GuildConfigRepository.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Interface: `PanelStateRepository`

Interface that defines the contract for `PanelStateRepository`.

**Source:** `src/main/java/de/tebrox/communitybot/community/persistence/repository/PanelStateRepository.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Interface: `WelcomedUserRepository`

Interface that defines the contract for `WelcomedUserRepository`.

**Source:** `src/main/java/de/tebrox/communitybot/community/persistence/repository/WelcomedUserRepository.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `List<WelcomedUser> findByGuildIdOrderByWelcomedAtDesc(String guildId)`
  - Finds the by guild id order by welcomed at desc.


## Package: `de.tebrox.communitybot.community.service`

### Class: `CommunityGuildConfigService`

Service class that contains the business logic for `CommunityGuildConfig`.

**Source:** `src/main/java/de/tebrox/communitybot/community/service/CommunityGuildConfigService.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void reconcile(JDA jda)`
  - Executes `reconcile`.
- `public CommunityGuildConfig getConfig(String guildId)`
  - Returns the config.
- `public Set<String> getRegisteredGuildIds()`
  - Returns the registered guild ids.
- `public void reload(JDA jda)`
  - Executes `reload`.
- `private CommunityGuildConfig deserialize(CommunityGuildConfigEntity entity)`
  - Executes `deserialize`.
- `private String serialize(CommunityGuildConfig cfg) throws JsonProcessingException`
  - Executes `serialize`.
- `private String serializeUnchecked(CommunityGuildConfig cfg)`
  - Executes `serializeUnchecked`.
- `private CommunityGuildConfig defaultGuildConfig()`
  - Executes `defaultGuildConfig`.

### Class: `PanelService`

Service class that contains the business logic for `Panel`.

**Source:** `src/main/java/de/tebrox/communitybot/community/service/PanelService.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public Optional<PanelState> findState(String guildId)`
  - Finds the state.
- `public void deleteState(String guildId)`
  - Deletes the state.

### Class: `WelcomeTrackingService`

Service class that contains the business logic for `WelcomeTracking`.

**Source:** `src/main/java/de/tebrox/communitybot/community/service/WelcomeTrackingService.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public List<WelcomedUser> getWelcomedUsers(String guildId)`
  - Returns the welcomed users.


## Package: `de.tebrox.communitybot.core.access`

### Class: `DashboardAccessService`

Service class that contains the business logic for `DashboardAccess`.

**Source:** `src/main/java/de/tebrox/communitybot/core/access/DashboardAccessService.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public DashboardUserEntity getCurrentUserOrNull()`
  - Returns the current user or null.
- `public boolean isSuperadmin()`
  - Checks whether the superadmin.
- `public boolean canAccessDashboard()`
  - Executes `canAccessDashboard`.
- `public List<DashboardUserEntity> listUsers()`
  - Lists the users.
- `public Optional<DashboardUserEntity> findUser(Long id)`
  - Finds the user.
- `public DashboardUserEntity createIfMissing(String discordId)`
  - Creates the if missing.

### Class: `PermissionGuard`

Type related to permission handling for `PermissionGuard`.

**Source:** `src/main/java/de/tebrox/communitybot/core/access/PermissionGuard.java`

**Constructors**

- `private PermissionGuard()`
  - Creates a new `PermissionGuard` instance.

**Methods**

- No methods declared.


## Package: `de.tebrox.communitybot.core.config`

### Class: `BuildInfoConfig`

Configuration class or configuration model used for `buildinfo`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/BuildInfoConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String appVersion()`
  - Executes `appVersion`.

### Record: `DashboardProperties`

Immutable record used to transport data for `DashboardProperties`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/DashboardProperties.java`

**Record components**

- `(
        @NotBlank(message = "DASHBOARD_PASSWORD_HASH must be set in application.yml or as env var")`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Record: `DiscordProperties`

Immutable record used to transport data for `DiscordProperties`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/DiscordProperties.java`

**Record components**

- `(
        @NotBlank(message = "Discord token missing: set DISCORD_TOKEN env var or discord.token in application.yml")`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Class: `JdaConfiguration`

Configuration class or configuration model used for `jdauration`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/JdaConfiguration.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void onContextClosed()`
  - Handles the `onContextClosed` event callback.

### Class: `SchedulingConfig`

Configuration class or configuration model used for `scheduling`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/SchedulingConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public ScheduledExecutorService sseScheduler()`
  - Executes `sseScheduler`.


## Package: `de.tebrox.communitybot.core.config.appconfig`

### Class: `AppConfig`

Configuration class or configuration model used for `app`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public DiscordConfig getDiscord()`
  - Returns the discord.
- `public void setDiscord(DiscordConfig d)`
  - Updates the discord.
- `public WebConfig getWeb()`
  - Returns the web.
- `public void setWeb(WebConfig w)`
  - Updates the web.
- `public AuthConfig getAuth()`
  - Returns the auth.
- `public void setAuth(AuthConfig a)`
  - Updates the auth.
- `public DatabaseConfig getDatabase()`
  - Returns the database.
- `public void setDatabase(DatabaseConfig d)`
  - Updates the database.

### Class: `AppConfig.AuthConfig`

Configuration class or configuration model used for `auth`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getPasswordHashBcrypt()`
  - Returns the password hash bcrypt.
- `public void setPasswordHashBcrypt(String h)`
  - Updates the password hash bcrypt.
- `public int getMaxFailedAttempts()`
  - Returns the max failed attempts.
- `public void setMaxFailedAttempts(int n)`
  - Updates the max failed attempts.
- `public int getLockMinutes()`
  - Returns the lock minutes.
- `public void setLockMinutes(int l)`
  - Updates the lock minutes.

### Class: `AppConfig.DatabaseConfig`

Configuration class or configuration model used for `database`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getType()`
  - Returns the type.
- `public void setType(String t)`
  - Updates the type.
- `public H2Config getH2()`
  - Returns the h2.
- `public void setH2(H2Config h)`
  - Updates the h2.
- `public MysqlConfig getMysql()`
  - Returns the mysql.
- `public void setMysql(MysqlConfig m)`
  - Updates the mysql.

### Class: `AppConfig.DatabaseConfig.H2Config`

Configuration class or configuration model used for `h2`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getFile()`
  - Returns the file.
- `public void setFile(String f)`
  - Updates the file.

### Class: `AppConfig.DatabaseConfig.MysqlConfig`

Configuration class or configuration model used for `mysql`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getHost()`
  - Returns the host.
- `public void setHost(String h)`
  - Updates the host.
- `public int getPort()`
  - Returns the port.
- `public void setPort(int p)`
  - Updates the port.
- `public String getDatabase()`
  - Returns the database.
- `public void setDatabase(String d)`
  - Updates the database.
- `public String getUsername()`
  - Returns the username.
- `public void setUsername(String u)`
  - Updates the username.
- `public String getPassword()`
  - Returns the password.
- `public void setPassword(String p)`
  - Updates the password.

### Class: `AppConfig.DiscordConfig`

Configuration class or configuration model used for `discord`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getToken()`
  - Returns the token.
- `public void setToken(String t)`
  - Updates the token.

### Class: `AppConfig.WebConfig`

Configuration class or configuration model used for `web`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String getHost()`
  - Returns the host.
- `public void setHost(String h)`
  - Updates the host.
- `public int getPort()`
  - Returns the port.
- `public void setPort(int p)`
  - Updates the port.
- `public SessionConfig getSession()`
  - Returns the session.
- `public void setSession(SessionConfig s)`
  - Updates the session.

### Class: `AppConfig.WebConfig.SessionConfig`

Configuration class or configuration model used for `session`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfig.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public boolean isSecure()`
  - Checks whether the secure.
- `public void setSecure(boolean s)`
  - Updates the secure.
- `public int getTimeoutMinutes()`
  - Returns the timeout minutes.
- `public void setTimeoutMinutes(int t)`
  - Updates the timeout minutes.

### Class: `AppConfigHolder`

Configuration class or configuration model used for `appholder`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/AppConfigHolder.java`

**Constructors**

- `private AppConfigHolder()`
  - Creates a new `AppConfigHolder` instance.

**Methods**

- `public static void set(AppConfig cfg)`
  - Executes `set`.
- `public static AppConfig get()`
  - Executes `get`.

### Class: `ConfigLoader`

Configuration class or configuration model used for `loader`.

**Source:** `src/main/java/de/tebrox/communitybot/core/config/appconfig/ConfigLoader.java`

**Constructors**

- `private ConfigLoader()`
  - Creates a new `ConfigLoader` instance.

**Methods**

- `public static AppConfig load()`
  - Executes `load`.
- `private static void validateConfig(AppConfig config)`
  - Validates the config.
- `private static boolean isBlank(String s)`
  - Checks whether the blank.
- `private static void writeTemplate(Path path)`
  - Executes `writeTemplate`.


## Package: `de.tebrox.communitybot.core.logging`

### Class: `LogBuffer`

Class responsible for `LogBuffer` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/logging/LogBuffer.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public static String redact(String message)`
  - Executes `redact`.
- `public synchronized List<LogEntry> getAll()`
  - Returns the all.
- `public synchronized List<LogEntry> getSince(int skipCount)`
  - Returns the since.
- `public synchronized int size()`
  - Executes `size`.
- `public void info(String message)`
  - Executes `info`.
- `public void warn(String message)`
  - Executes `warn`.
- `public void error(String message)`
  - Executes `error`.

### Record: `LogBuffer.LogEntry`

Immutable record used to transport data for `LogEntry`.

**Source:** `src/main/java/de/tebrox/communitybot/core/logging/LogBuffer.java`

**Record components**

- `(String timestamp, String level, String message)`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Class: `LogBufferAppender`

Class responsible for `LogBufferAppender` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/logging/LogBufferAppender.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void setApplicationContext(ApplicationContext ctx) throws BeansException`
  - Updates the application context.
- `protected void append(ILoggingEvent event)`
  - Executes `append`.
- `static void setBuffer(LogBuffer buffer)`
  - Updates the buffer.


## Package: `de.tebrox.communitybot.core.persistence.entity`

### Class: `DashboardUserEntity`

JPA entity representing the persisted `DashboardUser` data model.

**Source:** `src/main/java/de/tebrox/communitybot/core/persistence/entity/DashboardUserEntity.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public Long getId()`
  - Returns the id.
- `public String getDiscordId()`
  - Returns the discord id.
- `public void setDiscordId(String discordId)`
  - Updates the discord id.
- `public boolean isEnabled()`
  - Checks whether the enabled.
- `public void setEnabled(boolean enabled)`
  - Updates the enabled.
- `public String getUsername()`
  - Returns the username.
- `public void setUsername(String username)`
  - Updates the username.
- `public String getAvatarUrl()`
  - Returns the avatar url.
- `public void setAvatarUrl(String avatarUrl)`
  - Updates the avatar url.
- `public Instant getCreatedAt()`
  - Returns the created at.
- `public Instant getLastLoginAt()`
  - Returns the last login at.
- `public void setLastLoginAt(Instant lastLoginAt)`
  - Updates the last login at.

### Class: `DashboardUserGuildPermissionEntity`

JPA entity representing the persisted `DashboardUserGuildPermission` data model.

**Source:** `src/main/java/de/tebrox/communitybot/core/persistence/entity/DashboardUserGuildPermissionEntity.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public Long getId()`
  - Returns the id.
- `public Long getUserId()`
  - Returns the user id.
- `public void setUserId(Long userId)`
  - Updates the user id.
- `public String getGuildId()`
  - Returns the guild id.
- `public void setGuildId(String guildId)`
  - Updates the guild id.
- `public long getPermissions()`
  - Returns the permissions.
- `public void setPermissions(long permissions)`
  - Updates the permissions.

### Class: `LoginAttempt`

Class responsible for `LoginAttempt` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/persistence/entity/LoginAttempt.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.


## Package: `de.tebrox.communitybot.core.persistence.repository`

### Interface: `DashboardUserGuildPermissionRepository`

Interface that defines the contract for `DashboardUserGuildPermissionRepository`.

**Source:** `src/main/java/de/tebrox/communitybot/core/persistence/repository/DashboardUserGuildPermissionRepository.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `List<DashboardUserGuildPermissionEntity> findAllByUserId(Long userId)`
  - Finds the all by user id.

### Interface: `DashboardUserRepository`

Interface that defines the contract for `DashboardUserRepository`.

**Source:** `src/main/java/de/tebrox/communitybot/core/persistence/repository/DashboardUserRepository.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `Optional<DashboardUserEntity> findByDiscordId(String discordId)`
  - Finds the by discord id.

### Interface: `LoginAttemptRepository`

Interface that defines the contract for `LoginAttemptRepository`.

**Source:** `src/main/java/de/tebrox/communitybot/core/persistence/repository/LoginAttemptRepository.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `void deleteByIpAddress(String ip)`
  - Deletes the by ip address.
- `void deleteOlderThan(Instant before)`
  - Deletes the older than.


## Package: `de.tebrox.communitybot.core.security`

### Class: `AuthService`

Service class that contains the business logic for `Auth`.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/AuthService.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public boolean isLockedOut(String ipAddress)`
  - Checks whether the locked out.
- `private long countRecentFailed(String ipAddress)`
  - Counts the recent failed.
- `public static String maskIp(String ip)`
  - Masks the ip.
- `public void cleanupOldAttempts()`
  - Cleans up .

### Enum: `AuthService.AuthResult`

Enumeration that defines the supported values for `AuthResult`.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/AuthService.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Class: `DashboardOAuth2LoginSuccessHandler`

Class responsible for `DashboardOAuth2LoginSuccessHandler` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/DashboardOAuth2LoginSuccessHandler.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Enum: `DashboardPermission`

Enumeration that defines the supported values for `DashboardPermission`.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/DashboardPermission.java`

**Constructors**

- `DashboardPermission(long bit)`
  - Creates a new `DashboardPermission` instance.

**Methods**

- No methods declared.

### Class: `DashboardSecurityService`

Service class that contains the business logic for `DashboardSecurity`.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/DashboardSecurityService.java`

**Constructors**

- `public DashboardSecurityService(DashboardProperties dashboardProperties)`
  - Creates a new `DashboardSecurityService` instance.

**Methods**

- `public String getCurrentDiscordId()`
  - Returns the current discord id.
- `public String getCurrentUsername()`
  - Returns the current username.
- `public boolean isSuperadmin()`
  - Checks whether the superadmin.

### Class: `PasswordHasher`

Class responsible for `PasswordHasher` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/PasswordHasher.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public static void main(String[] args)`
  - Executes `main`.

### Class: `PermissionBits`

Type related to permission handling for `PermissionBits`.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/PermissionBits.java`

**Constructors**

- `private PermissionBits()`
  - Creates a new `PermissionBits` instance.

**Methods**

- No methods declared.

### Class: `SecurityConfiguration`

Configuration class or configuration model used for `securityuration`.

**Source:** `src/main/java/de/tebrox/communitybot/core/security/SecurityConfiguration.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public PasswordEncoder passwordEncoder()`
  - Executes `passwordEncoder`.


## Package: `de.tebrox.communitybot.core.util`

### Class: `ChannelGuard`

Class responsible for `ChannelGuard` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/util/ChannelGuard.java`

**Constructors**

- `private ChannelGuard()`
  - Creates a new `ChannelGuard` instance.

**Methods**

- No methods declared.

### Class: `RolesSafetyChecker`

Class responsible for `RolesSafetyChecker` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/util/RolesSafetyChecker.java`

**Constructors**

- `private RolesSafetyChecker()`
  - Creates a new `RolesSafetyChecker` instance.

**Methods**

- No methods declared.

### Class: `SnowflakeValidator`

Class responsible for `SnowflakeValidator` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/util/SnowflakeValidator.java`

**Constructors**

- `private SnowflakeValidator()`
  - Creates a new `SnowflakeValidator` instance.

**Methods**

- `public static boolean isValid(String value)`
  - Checks whether the valid.


## Package: `de.tebrox.communitybot.core.web`

### Class: `GlobalExceptionHandler`

Class responsible for `GlobalExceptionHandler` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/core/web/GlobalExceptionHandler.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `private static boolean isSseRequest(HttpServletRequest request)`
  - Checks whether the sse request.
- `public void handleAsyncNotUsable(AsyncRequestNotUsableException e)`
  - Executes `handleAsyncNotUsable`.
- `public void handleIo(IOException e) throws IOException`
  - Executes `handleIo`.


## Package: `de.tebrox.communitybot.dashboard.controller`

### Class: `AdminController`

Spring MVC controller responsible for `admin` related HTTP endpoints.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/controller/AdminController.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public boolean isSuperadmin()`
  - Checks whether the superadmin.
- `private boolean requireSuperadmin(RedirectAttributes ra)`
  - Executes `requireSuperadmin`.

### Class: `AuthController`

Spring MVC controller responsible for `auth` related HTTP endpoints.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/controller/AuthController.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Class: `BotApiController`

Spring MVC controller responsible for `botapi` related HTTP endpoints.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/controller/BotApiController.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public ResponseEntity<List<GuildDto>> getGuilds()`
  - Returns the guilds.

### Class: `DashboardController`

Spring MVC controller responsible for `dashboard` related HTTP endpoints.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/controller/DashboardController.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String legacy_dashboard(Model model)`
  - Executes `legacy_dashboard`.
- `private boolean ensureMembersLoaded(Guild guild)`
  - Executes `ensureMembersLoaded`.
- `private CommunityGuildConfig deepCopy(String guildId)`
  - Executes `deepCopy`.
- `private void refreshPanel(String guildId)`
  - Refreshes the panel.

### Class: `DashboardViewController`

Spring MVC controller responsible for `dashboardview` related HTTP endpoints.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/controller/DashboardViewController.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `) public String dashboard()`
  - Executes `dashboard`.

### Class: `LogController`

Spring MVC controller responsible for `log` related HTTP endpoints.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/controller/LogController.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public String logsPage(Model model)`
  - Executes `logsPage`.
- `public SseEmitter streamLogs(int since)`
  - Executes `streamLogs`.
- `public int getCount()`
  - Returns the count.
- `private String escapeHtml(String s)`
  - Executes `escapeHtml`.


## Package: `de.tebrox.communitybot.dashboard.demo`

### Class: `DemoGuildConfigSeeder`

Configuration class or configuration model used for `demoguildseeder`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/demo/DemoGuildConfigSeeder.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `public void run(String... args) throws Exception`
  - Executes `run`.


## Package: `de.tebrox.communitybot.dashboard.dto`

### Record: `GuildDto`

Immutable record used to transport data for `GuildDto`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/dto/GuildDto.java`

**Record components**

- `(String id, String name, String iconUrl)`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Record: `LoginRequest`

Immutable record used to transport data for `LoginRequest`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/dto/LoginRequest.java`

**Record components**

- `(String password)`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.


## Package: `de.tebrox.communitybot.dashboard.panel`

### Class: `LivePanelRefresher`

Component that refreshes or updates `livepanel` state.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/panel/LivePanelRefresher.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Class: `NoopPanelRefresher`

Component that refreshes or updates `nooppanel` state.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/panel/NoopPanelRefresher.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Interface: `PanelRefresher`

Interface that defines the contract for `PanelRefresher`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/panel/PanelRefresher.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.


## Package: `de.tebrox.communitybot.dashboard.service`

### Interface: `DashboardDiscordService`

Interface that defines the contract for `DashboardDiscordService`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/service/DashboardDiscordService.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `List<GuildInfo> listGuilds()`
  - Lists the guilds.
- `Optional<GuildInfo> getGuild(String guildId)`
  - Returns the guild.
- `List<RoleInfo> listRoles(String guildId)`
  - Lists the roles.
- `List<TextChannelInfo> listTextChannels(String guildId)`
  - Lists the text channels.

### Record: `DashboardDiscordService.GuildInfo`

Immutable record used to transport data for `GuildInfo`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/service/DashboardDiscordService.java`

**Record components**

- `(String id, String name, String iconUrl, int memberCount)`

**Constructors**

- No explicit constructors declared.

**Methods**

- No methods declared.

### Class: `DemoDashboardDiscordService`

Service class that contains the business logic for `DemoDashboardDiscord`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/service/DemoDashboardDiscordService.java`

**Constructors**

- `public DemoDashboardDiscordService()`
  - Creates a new `DemoDashboardDiscordService` instance.

**Methods**

- `public List<GuildInfo> listGuilds()`
  - Lists the guilds.
- `public Optional<GuildInfo> getGuild(String guildId)`
  - Returns the guild.
- `public List<RoleInfo> listRoles(String guildId)`
  - Lists the roles.
- `public List<TextChannelInfo> listTextChannels(String guildId)`
  - Lists the text channels.

### Class: `LiveDashboardDiscordService`

Service class that contains the business logic for `LiveDashboardDiscord`.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/service/LiveDashboardDiscordService.java`

**Constructors**

- `public LiveDashboardDiscordService(JDA jda)`
  - Creates a new `LiveDashboardDiscordService` instance.

**Methods**

- `public List<GuildInfo> listGuilds()`
  - Lists the guilds.
- `public Optional<GuildInfo> getGuild(String guildId)`
  - Returns the guild.
- `public List<RoleInfo> listRoles(String guildId)`
  - Lists the roles.
- `public List<TextChannelInfo> listTextChannels(String guildId)`
  - Lists the text channels.


## Package: `de.tebrox.communitybot.dashboard.web`

### Class: `DashboardModelAttributes`

Class responsible for `DashboardModelAttributes` related behavior.

**Source:** `src/main/java/de/tebrox/communitybot/dashboard/web/DashboardModelAttributes.java`

**Constructors**

- `public DashboardModelAttributes(DashboardSecurityService dashboardSecurityService)`
  - Creates a new `DashboardModelAttributes` instance.

**Methods**

- `public String currentDiscordId()`
  - Executes `currentDiscordId`.
- `public String currentUsername()`
  - Executes `currentUsername`.
- `public boolean isSuperadmin()`
  - Checks whether the superadmin.


# Test Code

## Package: `de.tebrox.communitybot.auth`

### Class: `AuthServiceTest`

Test class for `AuthService`.

**Source:** `src/test/java/de/tebrox/communitybot/auth/AuthServiceTest.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `void setUp()`
  - Test case that verifies `set up`.
- `void successfulLogin_clearsFailedAttempts()`
  - Test case that verifies `successful login_clears failed attempts`.
- `void failedLogin_doesNotClearAttempts()`
  - Test case that verifies `failed login_does not clear attempts`.
- `void isLockedOut_returnsTrueWhenLimitReached()`
  - Test case that verifies `is locked out_returns true when limit reached`.
- `void isLockedOut_returnsFalseWhenBelowLimit()`
  - Test case that verifies `is locked out_returns false when below limit`.
- `void attemptLogin_returnsLockedOut_whenAlreadyLocked()`
  - Test case that verifies `attempt login_returns locked out_when already locked`.
- `void attemptLogin_returnsNowLockedOut_whenThisAttemptTriggersLockout()`
  - Test case that verifies `attempt login_returns now locked out_when this attempt triggers lockout`.
- `void maskIp_masksLastIpv4Octet()`
  - Test case that verifies `mask ip_masks last ipv4octet`.
- `void maskIp_masksLastIpv6Segment()`
  - Test case that verifies `mask ip_masks last ipv6segment`.
- `void maskIp_handlesNull()`
  - Test case that verifies `mask ip_handles null`.
- `void maskIp_fullIpNeverInOutput()`
  - Test case that verifies `mask ip_full ip never in output`.
- `void cleanupOldAttempts_deletesRecordsOlderThan24h()`
  - Test case that verifies `cleanup old attempts_deletes records older than24h`.


## Package: `de.tebrox.communitybot.service`

### Class: `LogBufferTest`

Test class for `LogBuffer`.

**Source:** `src/test/java/de/tebrox/communitybot/service/LogBufferTest.java`

**Constructors**

- No explicit constructors declared.

**Methods**

- `void setUp()`
  - Test case that verifies `set up`.
- `void appender_writesEventIntoBuffer()`
  - Test case that verifies `appender_writes event into buffer`.
- `void redact_replacesDiscordToken()`
  - Test case that verifies `redact_replaces discord token`.
- `void redact_leavesNormalMessageUnchanged()`
  - Test case that verifies `redact_leaves normal message unchanged`.
- `void redact_replacesBearerToken()`
  - Test case that verifies `redact_replaces bearer token`.
- `void redact_bearerIsCaseInsensitive()`
  - Test case that verifies `redact_bearer is case insensitive`.
- `void ringBuffer_evictsOldestWhenFull()`
  - Test case that verifies `ring buffer_evicts oldest when full`.
- `void ringBuffer_neverExceedsMaxSize()`
  - Test case that verifies `ring buffer_never exceeds max size`.
- `void redact_replacesPasswordPattern()`
  - Test case that verifies `redact_replaces password pattern`.
- `void redact_replacesSecretPattern()`
  - Test case that verifies `redact_replaces secret pattern`.
- `void redact_replacesTokenPattern()`
  - Test case that verifies `redact_replaces token pattern`.
- `void redact_handlesNull()`
  - Test case that verifies `redact_handles null`.

