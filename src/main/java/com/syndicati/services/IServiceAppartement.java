package com.syndicati.services;

import com.syndicati.models.Appartement;

import java.sql.SQLDataException;
import java.util.List;

public interface IServiceAppartement <Appartment>{

    void AjouterAppartment(Appartement Appartement) throws SQLDataException;
    void ModifierAppartement(Appartment Appartement) throws SQLDataException;
    void SupprimerAppartement (Appartment Appartement) throws SQLDataException;
    List<Appartment> RecupererAppartement() throws SQLDataException;
}
