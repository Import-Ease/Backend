package com.example.ImportEase.dtos;

public class BrevoSmsRequest {
    private String sender;
    private String recipient;
    private String content;
    private String type;

    // Constructor
    public BrevoSmsRequest(String sender, String recipient, String content, String type) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.type = type;
    }

    // Getters and Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
