package de.tebrox.rolesbot.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CLI utility to generate a BCrypt hash for the dashboard password.
 * Usage: java -cp rolesbot.jar de.tebrox.rolesbot.util.PasswordHasher yourpassword
 */
public class PasswordHasher {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: PasswordHasher <password>");
            System.exit(1);
        }
        String hash = new BCryptPasswordEncoder(12).encode(args[0]);
        System.out.println("BCrypt hash (paste into config.yml auth.passwordHashBcrypt):");
        System.out.println(hash);
    }
}
