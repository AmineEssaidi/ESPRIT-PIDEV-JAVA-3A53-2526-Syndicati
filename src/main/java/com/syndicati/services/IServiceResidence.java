package com.syndicati.services;

import java.sql.SQLDataException;
import java.util.List;

public interface IServiceResidence <Residence> {

    void AjouterResidence(Residence Residence) throws SQLDataException;
    void ModifierResidence(Residence Residence) throws SQLDataException;
    void SupprimerResidence(Residence Residence) throws SQLDataException;
    List<Residence> RecupererResidence() throws SQLDataException;

}
