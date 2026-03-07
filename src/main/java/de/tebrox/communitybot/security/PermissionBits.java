package de.tebrox.communitybot.security;

public class PermissionBits {

    private PermissionBits() {}

    public static long add(long mask, DashboardPermission p) {
        return mask | p.bit;
    }

    public static long remove(long mask, DashboardPermission p) {
        return mask & ~p.bit;
    }

    public static boolean has(long mask, DashboardPermission p) {
        return (mask & p.bit) != 0;
    }
}
