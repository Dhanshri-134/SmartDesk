package com.smartdesk.config;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;
import org.springframework.stereotype.Component;

@Component
public class OpenCvBootstrap {

    private static boolean loaded;

    @PostConstruct
    public void load() {
        if (loaded) {
            return;
        }

        OpenCV.loadLocally();
        loaded = true;
    }
}
