package dev.heinisch.menumaestro.scheduler;

import dev.heinisch.menumaestro.service.PendingRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingRegistrationCleanupScheduler {

    private final PendingRegistrationService pendingRegistrationService;

    /**
     * Runs every 15 minutes to clean up expired pending registrations
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void cleanupExpiredRegistrations() {
        log.debug("Running scheduled cleanup of expired pending registrations");
        pendingRegistrationService.cleanupExpiredRegistrations();
    }
}
