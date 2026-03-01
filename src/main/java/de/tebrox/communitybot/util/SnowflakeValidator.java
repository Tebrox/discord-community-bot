package de.tebrox.communitybot.util;

/**
 * Validates Discord Snowflake IDs (numeric, 17-20 digits).
 */
public final class SnowflakeValidator {

    private SnowflakeValidator() {}

    public static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (!value.matches("\\d{17,20}")) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid Discord snowflake (17-20 digits), got: " + value);
        }
    }

    public static boolean isValid(String value) {
        return value != null && value.matches("\\d{17,20}");
    }
}
