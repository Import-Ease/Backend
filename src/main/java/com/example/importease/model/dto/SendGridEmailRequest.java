package com.example.importease.model.dto;

import java.util.List;

public class SendGridEmailRequest {
    private List<Personalization> personalizations;
    private From from;
    private String subject;
    private List<Content> content;

    public SendGridEmailRequest(String senderName, String senderEmail, String recipientEmail,
                                String subject, String htmlContent, String textContent) {
        this.personalizations = List.of(new Personalization(recipientEmail));
        this.from = new From(senderEmail, senderName);
        this.subject = subject;
        this.content = List.of(
                new Content("text/plain", textContent),
                new Content("text/html", htmlContent)
        );
    }

    public List<Personalization> getPersonalizations() { return personalizations; }
    public From getFrom() { return from; }
    public String getSubject() { return subject; }
    public List<Content> getContent() { return content; }

    public static class Personalization {
        private List<To> to;
        Personalization(String recipientEmail) { this.to = List.of(new To(recipientEmail)); }
        public List<To> getTo() { return to; }
    }

    public static class To {
        private String email;
        To(String email) { this.email = email; }
        public String getEmail() { return email; }
    }

    public static class From {
        private String email;
        private String name;
        From(String email, String name) { this.email = email; this.name = name; }
        public String getEmail() { return email; }
        public String getName() { return name; }
    }

    public static class Content {
        private String type;
        private String value;
        Content(String type, String value) { this.type = type; this.value = value; }
        public String getType() { return type; }
        public String getValue() { return value; }
    }
}
