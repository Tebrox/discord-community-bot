package de.tebrox.communitybot.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CLI utility to generate a BCrypt hash for the dashboard password.
 * Usage: java -cp CommunityBot.jar de.tebrox.util.communitybot.PasswordHasher yourpassword
 */
@Deprecated
public class PasswordHasher {

    public static void main(String[] args) {
        if (args.length == 0 || args[0].isBlank()) {
            System.err.println("Usage: java -cp CommunityBot-x.x.x.jar de.tebrox.util.communitybot.PasswordHasher <password>");
            System.err.println("Minimum password length: 8 characters");
            System.exit(1);
        }

        String password = args[0];
        if (password.length() < 8) {
            System.err.println("Password must be at least 8 characters long.");
            System.exit(1);
        }

        // BCrypt strength 12 – OWASP recommended minimum
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = encoder.encode(password);
        System.out.println(hash);
    }
}
