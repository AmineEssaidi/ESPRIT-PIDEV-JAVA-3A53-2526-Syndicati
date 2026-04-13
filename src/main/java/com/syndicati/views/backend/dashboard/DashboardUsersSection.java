package com.syndicati.views.backend.dashboard;

import com.syndicati.models.user.Onboarding;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

final class DashboardUsersSection {

    private DashboardUsersSection() {
    }

    static VBox build(DashboardView view) {
        VBox s = view.moduleShell("Users", "\uD83D\uDC65");
        VBox body = new VBox(16);
        HBox sub = view.moduleModeSwitcher(new String[]{"Users", "Profile", "Onboarding"}, "Users", key -> {
            body.getChildren().setAll(
                "Profile".equals(key) ? usersProfilePane(view) :
                "Onboarding".equals(key) ? usersOnboardingPane(view) :
                usersTablePane(view)
            );
        });
        body.getChildren().add(usersTablePane(view));
        s.getChildren().addAll(sub, body);
        return s;
    }

    private static VBox usersTablePane(DashboardView view) {
        VBox wrap = new VBox(16);
        List<User> users = view.dashboardAdminService().users();

        int registered = users.size();
        int verified = 0;
        int pending = 0;
        int active7Day = 0;
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        for (User user : users) {
            if (user.isVerified()) {
                verified++;
            } else {
                pending++;
            }

            LocalDateTime updatedAt = user.getUpdatedAt();
            if (updatedAt != null && updatedAt.isAfter(sevenDaysAgo)) {
                active7Day++;
            }
        }

        HBox stats = new HBox(16);
        stats.setFillHeight(true);
        view.addStatCards(stats,
            new String[]{"USR", "VER", "PEN", "ACT"},
            new String[]{"Registered", "Verified", "Pending", "7-Day Active"},
            new String[]{String.valueOf(registered), String.valueOf(verified), String.valueOf(pending), String.valueOf(active7Day)},
            new String[]{"#60a5fa", "#34d399", "#fbbf24", "#a78bfa"}
        );

        List<String[]> baseRows = new ArrayList<>();
        for (User u : users) {
            String fullName = ((u.getFirstName() == null ? "" : u.getFirstName()) + " " + (u.getLastName() == null ? "" : u.getLastName())).trim();
            if (fullName.isEmpty()) {
                fullName = "User #" + (u.getIdUser() == null ? "-" : u.getIdUser());
            }
            String status = u.isDisabled() ? "Disabled" : (u.isVerified() ? "Active" : "Pending");

            baseRows.add(new String[]{
                fullName,
                view.safe(u.getEmailUser()),
                view.safe(u.getRoleUser()),
                u.isVerified() ? "Yes" : "No",
                status
            });
        }

        // Users section owns query controls; shared card owns row pagination.
        DashboardTableQueryEngine.QueryState queryState = new DashboardTableQueryEngine.QueryState(1_000_000);

        TextField searchField = new TextField();
        searchField.setPromptText("Search users by name, email, role or status...");
        searchField.setPrefWidth(280);
        searchField.setFont(Font.font(view.lightFont(), FontWeight.NORMAL, 12));
        searchField.setStyle(
            "-fx-background-color:" + (view.isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.04)") + ";" +
            "-fx-border-color:" + (view.isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:10px;" +
            "-fx-background-radius:10px;" +
            "-fx-text-fill:" + (view.isDark() ? "white" : "#111827") + ";" +
            "-fx-prompt-text-fill:" + (view.isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.45)") + ";"
        );

        Button sortPill = view.pillAction("Order: A-Z", false);
        HBox primaryControls = new HBox(8, searchField, sortPill);
        primaryControls.setAlignment(Pos.CENTER_LEFT);

        String[][] filters = new String[][]{
            {"all", "All"},
            {"verified", "Verified"},
            {"pending", "Pending"},
            {"active", "Active"},
            {"disabled", "Disabled"}
        };

        FlowPane filterRow = new FlowPane();
        filterRow.setHgap(6);
        filterRow.setVgap(6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        Map<String, Button> filterButtons = new LinkedHashMap<>();

        VBox tableHost = new VBox();
        VBox controlsWrap = new VBox(6, primaryControls, filterRow);
        controlsWrap.setAlignment(Pos.CENTER_LEFT);

        for (String[] filter : filters) {
            String key = filter[0];
            Button b = view.pillAction(filter[1], "all".equals(key));
            b.setOnAction(e -> {
                queryState.filterKey = key;
                queryState.page = 1;
                filterButtons.forEach((k, btn) -> styleQueryPill(view, btn, k.equals(queryState.filterKey)));
                renderUsersTable(view, baseRows, queryState, tableHost, sortPill);
            });
            filterButtons.put(key, b);
            filterRow.getChildren().add(b);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            queryState.searchTerm = newVal == null ? "" : newVal;
            queryState.page = 1;
            renderUsersTable(view, baseRows, queryState, tableHost, sortPill);
        });

        sortPill.setOnAction(e -> {
            queryState.ascending = !queryState.ascending;
            renderUsersTable(view, baseRows, queryState, tableHost, sortPill);
        });

        filterButtons.forEach((k, btn) -> styleQueryPill(view, btn, "all".equals(k)));
        renderUsersTable(view, baseRows, queryState, tableHost, sortPill);

        wrap.getChildren().addAll(stats, controlsWrap, tableHost);
        return wrap;
    }

    private static void renderUsersTable(
        DashboardView view,
        List<String[]> baseRows,
        DashboardTableQueryEngine.QueryState queryState,
        VBox tableHost,
        Button sortPill
    ) {
        DashboardTableQueryEngine.QueryResult result = DashboardTableQueryEngine.apply(
            baseRows,
            queryState,
            (row, filterKey) -> {
                switch (filterKey) {
                    case "verified": return "Yes".equalsIgnoreCase(row[3]);
                    case "pending":  return "Pending".equalsIgnoreCase(row[4]);
                    case "active":   return "Active".equalsIgnoreCase(row[4]);
                    case "disabled": return "Disabled".equalsIgnoreCase(row[4]);
                    default:          return true;
                }
            },
            0
        );

        String[][] visibleRows;
        if (result.pageRows.isEmpty()) {
            visibleRows = new String[][]{{"No users found", "-", "-", "-", "-"}};
        } else {
            visibleRows = result.pageRows.toArray(new String[0][]);
        }

        tableHost.getChildren().setAll(view.dataTableWithCrud(
            "Users Table",
            "User",
            new String[]{"Name", "Email", "Role", "Verified", "Status"},
            visibleRows,
            true,
            true
        ));

        styleQueryPill(view, sortPill, queryState.ascending);
        sortPill.setText(queryState.ascending ? "Order: A-Z" : "Order: Z-A");
    }

    private static void styleQueryPill(DashboardView view, Button b, boolean active) {
        if (active) {
            b.setStyle(
                "-fx-background-color:" + view.accentRgba(0.24) + ";" +
                "-fx-border-color:" + view.accentRgba(0.34) + ";" +
                "-fx-border-width:1;" +
                "-fx-background-radius:100px;" +
                "-fx-border-radius:100px;" +
                "-fx-text-fill:white;" +
                "-fx-cursor:hand;"
            );
        } else {
            b.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:" + (view.isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.20)") + ";" +
                "-fx-border-width:1;" +
                "-fx-background-radius:100px;" +
                "-fx-border-radius:100px;" +
                "-fx-text-fill:" + (view.isDark() ? "rgba(255,255,255,0.80)" : "rgba(15,23,42,0.86)") + ";" +
                "-fx-cursor:hand;"
            );
        }
    }

    private static VBox usersProfilePane(DashboardView view) {
        VBox wrap = new VBox(16);
        List<Profile> profiles = view.dashboardAdminService().profiles();

        HBox stats = new HBox(16);
        stats.setFillHeight(true);
        int totalProfiles = profiles.size();
        int withAvatar = (int) profiles.stream().filter(p -> p.getAvatar() != null && !p.getAvatar().isEmpty()).count();
        int withDescription = (int) profiles.stream().filter(p -> p.getDescriptionProfile() != null && !p.getDescriptionProfile().isEmpty()).count();

        view.addStatCards(stats,
            new String[]{"PFL", "AVT", "BIO", "LOC"},
            new String[]{"Total Profiles", "With Avatar", "With Bio", "Locales Set"},
            new String[]{String.valueOf(totalProfiles), String.valueOf(withAvatar), String.valueOf(withDescription), "-"},
            new String[]{"#60a5fa", "#34d399", "#a78bfa", "#fbbf24"}
        );

        List<String[]> baseRows = new ArrayList<>();
        for (Profile p : profiles) {
            baseRows.add(new String[]{
                String.valueOf(p.getIdProfile()),
                String.valueOf(p.getUserId()),
                view.safe(p.getLocale()),
                p.getTheme() == null ? "-" : String.valueOf(p.getTheme()),
                p.getTimezone() == null ? "-" : String.valueOf(p.getTimezone()),
                view.safe(p.getAvatar())
            });
        }

        DashboardTableQueryEngine.QueryState queryState = new DashboardTableQueryEngine.QueryState(1_000_000);

        TextField searchField = new TextField();
        searchField.setPromptText("Search profiles by id, user, locale, theme or timezone...");
        searchField.setPrefWidth(280);
        searchField.setFont(Font.font(view.lightFont(), FontWeight.NORMAL, 12));
        searchField.setStyle(
            "-fx-background-color:" + (view.isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.04)") + ";" +
            "-fx-border-color:" + (view.isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:10px;" +
            "-fx-background-radius:10px;" +
            "-fx-text-fill:" + (view.isDark() ? "white" : "#111827") + ";" +
            "-fx-prompt-text-fill:" + (view.isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.45)") + ";"
        );

        Button sortPill = view.pillAction("Order: ID Asc", false);
        HBox primaryControls = new HBox(8, searchField, sortPill);
        primaryControls.setAlignment(Pos.CENTER_LEFT);

        String[][] filters = new String[][]{
            {"all", "All"},
            {"locale_set", "Locale Set"},
            {"theme_set", "Theme Set"},
            {"timezone_set", "Timezone Set"},
            {"with_avatar", "With Avatar"}
        };

        FlowPane filterRow = new FlowPane();
        filterRow.setHgap(6);
        filterRow.setVgap(6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        Map<String, Button> filterButtons = new LinkedHashMap<>();

        VBox tableHost = new VBox();
        VBox controlsWrap = new VBox(6, primaryControls, filterRow);
        controlsWrap.setAlignment(Pos.CENTER_LEFT);

        for (String[] filter : filters) {
            String key = filter[0];
            Button b = view.pillAction(filter[1], "all".equals(key));
            b.setOnAction(e -> {
                queryState.filterKey = key;
                queryState.page = 1;
                filterButtons.forEach((k, btn) -> styleQueryPill(view, btn, k.equals(queryState.filterKey)));
                renderProfilesTable(view, baseRows, queryState, tableHost, sortPill);
            });
            filterButtons.put(key, b);
            filterRow.getChildren().add(b);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            queryState.searchTerm = newVal == null ? "" : newVal;
            queryState.page = 1;
            renderProfilesTable(view, baseRows, queryState, tableHost, sortPill);
        });

        sortPill.setOnAction(e -> {
            queryState.ascending = !queryState.ascending;
            renderProfilesTable(view, baseRows, queryState, tableHost, sortPill);
        });

        filterButtons.forEach((k, btn) -> styleQueryPill(view, btn, "all".equals(k)));
        renderProfilesTable(view, baseRows, queryState, tableHost, sortPill);

        wrap.getChildren().addAll(stats, controlsWrap, tableHost);
        return wrap;
    }

    private static VBox usersOnboardingPane(DashboardView view) {
        VBox wrap = new VBox(14);
        List<Onboarding> onboardings = view.dashboardAdminService().onboardings();

        int total = onboardings.size();
        int completed = (int) onboardings.stream().filter(Onboarding::isCompleted).count();
        int inProgress = total - completed;
        int localeSet = (int) onboardings.stream().filter(o -> o.getSelectedLocale() != null && !o.getSelectedLocale().isBlank()).count();

        HBox stats = new HBox(16);
        stats.setFillHeight(true);
        view.addStatCards(stats,
            new String[]{"ONB", "CMP", "RUN", "LOC"},
            new String[]{"Total Onboarding", "Completed", "In Progress", "Locale Set"},
            new String[]{String.valueOf(total), String.valueOf(completed), String.valueOf(inProgress), String.valueOf(localeSet)},
            new String[]{"#60a5fa", "#34d399", "#fbbf24", "#a78bfa"}
        );

        List<String[]> baseRows = new ArrayList<>();
        for (Onboarding o : onboardings) {
            baseRows.add(new String[]{
                String.valueOf(o.getIdOnboarding()),
                String.valueOf(o.getUserId()),
                String.valueOf(o.getStep()),
                o.isCompleted() ? "Yes" : "No",
                view.safe(o.getSelectedLocale()),
                view.safe(o.getSelectedTheme()),
                view.formatDateTime(o.getUpdatedAt())
            });
        }

        DashboardTableQueryEngine.QueryState queryState = new DashboardTableQueryEngine.QueryState(1_000_000);

        TextField searchField = new TextField();
        searchField.setPromptText("Search onboarding by user, locale, theme, step or status...");
        searchField.setPrefWidth(300);
        searchField.setFont(Font.font(view.lightFont(), FontWeight.NORMAL, 12));
        searchField.setStyle(
            "-fx-background-color:" + (view.isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.04)") + ";" +
            "-fx-border-color:" + (view.isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:10px;" +
            "-fx-background-radius:10px;" +
            "-fx-text-fill:" + (view.isDark() ? "white" : "#111827") + ";" +
            "-fx-prompt-text-fill:" + (view.isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.45)") + ";"
        );

        Button sortPill = view.pillAction("Order: ID Asc", false);
        HBox primaryControls = new HBox(8, searchField, sortPill);
        primaryControls.setAlignment(Pos.CENTER_LEFT);

        String[][] filters = new String[][]{
            {"all", "All"},
            {"completed", "Completed"},
            {"in_progress", "In Progress"},
            {"theme_set", "Theme Set"},
            {"locale_set", "Locale Set"}
        };

        FlowPane filterRow = new FlowPane();
        filterRow.setHgap(6);
        filterRow.setVgap(6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        Map<String, Button> filterButtons = new LinkedHashMap<>();

        VBox tableHost = new VBox();
        VBox controlsWrap = new VBox(6, primaryControls, filterRow);
        controlsWrap.setAlignment(Pos.CENTER_LEFT);

        for (String[] filter : filters) {
            String key = filter[0];
            Button b = view.pillAction(filter[1], "all".equals(key));
            b.setOnAction(e -> {
                queryState.filterKey = key;
                queryState.page = 1;
                filterButtons.forEach((k, btn) -> styleQueryPill(view, btn, k.equals(queryState.filterKey)));
                renderOnboardingTable(view, baseRows, queryState, tableHost, sortPill);
            });
            filterButtons.put(key, b);
            filterRow.getChildren().add(b);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            queryState.searchTerm = newVal == null ? "" : newVal;
            queryState.page = 1;
            renderOnboardingTable(view, baseRows, queryState, tableHost, sortPill);
        });

        sortPill.setOnAction(e -> {
            queryState.ascending = !queryState.ascending;
            renderOnboardingTable(view, baseRows, queryState, tableHost, sortPill);
        });

        filterButtons.forEach((k, btn) -> styleQueryPill(view, btn, "all".equals(k)));
        renderOnboardingTable(view, baseRows, queryState, tableHost, sortPill);

        wrap.getChildren().addAll(stats, controlsWrap, tableHost);
        return wrap;
    }

    private static void renderProfilesTable(
        DashboardView view,
        List<String[]> baseRows,
        DashboardTableQueryEngine.QueryState queryState,
        VBox tableHost,
        Button sortPill
    ) {
        DashboardTableQueryEngine.QueryResult result = DashboardTableQueryEngine.apply(
            baseRows,
            queryState,
            (row, filterKey) -> {
                switch (filterKey) {
                    case "locale_set": return !"-".equals(row[2]);
                    case "theme_set": return !"-".equals(row[3]);
                    case "timezone_set": return !"-".equals(row[4]);
                    case "with_avatar": return !"-".equals(row[5]);
                    default: return true;
                }
            },
            0
        );

        String[][] visibleRows;
        if (result.pageRows.isEmpty()) {
            visibleRows = new String[][]{{"-", "-", "No profiles found", "-", "-", "-"}};
        } else {
            visibleRows = result.pageRows.toArray(new String[0][]);
        }

        tableHost.getChildren().setAll(view.dataTableWithCrud(
            "User Profile Data",
            "Profile",
            new String[]{"Profile ID", "User ID", "Locale", "Theme", "Timezone", "Avatar"},
            visibleRows,
            false,
            true
        ));

        styleQueryPill(view, sortPill, queryState.ascending);
        sortPill.setText(queryState.ascending ? "Order: ID Asc" : "Order: ID Desc");
    }

    private static void renderOnboardingTable(
        DashboardView view,
        List<String[]> baseRows,
        DashboardTableQueryEngine.QueryState queryState,
        VBox tableHost,
        Button sortPill
    ) {
        DashboardTableQueryEngine.QueryResult result = DashboardTableQueryEngine.apply(
            baseRows,
            queryState,
            (row, filterKey) -> {
                switch (filterKey) {
                    case "completed": return "Yes".equalsIgnoreCase(row[3]);
                    case "in_progress": return "No".equalsIgnoreCase(row[3]);
                    case "theme_set": return !"-".equals(row[5]);
                    case "locale_set": return !"-".equals(row[4]);
                    default: return true;
                }
            },
            0
        );

        String[][] visibleRows;
        if (result.pageRows.isEmpty()) {
            visibleRows = new String[][]{{"-", "-", "-", "-", "No onboarding found", "-", "-"}};
        } else {
            visibleRows = result.pageRows.toArray(new String[0][]);
        }

        tableHost.getChildren().setAll(view.dataTableWithCrud(
            "Onboarding Data",
            "Onboarding",
            new String[]{"Onboarding ID", "User ID", "Step", "Completed", "Locale", "Theme", "Updated"},
            visibleRows,
            false,
            true
        ));

        styleQueryPill(view, sortPill, queryState.ascending);
        sortPill.setText(queryState.ascending ? "Order: ID Asc" : "Order: ID Desc");
    }
}
