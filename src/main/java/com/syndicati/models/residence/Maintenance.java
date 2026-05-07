package com.syndicati.models.residence;

/**
 * Maintenance entity - tracks maintenance records for apartments.
 */
public class Maintenance {

    private Integer idMaintenance;
    private Integer idApartment;
    private String generalCondition;  // General state of apartment
    private String plumbingCondition;  // State of plumbing
    private String electricalCondition;  // State of electrical systems
    private String heatingCondition;  // State of heating
    private String lastMaintenanceDate;
    private String description;  // Maintenance description
    private String aiRecommendation;  // AI-generated recommendations

    public Maintenance() {}

    public Maintenance(Integer idApartment, String generalCondition, String plumbingCondition,
                       String electricalCondition, String heatingCondition, String lastMaintenanceDate,
                       String description, String aiRecommendation) {
        this.idApartment = idApartment;
        this.generalCondition = generalCondition;
        this.plumbingCondition = plumbingCondition;
        this.electricalCondition = electricalCondition;
        this.heatingCondition = heatingCondition;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.description = description;
        this.aiRecommendation = aiRecommendation;
    }

    public Maintenance(Integer idApartment, String generalCondition, String plumbingCondition,
                       String electricalCondition, String heatingCondition, String lastMaintenanceDate,
                       String description, String aiRecommendation, Integer idMaintenance) {
        this(idApartment, generalCondition, plumbingCondition, electricalCondition, heatingCondition,
             lastMaintenanceDate, description, aiRecommendation);
        this.idMaintenance = idMaintenance;
    }

    public Integer getIdMaintenance() {
        return idMaintenance;
    }

    public void setIdMaintenance(Integer idMaintenance) {
        this.idMaintenance = idMaintenance;
    }

    public Integer getIdApartment() {
        return idApartment;
    }

    public void setIdApartment(Integer idApartment) {
        this.idApartment = idApartment;
    }

    public String getGeneralCondition() {
        return generalCondition;
    }

    public void setGeneralCondition(String generalCondition) {
        this.generalCondition = generalCondition;
    }

    public String getPlumbingCondition() {
        return plumbingCondition;
    }

    public void setPlumbingCondition(String plumbingCondition) {
        this.plumbingCondition = plumbingCondition;
    }

    public String getElectricalCondition() {
        return electricalCondition;
    }

    public void setElectricalCondition(String electricalCondition) {
        this.electricalCondition = electricalCondition;
    }

    public String getHeatingCondition() {
        return heatingCondition;
    }

    public void setHeatingCondition(String heatingCondition) {
        this.heatingCondition = heatingCondition;
    }

    public String getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(String lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAiRecommendation() {
        return aiRecommendation;
    }

    public void setAiRecommendation(String aiRecommendation) {
        this.aiRecommendation = aiRecommendation;
    }

    @Override
    public String toString() {
        return "Maintenance{" +
                "idMaintenance=" + idMaintenance +
                ", idApartment=" + idApartment +
                ", lastMaintenanceDate='" + lastMaintenanceDate + '\'' +
                ", generalCondition='" + generalCondition + '\'' +
                '}';
    }
}
