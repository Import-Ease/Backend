package com.example.importease.model.dto;

import java.util.List;

public class BrevoEmailRequest {
    private Sender sender;
    private List<Recipient> to;
    private String subject;
    private String htmlContent;
    private String textContent;

    public BrevoEmailRequest(String senderName, String senderEmail, String recipientEmail, String subject, String htmlContent, String textContent) {
        this.sender = new Sender(senderName, senderEmail);
        this.to = List.of(new Recipient(recipientEmail));
        this.subject = subject;
        this.htmlContent = htmlContent;
        this.textContent = textContent;
    }

    public Sender getSender() { return sender; }
    public List<Recipient> getTo() { return to; }
    public String getSubject() { return subject; }
    public String getHtmlContent() { return htmlContent; }
    public String getTextContent() { return textContent; }

    static class Sender {
        private String name;
        private String email;
        Sender(String name, String email) { this.name = name; this.email = email; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    static class Recipient {
        private String email;
        Recipient(String email) { this.email = email; }
        public String getEmail() { return email; }
    }
}
