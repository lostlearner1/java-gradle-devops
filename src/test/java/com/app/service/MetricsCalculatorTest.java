package com.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MetricsCalculator Unit Tests")
class MetricsCalculatorTest {

    private MetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MetricsCalculator();
    }

    @Test
    @DisplayName("Should return 100% when all tests pass")
    void testCalculatePassRateAllPassed() {
        double passRate = calculator.calculatePassRate(10, 10);
        assertEquals(100.0, passRate, 0.001);
    }

    @Test
    @DisplayName("Should return 50% when half of tests pass")
    void testCalculatePassRateHalfPassed() {
        double passRate = calculator.calculatePassRate(5, 10);
        assertEquals(50.0, passRate, 0.001);
    }

    @Test
    @DisplayName("Should return 0.0 when total tests is 0")
    void testCalculatePassRateZeroTests() {
        double passRate = calculator.calculatePassRate(0, 0);
        assertEquals(0.0, passRate, 0.001);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when passed > total")
    void testCalculatePassRateInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculatePassRate(15, 10));
    }

    @ParameterizedTest(name = "Coverage: {0}%, Min: {1}%, Failed: {2}, Duration: {3}s, Max: {4}s -> Expected: {5}")
    @CsvSource({
        "85.0, 80.0, 0, 30, 60, true",
        "79.9, 80.0, 0, 30, 60, false",
        "95.0, 80.0, 1, 30, 60, false",
        "90.0, 80.0, 0, 70, 60, false",
        "80.0, 80.0, 0, 60, 60, true"
    })
    @DisplayName("Evaluate Quality Gate with various criteria")
    void testEvaluateQualityGate(
            double coverage,
            double minCoverage,
            int failedTests,
            long duration,
            long maxDuration,
            boolean expectedResult) {
        boolean result = calculator.evaluateQualityGate(coverage, minCoverage, failedTests, duration, maxDuration);
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Calculate Health Score bounded between 0 and 100")
    void testCalculateHealthScore() {
        double score = calculator.calculateHealthScore(100.0, 90.0, 10, 60);
        assertTrue(score >= 0.0 && score <= 100.0);
        assertTrue(score > 80.0, "Expected high health score for excellent metrics");
    }
}
