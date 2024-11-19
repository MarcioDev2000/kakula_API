package com.example.kakula.enums;

public enum Role {
    LOCADOR,
    ADMIN,
    LOCATARIO;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}