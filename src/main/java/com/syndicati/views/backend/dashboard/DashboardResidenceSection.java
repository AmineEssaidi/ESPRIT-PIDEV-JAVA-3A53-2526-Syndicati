package com.syndicati.views.backend.dashboard;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

final class DashboardResidenceSection {

    private DashboardResidenceSection() {
    }

    static VBox build(DashboardView view) {
        VBox s = view.moduleShell("Residence", "\uD83C\uDFE2");
        VBox body = new VBox(16);
        HBox sub = view.moduleModeSwitcher(new String[]{"Residences", "Appartements", "Maintenance"}, "Residences", key -> {
            body.getChildren().setAll(
                "Appartements".equals(key) ? apartmentsPane(view) :
                "Maintenance".equals(key) ? maintenancePane(view) :
                residencesPane(view)
            );
        });
        body.getChildren().add(residencesPane(view));
        s.getChildren().addAll(sub, body);
        return s;
    }

    private static VBox residencesPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Residences", "Residence",
            new String[]{"Residence", "Address", "Units", "Syndic", "Status"},
            new String[][]{
                {"Residence Jasmin", "Bardo", "120", "Ahmed B.", "Active"},
                {"Residence Mimosa", "Lac 2", "86", "Leila M.", "Active"},
                {"Residence Olive", "Menzah", "64", "Karim S.", "Maintenance"}
            }, true, true
        ));
        return wrap;
    }

    private static VBox apartmentsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Appartements", "Appartement",
            new String[]{"Unit", "Residence", "Resident", "Floor", "State"},
            new String[][]{
                {"A-101", "Jasmin", "Ahmed B.", "1", "Occupied"},
                {"A-202", "Jasmin", "Leila M.", "2", "Occupied"},
                {"B-105", "Mimosa", "-", "1", "Available"},
                {"C-401", "Olive", "-", "4", "Available"}
            }, true, true
        ));
        return wrap;
    }

    private static VBox maintenancePane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Maintenance", "Maintenance Ticket",
            new String[]{"Ticket", "Residence", "Issue", "Priority", "Status"},
            new String[][]{
                {"MNT-301", "Jasmin", "Water Pump", "High", "In Progress"},
                {"MNT-298", "Mimosa", "Garage Lighting", "Medium", "Open"},
                {"MNT-296", "Olive", "Lift Noise", "Low", "Scheduled"}
            }, false, false
        ));
        return wrap;
    }
}
