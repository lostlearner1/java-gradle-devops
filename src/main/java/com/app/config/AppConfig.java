package com.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and provides strongly-typed access to application configuration settings.
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String DEFAULT_PROPERTIES_FILE = "application.properties";

    private final Properties properties = new Properties();

    public AppConfig() {
        this(DEFAULT_PROPERTIES_FILE);
    }

    public AppConfig(String propertiesFile) {
        loadProperties(propertiesFile);
    }

    private void loadProperties(String propertiesFile) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                logger.warn("Property file '{}' not found in classpath. Using default values.", propertiesFile);
                return;
            }
            properties.load(input);
            logger.info("Successfully loaded configuration from '{}'", propertiesFile);
        } catch (IOException ex) {
            logger.error("Error reading configuration from '{}': {}", propertiesFile, ex.getMessage(), ex);
        }
    }

    public String getAppName() {
        return getProperty("app.name", "Java Gradle DevOps Application");
    }

    public String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }

    public String getEnvironment() {
        return getProperty("app.environment", "production");
    }

    public double getMinCodeCoverage() {
        String val = getProperty("pipeline.qualityGate.minCodeCoverage", "80.0");
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            logger.warn("Invalid minCodeCoverage value '{}', falling back to 80.0", val);
            return 80.0;
        }
    }

    public long getMaxBuildDurationSeconds() {
        String val = getProperty("pipeline.qualityGate.maxBuildDurationSeconds", "120");
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            logger.warn("Invalid maxBuildDurationSeconds value '{}', falling back to 120", val);
            return 120L;
        }
    }

    public boolean isAutoDeployEnabled() {
        return Boolean.parseBoolean(getProperty("pipeline.autoDeploy.enabled", "true"));
    }

    public String getProperty(String key, String defaultValue) {
        // System property overrides environment variable, which overrides file property
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        String envKey = key.replace('.', '_').toUpperCase();
        String envProp = System.getenv(envKey);
        if (envProp != null && !envProp.isBlank()) {
            return envProp;
        }
        return properties.getProperty(key, defaultValue);
    }
}
