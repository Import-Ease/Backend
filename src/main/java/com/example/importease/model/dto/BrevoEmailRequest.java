package com.example.importease.model.dto;

import java.util.List;

public class BrevoEmailRequest {
    private Sender sender;
    private List<Recipient> to;
    private String subject;
    private String htmlContent;

    public BrevoEmailRequest(Sender sender, List<Recipient> to, String subject, String htmlContent) {
        this.sender = sender;
        this.to = to;
        this.subject = subject;
        this.htmlContent = htmlContent;
    }

    // Getters and Setters
    public Sender getSender() { return sender; }
    public List<Recipient> getTo() { return to; }
    public String getSubject() { return subject; }
    public String getHtmlContent() { return htmlContent; }

    // Nested Helper Class for Sender object
    public static class Sender {
        private String name;
        private String email;

        public Sender(String name, String email) {
            this.name = name;
            this.email = email;
        }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    // Nested Helper Class for Recipient array object
    public static class Recipient {
        private String email;
        private String name;

        public Recipient(String email, String name) {
            this.email = email;
            this.name = name;
        }
        public String getEmail() { return email; }
        public String getName() { return name; }
    }
}