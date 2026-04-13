package com.syndicati.scratch;

import com.syndicati.models.entities.Evenement;
import com.syndicati.models.services.EvenementService;
import java.util.List;

public class DbCheck {
    public static void main(String[] args) {
        EvenementService service = new EvenementService();
        List<Evenement> events = service.getAllEvents();
        System.out.println("Total events found in DB: " + events.size());
        for (Evenement e : events) {
            System.out.println("ID: " + e.getIdEvent() + " | Title: " + e.getTitreEvent() + " | Date: " + e.getDateEvent());
        }
    }
}
