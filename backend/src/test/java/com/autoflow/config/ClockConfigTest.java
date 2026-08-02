package com.autoflow.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClockConfigTest {

    @Test
    void deveFornecerClockUtc() {
        Clock clock = new ClockConfig().clock();

        assertEquals(Clock.systemUTC().getZone(), clock.getZone());
    }
}
