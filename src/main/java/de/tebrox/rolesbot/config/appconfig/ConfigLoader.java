package de.tebrox.rolesbot.config.appconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String CONFIG_FILE = "config.yml";

    private ConfigLoader() {}

    public static AppConfig load() {
        Path configPath = Paths.get(CONFIG_FILE);

        if (!Files.exists(configPath)) {
            writeTemplate(configPath);
            log.error("=============================================================");
            log.error("  config.yml not found in working directory!");
            log.error("  A template has been created at: {}", configPath.toAbsolutePath());
            log.error("  Please edit it and restart the bot.");
            log.error("=============================================================");
            System.exit(1);
        }

        AppConfig config;
        try (InputStream is = Files.newInputStream(configPath)) {
            LoaderOptions opts = new LoaderOptions();
            Yaml yaml = new Yaml(new Constructor(AppConfig.class, opts));
            config = yaml.load(is);
        } catch (Exception e) {
            log.error("Failed to parse config.yml: {}", e.getMessage());
            System.exit(1);
            return null;
        }

        if (config == null) {
            log.error("config.yml is empty. Please fill in all required fields.");
            System.exit(1);
        }

        validateConfig(config);
        log.info("config.yml loaded successfully from {}", configPath.toAbsolutePath());
        return config;
    }

    private static void validateConfig(AppConfig config) {
        boolean valid = true;

        String envToken = System.getenv("DISCORD_TOKEN");
        String cfgToken = (config.getDiscord() != null) ? config.getDiscord().getToken() : null;
        boolean hasToken = !isBlank(envToken) || !isBlank(cfgToken);
        if (!hasToken) {
            log.error("Discord token missing. Set DISCORD_TOKEN env var or discord.token in config.yml.");
            valid = false;
        } else if (!isBlank(envToken)) {
            log.info("Discord token source: DISCORD_TOKEN environment variable");
        } else {
            log.info("Discord token source: config.yml");
        }

        if (config.getAuth() == null || isBlank(config.getAuth().getPasswordHashBcrypt())) {
            log.error("config.yml: auth.passwordHashBcrypt is required");
            valid = false;
        }

        if (config.getDatabase() != null) {
            String dbType = config.getDatabase().getType();
            if (!"h2".equalsIgnoreCase(dbType) && !"mysql".equalsIgnoreCase(dbType)) {
                log.error("config.yml: database.type must be 'h2' or 'mysql', got: {}", dbType);
                valid = false;
            }
        }

        if (!valid) {
            log.error("config.yml validation failed. Fix the errors above and restart.");
            System.exit(1);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static void writeTemplate(Path path) {
        String template = """
                # RolesBot v4 – config.yml
                # Edit all values before starting.
                # Place this file next to rolesbot.jar.

                discord:
                  # Leave token blank and set DISCORD_TOKEN env var instead (recommended).
                  token: ""

                web:
                  host: "127.0.0.1"
                  port: 8080
                  session:
                    secure: false
                    timeoutMinutes: 120

                auth:
                  # Generate hash: java -cp rolesbot.jar de.tebrox.rolesbot.util.PasswordHasher yourpassword
                  passwordHashBcrypt: "<BCRYPT_HASH_OF_YOUR_PASSWORD>"
                  maxFailedAttempts: 5
                  lockMinutes: 15

                database:
                  type: "h2"            # h2 | mysql
                  h2:
                    file: "./data/db"
                  mysql:
                    host: "127.0.0.1"
                    port: 3306
                    database: "rolesbot"
                    username: "rolesbot"
                    password: "<DB_PASSWORD>"
                """;
        try {
            Files.writeString(path, template);
        } catch (IOException e) {
            log.error("Could not write config.yml template: {}", e.getMessage());
        }
    }
}
