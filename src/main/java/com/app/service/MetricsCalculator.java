package com.app.service;

/**
 * Provides calculations and health score evaluations for CI/CD pipeline builds.
 */
public class MetricsCalculator {

    /**
     * Calculates the test pass rate as a percentage (0.0 to 100.0).
     */
    public double calculatePassRate(int passedTests, int totalTests) {
        if (totalTests <= 0) {
            return 0.0;
        }
        if (passedTests < 0 || passedTests > totalTests) {
            throw new IllegalArgumentException("Invalid test count arguments: passed=" + passedTests + ", total=" + totalTests);
        }
        return (double) passedTests / totalTests * 100.0;
    }

    /**
     * Evaluates whether the build satisfies quality gate criteria.
     */
    public boolean evaluateQualityGate(
            double actualCoverage,
            double minCoverageRequired,
            int failedTests,
            long durationSeconds,
            long maxDurationSeconds) {
        if (failedTests > 0) {
            return false;
        }
        if (actualCoverage < minCoverageRequired) {
            return false;
        }
        if (durationSeconds > maxDurationSeconds) {
            return false;
        }
        return true;
    }

    /**
     * Calculates an overall health score (0 to 100) based on pass rate, coverage, and build speed.
     */
    public double calculateHealthScore(double passRate, double coverage, long durationSeconds, long maxDurationSeconds) {
        double passWeight = 0.50;
        double coverageWeight = 0.35;
        double speedWeight = 0.15;

        double speedScore = 100.0;
        if (maxDurationSeconds > 0 && durationSeconds > 0) {
            double ratio = (double) durationSeconds / maxDurationSeconds;
            speedScore = Math.max(0.0, 100.0 * (1.0 - Math.min(1.0, ratio * 0.5)));
        }

        double score = (passRate * passWeight) + (coverage * coverageWeight) + (speedScore * speedWeight);
        return Math.min(100.0, Math.max(0.0, score));
    }
}
