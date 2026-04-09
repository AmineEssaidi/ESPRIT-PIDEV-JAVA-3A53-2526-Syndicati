package com.syndicati;

import com.syndicati.models.Residence;
import com.syndicati.services.ServiceResidence;

import java.sql.SQLDataException;
import java.sql.SQLException;

public class CRUD {

    public static void main(String[] args) throws SQLException {

        ServiceResidence ServiceResidence= new ServiceResidence();
        //ServiceResidence.AjouterResidence(new Residence("Residence Havaa", "Ariana", "a", "2024-01-10", 9, 4, 3));
        System.out.println("Hello world");
        try
        {
            ServiceResidence.ModifierResidence(new Residence("Residence Modifiée", "Tunisie", "a", "2025-09-11", 9, 4, "A", 17));
            System.out.println("Residence modifié!");
            //ServiceResidence.SupprimerResidence(new Residence("Residence Havaa", "Ariana", "a", "2024-01-10", 9, 4, 3, 18));
            System.out.println(ServiceResidence.RecupererResidence());

        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        }
}
