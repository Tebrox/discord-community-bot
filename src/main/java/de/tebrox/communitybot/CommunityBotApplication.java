package de.tebrox.communitybot;

import de.tebrox.communitybot.config.appconfig.AppConfig;
import de.tebrox.communitybot.config.appconfig.AppConfigHolder;
import de.tebrox.communitybot.config.appconfig.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
@ConfigurationPropertiesScan
public class CommunityBotApplication {

    private static final Logger log = LoggerFactory.getLogger(CommunityBotApplication.class);

    public static void main(String[] args) {
        AppConfig appConfig = ConfigLoader.load();
        AppConfigHolder.set(appConfig);

        SpringApplication app = new SpringApplication(CommunityBotApplication.class);
        app.setDefaultProperties(buildSpringProperties(appConfig));
        app.run(args);
    }

    private static Map<String, Object> buildSpringProperties(AppConfig cfg) {
        Map<String, Object> props = new HashMap<>();

        if (cfg.getWeb() != null) {
            props.put("server.port", cfg.getWeb().getPort());
            props.put("server.address", cfg.getWeb().getHost());
            if (cfg.getWeb().getSession() != null) {
                props.put("server.servlet.session.cookie.secure", cfg.getWeb().getSession().isSecure());
                props.put("server.servlet.session.timeout", cfg.getWeb().getSession().getTimeoutMinutes() + "m");
            }
        }

        String envToken = System.getenv("DISCORD_TOKEN");
        String cfgToken = (cfg.getDiscord() != null) ? cfg.getDiscord().getToken() : null;
        String token = (envToken != null && !envToken.isBlank()) ? envToken : cfgToken;
        if (token != null && !token.isBlank()) {
            props.put("discord.token", token);
        }

        if (cfg.getAuth() != null) {
            props.put("dashboard.password-hash", cfg.getAuth().getPasswordHashBcrypt());
            props.put("dashboard.max-login-attempts", cfg.getAuth().getMaxFailedAttempts());
            props.put("dashboard.lockout-duration-minutes", cfg.getAuth().getLockMinutes());
        }

        if (cfg.getDatabase() != null) {
            buildDbProperties(cfg.getDatabase(), props);
        }

        return props;
    }

    private static void buildDbProperties(AppConfig.DatabaseConfig db, Map<String, Object> props) {
        String type = (db.getType() != null) ? db.getType().toLowerCase() : "h2";

        if ("mysql".equals(type)) {
            AppConfig.DatabaseConfig.MysqlConfig mysql = db.getMysql();
            if (mysql != null) {
                String url = String.format(
                        "jdbc:mariadb://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8",
                        mysql.getHost(), mysql.getPort(), mysql.getDatabase()
                );

                props.put("spring.datasource.url", url);
                props.put("spring.datasource.username", mysql.getUsername());
                props.put("spring.datasource.password", mysql.getPassword());
                props.put("spring.datasource.driver-class-name", "org.mariadb.jdbc.Driver");

                props.put("spring.jpa.database-platform", "org.hibernate.dialect.MariaDBDialect");
            } else {
                // hilfreich: hart abbrechen statt “still” weiterlaufen
                throw new IllegalStateException("database.type=mysql but database.mysql is missing in config.yml");
            }
        } else {
            AppConfig.DatabaseConfig.H2Config h2 = db.getH2();
            String file = (h2 != null && h2.getFile() != null) ? h2.getFile() : "./data/db";
            props.put("spring.datasource.url", "jdbc:h2:file:" + file + ";AUTO_SERVER=TRUE");
            props.put("spring.datasource.username", "sa");
            props.put("spring.datasource.password", "");
            props.put("spring.datasource.driver-class-name", "org.h2.Driver");
        }
    }
}
