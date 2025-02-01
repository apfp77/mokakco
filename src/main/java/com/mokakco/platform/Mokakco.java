package com.mokakco.platform;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Mokakco extends ListenerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(Mokakco.class, args);
    }
}