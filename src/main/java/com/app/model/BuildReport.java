package com.app.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Encapsulates the results of a full CI/CD pipeline execution.
 */
public class BuildReport {
    private final String buildId;
    private final String projectName;
    private final String version;
    private final LocalDateTime timestamp;
    private final PipelineStatus status;
    private final long durationMillis;
    private final double codeCoverage;
    private final int totalTests;
    private final int passedTests;
    private final int failedTests;
    private final Map<String, PipelineStatus> stageResults;

    public BuildReport(
            String buildId,
            String projectName,
            String version,
            LocalDateTime timestamp,
            PipelineStatus status,
            long durationMillis,
            double codeCoverage,
            int totalTests,
            int passedTests,
            int failedTests,
            Map<String, PipelineStatus> stageResults) {
        this.buildId = buildId;
        this.projectName = projectName;
        this.version = version;
        this.timestamp = timestamp;
        this.status = status;
        this.durationMillis = durationMillis;
        this.codeCoverage = codeCoverage;
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.stageResults = stageResults != null ? Collections.unmodifiableMap(stageResults) : Collections.emptyMap();
    }

    public String getBuildId() {
        return buildId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public double getCodeCoverage() {
        return codeCoverage;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public int getPassedTests() {
        return passedTests;
    }

    public int getFailedTests() {
        return failedTests;
    }

    public Map<String, PipelineStatus> getStageResults() {
        return stageResults;
    }

    @Override
    public String toString() {
        return String.format(
            "BuildReport[id=%s, project=%s:%s, status=%s, duration=%dms, coverage=%.2f%%, tests=%d/%d passed]",
            buildId, projectName, version, status, durationMillis, codeCoverage, passedTests, totalTests
        );
    }
}
