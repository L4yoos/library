package com.library.userservice.model.enums;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum Role implements GrantedAuthority {
    ROLE_USER("Standard User"),
    ROLE_EDITOR("Editor"),
    ROLE_ADMIN("Admin");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}