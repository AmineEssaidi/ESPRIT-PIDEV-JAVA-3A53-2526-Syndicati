package com.syndicati.views.backend.dashboard;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

final class DashboardSyndicatSection {

    private DashboardSyndicatSection() {
    }

    static VBox build(DashboardView view) {
        VBox s = view.moduleShell("Syndicat", "\uD83C\uDFDB\uFE0F");
        VBox body = new VBox(16);
        HBox sub = view.moduleModeSwitcher(new String[]{"Reclamations", "Responses"}, "Reclamations", key -> {
            body.getChildren().setAll("Responses".equals(key) ? responsesPane(view) : reclamationsPane(view));
        });
        body.getChildren().add(reclamationsPane(view));
        s.getChildren().addAll(sub, body);
        return s;
    }

    private static VBox reclamationsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Reclamations", "Reclamation",
            new String[]{"Reference", "Resident", "Type", "Status", "Date"},
            new String[][]{
                {"REC-2024-0156", "Ahmed B.", "Maintenance", "Pending", "Mar 11"},
                {"REC-2024-0155", "Leila M.", "Noise", "Resolved", "Mar 10"},
                {"REC-2024-0154", "Karim S.", "Elevator", "Urgent", "Mar 9"}
            }, false, true
        ));
        return wrap;
    }

    private static VBox responsesPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Responses", "Response",
            new String[]{"Reclamation", "Agent", "Response", "Sent", "State"},
            new String[][]{
                {"REC-2024-0155", "Syndic Team", "Inspection completed", "Mar 10", "Delivered"},
                {"REC-2024-0152", "Syndic Team", "Technician scheduled", "Mar 8", "Delivered"},
                {"REC-2024-0149", "Support", "Need more details", "Mar 7", "Awaiting Reply"}
            }, false, false
        ));
        return wrap;
    }
}
