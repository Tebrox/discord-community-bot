package de.tebrox.rolesbot.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class BuildInfoConfig {

    @Bean("appVersion")
    public String appVersion() {
        Package pkg = BuildInfoConfig.class.getPackage();
        String version = pkg.getImplementationVersion();
        return version != null ? version : "DEV";
    }
}