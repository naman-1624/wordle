package com.wordle.wordle.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {}

    public static JwtPrincipal get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal principal) {
            return principal;
        }
        throw new SecurityException("No authenticated user");
    }

    public static boolean isAdmin() {
        JwtPrincipal p = get();
        return p != null && "ADMIN".equals(p.role());
    }
}