package com.syndicati.services.log;

import com.syndicati.models.log.AppEventLog;
import com.syndicati.models.log.data.AppEventLogRepository;
import com.syndicati.models.user.User;
import com.syndicati.utils.session.SessionManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserActivityLogger {

    private final AppEventLogRepository repository;
    private final LogBuffer logBuffer;

    public UserActivityLogger() {
        this(new AppEventLogRepository(), new LogBuffer());
    }

    public UserActivityLogger(AppEventLogRepository repository, LogBuffer logBuffer) {
        this.repository = repository;
        this.logBuffer = logBuffer;
    }

    public void log(String eventType, String entityType, Integer entityId, Map<String, Object> metadata, User user) {
        try {
            AppEventLog log = new AppEventLog();
            log.setEventType(eventType);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setMetadataJson(toJson(enrichMetadata(metadata)));

            User resolvedUser = user != null ? user : SessionManager.getInstance().getCurrentUser();
            if (resolvedUser != null && resolvedUser.getIdUser() != null && resolvedUser.getIdUser() > 0) {
                log.setUser(resolvedUser);
            }

            repository.create(log);
            logBuffer.push(log);
        } catch (Exception e) {
            System.out.println("[UserActivityLogger] Error: " + e.getMessage());
        }
    }

    public void logPageView(String route, String screenName, Map<String, Object> metadata) {
        Map<String, Object> data = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
        data.putIfAbsent("route", route);
        data.putIfAbsent("screen", screenName);
        log("PAGE_VIEW", "ROUTE", null, data, null);
    }

    public void logUiClick(String target, String text, Map<String, Object> metadata) {
        Map<String, Object> data = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
        data.putIfAbsent("target", target);
        data.putIfAbsent("text", text);
        log("UI_CLICK", "UI_ELEMENT", null, data, null);
    }

    public void logCrudAction(String action, String entityType, Integer entityId, Map<String, Object> metadata) {
        Map<String, Object> data = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
        data.putIfAbsent("action", action);
        log(action == null ? "CRUD" : action.toUpperCase(), entityType, entityId, data, null);
    }

    public List<AppEventLog> recentActivity(int limit) {
        return repository.findLatest(limit);
    }

    private Map<String, Object> enrichMetadata(Map<String, Object> metadata) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (metadata != null) {
            data.putAll(metadata);
        }

        data.putIfAbsent("host", hostName());
        data.putIfAbsent("os", System.getProperty("os.name"));
        data.putIfAbsent("os_version", System.getProperty("os.version"));
        data.putIfAbsent("java_version", System.getProperty("java.version"));
        data.putIfAbsent("user_name", System.getProperty("user.name"));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            data.putIfAbsent("session_user", currentUser.getEmailUser());
            data.putIfAbsent("session_role", currentUser.getRoleUser());
        }

        return data;
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return System.getProperty("user.name", "desktop");
        }
    }

    private String toJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(entry.getKey())).append('"').append(':').append(valueToJson(entry.getValue()));
        }
        json.append('}');
        return json.toString();
    }

    private String valueToJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return '"' + escape(String.valueOf(value)) + '"';
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}