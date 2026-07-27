package com.smartdesk.model;

public enum ProjectStatus {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    MAINTENANCE("Maintenance"),
    BLOCKED("Blocked");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
