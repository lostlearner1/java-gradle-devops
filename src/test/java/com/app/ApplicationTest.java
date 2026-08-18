package com.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Application Integration Test")
class ApplicationTest {

    @Test
    @DisplayName("Application main method should execute without unhandled exceptions")
    void testMainMethodExecution() {
        assertDoesNotThrow(() -> {
            Application.main(new String[]{});
        });
    }
}
