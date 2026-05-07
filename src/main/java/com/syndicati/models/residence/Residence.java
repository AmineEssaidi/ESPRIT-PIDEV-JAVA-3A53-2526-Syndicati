package com.syndicati.models.residence;

/**
 * Residence entity - represents a residential complex.
 */
public class Residence {

    private Integer idResidence;
    private String nameResidence;
    private String addressResidence;
    private String imageResidence;
    private String dateAdded;
    private Integer numberApartments;
    private Integer numberFloors;
    private String numberBlocks;

    public Residence() {}

    public Residence(String nameResidence, String addressResidence, String imageResidence, String dateAdded,
                     Integer numberApartments, Integer numberFloors, String numberBlocks) {
        this.nameResidence = nameResidence;
        this.addressResidence = addressResidence;
        this.imageResidence = imageResidence;
        this.dateAdded = dateAdded;
        this.numberApartments = numberApartments;
        this.numberFloors = numberFloors;
        this.numberBlocks = numberBlocks;
    }

    public Residence(String nameResidence, String addressResidence, String imageResidence, String dateAdded,
                     Integer numberApartments, Integer numberFloors, String numberBlocks, Integer idResidence) {
        this(nameResidence, addressResidence, imageResidence, dateAdded, numberApartments, numberFloors, numberBlocks);
        this.idResidence = idResidence;
    }

    public Integer getIdResidence() {
        return idResidence;
    }

    public void setIdResidence(Integer idResidence) {
        this.idResidence = idResidence;
    }

    public String getNameResidence() {
        return nameResidence;
    }

    public void setNameResidence(String nameResidence) {
        this.nameResidence = nameResidence;
    }

    public String getAddressResidence() {
        return addressResidence;
    }

    public void setAddressResidence(String addressResidence) {
        this.addressResidence = addressResidence;
    }

    public String getImageResidence() {
        return imageResidence;
    }

    public void setImageResidence(String imageResidence) {
        this.imageResidence = imageResidence;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Integer getNumberApartments() {
        return numberApartments;
    }

    public void setNumberApartments(Integer numberApartments) {
        this.numberApartments = numberApartments;
    }

    public Integer getNumberFloors() {
        return numberFloors;
    }

    public void setNumberFloors(Integer numberFloors) {
        this.numberFloors = numberFloors;
    }

    public String getNumberBlocks() {
        return numberBlocks;
    }

    public void setNumberBlocks(String numberBlocks) {
        this.numberBlocks = numberBlocks;
    }

    @Override
    public String toString() {
        return "Residence{" +
                "idResidence=" + idResidence +
                ", nameResidence='" + nameResidence + '\'' +
                ", addressResidence='" + addressResidence + '\'' +
                ", numberApartments=" + numberApartments +
                ", numberFloors=" + numberFloors +
                '}';
    }
}
