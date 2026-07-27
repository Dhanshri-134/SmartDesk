package com.smartdesk.model;

public enum UserRole {
    SUPERADMIN("Superadmin"),
    COMPANY_ADMIN("Company Admin"),
    EMPLOYEE("Employee");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
