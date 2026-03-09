package de.tebrox.communitybot.core.security;

public enum DashboardPermission {
    VIEW_GUILD      (1L << 0),
    MANAGE_ROLES    (1L << 1),
    MANAGE_WELCOME  (1L << 2),
    VIEW_LOGS       (1L << 3),
    ADMIN_GUILD     (1L << 4),
    MANAGE_TICKETS  (1L << 5),
    MANAGE_MESSAGES (1L << 6);

    public final long bit;
    DashboardPermission(long bit) {
        this.bit = bit;
    }

}
