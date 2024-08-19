package com.example.pinokkio.common.type;

public enum Gender {
    MALE, FEMALE;

    public static Gender fromString(String gender) {
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gender: " + gender);
        }
    }
}
