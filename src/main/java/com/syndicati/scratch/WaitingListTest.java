package com.syndicati.scratch;

import com.syndicati.models.entities.Evenement;
import com.syndicati.models.entities.Participation;
import com.syndicati.models.entities.User;
import com.syndicati.models.services.EvenementService;
import com.syndicati.models.services.ParticipationService;
import java.time.LocalDateTime;

public class WaitingListTest {
    public static void main(String[] args) {
        EvenementService es = new EvenementService();
        ParticipationService ps = new ParticipationService();

        System.out.println("=== TEST DE LA LISTE D'ATTENTE ===");

        // 1. Création d'un événement de test avec 1 seule place
        Evenement e = new Evenement();
        e.setTitreEvent("Test Waitlist " + System.currentTimeMillis());
        e.setNbPlaces(1);
        e.setNbRestants(1);
        e.setLieuEvent("Test Lab");
        e.setDateEvent(LocalDateTime.now().plusDays(1));
        
        // On suppose que l'utilisateur avec ID 1 existe (Admin ou Test)
        User user = new User();
        user.setIdUser(1); 
        user.setFirstName("Tester");
        user.setEmailUser("test@syndicati.com");
        e.setUser(user);

        if (es.createEvent(e)) {
            System.out.println("1. Événement créé avec 1 place. ID: " + e.getIdEvent());
            
            // 2. Inscription de l'utilisateur A (devrait être CONFIRMÉ)
            Participation p1 = new Participation();
            p1.setEvenement(e);
            p1.setUser(user);
            p1.setNbAccompagnants(0);
            p1.setDateParticipation(LocalDateTime.now());
            p1.setCommentaireParticipation("User A");
            p1.setFormulaireData("{}");
            
            if (ps.registerParticipation(p1)) {
                System.out.println("2. Utilisateur A inscrit. Statut: " + p1.getStatutParticipation());
            }

            // 3. Inscription de l'utilisateur B (devrait être en LISTE D'ATTENTE car plus de places)
            Participation p2 = new Participation();
            p2.setEvenement(e);
            p2.setUser(user);
            p2.setNbAccompagnants(0);
            p2.setDateParticipation(LocalDateTime.now());
            p2.setCommentaireParticipation("User B");
            p2.setFormulaireData("{}");

            if (ps.registerParticipation(p2)) {
                System.out.println("3. Utilisateur B inscrit. Statut: " + p2.getStatutParticipation());
            }

            // 4. Annulation de l'utilisateur A -> Devrait promouvoir B
            System.out.println("4. Annulation de l'utilisateur A...");
            if (ps.cancelParticipation(p1.getIdParticipation())) {
                System.out.println("✅ Annulation réussie.");
                
                // Vérification du statut de B après promotion
                ps.getParticipationById(p2.getIdParticipation()).ifPresent(updatedP2 -> {
                    System.out.println("5. Nouveau statut de l'utilisateur B: " + updatedP2.getStatutParticipation());
                    if ("confirme".equals(updatedP2.getStatutParticipation())) {
                        System.out.println("✨ TEST RÉUSSI : La promotion automatique fonctionne !");
                    } else {
                        System.out.println("❌ TEST ÉCHOUÉ : La promotion n'a pas eu lieu.");
                    }
                });
            }
        } else {
            System.out.println("Erreur lors de la création de l'événement de test.");
        }
    }
}
