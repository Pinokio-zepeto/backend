package com.example.pinokkio.config.jwt;

public enum Role {
    P("ROLE_POS"),
    K("ROLE_KIOSK"),
    T("ROLE_TELLER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role fromValue(String value) {
        for (Role role : values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + value);
    }
}