package com.app.service;

import com.app.config.AppConfig;
import com.app.model.BuildReport;
import com.app.model.PipelineStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DevOpsPipelineService Unit Tests")
class DevOpsPipelineServiceTest {

    private AppConfig appConfig;
    private MetricsCalculator metricsCalculator;
    private DevOpsPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig("test-config.properties");
        metricsCalculator = new MetricsCalculator();
        pipelineService = new DevOpsPipelineService(appConfig, metricsCalculator);
    }

    @Test
    @DisplayName("Pipeline should succeed when all tests pass and coverage is above threshold")
    void testSuccessfulPipelineRun() {
        BuildReport report = pipelineService.runPipeline("DemoApp", 85.0, 20, 20);

        assertNotNull(report);
        assertEquals(PipelineStatus.SUCCESS, report.getStatus());
        assertEquals(0, report.getFailedTests());
        assertEquals(20, report.getPassedTests());
        assertEquals(20, report.getTotalTests());
        assertTrue(report.getStageResults().containsKey("Checkout"));
        assertTrue(report.getStageResults().containsKey("Compile"));
        assertTrue(report.getStageResults().containsKey("Test"));
        assertTrue(report.getStageResults().containsKey("QualityGate"));
        assertTrue(report.getStageResults().containsKey("Package"));
        assertEquals(PipelineStatus.SUCCESS, report.getStageResults().get("QualityGate"));
    }

    @Test
    @DisplayName("Pipeline should fail if tests fail")
    void testFailedTestsPipelineRun() {
        BuildReport report = pipelineService.runPipeline("DemoApp", 85.0, 20, 18);

        assertNotNull(report);
        assertEquals(PipelineStatus.FAILED, report.getStatus());
        assertEquals(2, report.getFailedTests());
        assertEquals(PipelineStatus.FAILED, report.getStageResults().get("Test"));
        assertEquals(PipelineStatus.SKIPPED, report.getStageResults().get("QualityGate"));
        assertEquals(PipelineStatus.SKIPPED, report.getStageResults().get("Package"));
    }

    @Test
    @DisplayName("Pipeline should fail if coverage is below quality gate")
    void testQualityGateFailurePipelineRun() {
        // test-config.properties has minCodeCoverage = 75.0
        BuildReport report = pipelineService.runPipeline("DemoApp", 60.0, 20, 20);

        assertNotNull(report);
        assertEquals(PipelineStatus.FAILED, report.getStatus());
        assertEquals(PipelineStatus.SUCCESS, report.getStageResults().get("Test"));
        assertEquals(PipelineStatus.FAILED, report.getStageResults().get("QualityGate"));
        assertEquals(PipelineStatus.SKIPPED, report.getStageResults().get("Package"));
    }
}
