package com.syndicati.services.residence;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class ServiceEmailResidence {
    private static final String FROM = "syndicatires@gmail.com";
    private static final String PASSWORD = "llda acor qpcs rfmg";

    public void EnvoyerSMS(String nom, String email, String message) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });

        Message mail = new MimeMessage(session);
        try {
            mail.setFrom(new InternetAddress(FROM, nom));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        mail.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        mail.setSubject(nom + " est intéressé à votre appartmeent!");
        mail.setText(nom + "Vous a envoyé un message! \n" + message);
        Transport.send(mail);
    }
}