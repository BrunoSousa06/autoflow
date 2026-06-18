package com.autoflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class AutoflowApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(AutoflowApplication.class, args);
    }

    private static void loadDotenv() {
        try (InputStream is = AutoflowApplication.class.getResourceAsStream("/.env")) {
            if (is == null) return;
            new BufferedReader(new InputStreamReader(is)).lines().forEach(line -> {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) return;
                int eq = line.indexOf('=');
                if (eq < 1) return;
                String key = line.substring(0, eq).strip();
                String value = line.substring(eq + 1).strip();
                if (System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            });
        } catch (Exception ignored) {}
    }

}