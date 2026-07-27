package com.smartdesk.config;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;
import org.springframework.stereotype.Component;

@Component
public class OpenCVConfig {

    private static boolean loaded = false;

    @PostConstruct
    public void init() {
        if (!loaded) {
            try {
//                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                System.out.println("✅ OpenCV Loaded Successfully");
                loaded = true;
            } catch (UnsatisfiedLinkError e) {
                System.err.println("❌ OpenCV Failed To Load");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
