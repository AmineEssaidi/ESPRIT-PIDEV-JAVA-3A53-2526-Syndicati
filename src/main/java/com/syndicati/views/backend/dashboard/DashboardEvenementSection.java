package com.syndicati.views.backend.dashboard;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@SuppressWarnings("SpellCheckingInspection")
final class DashboardEvenementSection {

    private DashboardEvenementSection() {
    }

    static VBox build(DashboardView view) {
        return view.moduleModeView(
            "Evenement",
            "\uD83C\uDF89",
            new String[]{"Evenements", "Participations"},
            key -> "Participations".equals(key) ? participationsPane(view) : eventsPane(view)
        );
    }

    private static VBox eventsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Evenements", "Event",
            new String[]{"Title", "Type", "Date", "Enrolled", "Status"},
            new String[][]{
                {"Assemblee Generale 2024", "Meeting", "Mar 20", "47", "Open"},
                {"Fete de quartier", "Social", "Apr 5", "120", "Open"},
                {"Formation securite incendie", "Training", "Apr 12", "30", "Limited"}
            }, true
        ));
        return wrap;
    }

    private static VBox participationsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Participations", "Participation",
            new String[]{"Event", "Resident", "Seat", "Checked-in", "Date"},
            new String[][]{
                {"Assemblee Generale 2024", "Ahmed B.", "A12", "No", "Mar 20"},
                {"Fete de quartier", "Leila M.", "B03", "No", "Apr 5"},
                {"Formation securite incendie", "Omar Z.", "C08", "No", "Apr 12"}
            }, false
        ));
        return wrap;
    }
}
