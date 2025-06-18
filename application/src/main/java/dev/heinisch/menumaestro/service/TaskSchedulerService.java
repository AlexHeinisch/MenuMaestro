package dev.heinisch.menumaestro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class TaskSchedulerService {

    private final ImageService imageService;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.DAYS)
    public void imageCleanupTask() {
        imageService.cleanupImagesTask();
    }

}
