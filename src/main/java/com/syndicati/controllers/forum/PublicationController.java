package com.syndicati.controllers.forum;

import com.syndicati.models.forum.Publication;
import com.syndicati.models.user.User;
import com.syndicati.services.forum.PublicationService;
import com.syndicati.services.user.user.UserService;
import com.syndicati.services.mail.AsyncMailerService;
import com.syndicati.services.mail.SyndicatiEmailComposer;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller facade for forum publications.
 */
public class PublicationController {

    private final PublicationService publicationService;
    private final UserService userService;
    private final AsyncMailerService asyncMailerService;

    public PublicationController() {
        this.publicationService = new PublicationService();
        this.userService = new UserService();
        this.asyncMailerService = AsyncMailerService.getInstance();
    }

    public List<Publication> publications() {
        return publicationService.listAll();
    }

    public Optional<Publication> publicationById(Integer id) {
        return publicationService.findById(id);
    }

    public List<Publication> publicationsByUser(User user) {
        return publicationService.listByUser(user);
    }

    public List<Publication> publicationsByCategory(String category) {
        return publicationService.listByCategory(category);
    }

    public Integer publicationCreate(String titre, String description, String categorie, String image, User user) {
        Integer id = publicationService.create(titre, description, categorie, image, user);
        if (id != null && id > 0) {
            GlobalNotificationPillManager.created("Publication", "Publication created successfully.");
            notifyAllUsers(titre, description, categorie, user);
        }
        return id;
    }

    public Integer publicationCreate(String titre, String description, String categorie, String image, LocalDateTime dateCreation, User user) {
        Integer id = publicationService.create(titre, description, categorie, image, dateCreation, user);
        if (id != null && id > 0) {
            GlobalNotificationPillManager.created("Publication", "Publication created successfully.");
            notifyAllUsers(titre, description, categorie, user);
        }
        return id;
    }

    public boolean publicationUpdate(Integer id, String titre, String description, String categorie, String image) {
        boolean success = publicationService.update(id, titre, description, categorie, image);
        if (success) {
            GlobalNotificationPillManager.updated("Publication", "Publication updated successfully.");
            publicationService.findById(id).ifPresent(p -> notifyAllUsers(titre, description, categorie, p.getUser()));
        }
        return success;
    }

    public boolean publicationUpdate(Integer id, String titre, String description, String categorie, String image, LocalDateTime dateCreation) {
        boolean success = publicationService.update(id, titre, description, categorie, image, dateCreation);
        if (success) {
            GlobalNotificationPillManager.updated("Publication", "Publication updated successfully.");
            // Since we don't have the User in update params, we fetch the publication to get author
            publicationService.findById(id).ifPresent(p -> notifyAllUsers(titre, description, categorie, p.getUser()));
        }
        return success;
    }

    private void notifyAllUsers(String title, String description, String category, User author) {
        if (!"Announcement".equalsIgnoreCase(category)) {
            return;
        }

        String authorName = (author != null) ? author.getFirstName() + " " + author.getLastName() : "Syndicati Administration";
        List<String> recipients = userService.listUsers().stream()
            .map(User::getEmailUser)
            .filter(email -> email != null && !email.isBlank())
            .map(String::trim)
            .distinct()
            .collect(Collectors.toCollection(ArrayList::new));

        if (recipients.isEmpty()) {
            System.out.println("[Announcement-Mailing] No valid recipient emails found for announcement: " + title);
            return;
        }
        
        System.out.println("[Announcement-Mailing] Broadcasting announcement: " + title + " to " + recipients.size() + " users in one send.");

        String html = SyndicatiEmailComposer.publicationAnnouncement(
            "Community",
            title,
            description,
            authorName
        );

        asyncMailerService.sendHtmlAsync(recipients, "Syndicati Announcement: " + title, html);
    }

    public boolean publicationDelete(Integer id) {
        boolean success = publicationService.delete(id);
        if (success) {
            GlobalNotificationPillManager.deleted("Publication", "Publication deleted successfully.");
        }
        return success;
    }

    public PublicationService getPublicationService() {
        return publicationService;
    }
}
