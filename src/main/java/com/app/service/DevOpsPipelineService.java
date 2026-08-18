package com.app.service;

import com.app.config.AppConfig;
import com.app.model.BuildReport;
import com.app.model.PipelineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates DevOps pipeline execution stages: Checkout, Compile, Test, Quality Gate, Package, and Deploy.
 */
public class DevOpsPipelineService {
    private static final Logger logger = LoggerFactory.getLogger(DevOpsPipelineService.class);

    private final AppConfig appConfig;
    private final MetricsCalculator metricsCalculator;

    public DevOpsPipelineService(AppConfig appConfig, MetricsCalculator metricsCalculator) {
        this.appConfig = appConfig;
        this.metricsCalculator = metricsCalculator;
    }

    public BuildReport runPipeline(String projectName, double estimatedCoverage, int totalTests, int passedTests) {
        String buildId = "BUILD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long startTime = System.currentTimeMillis();
        Map<String, PipelineStatus> stageResults = new LinkedHashMap<>();

        logger.info("==================================================================");
        logger.info("Starting CI/CD Pipeline for '{}' [Build ID: {}]", projectName, buildId);
        logger.info("==================================================================");

        int failedTests = totalTests - passedTests;
        boolean pipelineFailed = false;

        // Stage 1: Checkout
        stageResults.put("Checkout", executeStage("Checkout", () -> true));

        // Stage 2: Compile
        stageResults.put("Compile", executeStage("Compile", () -> true));

        // Stage 3: Test
        boolean testsPassed = (failedTests == 0);
        stageResults.put("Test", executeStage("Test", () -> testsPassed));
        if (!testsPassed) {
            pipelineFailed = true;
        }

        // Stage 4: Quality Gate
        if (!pipelineFailed) {
            long currentDurationSec = (System.currentTimeMillis() - startTime) / 1000;
            boolean qgPassed = metricsCalculator.evaluateQualityGate(
                    estimatedCoverage,
                    appConfig.getMinCodeCoverage(),
                    failedTests,
                    currentDurationSec,
                    appConfig.getMaxBuildDurationSeconds()
            );
            stageResults.put("QualityGate", executeStage("QualityGate", () -> qgPassed));
            if (!qgPassed) {
                pipelineFailed = true;
                logger.warn("Quality Gate check failed! Coverage: {}% (Required: {}%), Failed Tests: {}",
                        estimatedCoverage, appConfig.getMinCodeCoverage(), failedTests);
            }
        } else {
            stageResults.put("QualityGate", PipelineStatus.SKIPPED);
        }

        // Stage 5: Package (Fat JAR)
        if (!pipelineFailed) {
            stageResults.put("Package", executeStage("Package", () -> true));
        } else {
            stageResults.put("Package", PipelineStatus.SKIPPED);
        }

        // Stage 6: Deploy
        if (!pipelineFailed && appConfig.isAutoDeployEnabled()) {
            stageResults.put("Deploy", executeStage("Deploy", () -> true));
        } else {
            stageResults.put("Deploy", pipelineFailed ? PipelineStatus.SKIPPED : PipelineStatus.PENDING);
        }

        long totalDurationMillis = System.currentTimeMillis() - startTime;
        PipelineStatus finalStatus = pipelineFailed ? PipelineStatus.FAILED : PipelineStatus.SUCCESS;

        BuildReport report = new BuildReport(
                buildId,
                projectName,
                appConfig.getAppVersion(),
                LocalDateTime.now(),
                finalStatus,
                totalDurationMillis,
                estimatedCoverage,
                totalTests,
                passedTests,
                failedTests,
                stageResults
        );

        logger.info("------------------------------------------------------------------");
        logger.info("Pipeline Finished with Status: {}", finalStatus);
        logger.info("Report Summary: {}", report);
        logger.info("==================================================================");

        return report;
    }

    private PipelineStatus executeStage(String stageName, StageAction action) {
        logger.info(">> Executing Stage: [{}]...", stageName);
        try {
            boolean success = action.execute();
            if (success) {
                logger.info(">> Stage [{}] COMPLETED SUCCESSFULLY.", stageName);
                return PipelineStatus.SUCCESS;
            } else {
                logger.error(">> Stage [{}] FAILED.", stageName);
                return PipelineStatus.FAILED;
            }
        } catch (Exception e) {
            logger.error(">> Stage [{}] encountered an uncaught error: {}", stageName, e.getMessage(), e);
            return PipelineStatus.FAILED;
        }
    }

    @FunctionalInterface
    public interface StageAction {
        boolean execute() throws Exception;
    }
}
