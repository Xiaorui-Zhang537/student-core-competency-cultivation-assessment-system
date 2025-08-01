package com.noncore.assessment.security;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;

/**
 * Custom Principal object to hold user details within the Spring Security Context.
 */
public class UserPrincipal implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final Long id;
    private final String username;

    public UserPrincipal(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
} 