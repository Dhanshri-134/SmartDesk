package com.smartdesk.model;

public enum SubscriptionPlan {
    PER_EMPLOYEE("Per Employee", "Flexible billing based on active employees"),
    YEARLY("Yearly Plan", "Fixed annual subscription for one year"),
    TWO_YEAR("2 Year Plan", "Discounted long-term contract for two years");

    private final String label;
    private final String description;

    SubscriptionPlan(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
