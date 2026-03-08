package de.tebrox.communitybot.core.config.appconfig;

import jakarta.security.auth.message.config.AuthConfig;

public class AppConfig {

    private WebConfig web;
    private AuthConfig auth;
    private DatabaseConfig database;

    public WebConfig getWeb()          { return web; }
    public void setWeb(WebConfig w)    { this.web = w; }
    public AuthConfig getAuth()        { return auth; }
    public void setAuth(AuthConfig a)  { this.auth = a; }
    public DatabaseConfig getDatabase() { return database; }
    public void setDatabase(DatabaseConfig d) { this.database = d; }

    public static class WebConfig {
        private String host = "0.0.0.0";
        private int port = 8080;
        private SessionConfig session = new SessionConfig();

        public String getHost() { return host; }
        public void setHost(String h) { this.host = h; }
        public int getPort() { return port; }
        public void setPort(int p) { this.port = p; }
        public SessionConfig getSession() { return session; }
        public void setSession(SessionConfig s) { this.session = s; }

        public static class SessionConfig {
            private boolean secure = false;
            private int timeoutMinutes = 120;
            public boolean isSecure() { return secure; }
            public void setSecure(boolean s) { this.secure = s; }
            public int getTimeoutMinutes() { return timeoutMinutes; }
            public void setTimeoutMinutes(int t) { this.timeoutMinutes = t; }
        }
    }

    public static class DatabaseConfig {
        private String type = "h2";
        private H2Config h2 = new H2Config();
        private MysqlConfig mysql = new MysqlConfig();

        public String getType() { return type; }
        public void setType(String t) { this.type = t; }
        public H2Config getH2() { return h2; }
        public void setH2(H2Config h) { this.h2 = h; }
        public MysqlConfig getMysql() { return mysql; }
        public void setMysql(MysqlConfig m) { this.mysql = m; }

        public static class H2Config {
            private String file = "./data/db";
            public String getFile() { return file; }
            public void setFile(String f) { this.file = f; }
        }

        public static class MysqlConfig {
            private String host = "127.0.0.1";
            private int port = 3306;
            private String database = "CommunityBot";
            private String username = "CommunityBot";
            private String password = "";
            public String getHost() { return host; }
            public void setHost(String h) { this.host = h; }
            public int getPort() { return port; }
            public void setPort(int p) { this.port = p; }
            public String getDatabase() { return database; }
            public void setDatabase(String d) { this.database = d; }
            public String getUsername() { return username; }
            public void setUsername(String u) { this.username = u; }
            public String getPassword() { return password; }
            public void setPassword(String p) { this.password = p; }
        }
    }
}
