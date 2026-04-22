package com.syndicati.controllers.log;

import com.syndicati.models.log.AppEventLog;
import com.syndicati.services.log.UserActivityLogger;
import java.util.List;
import java.util.Map;

public class ActivityLogController {

    private final UserActivityLogger logger;

    public ActivityLogController() {
        this.logger = new UserActivityLogger();
    }

    public void logPageView(String route, String screenName) {
        logger.logPageView(route, screenName, Map.of());
    }

    public void logPageView(String route, String screenName, Map<String, Object> metadata) {
        logger.logPageView(route, screenName, metadata);
    }

    public void logUiClick(String target, String text) {
        logger.logUiClick(target, text, Map.of());
    }

    public void logUiClick(String target, String text, Map<String, Object> metadata) {
        logger.logUiClick(target, text, metadata);
    }

    public void logCrudAction(String action, String entityType, Integer entityId, Map<String, Object> metadata) {
        logger.logCrudAction(action, entityType, entityId, metadata);
    }

    public List<AppEventLog> recentActivity(int limit) {
        return logger.recentActivity(limit);
    }
}