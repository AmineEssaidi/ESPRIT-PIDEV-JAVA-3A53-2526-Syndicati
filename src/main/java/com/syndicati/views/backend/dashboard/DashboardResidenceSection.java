package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.ResidenceController;
import com.syndicati.controllers.residence.MaintenanceController;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.models.residence.Review;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static VBox residencesPane(DashboardView view) {
        VBox wrap = new VBox(14);

        ResidenceController residenceCtrl = new ResidenceController();
        List<Residence> residences = residenceCtrl.residences();

        String[][] residenceData = new String[residences.size()][];
        for (int i = 0; i < residences.size(); i++) {
            Residence r = residences.get(i);
            residenceData[i] = new String[]{
                String.valueOf(r.getIdResidence()),
                r.getNameResidence(),
                r.getAddressResidence(),
                r.getImageResidence() != null ? r.getImageResidence() : "-",
                r.getNumberApartments() != null ? String.valueOf(r.getNumberApartments()) : "0",
                r.getNumberFloors() != null ? String.valueOf(r.getNumberFloors()) : "0",
                r.getNumberBlocks() != null ? r.getNumberBlocks() : "-"
            };
        }

        wrap.getChildren().add(view.dataTableWithCrud("Residences", "Residence",
            new String[]{"Residence", "Address", "Image", "Units", "Floors", "Blocks"},
            residenceData.length > 0 ? residenceData : new String[][]{{"No residences found", "", "", "", "", ""}},
            true
        ));
        return wrap;
    }

    private static VBox apartmentsPane(DashboardView view) {
        VBox wrap = new VBox(14);

        ResidenceController residenceCtrl = new ResidenceController();
        List<Apartment> apartments = residenceCtrl.apartments();

        String[][] apartmentData = new String[apartments.size()][];
        for (int i = 0; i < apartments.size(); i++) {
            Apartment a = apartments.get(i);
            apartmentData[i] = new String[]{
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
                a.getDateConstructed() != null ? a.getDateConstructed() : "-"
            };
        }

        wrap.getChildren().add(view.dataTableWithCrud("Appartements", "Appartement",
            new String[]{"Residence ID", "User ID", "Type", "Image", "Area", "Rent", "Sale", "Parking", "Available", "Built Date"},
            apartmentData.length > 0 ? apartmentData : new String[][]{{"No apartments found", "", "", "", "", "", "", "", "", ""}},
            true
        ));
        return wrap;
    }

    private static VBox maintenancePane(DashboardView view) {
        VBox wrap = new VBox(14);

        MaintenanceController maintenanceCtrl = new MaintenanceController();
        List<Maintenance> records = maintenanceCtrl.maintenanceRecords();

        String[][] maintenanceData = new String[records.size()][];
        for (int i = 0; i < records.size(); i++) {
            Maintenance m = records.get(i);

            maintenanceData[i] = new String[]{
                String.valueOf(m.getIdMaintenance()),
                m.getIdApartment() != null ? String.valueOf(m.getIdApartment()) : "-",
                m.getGeneralCondition() != null ? m.getGeneralCondition() : "-",
                m.getPlumbingCondition() != null ? m.getPlumbingCondition() : "-",
                m.getElectricalCondition() != null ? m.getElectricalCondition() : "-",
                m.getHeatingCondition() != null ? m.getHeatingCondition() : "-",
                m.getDescription() != null ? m.getDescription() : "-",
                m.getAiRecommendation() != null ? m.getAiRecommendation() : "-"
            };
        }

        wrap.getChildren().add(view.dataTableWithCrud("Maintenance", "Maintenance Ticket",
            new String[]{"Apartment ID", "General", "Plumbing", "Electrical", "Heating", "Description", "AI Recommendation"},
            maintenanceData.length > 0 ? maintenanceData : new String[][]{{"No maintenance records", "", "", "", "", "", ""}},
            true
        ));
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

        String[][] reviewData = new String[reviews.size()][];
        for (int i = 0; i < reviews.size(); i++) {
            Review r = reviews.get(i);
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

            reviewData[i] = new String[]{residenceName, apartmentName, stars};
        }

        wrap.getChildren().add(view.dataTableWithCrud("Reviews", "Review",
            new String[]{"Residence", "Apartment", "Stars"},
            reviewData.length > 0 ? reviewData : new String[][]{{"No reviews found", "", ""}},
            false
        ));
        return wrap;
    }
}
