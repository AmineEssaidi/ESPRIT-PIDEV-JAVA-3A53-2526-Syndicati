package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.ResidenceController;
import com.syndicati.controllers.residence.MaintenanceController;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.models.residence.Review;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SpellCheckingInspection")
final class DashboardResidenceSection {

    private DashboardResidenceSection() {
    }

    static VBox build(DashboardView view) {
        return view.moduleModeView(
            "Residence",
            "\uD83C\uDFE2",
            new String[]{"Residences", "Appartements", "Maintenance", "Reviews"},
            key -> "Reviews".equals(key) ? reviewsPane(view) :
                   "Appartements".equals(key) ? apartmentsPane(view) :
                   "Maintenance".equals(key) ? maintenancePane(view) :
                   residencesPane(view)
        );
    }

    private static void renderSearchableTable(
        DashboardView view,
        VBox host,
        String title,
        String entityLabel,
        String[] cols,
        List<String[]> baseRows,
        String[][] filterSpec,
        boolean allowAdd,
        DashboardTableQueryEngine.RowFilter filter
    ) {
        DashboardTableQueryEngine.QueryState state = new DashboardTableQueryEngine.QueryState(1_000_000);
        
        TextField search = new TextField();
        search.setPromptText("Search " + title.toLowerCase() + "...");
        search.setPrefWidth(220);
        search.setFont(Font.font(view.lightFont(), FontWeight.NORMAL, 12));
        search.setStyle(
            "-fx-background-color:" + (view.isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.04)") + ";" +
            "-fx-border-color:" + (view.isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:10px;" +
            "-fx-background-radius:10px;" +
            "-fx-text-fill:" + (view.isDark() ? "white" : "#111827") + ";" +
            "-fx-prompt-text-fill:" + (view.isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.45)") + ";"
        );
        Button sortPill = view.pillAction("Order: A-Z", false);
        HBox filterRow = new HBox(6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        Map<String, Button> filterBtns = new LinkedHashMap<>();

        HBox header = new HBox(8, filterRow, sortPill, search);
        header.setAlignment(Pos.CENTER_LEFT);

        Runnable refresh = () -> {
            DashboardTableQueryEngine.QueryResult res = DashboardTableQueryEngine.apply(
                baseRows, state, filter, 0
            );
            
            String[][] visible;
            if (res.pageRows.isEmpty()) {
                visible = new String[1][cols.length];
                Arrays.fill(visible[0], "");
                visible[0][0] = "No data found";
            } else {
                visible = res.pageRows.toArray(new String[0][]);
            }

            host.getChildren().setAll(view.dataTableWithCrud(title, entityLabel, cols, visible, allowAdd, header));
            
            styleQueryPill(view, sortPill, state.ascending);
            sortPill.setText(state.ascending ? "Order: A-Z" : "Order: Z-A");
        };

        search.textProperty().addListener((obs, old, nv) -> {
            state.searchTerm = nv;
            state.page = 1;
            refresh.run();
        });

        for (String[] f : filterSpec) {
            String key = f[0];
            Button b = view.pillAction(f[1], false);
            b.setOnAction(e -> {
                state.filterKey = key.equals(state.filterKey) ? "" : key;
                state.page = 1;
                filterBtns.forEach((k, btn) -> styleQueryPill(view, btn, k.equals(state.filterKey)));
                refresh.run();
            });
            filterBtns.put(key, b);
            filterRow.getChildren().add(b);
        }


        sortPill.setOnAction(e -> {
            state.ascending = !state.ascending;
            refresh.run();
        });

        refresh.run();
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

    private static VBox residencesPane(DashboardView view) {
        VBox wrap = new VBox(14);

        ResidenceController residenceCtrl = new ResidenceController();
        List<Residence> residences = residenceCtrl.residences();

        List<String[]> baseRows = new ArrayList<>();
        for (Residence r : residences) {
            baseRows.add(new String[]{
                String.valueOf(r.getIdResidence()),
                r.getNameResidence(),
                r.getAddressResidence(),
                r.getImageResidence() != null ? r.getImageResidence() : "-",
                r.getNumberApartments() != null ? String.valueOf(r.getNumberApartments()) : "0",
                r.getNumberFloors() != null ? String.valueOf(r.getNumberFloors()) : "0",
                r.getNumberBlocks() != null ? r.getNumberBlocks() : "-"
            });
        }

        VBox tableHost = new VBox();
        renderSearchableTable(view, tableHost, "Residences", "Residence",
            new String[]{"Name", "Address", "Image", "Apartments", "Floors", "Blocks"},
            baseRows,
            new String[][]{{"name", "Name"}, {"addr", "Address"}},
            true,
            (row, key) -> {
                if (key == null || key.isBlank() || "all".equalsIgnoreCase(key)) return true;
                if ("name".equals(key)) return row[1] != null && !row[1].isBlank();
                if ("addr".equals(key)) return row[2] != null && !row[2].isBlank();
                return true;
            }
        );
        wrap.getChildren().add(tableHost);
        return wrap;
    }

    private static VBox apartmentsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        ResidenceController residenceCtrl = new ResidenceController();
        MaintenanceController mCtrl = new MaintenanceController();
        List<Apartment> apartments = residenceCtrl.apartments();
        Map<Integer, String> latestRecs = new HashMap<>();
        try {
            for (Maintenance m : mCtrl.maintenanceRecords()) {
                if (m.getIdApartment() != null) {
                    latestRecs.put(m.getIdApartment(), m.getAiRecommendation());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String[]> baseRows = new ArrayList<>();
        for (Apartment a : apartments) {
            baseRows.add(new String[]{
                String.valueOf(a.getIdApartment()),
                a.getIdResidence() != null ? String.valueOf(a.getIdResidence()) : "-",
                a.getIdUser() != null ? String.valueOf(a.getIdUser()) : "-",
                a.getTypeApartment() != null ? a.getTypeApartment() : "-",
                a.getImageApartment() != null ? a.getImageApartment() : "-",
                a.getArea() != null ? String.valueOf(a.getArea()) : "-",
                a.getRentalPrice() != null ? String.valueOf(a.getRentalPrice()) : "-",
                a.getSalePrice() != null ? String.valueOf(a.getSalePrice()) : "-",
                a.getParking() != null ? String.valueOf(a.getParking()) : "0",
                a.getAvailable() != null ? String.valueOf(a.getAvailable()) : "0",
                a.getDateConstructed() != null ? a.getDateConstructed() : "-",
                latestRecs.getOrDefault(a.getIdApartment(), "-")
            });
        }

        VBox tableHost = new VBox();
        renderSearchableTable(view, tableHost, "Appartements", "Appartement",
            new String[]{"Residence ID", "User ID", "Type", "Image", "Area", "Rent", "Sale", "Parking", "Available", "Built Date", "AI Recommendation"},
            baseRows,
            new String[][]{{"type", "Type"}, {"res", "Residence"}},
            true,
            (row, key) -> {
                if (key == null || key.isBlank() || "all".equalsIgnoreCase(key)) return true;
                if ("type".equals(key)) return row[3] != null && !row[3].isBlank() && !"-".equals(row[3]);
                if ("res".equals(key)) return row[1] != null && !row[1].isBlank() && !"-".equals(row[1]);
                return true;
            }
        );
        wrap.getChildren().add(tableHost);
        return wrap;
    }

    private static VBox maintenancePane(DashboardView view) {
        VBox wrap = new VBox(14);
        MaintenanceController maintenanceCtrl = new MaintenanceController();
        List<Maintenance> records = maintenanceCtrl.maintenanceRecords();

        List<String[]> baseRows = new ArrayList<>();
        for (Maintenance m : records) {
            baseRows.add(new String[]{
                String.valueOf(m.getIdMaintenance()),
                m.getIdApartment() != null ? String.valueOf(m.getIdApartment()) : "-",
                m.getGeneralCondition() != null ? m.getGeneralCondition() : "-",
                m.getPlumbingCondition() != null ? m.getPlumbingCondition() : "-",
                m.getElectricalCondition() != null ? m.getElectricalCondition() : "-",
                m.getHeatingCondition() != null ? m.getHeatingCondition() : "-",
                m.getDescription() != null ? m.getDescription() : "-",
                m.getAiRecommendation() != null ? m.getAiRecommendation() : "-"
            });
        }

        VBox tableHost = new VBox();
        renderSearchableTable(view, tableHost, "Maintenance", "Maintenance Ticket",
            new String[]{"Apartment ID", "General", "Plumbing", "Electrical", "Heating", "Description", "AI Recommendation"},
            baseRows,
            new String[][]{{"apt", "Apartment"}, {"cond", "Condition"}},
            true,
            (row, key) -> {
                if (key == null || key.isBlank() || "all".equalsIgnoreCase(key)) return true;
                if ("apt".equals(key)) return row[1] != null && !row[1].isBlank() && !"-".equals(row[1]);
                if ("cond".equals(key)) return row[2] != null && !"Unknown".equalsIgnoreCase(row[2]) && !"-".equals(row[2]);
                return true;
            }
        );
        wrap.getChildren().add(tableHost);
        return wrap;
    }

    private static VBox reviewsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        ResidenceController residenceCtrl = new ResidenceController();
        MaintenanceController maintenanceCtrl = new MaintenanceController();

        List<Residence> residences = residenceCtrl.residences();
        List<Apartment> apartments = residenceCtrl.apartments();
        List<Review> reviews = maintenanceCtrl.reviews();

        Map<Integer, String> residenceNames = new HashMap<>();
        for (Residence r : residences) {
            if (r.getIdResidence() != null) {
                residenceNames.put(r.getIdResidence(), r.getNameResidence() != null ? r.getNameResidence() : "Unknown Residence");
            }
        }

        Map<Integer, Apartment> apartmentById = new HashMap<>();
        for (Apartment a : apartments) {
            if (a.getIdApartment() != null) {
                apartmentById.put(a.getIdApartment(), a);
            }
        }

        List<String[]> baseRows = new ArrayList<>();
        for (Review r : reviews) {
            Apartment apt = r.getIdApartment() != null ? apartmentById.get(r.getIdApartment()) : null;
            String residenceName = "-";
            String apartmentName = "Apartment";
            if (apt != null) {
                if (apt.getIdResidence() != null) {
                    residenceName = residenceNames.getOrDefault(apt.getIdResidence(), "Unknown Residence");
                }
                String type = (apt.getTypeApartment() != null && !apt.getTypeApartment().isBlank())
                    ? apt.getTypeApartment()
                    : "Apartment";
                apartmentName = "Apartment " + type;
            }

            int score = r.getScore() != null ? Math.max(0, Math.min(10, r.getScore())) : 0;
            String stars = "★".repeat(score) + "☆".repeat(10 - score) + " (" + score + "/10)";

            baseRows.add(new String[]{"0", residenceName, apartmentName, stars});
        }

        VBox tableHost = new VBox();
        renderSearchableTable(view, tableHost, "Reviews", "Review",
            new String[]{"Residence", "Apartment", "Stars"},
            baseRows,
            new String[][]{{"res", "Residence"}, {"score", "Score"}},
            false,
            (row, key) -> true
        );
        wrap.getChildren().add(tableHost);
        return wrap;
    }
}
