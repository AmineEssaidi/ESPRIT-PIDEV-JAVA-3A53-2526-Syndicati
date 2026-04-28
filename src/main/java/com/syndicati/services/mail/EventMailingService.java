package com.syndicati.services.mail;

import com.syndicati.models.entities.Evenement;
import com.syndicati.models.entities.Participation;
import com.syndicati.models.entities.User;
import java.time.format.DateTimeFormatter;

public class EventMailingService {
    private static EventMailingService instance;
    private final AsyncMailerService mailer;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' HH:mm");

    private EventMailingService() {
        this.mailer = AsyncMailerService.getInstance();
    }

    public static EventMailingService getInstance() {
        if (instance == null) {
            instance = new EventMailingService();
        }
        return instance;
    }

    /**
     * Send a confirmation email to a user when they join an event.
     */
    public void sendParticipationConfirmation(Participation p) {
        Evenement e = p.getEvenement();
        User user = p.getUser();
        
        if (user == null || user.getEmailUser() == null || user.getEmailUser().isBlank()) {
            System.err.println("[EventMailing] Cannot send participation email: Recipient has no email address.");
            return;
        }

        System.out.println("[EventMailing] Preparing participation email for: " + user.getEmailUser());
        String subject = "Confirmation: You're joined to " + e.getTitreEvent() + "!";
        
        String html = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;\">" +
                "  <div style=\"background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%); padding: 30px; text-align: center; color: white;\">" +
                "    <h1 style=\"margin: 0; font-size: 24px;\">Registration Confirmed!</h1>" +
                "  </div>" +
                "  <div style=\"padding: 30px; background-color: #ffffff; color: #333;\">" +
                "    <p>Hi <strong>" + user.getFirstName() + "</strong>,</p>" +
                "    <p>Great news! Your registration for the following event has been confirmed:</p>" +
                "    " +
                "    <div style=\"background-color: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #6366f1;\">" +
                "      <h2 style=\"margin: 0 0 10px 0; color: #1e293b;\">" + e.getTitreEvent() + "</h2>" +
                "      <p style=\"margin: 5px 0;\">📍 <strong>Location:</strong> " + e.getLieuEvent() + "</p>" +
                "      <p style=\"margin: 5px 0;\">📅 <strong>Date:</strong> " + (e.getDateEvent() != null ? e.getDateEvent().format(DATE_FORMATTER) : "TBD") + "</p>" +
                "      <p style=\"margin: 5px 0;\">👥 <strong>Companions:</strong> " + p.getNbAccompagnants() + "</p>" +
                "    </div>" +
                "    " +
                "    <p>Please present this email at the entrance. If you have any questions, feel free to contact the host.</p>" +
                "    <p style=\"margin-top: 30px; font-size: 14px; color: #64748b;\">Thank you for being part of the Syndicati community!</p>" +
                "  </div>" +
                "  <div style=\"background-color: #f1f5f9; padding: 15px; text-align: center; font-size: 12px; color: #94a3b8;\">" +
                "    &copy; 2026 Syndicati - Your Smart Co-living Companion" +
                "  </div>" +
                "</div>";

        mailer.sendHtmlAsync(user.getEmailUser(), subject, html, result -> {
            if (result.isSuccess()) System.out.println("[EventMailing] Participation email SENT to " + user.getEmailUser());
            else System.err.println("[EventMailing] FAILED to send participation email: " + result.getError());
        });
    }

    /**
     * Send a notification email when a new event is created.
     */
    public void sendEventCreationNotification(Evenement e) {
        User host = e.getUser();
        if (host == null || host.getEmailUser() == null || host.getEmailUser().isBlank()) {
            System.err.println("[EventMailing] Cannot send creation email: Host has no email address.");
            return;
        }

        System.out.println("[EventMailing] Preparing creation email for: " + host.getEmailUser());
        String subject = "Your Event is Live: " + e.getTitreEvent();

        String html = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;\">" +
                "  <div style=\"background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%); padding: 30px; text-align: center; color: white;\">" +
                "    <h1 style=\"margin: 0; font-size: 24px;\">Event Created Successfully!</h1>" +
                "  </div>" +
                "  <div style=\"padding: 30px; background-color: #ffffff; color: #333;\">" +
                "    <p>Hi <strong>" + host.getFirstName() + "</strong>,</p>" +
                "    <p>Congratulations! Your event is now live and ready for participants:</p>" +
                "    " +
                "    <div style=\"background-color: #f0fdf4; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #10b981;\">" +
                "      <h2 style=\"margin: 0 0 10px 0; color: #064e3b;\">" + e.getTitreEvent() + "</h2>" +
                "      <p style=\"margin: 5px 0;\">📍 <strong>Location:</strong> " + e.getLieuEvent() + "</p>" +
                "      <p style=\"margin: 5px 0;\">📅 <strong>Date:</strong> " + (e.getDateEvent() != null ? e.getDateEvent().format(DATE_FORMATTER) : "TBD") + "</p>" +
                "      <p style=\"margin: 5px 0;\">🎟 <strong>Capacity:</strong> " + e.getNbPlaces() + " spots</p>" +
                "    </div>" +
                "    " +
                "    <p>You can manage your event and view participants directly from the Syndicati Dashboard.</p>" +
                "    <p style=\"margin-top: 30px; font-size: 14px; color: #64748b;\">Thank you for hosting with Syndicati!</p>" +
                "  </div>" +
                "</div>";

        mailer.sendHtmlAsync(host.getEmailUser(), subject, html, result -> {
            if (result.isSuccess()) System.out.println("[EventMailing] Creation email SENT to " + host.getEmailUser());
            else System.err.println("[EventMailing] FAILED to send creation email: " + result.getError());
        });
    }
}
