package com.mokakco.platform.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfigure {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
