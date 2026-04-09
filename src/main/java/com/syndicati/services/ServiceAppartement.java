package com.syndicati.services;

import com.syndicati.models.Appartement;

import java.sql.SQLDataException;
import java.util.List;

public class ServiceAppartement implements IServiceAppartement<Appartement> {

    @Override
    public void AjouterAppartment(Appartement Appartement) throws SQLDataException {

    }

    @Override
    public void ModifierAppartement(Appartement Appartement) throws SQLDataException {

    }

    @Override
    public void SupprimerAppartement(Appartement Appartement) throws SQLDataException {

    }

    @Override
    public List<Appartement> RecupererAppartement() throws SQLDataException {
        return List.of();
    }
}
