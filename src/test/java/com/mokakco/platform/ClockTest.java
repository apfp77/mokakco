package com.mokakco.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ClockTest {
    @Autowired
    private Clock clock;

    @Test
    public void testClockBean() {
        assertNotNull(clock);
        assertEquals("Asia/Seoul", clock.getZone().getId());
    }
}
