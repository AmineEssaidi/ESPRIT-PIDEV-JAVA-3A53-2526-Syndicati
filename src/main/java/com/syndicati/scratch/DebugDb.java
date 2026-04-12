package com.syndicati.scratch;

import com.syndicati.models.repositories.PublicationRepository;
import com.syndicati.models.entities.Publication;
import java.util.List;

public class DebugDb {
    public static void main(String[] args) {
        PublicationRepository repo = new PublicationRepository();
        List<Publication> pubs = repo.findAllByDateDesc();
        for (Publication p : pubs) {
            System.out.println("Pub ID: " + p.getId());
            System.out.println("  User ID: " + p.getUserId());
            System.out.println("  Author Name: " + p.getAuthorFullName());
            System.out.println("  Author Avatar: [" + p.getAuthorAvatar() + "]");
        }
    }
}
