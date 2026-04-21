package com.syndicati;

import com.syndicati.models.residence.Residence;
import com.syndicati.services.residence.ServiceAppartement;
import com.syndicati.services.residence.ServiceResidence;
import com.syndicati.services.residence.ServiceMaintenance;

import java.sql.SQLDataException;
import java.sql.SQLException;

public class CRUD {

    public static void main(String[] args) throws SQLException {

        ServiceResidence ServiceResidence= new ServiceResidence();
        ServiceAppartement ServiceAppartement= new ServiceAppartement();
        ServiceMaintenance ServiceMaintenance= new ServiceMaintenance();

        //ServiceResidence.Ajouter(new Residence("Residence Havaa", "Ariana", "a", "2024-01-10", 9, 4, 3));
        System.out.println("Hello world");
        try
        {
            /*
            ServiceResidence.Modifier(new Residence("Residence Modifiée Inteface", "Tunisie", "a", "2025-09-11", 9, 4, "A", 17));
            System.out.println("Residence modifié!");
            //ServiceResidence.Supprimer(new Residence("Residence Havaa", "Ariana", "a", "2024-01-10", 9, 4, 3, 18));

            */

           System.out.println(ServiceResidence.Recuperer());
            System.out.println(ServiceAppartement.Recuperer());
            System.out.println(ServiceMaintenance.Recuperer());
                /*
            ServiceMaintenance.Modifier(new Maintenance("bien", "Test maintenance crud", "2026-04-11",
                    "bon", "bon", "bon", "bon", 72, 42));

                 */
            //ServiceAppartement.Ajouter(new Appartement(9, 10, 1,0, "image_cool", "S+4", "{\"bloc\": \"D\",\"floor\": \"0\",\"number\": \"8\",\"parking\": true,\"disponible\": false}", 300, 2000, 9999999, "2024-03-11"));
            //ServiceAppartement.Modifier(new Appartement(9, 10, 1,0, "image_modifia", "S+2", "{\"bloc\": \"D\",\"floor\": \"0\",\"number\": \"8\",\"parking\": true,\"disponible\": false}", 300, 2000, 9999999, "2024-03-11", 73));
            //ServiceAppartement.Supprimer(new Appartement(9, 10, 1,0, "image_cool", "S+4", "{\"bloc\": \"D\",\"floor\": \"0\",\"number\": \"8\",\"parking\": true,\"disponible\": false}", 300, 2000, 9999999, "2024-03-11", 73));
    System.out.println(ServiceResidence.TrouverResidenceParId(10));
            System.out.println("Appartements:"+ServiceAppartement.AppartementsParResidence(new Residence("Residence Narjess 1", "Tunisie",
                    "a", "2025-09-11", 9, 4, "A", 8)));

        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        }
}
