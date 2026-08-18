package com.app.model;

/**
 * Represents the execution state of a CI/CD pipeline stage or the overall pipeline.
 */
public enum PipelineStatus {
    PENDING("Stage is queued and waiting execution"),
    IN_PROGRESS("Stage is currently executing"),
    SUCCESS("Stage completed successfully"),
    FAILED("Stage encountered fatal errors"),
    SKIPPED("Stage was skipped due to condition or prior failure");

    private final String description;

    PipelineStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccessful() {
        return this == SUCCESS;
    }
}
