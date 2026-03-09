package de.tebrox.communitybot.core.config.appconfig;

import jakarta.security.auth.message.config.AuthConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppConfig {

    private WebConfig web;
    private DatabaseConfig database;

    @Setter
    @Getter
    public static class WebConfig {
        private String host = "0.0.0.0";
        private int port = 8080;
        private SessionConfig session = new SessionConfig();

        @Setter
        @Getter
        public static class SessionConfig {
            private boolean secure = false;
            private int timeoutMinutes = 120;

        }
    }

    @Setter
    @Getter
    public static class DatabaseConfig {
        private String type = "h2";
        private H2Config h2 = new H2Config();
        private MysqlConfig mysql = new MysqlConfig();

        @Setter
        @Getter
        public static class H2Config {
            private String file = "./data/db";

        }

        @Setter
        @Getter
        public static class MysqlConfig {
            private String host = "127.0.0.1";
            private int port = 3306;
            private String database = "CommunityBot";
            private String username = "CommunityBot";
            private String password = "";

        }
    }
}
