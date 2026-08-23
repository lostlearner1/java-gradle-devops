package com.app;

import com.app.config.AppConfig;
import com.app.model.BuildReport;
import com.app.model.PipelineStatus;
import com.app.service.DevOpsPipelineService;
import com.app.service.MetricsCalculator;

import java.util.Scanner;

/**
 * Main application entry point for the Java DevOps Pipeline CLI.
 */
public class Application {

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        MetricsCalculator calculator = new MetricsCalculator();
        DevOpsPipelineService pipelineService = new DevOpsPipelineService(config, calculator);

        printBanner(config);

        if (args.length == 0 || hasArg(args, "--demo")) {
            runAutomatedDemo(config, pipelineService, calculator);
        } else if (hasArg(args, "--interactive") || hasArg(args, "-i")) {
            runInteractiveMode(config, pipelineService, calculator);
        } else if (hasArg(args, "--custom")) {
            runCustomArgs(args, config, pipelineService, calculator);
        } else {
            printHelp();
        }
    }

    private static void printBanner(AppConfig config) {
        System.out.println("==========================================================================");
        System.out.println("   _     ___     __     ___  ____  _____     _____  _____  ____   ____    ");
        System.out.println("  | |   / _ \\   / /    / _ \\|  _ \\| ____|   |  ___||_   _||  _ \\ / ___|   ");
        System.out.println("  | |  / /_\\ \\ / /    / /_\\ \\ |_) |  _|     | |_     | |  | |_) | |       ");
        System.out.println("  | | / / _ \\ \\ / /  / / _ \\ \\  __/| |___    |  _|    | |  |  __/| |___    ");
        System.out.println("  |_|/_/   \\_\\_/    /_/   \\_\\_|   |_____|   |_|      |_|  |_|    \\____|   ");
        System.out.println("==========================================================================");
        System.out.printf(" Application: %s (v%s) | Target JDK: 21\n", config.getAppName(), config.getAppVersion());
        System.out.printf(" Quality Gate Threshold: Minimum %.1f%% Coverage | Timeout: %ds\n",
                config.getMinCodeCoverage(), config.getMaxBuildDurationSeconds());
        System.out.println("==========================================================================\n");
    }

    private static void runAutomatedDemo(AppConfig config, DevOpsPipelineService pipelineService, MetricsCalculator calculator) {
        System.out.println("▶ Starting Automated DevOps Pipeline Scenarios Showcase...\n");

        // Scenario 1: Golden Path (Success)
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" 🟢 SCENARIO 1: Golden Path (All Tests Pass, 92.5% Coverage)");
        System.out.println("--------------------------------------------------------------------------");
        BuildReport report1 = pipelineService.runPipeline("Payment-Gateway-Service", 92.5, 30, 30);
        printReportCard(report1, calculator, config);

        // Scenario 2: Quality Gate Failure (Low Code Coverage)
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println(" 🟡 SCENARIO 2: Quality Gate Violation (Coverage 64.0% < Required 80.0%)");
        System.out.println("--------------------------------------------------------------------------");
        BuildReport report2 = pipelineService.runPipeline("User-Authentication-Service", 64.0, 45, 45);
        printReportCard(report2, calculator, config);

        // Scenario 3: Test Failure (Broken Build)
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println(" 🔴 SCENARIO 3: Test Execution Failure (5 Unit Tests Failed)");
        System.out.println("--------------------------------------------------------------------------");
        BuildReport report3 = pipelineService.runPipeline("Order-Processing-Service", 88.0, 40, 35);
        printReportCard(report3, calculator, config);

        System.out.println("\n==========================================================================");
        System.out.println(" ✨ DEMO COMPLETE!");
        System.out.println(" You can test custom builds with: .\\gradlew.bat run --args=\"--interactive\"");
        System.out.println("==========================================================================");
    }

    private static void runInteractiveMode(AppConfig config, DevOpsPipelineService pipelineService, MetricsCalculator calculator) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("🛠️ Interactive DevOps Pipeline Simulator");
            System.out.print("Enter Project Name [e.g. MyService]: ");
            String projectName = scanner.nextLine().trim();
            if (projectName.isEmpty()) projectName = "Custom-DevOps-App";

            System.out.print("Enter Total Test Count [e.g. 50]: ");
            int totalTests = parseInt(scanner.nextLine().trim(), 50);

            System.out.print("Enter Passed Test Count [e.g. 50]: ");
            int passedTests = parseInt(scanner.nextLine().trim(), totalTests);

            System.out.print("Enter Estimated Code Coverage % [e.g. 85.5]: ");
            double coverage = parseDouble(scanner.nextLine().trim(), 85.5);

            System.out.println("\nExecuting Pipeline with your inputs...\n");
            BuildReport report = pipelineService.runPipeline(projectName, coverage, totalTests, passedTests);
            printReportCard(report, calculator, config);
        }
    }

    private static void runCustomArgs(String[] args, AppConfig config, DevOpsPipelineService pipelineService, MetricsCalculator calculator) {
        String projectName = getArgValue(args, "--project", "Custom-App");
        double coverage = parseDouble(getArgValue(args, "--coverage", "85.0"), 85.0);
        int totalTests = parseInt(getArgValue(args, "--tests", "20"), 20);
        int passedTests = parseInt(getArgValue(args, "--passed", "20"), totalTests);

        BuildReport report = pipelineService.runPipeline(projectName, coverage, totalTests, passedTests);
        printReportCard(report, calculator, config);
    }

    private static void printReportCard(BuildReport report, MetricsCalculator calculator, AppConfig config) {
        double passRate = calculator.calculatePassRate(report.getPassedTests(), report.getTotalTests());
        double healthScore = calculator.calculateHealthScore(
                passRate,
                report.getCodeCoverage(),
                report.getDurationMillis() / 1000,
                config.getMaxBuildDurationSeconds()
        );

        String statusBadge = report.getStatus() == PipelineStatus.SUCCESS ? "[SUCCESS] ✔" : "[FAILED] ✘";

        System.out.println("\n┌──────────────────────── PIPELINE EXECUTION SUMMARY ────────────────────────┐");
        System.out.printf("│ Build ID:       %-58s │\n", report.getBuildId());
        System.out.printf("│ Project:        %-58s │\n", report.getProjectName() + " (v" + report.getVersion() + ")");
        System.out.printf("│ Overall Status: %-58s │\n", statusBadge);
        System.out.printf("│ Tests Passed:   %-58s │\n", String.format("%d / %d (%.1f%%)", report.getPassedTests(), report.getTotalTests(), passRate));
        System.out.printf("│ Code Coverage:  %-58s │\n", String.format("%.2f%% (Min Required: %.1f%%)", report.getCodeCoverage(), config.getMinCodeCoverage()));
        System.out.printf("│ Health Score:   %-58s │\n", String.format("%.1f / 100.0", healthScore));
        System.out.println("├──────────────────────── STAGE-BY-STAGE BREAKDOWN ──────────────────────────┤");
        report.getStageResults().forEach((stage, status) -> {
            String icon = switch (status) {
                case SUCCESS -> "✔ PASS";
                case FAILED -> "✘ FAIL";
                case SKIPPED -> "↷ SKIP";
                default -> "⏳ PEND";
            };
            System.out.printf("│   • %-18s : %-50s │\n", stage, icon + " (" + status.getDescription() + ")");
        });
        System.out.println("└────────────────────────────────────────────────────────────────────────────┘\n");
    }

    private static boolean hasArg(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(flag)) return true;
        }
        return false;
    }

    private static String getArgValue(String[] args, String flag, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase(flag)) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }

    private static int parseInt(String str, int fallback) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double parseDouble(String str, double fallback) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static void printHelp() {
        System.out.println("Usage options:");
        System.out.println("  .\\gradlew.bat run                           Run automated 3-scenario demo");
        System.out.println("  .\\gradlew.bat run --args=\"--interactive\"    Run interactive simulator");
        System.out.println("  .\\gradlew.bat run --args=\"--custom --project MyService --coverage 90.0 --tests 50 --passed 49\"");
    }
}
