package com.syndicati.services.residence;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class ServiceEmailResidence {
    private static final String FROM = "syndicatires@gmail.com";
    private static final String PASSWORD = "llda acor qpcs rfmg";

    public void EnvoyerEmail(String nom, String email, String message) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(FROM);
        sender.setPassword(PASSWORD);
        sender.getJavaMailProperties().put("mail.smtp.auth", "true");
        sender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(FROM);
        mail.setTo(email);
        mail.setSubject(nom + " est intéressé à votre appartement!");
        mail.setText(nom + " vous a envoyé un message!\n" + message);
        sender.send(mail);
    }
}