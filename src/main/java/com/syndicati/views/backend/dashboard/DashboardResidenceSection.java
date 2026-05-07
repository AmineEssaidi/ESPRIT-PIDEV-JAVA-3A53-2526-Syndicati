package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.ResidenceController;
import com.syndicati.controllers.residence.MaintenanceController;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import javafx.scene.layout.VBox;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
final class DashboardResidenceSection {

    private DashboardResidenceSection() {
    }

    static VBox build(DashboardView view) {
        return view.moduleModeView(
            "Residence",
            "\uD83C\uDFE2",
            new String[]{"Residences", "Appartements", "Maintenance"},
            key -> "Appartements".equals(key) ? apartmentsPane(view) :
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
                r.getNumberApartments() != null ? String.valueOf(r.getNumberApartments()) : "0",
                r.getNumberFloors() != null ? String.valueOf(r.getNumberFloors()) : "0",
                r.getNumberBlocks() != null ? r.getNumberBlocks() : "-"
            };
        }

        wrap.getChildren().add(view.dataTableWithCrud("Residences", "Residence",
            new String[]{"ID", "Residence", "Address", "Units", "Floors", "Blocks"},
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
                a.getArea() != null ? String.valueOf(a.getArea()) : "-",
                a.getRentalPrice() != null ? String.valueOf(a.getRentalPrice()) : "-",
                a.getSalePrice() != null ? String.valueOf(a.getSalePrice()) : "-",
                a.getParking() != null ? String.valueOf(a.getParking()) : "0",
                a.getAvailable() != null ? String.valueOf(a.getAvailable()) : "0",
                a.getDateConstructed() != null ? a.getDateConstructed() : "-"
            };
        }

        wrap.getChildren().add(view.dataTableWithCrud("Appartements", "Appartement",
            new String[]{"ID", "Residence ID", "User ID", "Type", "Area", "Rent", "Sale", "Parking", "Available", "Built Date"},
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
            new String[]{"ID", "Apartment ID", "General", "Plumbing", "Electrical", "Heating", "Description", "AI Recommendation"},
            maintenanceData.length > 0 ? maintenanceData : new String[][]{{"No maintenance records", "", "", "", "", "", "", ""}},
            true
        ));
        return wrap;
    }
}
