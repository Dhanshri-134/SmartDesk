package com.smartdesk.service;

import com.smartdesk.model.ActivityLog;
import com.smartdesk.repository.ActivityLogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ActivityTracker {

    private final ActivityLogRepository repo;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("tracker-"));

    public ActivityTracker(ActivityLogRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void start() {
        // Stub tracker that logs a dummy app activity every minute
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ActivityLog log = new ActivityLog();
                log.setUsername("demo");
                log.setAppName("DemoEditor");
                log.setActiveSeconds(60);
                repo.save(log);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 60, TimeUnit.SECONDS);
    }
}
