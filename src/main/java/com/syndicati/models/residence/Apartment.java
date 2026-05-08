package com.syndicati.models.residence;

/**
 * Apartment entity - represents a unit within a residence.
 */
public class Apartment {

    private Integer idApartment;
    private Integer idResidence;
    private Integer idUser;
    private Integer parking;  // 0 or 1 (boolean as int)
    private Integer available;  // 0 or 1 (boolean as int)
    private String imageApartment;
    private String typeApartment;
    private String apartmentInfo;
    private Double area;  // square meters
    private Double rentalPrice;
    private Double salePrice;
    private String dateConstructed;
    private Maintenance maintenance;

    public Apartment() {}

    public Apartment(Integer idResidence, Integer idUser, Integer parking, Integer available, String imageApartment,
                     String typeApartment, String apartmentInfo, Double area, Double rentalPrice, Double salePrice,
                     String dateConstructed) {
        this.idResidence = idResidence;
        this.idUser = idUser;
        this.parking = parking;
        this.available = available;
        this.imageApartment = imageApartment;
        this.typeApartment = typeApartment;
        this.apartmentInfo = apartmentInfo;
        this.area = area;
        this.rentalPrice = rentalPrice;
        this.salePrice = salePrice;
        this.dateConstructed = dateConstructed;
    }

    public Apartment(Integer idResidence, Integer idUser, Integer parking, Integer available, String imageApartment,
                     String typeApartment, String apartmentInfo, Double area, Double rentalPrice, Double salePrice,
                     String dateConstructed, Integer idApartment) {
        this(idResidence, idUser, parking, available, imageApartment, typeApartment, apartmentInfo, area,
             rentalPrice, salePrice, dateConstructed);
        this.idApartment = idApartment;
    }

    public Integer getIdApartment() {
        return idApartment;
    }

    public void setIdApartment(Integer idApartment) {
        this.idApartment = idApartment;
    }

    public Integer getIdResidence() {
        return idResidence;
    }

    public void setIdResidence(Integer idResidence) {
        this.idResidence = idResidence;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public Integer getParking() {
        return parking;
    }

    public void setParking(Integer parking) {
        this.parking = parking;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public String getImageApartment() {
        return imageApartment;
    }

    public void setImageApartment(String imageApartment) {
        this.imageApartment = imageApartment;
    }

    public String getTypeApartment() {
        return typeApartment;
    }

    public void setTypeApartment(String typeApartment) {
        this.typeApartment = typeApartment;
    }

    public String getApartmentInfo() {
        return apartmentInfo;
    }

    public void setApartmentInfo(String apartmentInfo) {
        this.apartmentInfo = apartmentInfo;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public Double getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(Double rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public String getDateConstructed() {
        return dateConstructed;
    }

    public void setDateConstructed(String dateConstructed) {
        this.dateConstructed = dateConstructed;
    }

    public Maintenance getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
    }

    @Override
    public String toString() {
        return "Apartment{" +
                "idApartment=" + idApartment +
                ", typeApartment='" + typeApartment + '\'' +
                ", rentalPrice=" + rentalPrice +
                ", available=" + available +
                '}';
    }
}
